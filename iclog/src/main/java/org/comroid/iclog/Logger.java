package org.comroid.iclog;

import org.comroid.iclog.util.StackTraceUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;

import static java.time.Instant.now;

public abstract class Logger {
    private final String loggerPrefix;

    protected Logger(String prefix) {
        this.loggerPrefix = prefix;
    }

    protected abstract int printImpl(boolean err, String[] lines);

    public final <T extends Throwable, R> Function<T, R> exceptionLogger() {
        return throwable -> {
            at(Level.SEVERE).withMessage("A concurrent Exception occurred")
                    .log(throwable);

            return null;
        };
    }

    public final API at(Level level) {
        return new API(level);
    }

    protected boolean isErrorLevel(Level level) {
        return level.intValue() > 800;
    }

    public final class API {
        private final Level level;
        private final List<String> additionalMessages;

        private TracePolicy omitPolicy = TracePolicy.OMIT_SECONDARY;
        private int omitLimit = 3;

        private API(Level level) {
            this.level = level;
            this.additionalMessages = new ArrayList<>();
        }

        @Contract(mutates = "this")
        public final API withTracePolicy(TracePolicy policy, int limit) {
            this.omitPolicy = policy;
            this.omitLimit = limit;

            return this;
        }

        @Contract(mutates = "this")
        public final API withMessage(String additionalMessage) {
            additionalMessages.add(additionalMessage);

            return this;
        }

        public final boolean ping() {
            return log(
                    "Logger Ping from %s",
                    StackTraceUtils.callerClass(1)
                            .getName()
            );
        }

        public final boolean log(String format, Object... args) {
            return printImpl(isErrorLevel(level), compileLines(String.format(format, args), null)) > 0;
        }

        private String[] compileLines(@Nullable String mainMessage, @Nullable Throwable throwable) {
            final List<String> lines = new ArrayList<>();

            lines.add(String.format(
                    "[%s] %s %s%s",
                    level,
                    now(),
                    loggerPrefix,
                    Optional.ofNullable(mainMessage)
                            .map(msg -> ": " + msg)
                            .orElseGet(() -> Optional.ofNullable(throwable)
                                    .map(Throwable::getMessage)
                                    .map(msg -> ": " + msg)
                                    .orElse(""))
            ));

            if (!additionalMessages.isEmpty()) {
                lines.add("\tAdditional Information:");
                additionalMessages.stream()
                        .map(msg -> "\t-\t" + msg)
                        .forEachOrdered(lines::add);
            }

            appendThrowable(lines, throwable);

            return lines.toArray(new String[0]);
        }

        private void appendThrowable(
                final List<String> lines, @Nullable Throwable throwable
        ) {
            if (throwable == null) return;

            switch (omitPolicy) {
                case OMIT_EACH:
                    StackTraceUtils.putStackTrace(lines, throwable, omitLimit, true);
                    break;
                case OMIT_SECONDARY:
                    StackTraceUtils.putStackTrace(lines, throwable, -1, false);

                    Throwable cause = throwable.getCause();
                    if (cause != null) {
                        StackTraceUtils.putStackTrace(lines, cause, omitLimit, false);
                    }
                    break;
                case NEVER_OMIT:
                    StackTraceUtils.putStackTrace(lines, throwable, -1, true);
                    break;
            }
        }

        public final boolean log(Throwable throwable) {
            return printImpl(isErrorLevel(level), compileLines(null, throwable)) > 0;
        }
    }
}
