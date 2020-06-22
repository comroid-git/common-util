package org.comroid.varbind;

import com.google.common.flogger.FluentLogger;
import org.comroid.api.Disposable;
import org.comroid.api.Invocable;
import org.comroid.api.Junction;
import org.comroid.api.Polyfill;
import org.comroid.common.io.FileHandle;
import org.comroid.common.io.FileProcessor;
import org.comroid.mutatio.proc.Processor;
import org.comroid.trie.TrieMap;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FileCache<K, V extends DataContainer<D>, D>
        extends DataContainerCache<K, V, D>
        implements FileProcessor, Disposable.Container {
    public static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final Disposable disposable = new Disposable.Basic();
    private final SerializationAdapter<?, ?, ?> seriLib;
    private final FileHandle file;

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
        this(seriLib, idBind, null, file, largeThreshold, false, dependencyObject);
    }

    public FileCache(
            SerializationAdapter<?, ?, ?> seriLib,
            VarBind<?, ? super D, ?, K> idBind,
            Junction<K, String> converter,
            FileHandle file,
            int largeThreshold,
            boolean keyCaching,
            D dependencyObject
    ) {
        super(largeThreshold, converter == null
                ? new ConcurrentHashMap<>()
                : new TrieMap.Basic<>(converter, keyCaching), idBind, dependencyObject);

        this.seriLib = seriLib;
        this.file = file;

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

    @Override
    public synchronized void storeData() throws IOException {
        final UniArrayNode array = seriLib.createUniArrayNode(null);

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

}
