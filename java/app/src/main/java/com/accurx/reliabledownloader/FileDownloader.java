package com.accurx.reliabledownloader;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public interface FileDownloader {
    /**
     * Downloads a file, trying to use reliable downloading if possible
     *
     * @param contentFileUrl    The url which the file is hosted at
     * @param localFilePath     The local file path to save the file to
     * @param onProgressChanged An action to call which prints progress
     * @return True or false, depending on if download completes and writes to file system okay
     */
    CompletableFuture<Boolean> DownloadFile(final String contentFileUrl,
                                            final String localFilePath,
                                            final Consumer<FileProgress> onProgressChanged) throws IOException, ExecutionException, InterruptedException;

    /**
     * Cancels any in progress downloads
     */
    void CancelDownloads();


    /**
     * Resumes the current download (given that the file started downloading and the process was cached)
     */
    void AllowResumeDownloads();
}
