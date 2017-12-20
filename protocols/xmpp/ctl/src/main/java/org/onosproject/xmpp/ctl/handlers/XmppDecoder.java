package org.onosproject.xmpp.ctl.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.onosproject.xmpp.XmppConstants;
import org.onosproject.xmpp.ctl.XmppValidator;
import org.onosproject.xmpp.ctl.exception.UnsupportedStanzaTypeException;
import org.onosproject.xmpp.ctl.exception.XmppValidationException;
import org.onosproject.xmpp.stream.StreamClose;
import org.onosproject.xmpp.stream.StreamError;
import org.onosproject.xmpp.stream.StreamOpen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Translates XML Element to XMPP Packet.
 */
public class XmppDecoder extends MessageToMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private XmppValidator validator = new XmppValidator();

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Object object, List out) throws Exception {
            if(object instanceof Element) {
                Element root = (Element) object;
                if (root.getName().equals(XmppConstants.STREAM_QNAME)) {
                    StreamOpen streamOpen = new StreamOpen(root);
                    out.add(streamOpen);
                    return;
                }

                try {
                    Packet packet = recognizeAndReturnXmppPacket(root);
                    validate(packet);
                    out.add(packet);
                } catch (UnsupportedStanzaTypeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new XmppValidationException(false);
                }

            } else if (object instanceof XMLEvent) {

                XMLEvent event = (XMLEvent) object;
                if(event.isStartElement()) {
                    final StartElement element = event.asStartElement();

                    if(element.getName().getLocalPart().equals(XmppConstants.STREAM_QNAME)) {
                        DocumentFactory df = DocumentFactory.getInstance();
                        QName qname = (element.getName().getPrefix() == null) ?
                                df.createQName(element.getName().getLocalPart(), element.getName().getNamespaceURI()) :
                                df.createQName(element.getName().getLocalPart(), element.getName().getPrefix(), element.getName().getNamespaceURI());

                        Element newElement = df.createElement(qname);

                        Iterator nsIt = element.getNamespaces();
                        // add all relevant XML namespaces to Element
                        while(nsIt.hasNext()) {
                            Namespace ns = (Namespace) nsIt.next();
                            newElement.addNamespace(ns.getPrefix(), ns.getNamespaceURI());
                        }

                        Iterator attrIt = element.getAttributes();
                        // add all attributes to Element
                        while(attrIt.hasNext()) {
                            Attribute attr = (Attribute) attrIt.next();
//                            if(attr.getName().getLocalPart().equals("to") || attr.getName().getLocalPart().equals("from")) {
//                                validator.validateJID(attr.getName().getLocalPart());
//                            }
                            newElement.addAttribute(attr.getName().getLocalPart(), attr.getValue());
                        }
                        StreamOpen streamOpen = new StreamOpen(newElement);
                        validator.validateStream(streamOpen);
                        out.add(streamOpen);
                    }
                } else if(event.isEndElement()) {
                    out.add(new StreamClose());
                }
            }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.info("Exception caught: {}", cause.getMessage());
        if(cause.getCause() instanceof XmppValidationException) {
            if(((XmppValidationException) cause.getCause()).isStreamValidationException()) {
                StreamError.Condition condition = StreamError.Condition.bad_format;
                StreamError error = new StreamError(condition);
                ctx.channel().writeAndFlush(error);
                ctx.channel().writeAndFlush(new StreamClose());
                return;
            }
        }
        logger.info("Not a StreamValidationException. Sending exception upstream.");
        ctx.fireExceptionCaught(cause);
    }


    private void validate(Packet packet) throws UnsupportedStanzaTypeException, XmppValidationException {
        validator.validate(packet);
    }

    private Packet recognizeAndReturnXmppPacket(Element root) throws UnsupportedStanzaTypeException, IllegalArgumentException {
        checkNotNull(root);

        Packet packet = null;
        if(root.getName().equals(XmppConstants.IQ_QNAME)) {
            packet = new IQ(root);
        } else if (root.getName().equals(XmppConstants.MESSAGE_QNAME)) {
            packet = new Message(root);
        } else if (root.getName().equals(XmppConstants.PRESENCE_QNAME)) {
            packet = new Presence(root);
        } else {
            throw new UnsupportedStanzaTypeException("Unrecognized XMPP Packet");
        }
        logger.info("XMPP Packet received\n" + root.asXML());
        return packet;
    }

}
