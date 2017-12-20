package org.onosproject.pubsub.api;

/**
 *
 */
public class PubSubError {

    private ErrorType errorType;

    public PubSubError(ErrorType type) {
        this.errorType = type;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public static enum ErrorType {
        ITEM_NOT_FOUND,
        NOT_SUBSCRIBED,
        INVALID_PAYLOAD
    }

}
