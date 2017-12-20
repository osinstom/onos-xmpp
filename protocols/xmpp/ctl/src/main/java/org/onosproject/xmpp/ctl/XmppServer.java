package org.onosproject.xmpp.ctl;

import com.google.common.base.Strings;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Dictionary;
import java.util.concurrent.Executors;

import static org.onlab.util.Tools.get;
import static org.onlab.util.Tools.groupedThreads;

/**
 *  The XMPP server class.  Starts XMPP server and listens to new XMPP device connections.
 */
public class XmppServer {

    protected static final Logger logger = LoggerFactory.getLogger(XmppServer.class);

    protected final int SEND_BUFFER_SIZE = 4 * 1024 * 1024;

    protected Integer port = 5259;

    private Channel channel;
    private ChannelFuture channelFuture;
    private EventLoopGroup eventLoopGroup;
    private Class<? extends AbstractChannel> channelClass;


    /**
     * Initializes XMPP server.
     */
    public void init() {

    }

    /**
     * Runs XMPP server thread.
     */
    public void run() {
        try {
            final ServerBootstrap bootstrap = createServerBootStrap();

            InetSocketAddress socketAddress = new InetSocketAddress(port);
            channel = bootstrap.bind(socketAddress).sync().channel().closeFuture().channel();

            logger.info("Listening for device connections on {}", socketAddress);

        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private ServerBootstrap createServerBootStrap() {

        ServerBootstrap bootstrap = new ServerBootstrap();
        configureBootstrap(bootstrap);
        initEventLoopGroup();

        bootstrap.group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new XmppChannelInitializer(this));

        return bootstrap;
    }

    /**
     * Initializes event loop group.
     */
    private void initEventLoopGroup() {

        // try to use EpollEventLoopGroup if possible,
        // if OS does not support native Epoll, fallback to use netty NIO
//        try {
//            eventLoopGroup = new EpollEventLoopGroup();
//            channelClass = EpollSocketChannel.class;
//        } catch (NoClassDefFoundError e) {
//            logger.debug("Failed to initialize native (epoll) transport. "
//                    + "Reason: {}. Proceeding with NIO event group.", e);
//        }
        eventLoopGroup = new NioEventLoopGroup();
        channelClass = NioServerSocketChannel.class;
    }

    private void configureBootstrap(ServerBootstrap bootstrap) {
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.SO_RCVBUF, 2048);
    }

    /**
     * TLS/SSL setup. If needed.
     */
    private void initTls() {

    }

    /**
     * Sets configuration parameters defined via ComponentConfiguration subsystem.
     * @param properties
     */
    public void setConfiguration(Dictionary<?, ?> properties) {
        String port = get(properties, "xmppPort");
        if(!Strings.isNullOrEmpty(port)) {
            this.port = Integer.parseInt(port);
        }
        logger.debug("XMPP port set to {}", this.port);
    }

    /**
     * Starts XMPP server.
     */
    public void start() {
        logger.info("XMPP Server has started.");
        this.run();
    }

    /**
     * Stops XMPP server.
     */
    public void stop() {

    }
}
