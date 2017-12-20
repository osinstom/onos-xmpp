package org.onosproject.xmpp.ctl.handlers;


import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.evt.EventAllocatorImpl;
import com.fasterxml.aalto.evt.IncompleteEvent;
import com.fasterxml.aalto.stax.InputFactoryImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.codehaus.stax2.ri.evt.StartElementEventImpl;
import org.codehaus.stax2.ri.evt.Stax2EventAllocatorImpl;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.onosproject.xmpp.XmppConstants;
import org.onosproject.xmpp.ctl.exception.XmlRestrictionsException;
import org.onosproject.xmpp.stream.StreamClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Decodes an XMPP message for netty pipeline
 */
public class XmlStreamDecoder extends ByteToMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final AsyncXMLInputFactory XML_INPUT_FACTORY = new InputFactoryImpl();
    private Stax2EventAllocatorImpl allocator = new Stax2EventAllocatorImpl();
    private AsyncXMLStreamReader<AsyncByteArrayFeeder> streamReader = XML_INPUT_FACTORY.createAsyncForByteArray();
    private DocumentFactory df = DocumentFactory.getInstance();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        AsyncByteArrayFeeder streamFeeder = streamReader.getInputFeeder();
        logger.info("Decoding XMPP data.. ");

        byte[] buffer = new byte[in.readableBytes()];
        in.readBytes(buffer);
        logger.info("Buffer length: " + buffer.length);
        try {
            streamFeeder.feedInput(buffer, 0, buffer.length);
        } catch (XMLStreamException exception) {
            logger.info(exception.getMessage());
            in.skipBytes(in.readableBytes());
            logger.info("Bytes skipped");
            throw exception;
        }

        while (streamReader.hasNext() && streamReader.next() != AsyncXMLStreamReader.EVENT_INCOMPLETE) {
            out.add(allocator.allocate(streamReader));
        }

    }
}
