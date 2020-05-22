package org.comroid.common.io;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

import static java.io.File.separator;
import static java.io.File.separatorChar;

@Deprecated
public final class FileProvider {
    private final static String PREFIX = "/var/dcb/candybot/";

    public static File getFile(String subPath) {
        final String path = (PREFIX + subPath).replace('/', separatorChar);
        System.out.printf("Acquiring File [ %s ]\n", path);

        createDirs(path);

        final File file = new File(path);

        if (!file.exists()) {
            System.out.printf("File [ %s ] does not exist. Trying to create it...\n", path);

            try {
                if (!file.createNewFile()) {
                    System.out.printf(" FAIL: Could not create File [ %s ] for unknown reason. Exiting.\n", path);
                    System.exit(1);
                    return null; // lol
                } else System.out.print(" OK!\n");
            } catch (IOException e) {
                System.out.printf(" FAIL: An [ %s ] occurred creating File [ %s ]. Exiting.\n", e.getClass().getSimpleName(), path);
                e.printStackTrace(System.out);
                System.exit(1);
                return null; // lol
            }
        }

        return file;
    }

    private static void createDirs(final String forPath) {
        System.out.printf("Checking directories for file [ %s ]...", forPath);

        final String[] paths = forPath.split(separator.equals("\\") ? separator + separator : separator);

        if (paths.length <= 1) {
            System.out.printf(" OK! [ %d ]\n", paths.length);
            return;
        }

        int[] printed = new int[]{0};

        IntStream.range(0, paths.length)
                .mapToObj(value -> {
                    String[] myPath = new String[value];
                    System.arraycopy(paths, 0, myPath, 0, value);
                    return myPath;
                })
                .map(strs -> String.join(separator, strs))
                .filter(str -> !str.isEmpty())
                .forEachOrdered(path -> {
                    final File file = new File(path);

                    if (file.exists() && file.isDirectory())
                        return;

                    printed[0]++;
                    System.out.printf(" FAIL\nDirectory [ %s ] does not exist, trying to create it...", path);

                    if (file.mkdir()) {
                        printed[0]++;
                        System.out.printf(" OK!\nCreated directory [ %s ] for file [ %s ]\n", path, forPath);
                    } else {
                        printed[0]++;
                        System.out.printf(" FAIL\nCould not create directory [ %s ] for file [ %s ]! Exiting.\n", path, forPath);
                        System.exit(1);
                    }
                });

        if (printed[0] == 0)
            System.out.print(" OK!\n");
        else System.out.println();
    }
}
