package com.accurx.reliabledownloader;

import java.time.Duration;

public class FileProgress {
    private final long totalFileSize;
    private final long totalBytesDownloaded;
    private final double progressPercent;
    private final Duration estimatedRemainingInSeconds;

    public FileProgress(final long totalFileSize,
                        final long totalBytesDownloaded,
                        final double progressPercent,
                        final Duration estimatedRemainingInSeconds) {
        this.totalFileSize = totalFileSize;
        this.totalBytesDownloaded = totalBytesDownloaded;
        this.progressPercent = progressPercent;
        this.estimatedRemainingInSeconds = estimatedRemainingInSeconds;
    }

    @Override
    public String toString() {
        return "FileProgress{" +
                "totalFileSize=" + totalFileSize +
                ", totalBytesDownloaded=" + totalBytesDownloaded +
                ", progressPercent=" + progressPercent +
                ", estimatedRemainingInSeconds=" + estimatedRemainingInSeconds.getSeconds() +
                '}';
    }
}