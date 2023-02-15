package it.unimore.fum.iot.smartIrrigation.process;


import it.unimore.fum.iot.smartIrrigation.device.IrrConMQTTSmartObject;
import it.unimore.fum.iot.smartIrrigation.resource.EnvironmentalMonitoringSmartObjectResource;
import it.unimore.fum.iot.smartIrrigation.resource.IrrigationControllerSmartObjectResource;
import it.unimore.fum.iot.smartIrrigation.resource.IrrigationSensorActuatorResource;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;

import static it.unimore.fum.iot.smartIrrigation.process.MQTTConfigurationParameters.BROKER_ADDRESS;
import static it.unimore.fum.iot.smartIrrigation.process.MQTTConfigurationParameters.BROKER_PORT;


public class IrrConSmartObjectProcess {

    private static final Logger logger = LoggerFactory.getLogger(IrrConSmartObjectProcess.class);

//    private static String MQTT_BROKER_IP = "127.0.0.1";

//    private static int MQTT_BROKER_PORT = 1883;

    public static void main(String[] args) {

        try{

            //Generate Random Irrigation Controller UUID
            String irrConID = UUID.randomUUID().toString();

            //Create MQTT Client
            MqttClientPersistence persistence = new MemoryPersistence();
            IMqttClient mqttClient = new MqttClient(String.format("tcp://%s:%s",
                    BROKER_ADDRESS,
                    BROKER_PORT),
                    irrConID,
                    persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            //Connect to MQTT Broker
            mqttClient.connect(options);

            logger.info("MQTT Client Connected ! Client Id: {}", irrConID);

            IrrConMQTTSmartObject irrConMQTTSmartObject = new IrrConMQTTSmartObject();
            irrConMQTTSmartObject.init(irrConID, mqttClient, new HashMap<String, IrrigationControllerSmartObjectResource>(){
                {
                    put("status", new IrrigationSensorActuatorResource());
                }
            });

            irrConMQTTSmartObject.start();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}

