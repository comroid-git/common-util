package org.comroid.common.io;

import org.comroid.api.Disposable;

import java.io.IOException;

public interface FileProcessor extends Disposable {
    FileHandle getFile();

    int storeData() throws IOException;

    int reloadData() throws IOException;

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
