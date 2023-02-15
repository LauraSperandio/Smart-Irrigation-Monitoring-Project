package it.unimore.fum.iot.smartIrrigation.consumer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimore.fum.iot.smartIrrigation.message.ControlMessage;
import it.unimore.fum.iot.smartIrrigation.message.TelemetryMessage;
import it.unimore.fum.iot.smartIrrigation.resource.BatteryEMSensorResource;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

import static it.unimore.fum.iot.smartIrrigation.process.MQTTConfigurationParameters.BROKER_ADDRESS;
import static it.unimore.fum.iot.smartIrrigation.process.MQTTConfigurationParameters.BROKER_PORT;

public class BatteryMonitoringEMConsumer {

    private static final String CONTROL_TOPIC = "control";

    public static final double ALARM_BATTERY_LEVEL = 20.0;

    private final static Logger logger = LoggerFactory.getLogger(BatteryMonitoringEMConsumer.class);

    //IP Address of the target MQTT Broker
//    private static String BROKER_ADDRESS = "155.185.228.20";

    //PORT of the target MQTT Broker
//    private static int BROKER_PORT = 7883;

    private static final String TARGET_TOPIC = "/iot/smartIrrigation/env-mon/+/telemetry/battery";

    private static ObjectMapper mapper;

    private static boolean isAlarmNotified = false;

    public static void main(String [ ] args) {

        logger.info("MQTT Consumer Tester Started ...");

        try{

            //Generate a random MQTT client ID using the UUID class
            String clientId = UUID.randomUUID().toString();

            //Represents a persistent data store, used to store outbound and inbound messages while they
            //are in flight, enabling delivery to the QoS specified. In that case use a memory persistence.
            //When the application stops all the temporary data will be deleted.
            MqttClientPersistence persistence = new MemoryPersistence();

            //The the persistence is not passed to the constructor the default file persistence is used.
            //In case of a file-based storage the same MQTT client UUID should be used
            IMqttClient client = new MqttClient(
                    String.format("tcp://%s:%d", BROKER_ADDRESS, BROKER_PORT), //Create the URL from IP and PORT
                    clientId,
                    persistence);

            //Define MQTT Connection Options such as reconnection, persistent/clean session and connection timeout
            //Authentication option can be added -> See AuthProducer example
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            //Connect to the target broker
            client.connect(options);

            logger.info("Connected ! Client Id: {}", clientId);

            mapper = new ObjectMapper();

            //Subscribe to the target topic #. In that case the consumer will receive (if authorized) all the message
            //passing through the broker
            client.subscribe(TARGET_TOPIC, (topic, msg) -> {

                Optional<TelemetryMessage> telemetryMessageOptional = parseTelemetryMessagePayload(msg);

                if(telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType().equals(BatteryEMSensorResource.RESOURCE_TYPE)){

                    Double newBatteryLevel = (Double)telemetryMessageOptional.get().getDataValue();
                    logger.info("New Battery Telemetry Data Received ! Battery Level: {}", newBatteryLevel);


                    if(isBatteryLevelAlarm(newBatteryLevel) && isAlarmNotified){

                        logger.info("BATTERY LEVEL ALARM DETECTED ! Sending Control Notification ...");
                        isAlarmNotified = true;

                        String controlTopic = String.format("%s/%s", topic.replace("telemetry/battery", ""), CONTROL_TOPIC);

                        publishControlMessage(client, controlTopic, new ControlMessage());

                    }
                }

            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static boolean isBatteryLevelAlarm(Double newlValue){
        if(newlValue <= ALARM_BATTERY_LEVEL)
            return true;
        else
            return false;
    }


    private static Optional<TelemetryMessage> parseTelemetryMessagePayload(MqttMessage mqttMessage){

        try {

            if(mqttMessage == null)
                return Optional.empty();

            byte [] payloadByteArray = mqttMessage.getPayload();
            String payloadString = new String(payloadByteArray);

            TelemetryMessage telemetryMessage = (TelemetryMessage)mapper.readValue(payloadString, TelemetryMessage.class);

            return Optional.of(telemetryMessage);

        }catch (Exception e){
            return Optional.empty();
        }

    }

    private static void publishControlMessage(IMqttClient mqttClient, String topic, ControlMessage controlMessage) throws MqttException, JsonProcessingException {

        logger.info("Sending to topic: {} -> Data: {}", topic, controlMessage);

        if(mqttClient != null && mqttClient.isConnected() && controlMessage != null && topic != null){

            String messagePayload = mapper.writeValueAsString(controlMessage);

            MqttMessage mqttMessage = new MqttMessage(messagePayload.getBytes());
            mqttMessage.setQos(0);

            mqttClient.publish(topic, mqttMessage);

            logger.info("Data Correctly Published to topic: {}", topic);

        }
        else
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");

    }

}
