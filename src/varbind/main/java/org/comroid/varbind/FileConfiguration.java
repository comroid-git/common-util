package org.comroid.varbind;

import org.comroid.common.io.FileHandle;
import org.comroid.common.io.FileProcessor;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.container.DataContainerBase;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class FileConfiguration extends DataContainerBase<Object> implements FileProcessor {
    private final Collection<AutoCloseable> children = new ArrayList<>();
    private final SerializationAdapter<?, ?, ?> serializationAdapter;
    private final FileHandle file;

    {
        try {
            reloadData();
        } catch (IOException e) {
            throw new RuntimeException("Could not load data", e);
        }
    }

    @Override
    public final FileHandle getFile() {
        return file;
    }

    @Override
    public final Collection<AutoCloseable> getChildren() {
        return children;
    }

    public FileConfiguration(
            SerializationAdapter<?, ?, ?> serializationAdapter,
            @Nullable Class<? extends FileConfiguration> containingClass,
            FileHandle file
    ) {
        super(null, null, containingClass);

        this.serializationAdapter = serializationAdapter;
        this.file = file;
    }

    @Override
    public final void storeData() throws IOException {
        final UniObjectNode data = toObjectNode(serializationAdapter);

        try (FileWriter fw = new FileWriter(file, false)) {
            fw.append(data.toString());
        }
    }

    @Override
    public final void reloadData() throws IOException {
        final UniNode data = serializationAdapter.createUniNode(file.getContent());

        updateFrom(data.asObjectNode());
    }

    @Override
    public final void addChildren(AutoCloseable child) {
        children.add(child);
    }
}
