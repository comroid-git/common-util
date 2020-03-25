package org.comroid.iclog;

import java.util.logging.Level;

import org.comroid.iclog.model.IcLogger;

public class Test {
    public static void main(String[] args) {
        Logger log = new IcLogger(Test.class, false);

        log.at(Level.SEVERE)
           .log("This is a severe line");

        log.at(Level.FINE)
           .withMessage("Unknown Exception")
           .withMessage("another message")
           .log(new AssertionError("ThrowableMessage"));

        log.at(Level.FINE)
           .withMessage("Fine Exception")
           .withTracePolicy(TracePolicy.OMIT_SECONDARY, 2)
           .log(new AssertionError("ThrowableMessage", new NullPointerException()));

        log.at(Level.WARNING).ping();

        new Throwable().printStackTrace();
    }
}
