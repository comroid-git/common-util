package org.comroid.common.exception;

import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;

public class MultipleExceptions extends RuntimeException {
    public MultipleExceptions(String message, Collection<? extends Throwable> causes) {
        super(composeMessage(message, causes));
    }

    public MultipleExceptions(Collection<? extends Throwable> causes) {
        super(composeMessage(null, causes));
    }

    private static String composeMessage(@Nullable String baseMessage, Collection<? extends Throwable> throwables) {
        class StringStream extends OutputStream {
            private final StringBuilder sb = new StringBuilder();

            @Override
            public void write(int b) {
                sb.append((char) b);
            }

            @Override
            public String toString() {
                return sb.toString();
            }
        }

        if (baseMessage == null)
            baseMessage = "Multiple Exceptions were thrown";
        final StringStream out    = new StringStream();
        final PrintStream  string = new PrintStream(out);

        string.println(baseMessage);
        string.println("Sub Stacktraces in order:");
        throwables.forEach(t -> t.printStackTrace(string));

        return out.toString();
    }
}
