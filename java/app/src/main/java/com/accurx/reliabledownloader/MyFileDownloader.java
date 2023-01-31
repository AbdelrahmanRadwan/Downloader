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

import static com.accurx.reliabledownloader.DownloadUtils.*;

public class MyFileDownloader implements FileDownloader {
    // TODO: Use the download speed of the current client instead fo the minimum download speed by a 3G.
    private static final int DOWNLOAD_CHUNK_SIZE_PER_SECOND = 12500; // Typical 3G (Basic) Download Speed 0.1Mbit/s
    private boolean isDownloadActive = true;

    public CompletableFuture<Boolean> DownloadFile(final String contentFileUrl,
                                                   final String localFilePath,
                                                   final Consumer<FileProgress> onProgressChanged) throws ExecutionException, InterruptedException, IOException {
        this.isDownloadActive = true; // Allow the download to start if it was previously stopped.
        final Path outputFilePath = Paths.get(localFilePath);

        final long totalContentLengthToDownload = getTotalContentLengthToDownload(contentFileUrl);
        long totalWrittenBytes = getTotalWrittenBytes(outputFilePath);

        while (totalWrittenBytes < totalContentLengthToDownload && this.isDownloadActive) {
            onProgressChanged.accept(getDownloadProgress(totalContentLengthToDownload, totalWrittenBytes));
            final long startDownloadingBytes = totalWrittenBytes;
            final long bytesToDownload = Math.min(DOWNLOAD_CHUNK_SIZE_PER_SECOND, totalContentLengthToDownload - totalWrittenBytes);
            final long endDownloadingBytes = startDownloadingBytes + bytesToDownload;
            final byte[] downloadedContent = getPartiallyDownloadedContent(contentFileUrl, startDownloadingBytes, endDownloadingBytes);
            Files.write(outputFilePath, downloadedContent, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            totalWrittenBytes += downloadedContent.length;
        }

        onProgressChanged.accept(getDownloadProgress(totalContentLengthToDownload, totalWrittenBytes));
        return CompletableFuture.completedFuture(totalWrittenBytes == totalContentLengthToDownload);
    }

    public void CancelDownloads() {
        this.isDownloadActive = false;
    }

    public void AllowResumeDownloads() {
        this.isDownloadActive = true;
    }

    private FileProgress getDownloadProgress(final long totalContentLengthToDownload,
                                             final long totalDownloadedBytes) {
        final double percentageDownloaded = 100.0 * totalDownloadedBytes / totalContentLengthToDownload;
        final long totalRemainingBytesToDownload = Math.max(0, totalContentLengthToDownload - totalDownloadedBytes);
        final Duration remainingTimeToDownloadInSeconds = Duration.ofSeconds(totalRemainingBytesToDownload / DOWNLOAD_CHUNK_SIZE_PER_SECOND);
        return new FileProgress(totalContentLengthToDownload, totalDownloadedBytes, percentageDownloaded, remainingTimeToDownloadInSeconds);
    }
}
