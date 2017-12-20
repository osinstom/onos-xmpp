package org.onosproject.xmpp.ctl.handlers;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import io.netty.util.CharsetUtil;
import org.onosproject.xmpp.stream.StreamEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Encodes XMPP message into a ChannelBuffer for netty pipeline
 */
public class XmppEncoder extends MessageToByteEncoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        byte[] bytes = null;

        if(msg instanceof StreamEvent) {
            StreamEvent streamEvent = (StreamEvent) msg;
            logger.info("SENDING: {}", streamEvent.toXML());
            bytes = streamEvent.toXML().getBytes(CharsetUtil.UTF_8);
        }

        if(msg instanceof Packet) {
            Packet pkt = (Packet) msg;
            logger.info("SENDING /n, {}", pkt.toString());
            bytes = pkt.toXML().getBytes(CharsetUtil.UTF_8);
        }

        out.writeBytes(checkNotNull(bytes));
    }
}
