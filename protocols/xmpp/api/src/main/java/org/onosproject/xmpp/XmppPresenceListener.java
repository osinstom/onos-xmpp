package org.onosproject.xmpp;

import org.xmpp.packet.Presence;

/**
 * Allows for providers interested in XMPP Presence stanzas to be notified.
 */
public interface XmppPresenceListener {

    /**
     * Invoke if new Presence stanza from XMPP device received.
     */
    void handlePresenceStanza(Presence presence);

}
