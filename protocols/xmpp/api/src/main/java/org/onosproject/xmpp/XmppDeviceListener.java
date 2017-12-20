package org.onosproject.xmpp;

/**
 * Allows for providers interested in XMPP device events to be notified.
 */
public interface XmppDeviceListener {

    void deviceConnected(XmppDeviceId deviceId);

    void deviceDisconnected(XmppDeviceId deviceId);

}
