package org.comroid.common.io;

import org.comroid.common.exception.MultipleExceptions;
import org.comroid.common.func.Disposable;

import java.io.File;
import java.io.IOException;

public interface FileProcessor extends Disposable {
    FileHandle getFile();

    void storeData() throws IOException;

    void reloadData() throws IOException;

    @Override
    default void close() throws MultipleExceptions {
        try {
            storeData();
        } catch (IOException e) {
            throw new RuntimeException("Could not store data", e);
        } finally {
            disposeThrow();
        }
    }
}
