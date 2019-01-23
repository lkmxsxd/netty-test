package com.lkm.netty.netty_test;

import com.lkm.netty.netty_test.Img_server.HttpStaticFileServer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ^^^^^^^^^^^^^^^^^^^^|^^^^^^^^^^^^^^^^^^^^^
 * ^^^^^^^^^^^^^^^|    |    | ^^^^^^^^^^^^^^^人以群分
 * ^^^^^^^^^^^^^)_)  )_)  )_)^^^^^^^^^^^^^和为组织
 * ^^^^^^^^^^^^)___))___))___)/^^^^^^^^^聚于思想
 * ^^^^^^^^^^^)____)____)_____)//^^^^^获于感悟
 * ^^^^^^^^^_____|____|____|____///__
 * ---------/                   /---------
 * ^^^^^ ^^^^^ ^^^^  ^^^^^^^  ^^^^^    ^^^
 * 独学而无友 则孤陋而寡闻
 *
 * @Auto lkm
 * @Date 2019/1/15 20:37
 * @Tip 测试下载文件
 * @Version 1.0
 **/
public class HttpNettyTest {
    protected static Logger logger = LoggerFactory.getLogger(HttpStaticFileServer.class);

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    private static AtomicInteger atomicIntegerUpload = new AtomicInteger(0);
    private static AtomicInteger uploadFailureCount = new AtomicInteger(0);

    private static FileBody fileBody = null;
    private static Long start = System.currentTimeMillis();

    private static String host = null;
    private static String port = null;

    private static String upload_uri = null;

    //Server file directory
    private static String downloadFileDir = null;
    //The path where the file to be uploaded is located
    private static String uploadFileDir = null;
    //File name to upload
    private static String fileName = null;

    //Concurrent quantity
    private static final int TOTAL = 1;

    static {
        //Read configuration file
        try {
            final Configuration config = new PropertiesConfiguration("http-test.config");
            host = config.getString("remote.file.host");
            port = config.getString("remote.file.port");

            upload_uri = config.getString("remote.file.upload.server");

            //获取远程服务器的下载目录
            downloadFileDir = config.getString("downloadFileDir");
            uploadFileDir = config.getString("uploadFileDir");
            fileName = config.getString("fileName");

            fileBody = new FileBody(new File(uploadFileDir + fileName));
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        for (int i = 0; i < TOTAL; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    upload(upload_uri, host, port, downloadFileDir, fileBody);
                }
            }).start();
            countDownLatch.countDown();
        }

    }


    public static boolean upload(String uri, String host, String port, String dir, FileBody fileBody) {
        boolean result = false;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        uri = uri.replace("{host}", host);
        uri = uri.replace("{port}", port);
        HttpPost httpPost = new HttpPost(uri);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(20000).setSocketTimeout(20000).build();
        httpPost.setConfig(requestConfig);

        StringBody comment = new StringBody(dir, ContentType.APPLICATION_JSON);
        HttpEntity httpEntity = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .setCharset(Charset.forName("UTF-8"))
                .addPart("file", fileBody)
                .addPart("dir", comment)
                .build();
        httpPost.setEntity(httpEntity);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                atomicIntegerUpload.getAndIncrement();
                result = true;
            } else {
                atomicIntegerUpload.getAndIncrement();
                result = false;
            }
        } catch (IOException e) {
            atomicIntegerUpload.getAndIncrement();
            uploadFailureCount.getAndIncrement();
            logger.error("File upload exception:{}", e.getMessage());
            result = false;
        } finally {
            try {
                httpClient.close();
                if (atomicIntegerUpload.intValue() == TOTAL) {
                    logger.info("File upload result：Plan upload" + TOTAL + "times，success=" + (TOTAL - uploadFailureCount.intValue()) + "times，failure=" + uploadFailureCount.intValue() + "times;Total time=" + (System.currentTimeMillis() - start));
                }
            } catch (IOException e) {
                logger.error("Http close exception after uploading file：{}", e.getMessage());
            }
        }
        return result;
    }
}
