package org.comroid.iclog.model;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;

import org.comroid.iclog.Logger;

public final class IcLogger extends Logger {
    private final PrintStream out;
    private final PrintStream err;

    public IcLogger(Class<?> target, boolean replaceStdOut) {
        this(target.getName(), replaceStdOut);
    }

    public IcLogger(String prefix, boolean replaceStdOut) {
        super(prefix);

        if (replaceStdOut) {
            PrintStream loggerOut = new PrintStream(new LoggerStdOut(), true);
            System.setOut(loggerOut);

            this.out = loggerOut;
        } else this.out = System.out;

        this.err = System.err;
    }

    @Override
    protected int printImpl(boolean isErr, String[] lines) {
        int i = 0;
        for (; i < lines.length; i++) {
            (isErr ? err : out).println(lines[i]);
        }

        return i;
    }

    private class LoggerStdOut extends OutputStream {
        private StringBuilder sb = new StringBuilder();

        @Override
        public void write(int b) {
            sb.append((char) b);
        }

        @Override
        public void flush() {
            at(Level.INFO).log(sb.toString());

            sb = new StringBuilder();
        }
    }
}
