package it.unimore.fum.iot.smartIrrigation.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimore.fum.iot.smartIrrigation.message.TelemetryMessage;
import it.unimore.fum.iot.smartIrrigation.resource.*;
import it.unimore.fum.iot.smartIrrigation.utils.MQTTConfigurationParameters;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static it.unimore.fum.iot.smartIrrigation.utils.MQTTConfigurationParameters.MQTT_BASIC_TOPIC;

public class IrrConMQTTSmartObject {
    private static final Logger logger = LoggerFactory.getLogger(IrrConMQTTSmartObject.class);

//    private static final String BASIC_TOPIC = "/iot/smartIrrigation/env-mon";

//    private static final String TELEMETRY_IRR_CON_TOPIC = "irr-con";

    private String IrrConID;

    private ObjectMapper mapper;

    private IMqttClient mqttClient;

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


            }

        }catch (Exception e){
            logger.error("Error Starting the Irrigation Controller Emulator ! Msg: {}", e.getLocalizedMessage());
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

        logger.info("Sending to topic: {} -> Data: {}", topic, telemetryMessage);

        if(this.mqttClient != null && this.mqttClient.isConnected() && telemetryMessage != null && topic != null){

            String messagePayload = mapper.writeValueAsString(telemetryMessage);

            MqttMessage mqttMessage = new MqttMessage(messagePayload.getBytes());
            mqttMessage.setQos(0);

            mqttClient.publish(topic, mqttMessage);

            logger.info("Data Correctly Published to topic: {}", topic);

        }
        else
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");

    }


}
