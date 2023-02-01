package com.accurx.reliabledownloader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;

import static com.accurx.reliabledownloader.DownloadUtils.getTotalContentLengthToDownload;
import static com.accurx.reliabledownloader.DownloadUtils.getTotalWrittenBytes;


public class MyFileDownloaderTest {
    static final String CONTENT_FILE_URL_LARGE_FILE = "https://norvig.com/big.txt";
    static final String LOCAL_FILE_PATH = "src/test/downloadedTestContent/textFile.txt";
    static final long TIME_OUT_IN_SECONDS = 20;
    static final long SLEEP_TIME_IN_MILLISECONDS = 2_000;

    MyFileDownloader fileDownloader = new MyFileDownloader();

    @BeforeEach
    public void setUp() throws IOException {
        fileDownloader.AllowResumeDownloads();
        Files.deleteIfExists(Path.of(LOCAL_FILE_PATH));
    }

    @Test
    public void downloadLargeFileTest() throws IOException, ExecutionException, InterruptedException {
        Assertions.assertTrue(Files.notExists(Path.of(LOCAL_FILE_PATH)));
        Assertions.assertTrue(fileDownloader.DownloadFile(CONTENT_FILE_URL_LARGE_FILE, LOCAL_FILE_PATH, (progress) -> {}).get());
        Assertions.assertEquals(getTotalContentLengthToDownload(CONTENT_FILE_URL_LARGE_FILE), getTotalWrittenBytes(Path.of(LOCAL_FILE_PATH)));
        Assertions.assertTrue(Files.exists(Path.of(LOCAL_FILE_PATH)));
    }

    @Test
    public void downloadLargeFileTestGetsInterrupt() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        Assertions.assertTrue(Files.notExists(Path.of(LOCAL_FILE_PATH)));

        // Create an action to download the file.
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<Void> action = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                fileDownloader.DownloadFile(CONTENT_FILE_URL_LARGE_FILE, LOCAL_FILE_PATH, (progress) -> {});
                return null;
            }
        };
        Future<Void> future = executor.submit(action);
        try {
            // Interrupt the download. after TIME_OUT_IN_SECONDS seconds.
            future.get(TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            future.cancel(true);
        }
        // Assert that the file was not completely downloaded yet.
        Assertions.assertNotEquals(getTotalContentLengthToDownload(CONTENT_FILE_URL_LARGE_FILE), getTotalWrittenBytes(Path.of(LOCAL_FILE_PATH)));
        Assertions.assertTrue(Files.exists(Path.of(LOCAL_FILE_PATH)));
        // Assert that resuming the download can be done successfully.
        Assertions.assertTrue(fileDownloader.DownloadFile(CONTENT_FILE_URL_LARGE_FILE, LOCAL_FILE_PATH, (progress) -> {}).get());
        Assertions.assertEquals(getTotalContentLengthToDownload(CONTENT_FILE_URL_LARGE_FILE), getTotalWrittenBytes(Path.of(LOCAL_FILE_PATH)));
        Assertions.assertTrue(Files.exists(Path.of(LOCAL_FILE_PATH)));

        executor.shutdown();
    }

    @Test
    public void downloadLargeFileTestGetsCancelledAndRestarted() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        Assertions.assertTrue(Files.notExists(Path.of(LOCAL_FILE_PATH)));

        // Create an action to download the file, and another to cancel it.
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Void> downloadAction = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                fileDownloader.DownloadFile(CONTENT_FILE_URL_LARGE_FILE, LOCAL_FILE_PATH, (progress) -> {});
                return null;
            }
        };
        Callable<Void> downloadCancelAction = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Thread.sleep(SLEEP_TIME_IN_MILLISECONDS);
                fileDownloader.CancelDownloads();
                return null;
            }
        };

        Future<Void> downloadActionFuture = executor.submit(downloadAction);
        Future<Void> downloadCancelActionFuture = executor.submit(downloadCancelAction);
        downloadActionFuture.get();
        downloadCancelActionFuture.get();
        // Assert that the file was not completely downloaded yet.
        Assertions.assertNotEquals(getTotalContentLengthToDownload(CONTENT_FILE_URL_LARGE_FILE), getTotalWrittenBytes(Path.of(LOCAL_FILE_PATH)));
        Assertions.assertTrue(Files.exists(Path.of(LOCAL_FILE_PATH)));
        // Assert that resuming the download can be done successfully.
        Assertions.assertTrue(fileDownloader.DownloadFile(CONTENT_FILE_URL_LARGE_FILE, LOCAL_FILE_PATH, (progress) -> {}).get());
        Assertions.assertEquals(getTotalContentLengthToDownload(CONTENT_FILE_URL_LARGE_FILE), getTotalWrittenBytes(Path.of(LOCAL_FILE_PATH)));
        Assertions.assertTrue(Files.exists(Path.of(LOCAL_FILE_PATH)));

        executor.shutdown();
    }
}
