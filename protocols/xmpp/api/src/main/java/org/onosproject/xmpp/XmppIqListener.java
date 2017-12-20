package org.onosproject.xmpp;

import org.xmpp.packet.IQ;

/**
 * Allows for providers interested in XMPP IQ stanzas to be notified.
 */
public interface XmppIqListener {

    /**
     * Invoke if new IQ stanza from XMPP device received.
     */
    void handleIqStanza(IQ iq);

}
