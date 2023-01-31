package com.accurx.reliabledownloader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class MyFileDownloader implements FileDownloader {
    private static final int DOWNLOAD_CHUNK_SIZE_PER_SECOND = 12500; // Typical 3G (Basic) Download Speed 0.1Mbit/s
    private boolean isDownloadActive = true;
    DefaultWebSystemCall defaultWebSystemCall = new DefaultWebSystemCall();

    public CompletableFuture<Boolean> DownloadFile(final String contentFileUrl,
                                                   final String localFilePath,
                                                   final Consumer<FileProgress> onProgressChanged) throws ExecutionException, InterruptedException, IOException {
        final Path outputFilePath = Paths.get(localFilePath);

        final long totalContentLengthToDownload = Long.parseLong(defaultWebSystemCall.GetHeaders(contentFileUrl).get().headers().firstValue("Content-Length").orElse("0"));
        long totalDownloadedBytes = 0;
        if (Files.exists(outputFilePath)) {
            totalDownloadedBytes = Files.readAllBytes(outputFilePath).length;
        }

        while (totalDownloadedBytes < totalContentLengthToDownload) {
            if(!this.isDownloadActive) {
                break;
            }
            showDownloadProgress(onProgressChanged, totalContentLengthToDownload, totalDownloadedBytes);
            long startDownloadingBytes = totalDownloadedBytes;
            long endDownloadingBytes = Math.min(startDownloadingBytes + DOWNLOAD_CHUNK_SIZE_PER_SECOND, totalContentLengthToDownload);
            byte[] downloadedContent = defaultWebSystemCall.DownloadPartialContent(contentFileUrl, startDownloadingBytes, endDownloadingBytes).get().body();
            Files.write(outputFilePath, downloadedContent, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            totalDownloadedBytes = endDownloadingBytes;
        }
        showDownloadProgress(onProgressChanged, totalContentLengthToDownload, totalDownloadedBytes);
        return CompletableFuture.completedFuture(totalDownloadedBytes == totalContentLengthToDownload);
    }

    public void CancelDownloads() {
        this.isDownloadActive = false;
    }

    private void showDownloadProgress(final Consumer<FileProgress> onProgressChanged,
                                      final long totalContentLengthToDownload,
                                      final long totalDownloadedBytes) {
        final double percentageDownloaded = 100.0 * totalDownloadedBytes / totalContentLengthToDownload;
        final long totalRemainingBytesToDownload = Math.max(0, totalContentLengthToDownload - totalDownloadedBytes);
        final Duration remainingTimeToDownloadInSeconds = Duration.ofSeconds(totalRemainingBytesToDownload / DOWNLOAD_CHUNK_SIZE_PER_SECOND);
        onProgressChanged.accept(new FileProgress(totalContentLengthToDownload, totalDownloadedBytes, percentageDownloaded, remainingTimeToDownloadInSeconds));
    }
}
