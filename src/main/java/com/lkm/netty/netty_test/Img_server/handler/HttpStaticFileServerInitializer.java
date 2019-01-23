package com.lkm.netty.netty_test.Img_server.handler;

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
 * @Date 2019/1/14 15:42
 * @Tip 初始channel
 * @Version 1.0
 **/
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.apache.commons.configuration.Configuration;

import java.nio.charset.Charset;

public class HttpStaticFileServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    private final Configuration config;
    public HttpStaticFileServerInitializer(SslContext sslCtx, Configuration config) {
        this.sslCtx = sslCtx;
        this.config=config;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new StringDecoder(Charset.forName("UTF-8")));
        pipeline.addLast(new StringEncoder(Charset.forName("UTF-8")));
        //If you add the file uploaded by the handler will be limited by the size here limit 10M
        pipeline.addLast(new HttpObjectAggregator(10485760));
        //Support large file asynchronous transfer？
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpUploadHandler(config));
    }
}
