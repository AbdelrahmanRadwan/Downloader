package com.accurx.reliabledownloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class DownloadUtils {
    private static final DefaultWebSystemCall defaultWebSystemCall = new DefaultWebSystemCall();

    public static long getTotalWrittenBytes(final Path outputFilePath) throws IOException {
        if (Files.exists(outputFilePath)) {
            return Files.readAllBytes(outputFilePath).length;
        }
        return 0;
    }

    public static long getTotalContentLengthToDownload(final String contentFileUrl) throws ExecutionException, InterruptedException {
        return Long.parseLong(defaultWebSystemCall.GetHeaders(contentFileUrl).get().headers().firstValue("Content-Length").orElse("0"));
    }

    public static byte[] getPartiallyDownloadedContent(final String contentFileUrl, long startDownloadingBytes, long endDownloadingBytes) throws ExecutionException, InterruptedException {
        return defaultWebSystemCall.DownloadPartialContent(contentFileUrl, startDownloadingBytes, endDownloadingBytes).get().body();
    }
}
