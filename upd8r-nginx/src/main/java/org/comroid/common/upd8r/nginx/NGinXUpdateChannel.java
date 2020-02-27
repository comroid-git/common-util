package org.comroid.common.upd8r.nginx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.comroid.common.Version;
import org.comroid.common.upd8r.model.UpdateChannel;
import org.comroid.common.upd8r.nginx.model.JsonFileInfo;

import com.alibaba.fastjson.JSONArray;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NGinXUpdateChannel implements UpdateChannel {
    private final OkHttpClient httpClient = new OkHttpClient.Builder().build();

    private final Function<String, Version> filenameVersioning;
    private final Version.Container versionContainer;
    private final URL baseURL;

    public NGinXUpdateChannel(Version.Container versionContainer, URL baseURL, Function<String, Version> filenameVersioning) {
        this.versionContainer = versionContainer;
        this.baseURL = baseURL;
        this.filenameVersioning = filenameVersioning;
    }

    @Override
    public URL getBaseURL() {
        return baseURL;
    }

    @Override
    public Version currentVersion() {
        return versionContainer.getVersion();
    }

    @Override
    public boolean canUpdate(Version targetVersion) {
        return currentVersion().compareTo(targetVersion) > 0;
    }

    @Override
    public CompletableFuture<URL> requestLatest() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final Request request = new Request.Builder()
                        .get()
                        .url(getBaseURL())
                        .build();

                final Response response = httpClient.newCall(request).execute();

                if (response.body() != null)
                    return JSONArray.parseArray(response.body().string(), JsonFileInfo.class);
                else throw new NullPointerException("No response body received");
            } catch (IOException e) {
                throw new RuntimeException("Error requesting files", e);
            }
        })
                .thenApply(files -> files.stream()
                        .map(JsonFileInfo::getFileName)
                        .max(Comparator.comparing(filenameVersioning))
                        .orElseThrow(AssertionError::new))
                .thenApply(filename -> {
                    try {
                        return new URL(getBaseURL().toExternalForm() + '/' + filename);
                    } catch (MalformedURLException e) {
                        throw new AssertionError(e);
                    }
                });
    }

    @Override
    public CompletableFuture<File> downloadLatest() {
        return requestLatest()
                .thenApply(url -> {
                    try {
                        return url.openStream();
                    } catch (IOException e) {
                        throw new RuntimeException("Could not open URL Stream");
                    }
                })
                .thenApplyAsync(stream -> {
                    try {
                        final File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
                        final FileOutputStream outputStream = new FileOutputStream(tempFile);

                        //stream.transferTo(outputStream);
                        Objects.requireNonNull(outputStream, "outputStream");
                        long transferred = 0;
                        byte[] buffer = new byte[128];
                        int read;
                        while ((read = stream.read(buffer, 0, 128)) >= 0) {
                            outputStream.write(buffer, 0, read);
                            transferred += read;
                        }

                        return tempFile;
                    } catch (IOException e) {
                        throw new RuntimeException("Could not download file", e);
                    }
                });
    }
}
