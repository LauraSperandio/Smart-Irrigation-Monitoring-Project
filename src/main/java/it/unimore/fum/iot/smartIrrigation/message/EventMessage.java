package it.unimore.fum.iot.smartIrrigation.message;

import java.util.Map;

public class EventMessage extends GenericMessage{

    public EventMessage() {
    }

    public EventMessage(String type, Map<String, Object> metadata) {
        super(type, metadata);
    }

    public EventMessage(String type, long timestamp, Map<String, Object> metadata) {
        super(type, timestamp, metadata);
    }

}
