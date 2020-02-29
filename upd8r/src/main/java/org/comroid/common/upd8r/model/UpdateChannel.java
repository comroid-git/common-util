package org.comroid.common.upd8r.model;

import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import org.comroid.common.Version;

public interface UpdateChannel {
    String getBaseURL();
    
    Version currentVersion();
    
    boolean canUpdate(Version version);
    
    CompletableFuture<URL> requestLatest();
    
    CompletableFuture<File> downloadLatest();
}
