package org.comroid.common.upd8r.model;

import org.comroid.common.Version;

import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public interface UpdateChannel {
    String getBaseURL();

    Version currentVersion();

    boolean canUpdate(Version version);

    CompletableFuture<URL> requestLatest();

    CompletableFuture<File> downloadLatest();
}
