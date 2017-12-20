package org.onosproject.xmpp.ctl;

import org.dom4j.Element;
import org.onosproject.xmpp.XmppConstants;
import org.onosproject.xmpp.ctl.exception.XmppValidationException;
import org.onosproject.xmpp.stream.StreamEvent;
import org.onosproject.xmpp.stream.StreamOpen;
import org.xmpp.packet.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by autonet on 07.10.17.
 */
public class XmppValidator {

    public void validateStream(StreamOpen stream) throws XmppValidationException {
        try {
//            JID jid = stream.getFromJID();
//            validateJID(jid);
            String jid = stream.getElement().attribute("from").getValue();
            validateJID(jid);
        } catch (Exception e) {
            throw new XmppValidationException(true);
        }
    }

    public void validate(Packet packet) throws XmppValidationException {

        validateBasicXmpp(packet);

        Element root = packet.getElement();
        if(root.getName().equals(XmppConstants.IQ_QNAME)) {
            validateIQ((IQ) packet);
        } else if (root.getName().equals(XmppConstants.MESSAGE_QNAME)) {
            validateMessage((Message) packet);
        } else if (root.getName().equals(XmppConstants.PRESENCE_QNAME)) {
            validatePresence((Presence) packet);
        }
    }

    public void validateIQ(IQ iq) throws XmppValidationException{
        try {

        } catch(Exception e) {
            throw new XmppValidationException(false);
        }
    }

    public void validateMessage(Message message) throws XmppValidationException {
        try {

        } catch(Exception e) {
            throw new XmppValidationException(false);
        }
    }

    public void validatePresence(Presence presence) throws XmppValidationException {
        try {

        } catch(Exception e) {
            throw new XmppValidationException(false);
        }
    }

    private void validateBasicXmpp(Packet packet) throws XmppValidationException {
        try {
            validateJID(packet.getFrom());
            validateJID(packet.getTo());
        } catch(Exception e) {
            throw new XmppValidationException(false);
        }
    }

    public void validateJID(String jid) throws XmppValidationException {
        try {
            checkNotNull(jid);
            JID testJid = new JID(jid);
        } catch (Exception e) {
            throw new XmppValidationException(false);
        }
    }

    public void validateJID(JID jid) {
        checkNotNull(jid);
    }
}
