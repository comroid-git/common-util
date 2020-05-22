package org.comroid.common.os;

import org.comroid.common.io.FileGroup;
import org.comroid.common.io.FileHandle;

import java.util.Optional;

public interface OSBasedFileProvider {
    OS getOperatingSystem();

    FileHandle getBaseDirectory();

    static <E extends Enum<E> & OSBasedFileProvider> Optional<E> autoDetect(Class<E> fromEnum) {
        for (E enumConstant : fromEnum.getEnumConstants()) {
            if (enumConstant.getOperatingSystem().equals(OS.current))
                return Optional.of(enumConstant);
        }

        return Optional.empty();
    }

    default FileHandle getFile(FileGroup group, String name) {
        return new FileHandle(getBaseDirectory().getAbsolutePath() + group.getBasePathExtension() + name);
    }
}
