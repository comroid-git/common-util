package org.comroid.common.exception;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;

import org.jetbrains.annotations.Nullable;

public class MultipleExceptions extends RuntimeException {
    public MultipleExceptions(String message, Collection<? extends Throwable> causes) {
        super(composeMessage(message, causes));
    }

    private static String composeMessage(
            @Nullable String baseMessage, Collection<? extends Throwable> throwables
    ) {
        class StringStream extends OutputStream {
            @Override
            public void write(int b) {
                sb.append((char) b);
            }

            @Override
            public String toString() {
                return sb.toString();
            }
            private final StringBuilder sb = new StringBuilder();
        }

        if (baseMessage == null) {
            baseMessage = "Multiple Exceptions were thrown";
        }
        final StringStream out    = new StringStream();
        final PrintStream  string = new PrintStream(out);

        string.println(baseMessage);
        string.println("Sub Stacktraces in order:");
        throwables.forEach(t -> t.printStackTrace(string));

        return out.toString();
    }

    public MultipleExceptions(Collection<? extends Throwable> causes) {
        super(composeMessage(null, causes));
    }
}
