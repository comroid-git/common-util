package org.comroid.varbind;

import com.google.common.flogger.FluentLogger;
import org.comroid.common.Polyfill;
import org.comroid.common.func.Disposable;
import org.comroid.common.func.Invocable;
import org.comroid.common.func.Processor;
import org.comroid.common.io.FileHandle;
import org.comroid.common.io.FileProcessor;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.cache.BasicCache;
import org.comroid.uniform.cache.Cache;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.comroid.varbind.container.DataContainer;
import org.comroid.varbind.container.DataContainerBase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FileCache<K, V extends DataContainer<D>, D> extends BasicCache<K, V>
        implements Cache<K, V>, FileProcessor, Disposable.Container {
    public static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final Disposable disposable = new Disposable.Basic();
    private final SerializationAdapter<?, ?, ?> seriLib;
    private final VarBind<?, D, ?, K> idBind;
    private final FileHandle file;
    private final D dependencyObject;

    @Override
    public FileHandle getFile() {
        return file;
    }

    @Override
    public Disposable getUnderlyingDisposable() {
        return disposable;
    }

    public FileCache(
            SerializationAdapter<?, ?, ?> seriLib,
            VarBind<?, ? super D, ?, K> idBind,
            FileHandle file,
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
        logger.at(Level.INFO).log("Storing data in file %s", file);
        final UniArrayNode array = seriLib.createUniArrayNode();

        stream().filter(ref -> !ref.isNull())
                .forEach(ref -> {
                    final V it = ref.get();

                    if (it == null) {
                        array.addNull();
                        return;
                    }

                    it.toObjectNode(array.addObject());
                });

        final FileWriter writer = new FileWriter(file, false);
        writer.write(array.toString());
        writer.close();
    }

    @Override
    public synchronized void reloadData() throws IOException {
        logger.at(Level.INFO).log("Reading File %s...", file);

        if (!file.validateExists())
            throw new UnsupportedOperationException("Cannot create file: " + file);

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

                    if (id == null) {
                        logger.at(Level.WARNING).log("Skipped generation; no ID could be found for name %s. Data: %s",
                                idBind.getFieldName(),
                                node);
                        return;
                    }

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
        return (Optional<? extends V>) idBind.getGroup()
                .findGroupForData(node)
                .flatMap(group -> group.getConstructor())
                .map(constr -> constr.autoInvoke(dependencyObject, node));
    }
}
