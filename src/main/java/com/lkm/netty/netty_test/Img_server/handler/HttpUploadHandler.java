package com.lkm.netty.netty_test.Img_server.handler;

import com.lkm.netty.netty_test.Img_server.parse.RequestParser;
import com.lkm.netty.netty_test.Img_server.util.ResponseUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.PlatformDependent;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

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
 * <p>
 *
 * @Auto lkm
 * @Date 2019/1/14 16:23
 * @Tip 基于HTTP协议文件上传
 * @Version 1.0
 **/
@ChannelHandler.Sharable
public class HttpUploadHandler extends SimpleChannelInboundHandler<HttpObject> {
    protected static Logger logger = LoggerFactory.getLogger(HttpUploadHandler.class);
    //HTTP request path
    private static final String CLASS_FULL_NAME = "com.lkm.netty.netty_test.Img_server.handler.HttpUploadHandler";
    //HTTP request path
    private static String URI = "/upload";

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private static HttpPostRequestDecoder httpDecoder;
    private static HttpRequest request;

    public HttpUploadHandler(Configuration config) {
        super(true);// 设置为true 不需要ReferenceCountUtil.release(httpObject);
        URI = config.getString(CLASS_FULL_NAME, URI);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpObject httpObject) throws Exception {
        String dir = "";
        if (httpObject instanceof HttpRequest) {
            request = (HttpRequest) httpObject;

            if (request.uri().startsWith(URI) && request.method().equals(HttpMethod.POST)) {
                httpDecoder = new HttpPostRequestDecoder(factory, request);
                httpDecoder.setDiscardThreshold(0);
            } else {
                ctx.fireChannelRead(httpObject);
            }
        }
        //HTTP 请求内容
        if (httpObject instanceof HttpContent) {
            log("Used off-heap memory before cleaning");
            if (httpDecoder != null) {
                HttpContent chunk = (HttpContent) httpObject;
                httpDecoder.offer(chunk);
                if (chunk instanceof LastHttpContent) {
                    //Get the uploaded file to save the directory
                    dir = RequestParser.newInstances().parsePost(httpDecoder).get("dir");
                    if (StringUtils.isBlank(dir)) {
                        ResponseUtil.sendResult(ctx, BAD_REQUEST);
                    } else {
                        writeChunk(ctx, dir);
                    }
                    httpDecoder.destroy();
                    httpDecoder = null;
                    ResponseUtil.sendResult(ctx, OK);
                }
//                ReferenceCountUtil.release(httpObject);
                log("Used off-heap memory after cleanup");
            } else {
                ctx.fireChannelRead(httpObject);
            }
        }
    }

    /**
     * 任何文件都可以传
     * @param ctx
     * @throws IOException
     */
    private void writeChunk(ChannelHandlerContext ctx, String dir){
        boolean flag = true;
        try {
            while (httpDecoder.hasNext()) {
                InterfaceHttpData data = httpDecoder.next();
                if (data != null && InterfaceHttpData.HttpDataType.FileUpload.equals(data.getHttpDataType())) {
                    final FileUpload fileUpload = (FileUpload) data;
                    final File file = new File(dir + fileUpload.getFilename());
                    if (!file.getParentFile().exists())
                        file.getParentFile().mkdirs();
                    try (FileChannel inputChannel = new FileInputStream(fileUpload.getFile()).getChannel();
                         FileChannel outputChannel = new FileOutputStream(file).getChannel()) {
//                     outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                        inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                    }
                    flag = false;
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        if (flag) {
            ResponseUtil.sendResult(ctx, NO_CONTENT);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.channel().close();
    }

    /**
     * 释放不活动的
     *
     * @param ctx
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.channel().close();
    }

    /**
     * Print out of heap memory by reflection
     *
     * @param str
     */
    private static void log(String str) {
        Class clazz = PlatformDependent.class;
        Object obj = null;
        try {
            obj = clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Field field = null;
        try {
            field = clazz.getDeclaredField("DIRECT_MEMORY_COUNTER");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        field.setAccessible(true);
        try {
            AtomicLong atomicLong = (AtomicLong) field.get(obj);
            logger.info(str + "={}", atomicLong.intValue());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}