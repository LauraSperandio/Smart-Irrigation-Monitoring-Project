package it.unimore.fum.iot.smartIrrigation.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BrightnessSensorResource extends EnvironmentalMonitoringSmartObjectResource<Double> {

    private static final Logger logger = LoggerFactory.getLogger(BrightnessSensorResource.class);

    private static final double MIN_BRIGHTNESS = 0.0;

    private static final double MAX_BRIGHTNESS = 1000.0;

    private static final double MIN_BRIGHTNESS_CHANGE = 0.1;

    private static final double MAX_BRIGHTNESS_CHANGE = 10.0;

    private static final long UPDATE_PERIOD = 5000; //5 Seconds

    private static final long TASK_DELAY_TIME = 5000; //Seconds before starting the periodic update task

    public static final String RESOURCE_TYPE = "iot:environmentalsensor:Brightness";

    private double updatedBrightness;

    private Random random = null;

    private Timer updateTimer = null;

    public BrightnessSensorResource() {
        super(UUID.randomUUID().toString(), BrightnessSensorResource.RESOURCE_TYPE);
        init();
    }

    public BrightnessSensorResource(String id, String type) {
        super(id, type);
        init();
    }

    /**
     * Init internal random Brightness in th range [MIN_BRIGHTNESS, MAX_BRIGHTNESS]
     */
    private void init() {

        try {

            this.random = new Random(System.currentTimeMillis());

            this.updatedBrightness = MIN_BRIGHTNESS + this.random.nextDouble() * (MAX_BRIGHTNESS - MIN_BRIGHTNESS);

            startPeriodicEventValueUpdateTask();

        } catch (Exception e) {
            logger.error("Error init Brightness Resource Object ! Msg: {}", e.getLocalizedMessage());
        }

    }

    private void startPeriodicEventValueUpdateTask() {

        try {

            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            this.updateTimer = new Timer();
            this.updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updatedBrightness = updatedBrightness - (MIN_BRIGHTNESS_CHANGE + MAX_BRIGHTNESS_CHANGE * random.nextDouble());
                    //logger.info("Updated Brightness: {}", updatedBrightness);

                    if (random.nextDouble() >= 0.5) {
                        updatedBrightness = updatedBrightness - (MIN_BRIGHTNESS_CHANGE + MAX_BRIGHTNESS_CHANGE * random.nextDouble());

                      } else {
                        updatedBrightness = updatedBrightness + (MIN_BRIGHTNESS_CHANGE + MAX_BRIGHTNESS_CHANGE * random.nextDouble());

                    }

                    if (updatedBrightness < 0) {
                        updatedBrightness = 0;
                    } else if (updatedBrightness > 1000) {
                        updatedBrightness = 1000;
                    }

                    notifyUpdate(updatedBrightness);

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        } catch (Exception e) {
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Double loadUpdatedValue() {
        return this.updatedBrightness;
    }

    public static void main(String[] args) {

        BrightnessSensorResource brightnessSensorResource = new BrightnessSensorResource();
        logger.info("New {} Resource Created with Id: {} ! Brightness: {}",
                brightnessSensorResource.getType(),
                brightnessSensorResource.getId(),
                brightnessSensorResource.loadUpdatedValue());



        //Add Resource Listener
        brightnessSensorResource.addDataListener(new ResourceDataListenerEM<Double>()

        {
            @Override
            public void onDataChanged (EnvironmentalMonitoringSmartObjectResource < Double > resource, Double updatedValue){
                if (resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Brightness Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });
    }
}
