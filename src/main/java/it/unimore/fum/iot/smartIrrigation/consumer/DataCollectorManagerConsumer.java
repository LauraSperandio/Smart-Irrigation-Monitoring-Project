package it.unimore.fum.iot.smartIrrigation.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimore.fum.iot.smartIrrigation.message.ControlMessage;
import it.unimore.fum.iot.smartIrrigation.message.TelemetryMessage;
import it.unimore.fum.iot.smartIrrigation.resource.BatteryEMSensorResource;
import it.unimore.fum.iot.smartIrrigation.resource.BatteryICSensorResource;
import it.unimore.fum.iot.smartIrrigation.resource.RainSensorResource;
import it.unimore.fum.iot.smartIrrigation.resource.TemperatureSensorResource;
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

    private static final double MINIMUM_TEMPERATURE_LEVEL = 20.0;

    private static boolean isAlarmNotifiedEM = false;

    private static boolean isAlarmNotifiedIC = false;

    private static boolean isAlarmNotifiedIRR = false;

    private static final String ALARM_MESSAGE_STOP_IRRIGATION_TYPE = "stop_irrigation_message";

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

            // subscribe to topic for EM battery level and log if battery is under 20%
            client.subscribe(MQTTConfigurationParameters.TARGET_BATTERY_EM_TOPIC, (topic, msg) -> {
                //The topic variable contain the specific topic associated to the received message. Using MQTT wildcards
                //messaged from multiple and different topic can be received with the same subscription
                //The msg variable is a MqttMessage object containing all the information about the received message

                Optional<TelemetryMessage<Double>> telemetryMessageOptional = parseTelemetryMessagePayload(msg);

                if(telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType().equals(BatteryEMSensorResource.RESOURCE_TYPE)){

                    Double newBatteryLevel = telemetryMessageOptional.get().getDataValue();
                    logger.info("New Battery Telemetry Data Received ! Battery EM Level: {}", newBatteryLevel);

                    if(isBatteryLevelAlarm(newBatteryLevel) && !isAlarmNotifiedEM){
                        logger.info("BATTERY EM LEVEL ALARM DETECTED ! Sending Control Notification ...");
                        isAlarmNotifiedEM = true;
                    }


                }
            });


            // subscribe to topic for IC battery level and log if battery is under 20%
            client.subscribe(MQTTConfigurationParameters.TARGET_BATTERY_IC_TOPIC, (topic, msg) -> {
                //The topic variable contain the specific topic associated to the received message. Using MQTT wildcards
                //messaged from multiple and different topic can be received with the same subscription
                //The msg variable is a MqttMessage object containing all the information about the received message

                Optional<TelemetryMessage<Double>> telemetryMessageOptional = parseTelemetryMessagePayload(msg);

                if(telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType().equals(BatteryICSensorResource.RESOURCE_TYPE)){

                    Double newBatteryLevel = telemetryMessageOptional.get().getDataValue();
                    logger.info("New Battery Telemetry Data Received ! Battery IC Level: {}", newBatteryLevel);

                    if(isBatteryLevelAlarm(newBatteryLevel) && !isAlarmNotifiedIC){
                        logger.info("BATTERY IC LEVEL ALARM DETECTED ! Sending Control Notification ...");
                        isAlarmNotifiedIC = true;

                    }


                }
            });


            // subscribe to topic for temperature level and publish the stop to irrigation if temperature is under 20°C
            client.subscribe(MQTTConfigurationParameters.TARGET_TEMPERATURE_TOPIC, (topic, msg) -> {
                //The topic variable contain the specific topic associated to the received message. Using MQTT wildcards
                //messaged from multiple and different topic can be received with the same subscription
                //The msg variable is a MqttMessage object containing all the information about the received message

                Optional<TelemetryMessage<Double>> telemetryMessageOptional = parseTelemetryMessagePayload(msg);

                if(telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType().equals(TemperatureSensorResource.RESOURCE_TYPE)){

                    Double newTemperature = telemetryMessageOptional.get().getDataValue();
                    logger.info("New Temperature Telemetry Data Received ! Temperature: {}", newTemperature);

                    if(isLowTemperature(newTemperature) && !isAlarmNotifiedIRR){
                        logger.info("LOW TEMPERATURE DETECTED ! Sending Control Notification ...");
                        isAlarmNotifiedIRR = true;


                        publishIrrigationControlMessage(client, MQTTConfigurationParameters.CHANGE_IRRIGATION_TOPIC, new ControlMessage(ALARM_MESSAGE_STOP_IRRIGATION_TYPE, new HashMap(){
                            {
                                put("accensione", false);
                                put("policyConfiguration", "Day");
                                put("livelloIrrigazione", "LOW");
                                put("tipologiaIrrigazioneRotazione", false);
                            }
                        }));
                    }
                }
            });

            // subscribe to topic for rain and publish the stop to irrigation if it's raining
            client.subscribe(MQTTConfigurationParameters.TARGET_RAIN_TOPIC, (topic, msg) -> {
                //The topic variable contain the specific topic associated to the received message. Using MQTT wildcards
                //messaged from multiple and different topic can be received with the same subscription
                //The msg variable is a MqttMessage object containing all the information about the received message

                Optional<TelemetryMessage<Boolean>> telemetryMessageOptional = parseTelemetryMessagePayload(msg);

                if(telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType().equals(RainSensorResource.RESOURCE_TYPE)){

                    Boolean newRainData = telemetryMessageOptional.get().getDataValue();
                    logger.info("New Rain Telemetry Data Received ! it's raining: {}", newRainData);

/*                    if(newRainData && !isAlarmNotifiedIRR){
                        logger.info("LOW TEMPERATURE DETECTED ! Sending Control Notification ...");
                        isAlarmNotifiedIRR = true;


                        publishIrrigationControlMessage(client, MQTTConfigurationParameters.CHANGE_IRRIGATION_TOPIC, new ControlMessage(ALARM_MESSAGE_STOP_IRRIGATION_TYPE, new HashMap(){
                            {
                                put("accensione", false);
                                put("policyConfiguration", "Day");
                                put("livelloIrrigazione", "LOW");
                                put("tipologiaIrrigazioneRotazione", false);
                            }
                        }));
                    }   */
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static boolean isBatteryLevelAlarm(Double newValue){
        return newValue <= ALARM_BATTERY_LEVEL;
    }

    private static boolean isLowTemperature(Double newValue){
        return newValue <= MINIMUM_TEMPERATURE_LEVEL;
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

    private static void publishIrrigationControlMessage(IMqttClient mqttClient, String topic, ControlMessage controlMessage) throws MqttException, JsonProcessingException {

        logger.info("Sending to topic: {} -> Data: {}", topic, controlMessage);

        if(mqttClient != null && mqttClient.isConnected() && controlMessage != null && topic != null){

            String messagePayload = mapper.writeValueAsString(controlMessage);

            MqttMessage mqttMessage = new MqttMessage(messagePayload.getBytes());
            mqttMessage.setQos(1);

            mqttClient.publish(topic, mqttMessage);

            logger.info("Data Correctly Published to topic: {}", topic);

        }
        else
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");

    }

}