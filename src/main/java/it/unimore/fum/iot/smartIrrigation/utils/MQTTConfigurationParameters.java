package it.unimore.fum.iot.smartIrrigation.utils;

public class MQTTConfigurationParameters {

    public static String BROKER_ADDRESS = "155.185.228.20";
    public static int BROKER_PORT = 7883;
    public static final String MQTT_USERNAME="262716@studenti.unimore.it";
    public static final String MQTT_PASSWORD="ewmwmyijckwrxgdx";
    public static final String MQTT_BASIC_TOPIC= String.format("/iot/user/%s", MQTT_USERNAME);

    public static final String TELEMETRY_ENV_MON_TOPIC = "env-mon";
    public static final String TELEMETRY_IRR_CON_TOPIC = "irr-con";
    public static final String TELEMETRY_PEOP_COUNT_TOPIC = "peop-count";
    public static final String CHANGE_IRRIGATION_TOPIC = "irr-change";


    public static final String TARGET_BATTERY_EM_TOPIC = "/iot/user/262716@studenti.unimore.it/+/env-mon/battery";
    public static final String TARGET_BATTERY_IC_TOPIC = "/iot/user/262716@studenti.unimore.it/+/irr-con/battery";
    public static final String TARGET_TEMPERATURE_TOPIC = "/iot/user/262716@studenti.unimore.it/+/env-mon/temperature";
    public static final String TARGET_RAIN_TOPIC = "/iot/user/262716@studenti.unimore.it/+/env-mon/rain";
    public static final String TARGET_PRESENCE_TOPIC = "/iot/user/262716@studenti.unimore.it/+/peop-count/presence";




}
