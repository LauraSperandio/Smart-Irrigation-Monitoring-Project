package it.unimore.fum.iot.smartIrrigation.message;

import java.util.Map;

public class CommandMessage extends GenericMessage {

    public CommandMessage() {
    }

    public CommandMessage(String type, Map<String, Object> metadata) {
        super(type, metadata);
    }

    public CommandMessage(String type, long timestamp, Map<String, Object> metadata) {
        super(type, timestamp, metadata);
    }
}
