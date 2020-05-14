package org.comroid.varbind;

import org.comroid.common.io.FileProcessor;
import org.comroid.common.io.IOHelper;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.container.DataContainerBase;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class FileConfiguration extends DataContainerBase<Object> implements FileProcessor {
    private final Collection<AutoCloseable> children = new ArrayList<>();
    private final SerializationAdapter<?, ?, ?> serializationAdapter;
    private final File file;

    {
        try {
            reloadData();
        } catch (IOException e) {
            throw new RuntimeException("Could not load data", e);
        }
    }

    @Override
    public final File getFile() {
        return file;
    }

    @Override
    public final Collection<AutoCloseable> getChildren() {
        return children;
    }

    public FileConfiguration(SerializationAdapter<?, ?, ?> serializationAdapter, @Nullable Class<? extends FileConfiguration> containingClass, File file) {
        super(null, null, containingClass);

        this.serializationAdapter = serializationAdapter;
        this.file = file;
    }

    @Override
    public final void storeData() throws IOException {
        final UniObjectNode data = toObjectNode();

        try (FileWriter fw = new FileWriter(file, false)) {
            fw.append(data.toString());
        }
    }

    @Override
    public final void reloadData() throws IOException {
        final UniNode data = serializationAdapter.createUniNode(IOHelper.lines(file).collect(Collectors.joining()));

        updateFrom(data.asObjectNode());
    }

    @Override
    public final void addChildren(AutoCloseable child) {
        children.add(child);
    }
}
