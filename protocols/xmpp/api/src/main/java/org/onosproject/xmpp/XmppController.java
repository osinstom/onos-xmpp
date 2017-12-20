package org.onosproject.xmpp;

/**
 * Controls XMPP protocol behaviour.
 */
public interface XmppController {

    XmppDevice getDevice(XmppDeviceId xmppDeviceId);
    
    void addXmppDeviceListener(XmppDeviceListener deviceListener);

    void removeXmppDeviceListener(XmppDeviceListener deviceListener);

    void addXmppIqListener(XmppIqListener iqListener);

    void removeXmppIqListener(XmppIqListener iqListener);

    void addXmppMessageListener(XmppMessageListener messageListener);

    void removeXmppMessageListener(XmppMessageListener messageListener);

    void addXmppPresenceListener(XmppPresenceListener presenceListener);

    void removeXmppPresenceListener(XmppPresenceListener presenceListener);

}
