package org.comroid.uniform.cache;

import com.google.common.flogger.FluentLogger;
import org.comroid.common.Polyfill;
import org.comroid.common.func.Disposable;
import org.comroid.common.func.Invocable;
import org.comroid.common.func.Processor;
import org.comroid.common.io.FileProcessor;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.comroid.varbind.container.DataContainer;
import org.comroid.varbind.container.DataContainerBase;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FileCache<K, V extends DataContainer<D>, D> extends BasicCache<K, V> implements Cache<K, V>, FileProcessor, Disposable.Container {
    public static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final Disposable disposable = new Disposable.Basic();
    private final SerializationAdapter<?, ?, ?> seriLib;
    private final VarBind<?, D, ?, K> idBind;
    private final File file;
    private final D dependencyObject;

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public Disposable getUnderlyingDisposable() {
        return disposable;
    }

    public FileCache(
            SerializationAdapter<?, ?, ?> seriLib,
            VarBind<?, ? super D, ?, K> idBind,
            File file,
            int largeThreshold,
            D dependencyObject
    ) {
        super(largeThreshold);

        this.seriLib = seriLib;
        this.idBind = Polyfill.uncheckedCast(idBind);
        this.file = file;
        this.dependencyObject = dependencyObject;

        try {
            reloadData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean add(V value) {
        final K key = value.requireNonNull(idBind);

        set(key, value);

        return true;
    }

    public final <T extends V> Processor<T> autoUpdate(Class<T> type, UniObjectNode data) {
        return autoUpdate(DataContainerBase.findRootBind(type), data);
    }

    public final <T extends V> Processor<T> autoUpdate(GroupBind<? super T, ? super D> group, UniObjectNode data) {
        return autoUpdate(group.getConstructor()
                .orElseThrow(() -> new NoSuchElementException("No constructor defined in group " + group)), data);
    }

    public final <T extends V> Processor<T> autoUpdate(Invocable<? super T> creator, UniObjectNode data) {
        final K key = idBind.getFrom(data);

        if (containsKey(key))
            //noinspection unchecked
            return getReference(key, false)
                    .process()
                    .peek(it -> it.updateFrom(data))
                    .map(it -> (T) it);
        else //noinspection unchecked
            return Processor.ofConstant(tryConstruct(data))
                    .map(opt -> {
                        if (!opt.isPresent())
                            return creator.autoInvoke(data, dependencyObject);
                        return opt.get();
                    })
                    .map(it -> (T) it)
                    .peek(it -> getReference(key, true).set(it));
    }

    @Override
    public synchronized void storeData() throws IOException {
        final UniArrayNode data = UniArrayNode.ofList(seriLib, stream()
                .filter(ref -> !ref.isNull())
                .map(Reference::requireNonNull)
                .map(v -> v.toObjectNode(seriLib))
                .collect(Collectors.toList()));
        final FileWriter writer = new FileWriter(file, false);
        writer.write(data.toString());
        writer.close();
    }

    @Override
    public synchronized void reloadData() throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        final String str = reader.lines().collect(Collectors.joining());

        if (str.isEmpty()) {
            reader.close();
            return;
        }

        final UniNode uniNode = seriLib.createUniNode(str);
        if (!uniNode.isArrayNode())
            return;

        final UniArrayNode data = uniNode.asArrayNode();

        data.asNodeList().stream()
                .filter(UniNode::isObjectNode)
                .map(UniNode::asObjectNode)
                .forEach(node -> {
                    final K id = idBind.getFrom(node);

                    if (containsKey(id)) {
                        getReference(id, false).requireNonNull().updateFrom(node);
                    } else {
                        final Object generated = tryConstruct(node).orElse(null);

                        if (generated == null) {
                            logger.at(Level.WARNING).log("Skipped generation; no suitable constructor could be found. Data: %s", node);
                            return;
                        }

                        getReference(id, true).set(Polyfill.uncheckedCast(generated));
                    }
                });
        reader.close();
    }

    private Optional<? extends V> tryConstruct(UniObjectNode node) {
        //noinspection unchecked
        return (Optional<? extends V>) idBind.getGroup().findGroupForData(node)
                .flatMap(GroupBind::getConstructor)
                .map(constr -> constr.silentAutoInvoke(dependencyObject, node));
    }
}
