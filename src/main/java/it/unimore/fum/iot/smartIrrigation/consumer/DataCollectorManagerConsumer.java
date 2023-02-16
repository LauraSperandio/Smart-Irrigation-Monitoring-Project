package it.unimore.fum.iot.smartIrrigation.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimore.fum.iot.smartIrrigation.message.TelemetryMessage;
import it.unimore.fum.iot.smartIrrigation.resource.BatteryEMSensorResource;
import it.unimore.fum.iot.smartIrrigation.resource.BatteryICSensorResource;
import it.unimore.fum.iot.smartIrrigation.utils.MQTTConfigurationParameters;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class DataCollectorManagerConsumer {

    private final static Logger logger = LoggerFactory.getLogger(SimpleTestConsumer.class);

    private static final double ALARM_BATTERY_LEVEL = 20.0;

    private static boolean isAlarmNotifiedEM = false;

    private static boolean isAlarmNotifiedIC = false;

    private static ObjectMapper mapper;


    //IP Address of the target MQTT Broker
//    private static String BROKER_ADDRESS = "155.185.228.20";

    //PORT of the target MQTT Broker
//    private static int BROKER_PORT = 7883;

//    private static final String TARGET_BATTERY_TOPIC = "/iot/user/262716@studenti.unimore.it/+/env-mon/battery";

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
                    String.format("tcp://%s:%d", MQTTConfigurationParameters.BROKER_ADDRESS, MQTTConfigurationParameters.BROKER_PORT), //Create the URL from IP and PORT
                    clientId,
                    persistence);

            //Define MQTT Connection Options such as reconnection, persistent/clean session and connection timeout
            //Authentication option can be added -> See AuthProducer example
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            options.setUserName(MQTTConfigurationParameters.MQTT_USERNAME);
            options.setPassword((MQTTConfigurationParameters.MQTT_PASSWORD).toCharArray());

            //Connect to the target broker
            client.connect(options);

            logger.info("Connected ! Client Id: {}", clientId);

            mapper = new ObjectMapper();

            //Subscribe to the target topic #. In that case the consumer will receive (if authorized) all the message
            //passing through the broker
            client.subscribe(MQTTConfigurationParameters.TARGET_BATTERY_EM_TOPIC, (topic, msg) -> {
                //The topic variable contain the specific topic associated to the received message. Using MQTT wildcards
                //messaged from multiple and different topic can be received with the same subscription
                //The msg variable is a MqttMessage object containing all the information about the received message

                Optional<TelemetryMessage<Double>> telemetryMessageOptional = parseTelemetryMessagePayload(msg);

                if(telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType().equals(BatteryEMSensorResource.RESOURCE_TYPE) || telemetryMessageOptional.get().getType().equals(BatteryICSensorResource.RESOURCE_TYPE)){

                    Double newBatteryLevel = telemetryMessageOptional.get().getDataValue();
                    logger.info("New Battery Telemetry Data Received ! Battery EM Level: {}", newBatteryLevel);

                    if(isBatteryLevelAlarm(newBatteryLevel) && !isAlarmNotifiedEM){
                        logger.info("BATTERY EM LEVEL ALARM DETECTED ! Sending Control Notification ...");
                        isAlarmNotifiedEM = true;
                    }


                }
            });

            client.subscribe(MQTTConfigurationParameters.TARGET_BATTERY_IC_TOPIC, (topic, msg) -> {
                //The topic variable contain the specific topic associated to the received message. Using MQTT wildcards
                //messaged from multiple and different topic can be received with the same subscription
                //The msg variable is a MqttMessage object containing all the information about the received message

                Optional<TelemetryMessage<Double>> telemetryMessageOptional = parseTelemetryMessagePayload(msg);

                if(telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType().equals(BatteryEMSensorResource.RESOURCE_TYPE) || telemetryMessageOptional.get().getType().equals(BatteryICSensorResource.RESOURCE_TYPE)){

                    Double newBatteryLevel = telemetryMessageOptional.get().getDataValue();
                    logger.info("New Battery Telemetry Data Received ! Battery IC Level: {}", newBatteryLevel);

                    if(isBatteryLevelAlarm(newBatteryLevel) && !isAlarmNotifiedIC){
                        logger.info("BATTERY IC LEVEL ALARM DETECTED ! Sending Control Notification ...");
                        isAlarmNotifiedIC = true;

                    }


                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static boolean isBatteryLevelAlarm(Double newValue){
        return newValue <= ALARM_BATTERY_LEVEL;
    }

    private static Optional<TelemetryMessage<Double>> parseTelemetryMessagePayload(MqttMessage mqttMessage){

        try{

            if(mqttMessage == null)
                return Optional.empty();

            byte[] payloadByteArray = mqttMessage.getPayload();
            String payloadString = new String(payloadByteArray);

            return Optional.of(mapper.readValue(payloadString, new TypeReference<TelemetryMessage<Double>>() {}));


        }catch (Exception e){
            return Optional.empty();
        }
    }

}
