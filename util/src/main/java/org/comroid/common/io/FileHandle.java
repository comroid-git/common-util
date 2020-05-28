package org.comroid.common.io;

import org.comroid.common.func.Disposable;
import org.comroid.common.os.OSBasedFileMover;
import org.comroid.common.ref.Named;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileHandle extends File implements Named {
    @NotNull
    @Override
    public final String getName() {
        return super.getAbsolutePath();
    }

    public List<String> getLines() {
        List<String> yields;

        try (
                FileReader reader = new FileReader(this);
                BufferedReader bufferedReader = new BufferedReader(reader)
        ) {
            yields = bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Collections.unmodifiableList(yields);
    }

    @Override
    public boolean isFile() {
        return getAbsolutePath().endsWith(File.separator);
    }

    public FileHandle(File file) {
        this(file.getAbsolutePath());
    }

    public FileHandle(String path) {
        super(path);
    }

    @Override
    public boolean mkdirs() {
        if (isFile())
            return new FileHandle(getParent()).mkdirs();
        else return super.mkdirs();
    }

    public FileHandle createSub(String name) {
        validateDir();

        return new FileHandle(getAbsolutePath() + name);
    }

    public FileHandle createSubDir(String name) throws UnsupportedOperationException {
        return createSub(name.endsWith(File.separator) ? name : name + File.separator);
    }

    public FileHandle createSubFile(String name) throws UnsupportedOperationException {
        if (name.endsWith(File.separator))
            throw new IllegalArgumentException("File name cannot end with " + File.separator);

        return createSub(name);
    }

    public CompletableFuture<FileHandle> move(FileHandle target) {
        return move(target, ForkJoinPool.commonPool());
    }

    public CompletableFuture<FileHandle> move(FileHandle target, Executor executor) {
        if (isDirectory())
            return OSBasedFileMover.current.moveDirectory(this, target, executor);
        else if (isFile())
            return OSBasedFileMover.current.moveFile(this, target, executor);

        throw new AssertionError("Unknown File Category");
    }

    public void validateDir() throws UnsupportedOperationException {
        if (!isDirectory())
            throw new UnsupportedOperationException(String
                    .format("File { %s } is not a directory", getAbsolutePath()));
    }

    public String getLinesContent() {
        return String.join("", getLines());
    }
}
