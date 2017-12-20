package org.onosproject.provider.xmpp.pubsub;

import org.xmpp.packet.PacketError;

/**
 * Created by autonet on 16.12.17.
 */
public class PubSubValidationException extends Exception {

    private PacketError.Condition condition;
    private final String PUB_SUB_ERROR_NAMESPACE = "http://jabber.org/protocol/pubsub#errors";
    private String additionalCondition;

    public PubSubValidationException(PacketError.Condition condition, String additionalCondition) {
        super();
        this.condition = condition;
        this.additionalCondition = additionalCondition;
    }

    public PacketError asPacketError() {
        PacketError packetError = new PacketError(this.condition);
        packetError.setApplicationCondition(additionalCondition, PUB_SUB_ERROR_NAMESPACE);
        return packetError;
    }



}
