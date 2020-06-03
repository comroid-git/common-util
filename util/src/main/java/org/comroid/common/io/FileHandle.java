package org.comroid.common.io;

import jdk.internal.joptsimple.internal.Strings;
import org.comroid.common.ref.Named;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class FileHandle extends File implements Named {
    private final boolean dir;

    @NotNull
    @Override
    public String getName() {
        return getAbsolutePath();
    }

    @Override
    public boolean isDirectory() {
        return dir || super.isDirectory();
    }

    @Override
    public FileHandle getParentFile() {
        return new FileHandle(super.getParentFile());
    }

    public List<String> getLines() {
        final List<String> yields = new ArrayList<>();

        try (
                FileReader fr = new FileReader(this);
                BufferedReader br = new BufferedReader(fr)
        ) {
            br.lines().forEachOrdered(yields::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return yields;
    }

    public String getContent() {
        return Strings.join(getLines(), "");
    }

    public FileHandle(File file) {
        this(file.getAbsolutePath(), file.isDirectory());
    }

    public FileHandle(String absolutePath) {
        this(absolutePath, absolutePath.endsWith(File.separator));
    }

    public FileHandle(String absolutePath, boolean dir) {
        super(absolutePath);

        this.dir = dir;
    }

    public FileHandle createSubFile(String name) {
        return createSub(name, false);
    }

    public FileHandle createSubDir(String name) {
        return createSub(name, true);
    }

    public FileHandle createSub(String name, boolean dir) {
        if (!validateDir())
            return null;

        if (dir && !name.endsWith(File.separator))
            name += File.separator;

        FileHandle created = new FileHandle(getAbsolutePath() + name, dir);
        if (dir && !created.validateDir())
            throw new UnsupportedOperationException("Could not validate directory: " + created.getAbsolutePath());

        return created;
    }

    public boolean validateDir() {
        if (exists() && !super.isDirectory())
            throw new UnsupportedOperationException("File is not a directory!");
        if (isDirectory() && !exists() && !mkdirs())
            throw new UnsupportedOperationException("Could not create directory: " + getAbsolutePath());

        return isDirectory();
    }

    @Override
    public boolean mkdir() {
        return mkdirs();
    }

    @Override
    public boolean mkdirs() {
        if (isDirectory())
            return super.mkdirs();
        else return getParentFile().mkdirs();
    }
}
