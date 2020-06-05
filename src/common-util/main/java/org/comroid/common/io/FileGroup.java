package org.comroid.common.io;

import org.comroid.common.ref.Named;

import java.io.File;

public interface FileGroup extends Named {
    default String getBasePathExtension() {
        return getName() + File.separatorChar;
    }
}
