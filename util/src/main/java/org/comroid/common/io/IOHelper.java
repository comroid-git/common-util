package org.comroid.common.io;

import java.io.*;
import java.util.stream.Stream;

public class IOHelper {
    public static Stream<String> lines(File file) throws IOException {
        try (
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr)
        ) {
            return br.lines();
        }
    }
}
