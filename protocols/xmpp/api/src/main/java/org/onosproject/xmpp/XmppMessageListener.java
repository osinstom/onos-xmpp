package org.onosproject.xmpp;

import org.xmpp.packet.Message;


/**
 * Allows for providers interested in XMPP Message stanzas to be notified.
 */
public interface XmppMessageListener {

    /**
     * Invoke if new Message stanza from XMPP device received.
     */
    void handleMessageStanza(Message message);

}
