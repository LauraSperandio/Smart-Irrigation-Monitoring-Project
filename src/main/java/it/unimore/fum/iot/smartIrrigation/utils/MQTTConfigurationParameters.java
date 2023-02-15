package it.unimore.fum.iot.smartIrrigation.utils;

public class MQTTConfigurationParameters {
    //Do not touch
    public static String BROKER_ADDRESS = "155.185.228.20";
    public static int BROKER_PORT = 7883;
    public static final String MQTT_USERNAME="262716@studenti.unimore.it";
    public static final String MQTT_PASSWORD="ewmwmyijckwrxgdx";
    public static final String MQTT_BASIC_TOPIC= String.format("/iot/user/%s", MQTT_USERNAME);
}
