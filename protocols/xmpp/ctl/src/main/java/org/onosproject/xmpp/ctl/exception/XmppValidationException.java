package org.onosproject.xmpp.ctl.exception;

/**
 * Created by autonet on 07.10.17.
 */
public class XmppValidationException extends Exception {

    private boolean streamValidation;

    public XmppValidationException(boolean streamValidation) {
        this.streamValidation = streamValidation;
    }

    public boolean isStreamValidationException() {
        return streamValidation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XmppValidationException that = (XmppValidationException) o;

        return streamValidation == that.streamValidation;
    }

    @Override
    public int hashCode() {
        return (streamValidation ? 1 : 0);
    }
}
