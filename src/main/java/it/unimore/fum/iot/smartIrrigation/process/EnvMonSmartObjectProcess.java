package it.unimore.fum.iot.smartIrrigation.process;

import it.unimore.fum.iot.smartIrrigation.device.EnvMonMQTTSmartObject;
import it.unimore.fum.iot.smartIrrigation.resource.*;
import it.unimore.fum.iot.smartIrrigation.utils.MQTTConfigurationParameters;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;


public class EnvMonSmartObjectProcess {

    private static final Logger logger = LoggerFactory.getLogger(EnvMonSmartObjectProcess.class);

    public static void main(String[] args) {

        try {

            //Generate Random envMon UUID
            String envMonId = UUID.randomUUID().toString();

            //Create MQTT Client
            MqttClientPersistence persistence = new MemoryPersistence();
            IMqttClient mqttClient = new MqttClient(String.format("tcp://%s:%s",
                    MQTTConfigurationParameters.BROKER_ADDRESS,
                    MQTTConfigurationParameters.BROKER_PORT),
                    envMonId,
                    persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            options.setUserName(MQTTConfigurationParameters.MQTT_USERNAME);
            options.setPassword((MQTTConfigurationParameters.MQTT_PASSWORD).toCharArray());

            //Connect to MQTT Broker
            mqttClient.connect(options);

            logger.info("MQTT Client Connected ! Client Id: {}", envMonId);

            EnvMonMQTTSmartObject envMonMQTTSmartObject = new EnvMonMQTTSmartObject();
            envMonMQTTSmartObject.init(envMonId, mqttClient, new HashMap<String, EnvironmentalMonitoringSmartObjectResource>(){
                {
                    put("battery", new BatteryEMSensorResource());
                    put("brightness", new BrightnessSensorResource());
                    put("humidity", new HumiditySensorResource());
                    put("temperature", new TemperatureSensorResource());
                    put("rain", new RainSensorResource());
                }
            });
            envMonMQTTSmartObject.start();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
