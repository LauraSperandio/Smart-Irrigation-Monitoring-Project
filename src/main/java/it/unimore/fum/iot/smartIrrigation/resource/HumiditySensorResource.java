package it.unimore.fum.iot.smartIrrigation.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class HumiditySensorResource extends EnvironmentalMonitoringSmartObjectResource<Double> {

    private static final Logger logger = LoggerFactory.getLogger(HumiditySensorResource.class);

    private static final double MIN_HUMIDITY = 0.0;

    private static final double MAX_HUMIDITY = 100.0;

    private static final double MIN_HUMIDITY_CHANGE = 0.1;

    private static final double MAX_HUMIDITY_CHANGE = 10.0;

    private static final long UPDATE_PERIOD = 5000; //5 Seconds

    private static final long TASK_DELAY_TIME = 5000; //Seconds before starting the periodic update task

    public static final String RESOURCE_TYPE = "iot:environmentalsensor:humidity";

    private double updatedHumidity;

    private Random random = null;

    private Timer updateTimer = null;

    public HumiditySensorResource() {
        super(UUID.randomUUID().toString(), HumiditySensorResource.RESOURCE_TYPE);
        init();
    }

    public HumiditySensorResource(String id, String type) {
        super(id, type);
        init();
    }

    /**
     * Init internal random humidity in th range [MIN_HUMIDITY, MAX_HUMIDITY]
     */
    private void init() {

        try {

            this.random = new Random(System.currentTimeMillis());
            this.updatedHumidity = MIN_HUMIDITY + this.random.nextDouble() * (MAX_HUMIDITY - MIN_HUMIDITY);

            startPeriodicEventValueUpdateTask();

        } catch (Exception e) {
            logger.error("Error init Humidity Resource Object ! Msg: {}", e.getLocalizedMessage());
        }

    }

    private void startPeriodicEventValueUpdateTask() {

        try {

            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            this.updateTimer = new Timer();
            this.updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (random.nextDouble() >= 0.5) {
                        updatedHumidity = updatedHumidity - (MIN_HUMIDITY_CHANGE + MAX_HUMIDITY_CHANGE * random.nextDouble());

                    } else {
                        updatedHumidity = updatedHumidity + (MIN_HUMIDITY_CHANGE + MAX_HUMIDITY_CHANGE * random.nextDouble());

                    }

                    //logger.info("Updated Humidity: {}", updatedTemperature);

                    if (updatedHumidity < 0) {
                        updatedHumidity = 0;
                    } else if (updatedHumidity > 100) {
                        updatedHumidity = 100;
                    }

                    notifyUpdate(updatedHumidity);

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        } catch (Exception e) {
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Double loadUpdatedValue() {
        return this.updatedHumidity;
    }

    public static void main(String[] args) {

        HumiditySensorResource humiditySensorResource = new HumiditySensorResource();
        logger.info("New {} Resource Created with Id: {} ! Humidity: {}",
                humiditySensorResource.getType(),
                humiditySensorResource.getId(),
                humiditySensorResource.loadUpdatedValue());



        //Add Resource Listener
        humiditySensorResource.addDataListener(new ResourceDataListenerEM<Double>()

        {
            @Override
            public void onDataChanged (EnvironmentalMonitoringSmartObjectResource < Double > resource, Double updatedValue){
                if (resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Humidity Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });
    }
}
