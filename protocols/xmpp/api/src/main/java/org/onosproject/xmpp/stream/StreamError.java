package org.onosproject.xmpp.stream;

/**
 * Created by autonet on 13.09.17.
 */
public class StreamError extends org.xmpp.packet.StreamError implements StreamEvent {

    public StreamError(Condition condition) {
        super(condition);
    }

    @Override
    public String toXML() {
        return super.toXML();
    }

}
