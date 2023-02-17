package it.unimore.fum.iot.smartIrrigation.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimore.fum.iot.smartIrrigation.message.ControlMessage;
import it.unimore.fum.iot.smartIrrigation.message.TelemetryMessage;
import it.unimore.fum.iot.smartIrrigation.resource.*;
import it.unimore.fum.iot.smartIrrigation.utils.MQTTConfigurationParameters;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import java.util.Map;
import java.util.Optional;

import static it.unimore.fum.iot.smartIrrigation.utils.MQTTConfigurationParameters.MQTT_BASIC_TOPIC;

public class IrrConMQTTSmartObject {
    private static final Logger logger = LoggerFactory.getLogger(IrrConMQTTSmartObject.class);

//    private static final String BASIC_TOPIC = "/iot/smartIrrigation/env-mon";

//    private static final String TELEMETRY_IRR_CON_TOPIC = "irr-con";

    private String IrrConID;

    private static ObjectMapper mapper;

    private IMqttClient mqttClient;

    private boolean isStoppedIRR = false;

    private Map<String, IrrigationControllerSmartObjectResource> resourceMap;

    public IrrConMQTTSmartObject() {
        this.mapper = new ObjectMapper();
    }

    public void init(String IrrConID, IMqttClient mqttClient, HashMap<String, IrrigationControllerSmartObjectResource> resourceMap){

        this.IrrConID = IrrConID;
        this.mqttClient = mqttClient;
        this.resourceMap = resourceMap;

        logger.info("Irrigation Controller Smart Object correctly created ! Resource Number: {}", resourceMap.keySet().size());
    }

    public void start(){

        try{

            if(this.mqttClient != null &&
                    this.IrrConID != null  && this.IrrConID.length() > 0 &&
                    this.resourceMap != null && resourceMap.keySet().size() > 0){

                logger.info("Starting Irrigation Controller Emulator ....");

//                registerToControlChannel();

                registerToAvailableResources();

                registerToControlChannel();


            }

        }catch (Exception e){
            logger.error("Error Starting the Irrigation Controller Emulator ! Msg: {}", e.getLocalizedMessage());
        }

    }

    private void registerToControlChannel() throws MqttException {

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);

        options.setUserName(MQTTConfigurationParameters.MQTT_USERNAME);
        options.setPassword((MQTTConfigurationParameters.MQTT_PASSWORD).toCharArray());

        try{
            //Connect to the target broker
            this.mqttClient.connect(options);

            logger.info("Connected ! Client Id: {}", this.mqttClient);
            // subscribe to topic for rain and publish the stop to irrigation if it's raining
            this.mqttClient.subscribe(MQTTConfigurationParameters.TARGET_CHANGE_IRRIGATION_TOPIC, (topic, msg) -> {
                //The topic variable contain the specific topic associated to the received message. Using MQTT wildcards
                //messaged from multiple and different topic can be received with the same subscription
                //The msg variable is a MqttMessage object containing all the information about the received message

                Optional<TelemetryMessage<Boolean>> telemetryMessageOptional = parseTelemetryMessagePayloadBool(msg);

                if(telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType().equals(PresenceSensorResource.RESOURCE_TYPE)){

                    Boolean newStopIrr = telemetryMessageOptional.get().getDataValue();
                    logger.info("New Rain Telemetry Data Received ! it's raining: {}", newStopIrr);

                    if(newStopIrr && !isStoppedIRR){
                        logger.info("IT'S RAINING ! Sending Control Notification ...");
                        isStoppedIRR = true;
    //                    accensione = false;

    //                    updatedIrrigationControllerDescriptor.setAccensione(accensione);

                    }
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void registerToAvailableResources(){
        try{

            this.resourceMap.entrySet().forEach(resourceEntry -> {

                if (resourceEntry.getKey() != null && resourceEntry.getValue() != null) {
                    IrrigationControllerSmartObjectResource irrigationControllerSmartObjectResource = resourceEntry.getValue();

                    logger.info("Registering to Resource {} (id: {}) notifications ...",
                            irrigationControllerSmartObjectResource.getType(),
                            irrigationControllerSmartObjectResource.getId());

                    if (irrigationControllerSmartObjectResource.getType().equals(IrrigationSensorActuatorResource.RESOURCE_TYPE) || irrigationControllerSmartObjectResource.getType().equals(BatteryICSensorResource.RESOURCE_TYPE)){
                        irrigationControllerSmartObjectResource.addDataListenerIC(new ResourceDataListenerIC() {
                            @Override
                            public void onDataChanged(IrrigationControllerSmartObjectResource resource, Object updatedValue) {


                                try {
                                    publishTelemetryData(String.format("%s/%s/%s/%s", MQTT_BASIC_TOPIC, IrrConID, MQTTConfigurationParameters.TELEMETRY_IRR_CON_TOPIC, resourceEntry.getKey()),
                                            new TelemetryMessage(irrigationControllerSmartObjectResource.getType(), updatedValue));
                                } catch (MqttException | JsonProcessingException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                    }

                }
            });
        }catch (Exception e){
            logger.error("Error Registering to Resource ! Msg: {}", e.getLocalizedMessage());

        }
    }

    private void publishTelemetryData(String topic, TelemetryMessage telemetryMessage) throws MqttException, JsonProcessingException {

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    logger.info("Sending to topic: {} -> Data: {}", topic, telemetryMessage);

                    if(mqttClient != null && mqttClient.isConnected() && telemetryMessage != null && topic != null){

                        String messagePayload = mapper.writeValueAsString(telemetryMessage);

                        MqttMessage mqttMessage = new MqttMessage(messagePayload.getBytes());
                        mqttMessage.setQos(0);

                        mqttClient.publish(topic, mqttMessage);

                        logger.info("Data Correctly Published to topic: {}", topic);

                    }
                    else
                        logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

    }

    private static Optional<TelemetryMessage<Boolean>> parseTelemetryMessagePayloadBool(MqttMessage mqttMessage){

        try{

            if(mqttMessage == null)
                return Optional.empty();

            byte[] payloadByteArray = mqttMessage.getPayload();
            String payloadString = new String(payloadByteArray);

            return Optional.of(mapper.readValue(payloadString, new TypeReference<TelemetryMessage<Boolean>>() {}));


        }catch (Exception e){
            return Optional.empty();
        }
    }

}
