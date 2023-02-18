package it.unimore.fum.iot.smartIrrigation.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class TemperatureSensorResource extends EnvironmentalMonitoringSmartObjectResource<Double> {

    private static final Logger logger = LoggerFactory.getLogger(TemperatureSensorResource.class);

    private static final double MIN_TEMPERATURE = 10.0;

    private static final double MAX_TEMPERATURE = 35.0;

    private static final double MIN_TEMPERATURE_CHANGE = 0.1;

    private static final double MAX_TEMPERATURE_CHANGE = 1.0;

    private static final long UPDATE_PERIOD = 5000; //5 Seconds

    private static final long TASK_DELAY_TIME = 5000; //Seconds before starting the periodic update task

    public static final String RESOURCE_TYPE = "iot:environmentalsensor:temperature";

    private double updatedTemperature;

    private Random random = null;

    private Timer updateTimer = null;

    public TemperatureSensorResource() {
        super(UUID.randomUUID().toString(), TemperatureSensorResource.RESOURCE_TYPE);
        init();
    }

    public TemperatureSensorResource(String id, String type) {
        super(id, type);
        init();
    }

    /**
     * Init internal random temperature in th range [MIN_TEMPERATURE, MAX_TEMPERATURE]
     */
    private void init() {

        try {

            this.random = new Random(System.currentTimeMillis());
            this.updatedTemperature = MIN_TEMPERATURE + this.random.nextDouble() * (MAX_TEMPERATURE - MIN_TEMPERATURE);

            startPeriodicEventValueUpdateTask();

        } catch (Exception e) {
            logger.error("Error init Temperature Resource Object ! Msg: {}", e.getLocalizedMessage());
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
                        updatedTemperature = updatedTemperature - (MIN_TEMPERATURE_CHANGE + MAX_TEMPERATURE_CHANGE * random.nextDouble());

                    } else {
                        updatedTemperature = updatedTemperature + (MIN_TEMPERATURE_CHANGE + MAX_TEMPERATURE_CHANGE * random.nextDouble());

                    }

                    if (updatedTemperature < 10.0) {
                        updatedTemperature = 10.0;
                    } else if (updatedTemperature > 35.0) {
                        updatedTemperature = 35.0;
                    }

                    notifyUpdate(updatedTemperature);

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        } catch (Exception e) {
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Double loadUpdatedValue() {
        return this.updatedTemperature;
    }

    public static void main(String[] args) {

        TemperatureSensorResource temperatureSensorResource = new TemperatureSensorResource();
        logger.info("New {} Resource Created with Id: {} ! Temperature: {}",
                temperatureSensorResource.getType(),
                temperatureSensorResource.getId(),
                temperatureSensorResource.loadUpdatedValue());



        //Add Resource Listener
            temperatureSensorResource.addDataListener(new ResourceDataListenerEM<Double>()

        {
            @Override
            public void onDataChanged (EnvironmentalMonitoringSmartObjectResource < Double > resource, Double updatedValue){
            if (resource != null && updatedValue != null)
                logger.info("Device: {} -> New Temperature Received: {}", resource.getId(), updatedValue);
            else
                logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
        }
        });
    }
}
