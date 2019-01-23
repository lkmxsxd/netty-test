package com.lkm.netty.netty_test.Img_server;

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
 * @Date 2019/1/14 17:08
 * @Tip Netty启动
 * @Version 1.0
 **/

import com.lkm.netty.netty_test.Img_server.handler.HttpStaticFileServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public final class HttpStaticFileServer {
    protected static Logger logger = LoggerFactory.getLogger(HttpStaticFileServer.class);
    static final boolean SSL = System.getProperty("ssl") != null;

    public static void main(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                    .sslProvider(SslProvider.JDK).build();
        } else {
            sslCtx = null;
        }

        //读取配置文件
        final Configuration config = new PropertiesConfiguration("netty-http-server.config");

        //判断是否是ssl来选择端口
        int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : config.getString("port")));

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpStaticFileServerInitializer(sslCtx,config));

            Channel ch = b.bind(PORT).sync().channel();

            logger.info("Open your web browser and navigate to " +
                    (SSL ? "https" : "http") + "://127.0.0.1:" + PORT + '/');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
