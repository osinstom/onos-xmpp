package org.onosproject.xmpp.ctl;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.onosproject.xmpp.ctl.handlers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Creates pipeline for server-side XMPP channel.
 */
public class XmppChannelInitializer extends ChannelInitializer<SocketChannel> {

    protected static final Logger logger = LoggerFactory.getLogger(XmppChannelInitializer.class);

    protected XmppServer xmppServer;

    public XmppChannelInitializer(XmppServer server) {
        this.xmppServer = server;
    }

    /**
     * Initializes pipeline for XMPP channel.
     * @throws Exception
     */
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        XmppChannelHandler handler = new XmppChannelHandler();

        pipeline.addLast("xmppencoder", new XmppEncoder());
        pipeline.addLast("xmlstreamdecoder", new XmlStreamDecoder());
        pipeline.addLast("xmlmerger", new XmlMerger());
        pipeline.addLast("xmppdecoder", new XmppDecoder());
        pipeline.addLast("handler", handler);

    }
}
