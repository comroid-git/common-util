package org.comroid.uniform.cache;

import com.google.common.flogger.FluentLogger;
import lombok.Builder;
import org.comroid.common.Polyfill;
import org.comroid.common.func.Disposable;
import org.comroid.common.io.FileProcessor;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.comroid.varbind.container.DataContainer;

import java.io.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FileCache<K, V extends DataContainer<D>, D> extends BasicCache<K, V> implements FileProcessor, Disposable.Container {
    public static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final Disposable disposable = new Disposable.Basic();
    private final SerializationAdapter<?, ?, ?> seriLib;
    private final VarBind<?, ?, ?, K> idBind;
    private final File file;
    private final D dependencyObject;

    @Builder
    public FileCache(SerializationAdapter<?, ?, ?> seriLib, VarBind<?, ?, ?, K> idBind, File file, int largeThreshold, D dependencyObject) {
        super(largeThreshold);

        this.seriLib = seriLib;
        this.idBind = idBind;
        this.file = file;
        this.dependencyObject = dependencyObject;

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
                .map(DataContainer::toObjectNode)
                .collect(Collectors.toList()));
        final FileWriter writer = new FileWriter(file, false);
        addChildren(writer);

        writer.write(data.toString());
        writer.close();
    }

    @Override
    public synchronized void reloadData() throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        addChildren(reader);
        final UniArrayNode data = seriLib.createUniNode(reader.lines().collect(Collectors.joining())).asArrayNode();

        data.asNodeList().stream()
                .map(UniNode::asObjectNode)
                .forEach(node -> {
                    final K id = idBind.getFrom(node);
                    final Object generated = idBind.getGroup().findGroupForData(node)
                            .flatMap(GroupBind::getConstructor)
                            .map(constr -> constr.silentAutoInvoke(dependencyObject, node))
                            .orElse(null);

                    if (generated == null) {
                        logger.at(Level.WARNING).log("Skipped generation; no suitable constructor could be found. Data: %s");
                        return;
                    }

                    getReference(id, true).set(Polyfill.uncheckedCast(generated));
                });
    }

    @Override
    public Disposable getUnderlyingDisposable() {
        return disposable;
    }
}
