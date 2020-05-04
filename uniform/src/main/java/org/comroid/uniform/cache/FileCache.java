package org.comroid.uniform.cache;

import org.comroid.common.func.Disposable;
import org.comroid.common.io.FileProcessor;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.varbind.VarCarrier;

import java.io.*;
import java.util.stream.Collectors;

public class FileCache<K, V extends VarCarrier<D>, D> extends BasicCache<K, V> implements FileProcessor, Disposable.Container {
    private final Disposable disposable = new Disposable.Basic();
    private final SerializationAdapter<?, ?, ?> seriLib;
    private final File file;

    public FileCache(SerializationAdapter<?, ?, ?> seriLib, File file, int largeThreshold) {
        super(largeThreshold);

        this.seriLib = seriLib;
        this.file = file;

        try {
            reloadData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public synchronized void storeData() throws IOException {
        final UniArrayNode data = UniArrayNode.ofList(seriLib, stream().map(Reference::requireNonNull)
                .map(VarCarrier::toObjectNode)
                .collect(Collectors.toList()));
        final FileWriter writer = new FileWriter(file, false);

        writer.write(data.toString());
    }

    @Override
    public synchronized void reloadData() throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        final UniArrayNode data = seriLib.createUniNode(reader.lines().collect(Collectors.joining())).asArrayNode();
        //todo What do here
    }

    @Override
    public Disposable getUnderlyingDisposable() {
        return disposable;
    }
}
