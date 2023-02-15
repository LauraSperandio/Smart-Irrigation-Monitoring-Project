package it.unimore.fum.iot.smartIrrigation.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimore.fum.iot.smartIrrigation.message.TelemetryMessage;
import it.unimore.fum.iot.smartIrrigation.resource.*;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static it.unimore.fum.iot.smartIrrigation.utils.MQTTConfigurationParameters.MQTT_BASIC_TOPIC;

public class PeopCountMQTTSmartObject {
    private static final Logger logger = LoggerFactory.getLogger(PeopCountMQTTSmartObject.class);

//    private static final String BASIC_TOPIC = "/iot/smartIrrigation/env-mon";

    private static final String TELEMETRY_PEOP_COUNT_TOPIC = "peop-count";

    private String peopCountId;

    private ObjectMapper mapper;

    private IMqttClient mqttClient;

    private Map<String, PeopleCounterSmartObjectResource> resourceMap;

    public PeopCountMQTTSmartObject() {
        this.mapper = new ObjectMapper();
    }

    public void init(String peopCountId, IMqttClient mqttClient, HashMap<String, PeopleCounterSmartObjectResource> resourceMap){

        this.peopCountId = peopCountId;
        this.mqttClient = mqttClient;
        this.resourceMap = resourceMap;

        logger.info("People Counter Object correctly created ! Resource Number: {}", resourceMap.keySet().size());
    }

    public void start(){

        try{

            if(this.mqttClient != null &&
                    this.peopCountId != null  && this.peopCountId.length() > 0 &&
                    this.resourceMap != null && resourceMap.keySet().size() > 0){

                logger.info("Starting People Counter Emulator ....");

//                registerToControlChannel();

                registerToAvailableResources();


            }

        }catch (Exception e){
            logger.error("Error Starting the People Counter Emulator ! Msg: {}", e.getLocalizedMessage());
        }

    }
    private void registerToAvailableResources(){
        try{

            this.resourceMap.entrySet().forEach(resourceEntry -> {

                if (resourceEntry.getKey() != null && resourceEntry.getValue() != null) {
                    PeopleCounterSmartObjectResource peopleCounterSmartObjectResource = resourceEntry.getValue();

                    logger.info("Registering to Resource {} (id: {}) notifications ...",
                            peopleCounterSmartObjectResource.getType(),
                            peopleCounterSmartObjectResource.getId());

                    if (peopleCounterSmartObjectResource.getType().equals(PresenceSensorResource.RESOURCE_TYPE) || peopleCounterSmartObjectResource.getType().equals(BrightnessSensorResource.RESOURCE_TYPE) || peopleCounterSmartObjectResource.getType().equals(HumiditySensorResource.RESOURCE_TYPE) || peopleCounterSmartObjectResource.getType().equals(RainSensorResource.RESOURCE_TYPE) || peopleCounterSmartObjectResource.getType().equals(TemperatureSensorResource.RESOURCE_TYPE)){
                        peopleCounterSmartObjectResource.addDataListenerPC(new ResourceDataListenerPC() {
                            @Override
                            public void onDataChanged(PeopleCounterSmartObjectResource resource, Object updatedValue) {


                                try {
                                    publishTelemetryData(String.format("%s/%s/%s/%s", MQTT_BASIC_TOPIC, peopCountId, TELEMETRY_PEOP_COUNT_TOPIC, resourceEntry.getKey()),
                                            new TelemetryMessage(peopleCounterSmartObjectResource.getType(), updatedValue));
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
