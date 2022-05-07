package com.github.chhsiao90.nitmproxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NitmProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(NitmProxy.class);

    private final NitmProxyConfig config;

    public NitmProxy(NitmProxyConfig config) {
        this.config = config;
    }

    public void start() throws Exception {
        config.init();

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new NitmProxyInitializer(config));
            Channel channel = bootstrap
                    .bind(config.getHost(), config.getPort())
                    .sync()
                    .channel();

            LOGGER.info("nitmproxy is listening at {}:{}",
                              config.getHost(), config.getPort());

            if (config.getStatusListener() != null) {
                config.getStatusListener().onStart();
            }

            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        final NitmProxyConfig config = new NitmProxyConfig();
        new NitmProxy(config).start();
    }
}