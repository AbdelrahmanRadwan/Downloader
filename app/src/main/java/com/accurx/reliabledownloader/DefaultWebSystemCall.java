package com.accurx.reliabledownloader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DefaultWebSystemCall implements WebSystemCalls {

    private final HttpClient client = HttpClient.newBuilder().build();

    @Override
    public CompletableFuture<HttpResponse<Void>> GetHeaders(final String url) {
        var request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.discarding());
    }

    @Override
    public CompletableFuture<HttpResponse<byte[]>> DownloadContent(final String url) {
        var request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .GET()
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray()).orTimeout(2, TimeUnit.MINUTES);
    }

    @Override
    public CompletableFuture<HttpResponse<byte[]>> DownloadPartialContent(final String url, final long from, final long to) {
        var request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .header("Range", String.format("bytes=%s-%s", from, to))
                .GET()
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray()).orTimeout(2, TimeUnit.MINUTES);
    }
}
