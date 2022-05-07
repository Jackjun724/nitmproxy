package com.github.chhsiao90.nitmproxy;

import com.github.chhsiao90.nitmproxy.enums.ProxyMode;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.github.chhsiao90.nitmproxy.tls.CertUtil.*;

public class NitmProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(NitmProxy.class);

    private final NitmProxyConfig config;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private NitmProxyStatus status = NitmProxyStatus.NOTCONFIGURED;
    public NitmProxy(NitmProxyConfig config) {
        this.config = config;
    }

    public void start() throws Exception {
        config.init();

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
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

            status = NitmProxyStatus.STARTED;

            if (config.getStatusListener() != null) {
                config.getStatusListener().onStart();
            }

            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            status = NitmProxyStatus.STOPPED;
        }
    }

    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        status = NitmProxyStatus.STOPPED;

        if (config.getStatusListener() != null) {
            config.getStatusListener().onStop();
        }
    }

    public NitmProxyStatus getStatus() {
        return status;
    }

    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption(
                Option.builder("m")
                      .longOpt("mode")
                      .hasArg()
                      .argName("MODE")
                      .desc("proxy mode(HTTP, SOCKS, TRANSPARENT), default: HTTP")
                      .build());
        options.addOption(
                Option.builder("h")
                      .longOpt("host")
                      .hasArg()
                      .argName("HOST")
                      .desc("listening host, default: 127.0.0.1")
                      .build());
        options.addOption(
                Option.builder("p")
                      .longOpt("port")
                      .hasArg()
                      .argName("PORT")
                      .desc("listening port, default: 8080")
                      .build());
        options.addOption(
                Option.builder()
                      .longOpt("cert")
                      .hasArg()
                      .argName("CERTIFICATE")
                      .desc("x509 certificate used by server(*.pem), default: server.pem")
                      .build());
        options.addOption(
                Option.builder()
                      .longOpt("key")
                      .hasArg()
                      .argName("KEY")
                      .desc("key used by server(*.pem), default: key.pem")
                      .build());
        options.addOption(
                Option.builder("k")
                      .longOpt("insecure")
                      .hasArg(false)
                      .desc("not verify on server certificate")
                      .build());

        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            new HelpFormatter().printHelp("nitmproxy", options, true);
            System.exit(-1);
        }

        new NitmProxy(parse(commandLine)).start();
    }

    private static NitmProxyConfig parse(CommandLine commandLine) {
        NitmProxyConfig config = new NitmProxyConfig();
        if (commandLine.hasOption("m")) {
            config.setProxyMode(ProxyMode.of(commandLine.getOptionValue("m")));
        }
        if (commandLine.hasOption("h")) {
            config.setHost(commandLine.getOptionValue("h"));
        }
        if (commandLine.hasOption("p")) {
            try {
                config.setPort(Integer.parseInt(commandLine.getOptionValue("p")));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Not a legal port: " + commandLine.getOptionValue("p"));
            }
        }
        if (commandLine.hasOption("cert")) {
            String certFile = commandLine.getOptionValue("cert");
            if (!new File(certFile).exists()) {
                throw new IllegalArgumentException("No cert file found: " + certFile);
            }
            config.setCertificate(readPemFromFile(certFile));
        }
        if (commandLine.hasOption("key")) {
            String certKey = commandLine.getOptionValue("key");
            if (!new File(certKey).exists()) {
                throw new IllegalArgumentException("No key found: " + certKey);
            }
            config.setKey(readPrivateKeyFromFile(certKey));
        }
        if (commandLine.hasOption("k")) {
            config.setInsecure(true);
        }

        LOGGER.info("{}", config);
        return config;
    }
}