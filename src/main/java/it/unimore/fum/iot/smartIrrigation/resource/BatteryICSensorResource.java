package it.unimore.fum.iot.smartIrrigation.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class BatteryICSensorResource extends IrrigationControllerSmartObjectResource<Double> {

    private static final Logger logger = LoggerFactory.getLogger(BatteryICSensorResource.class);

    private static final double MIN_BATTERY_LEVEL = 50.0;

    private static final double MAX_BATTERY_LEVEL = 70.0;

    private static final double MIN_BATTERY_LEVEL_CONSUMPTION = 0.1;

    private static final double MAX_BATTERY_LEVEL_CONSUMPTION = 1.0;

    private static final long UPDATE_PERIOD = 5000; //5 Seconds

    private static final long TASK_DELAY_TIME = 5000; //Seconds before starting the periodic update task

    public static final String RESOURCE_TYPE = "iot:irrigationcontroller:battery";

    private double updatedBatteryLevel;

    private Random random = null;

    private Timer updateTimer = null;

    public BatteryICSensorResource() {
        super(UUID.randomUUID().toString(), BatteryICSensorResource.RESOURCE_TYPE);
        init();
    }

    public BatteryICSensorResource(String id, String type) {
        super(id, type);
        init();
    }


    /**
     * Init internal random battery level in th range [MIN_BATTERY_LEVEL, MAX_BATTERY_LEVEL]
     */
    private void init(){

        try{

            this.random = new Random(System.currentTimeMillis());
            this.updatedBatteryLevel = MIN_BATTERY_LEVEL + this.random.nextDouble()*(MAX_BATTERY_LEVEL - MIN_BATTERY_LEVEL);

            startPeriodicEventValueUpdateTask();

        }catch (Exception e){
            logger.error("Error init Battery Resource Object ! Msg: {}", e.getLocalizedMessage());
        }

    }

    private void startPeriodicEventValueUpdateTask(){

        try{

            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            this.updateTimer = new Timer();
            this.updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updatedBatteryLevel = updatedBatteryLevel - (MIN_BATTERY_LEVEL_CONSUMPTION + MAX_BATTERY_LEVEL_CONSUMPTION * random.nextDouble());
                    //logger.info("Updated Battery Level: {}", updatedBatteryLevel);

                    if (updatedBatteryLevel < 0) {
                        logger.info("Battery Charging");
                        updatedBatteryLevel = 100;
                    }

                    notifyUpdateIC(updatedBatteryLevel);

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Double loadUpdatedValueIC() {
        return this.updatedBatteryLevel;
    }

    public static void main(String[] args) {

        BatteryICSensorResource batteryICSensorResource = new BatteryICSensorResource();
        logger.info("New {} Resource Created with Id: {} ! Battery Level: {}",
                batteryICSensorResource.getType(),
                batteryICSensorResource.getId(),
                batteryICSensorResource.loadUpdatedValueIC());

        //Add Resource Listener
        batteryICSensorResource.addDataListenerIC(new ResourceDataListenerIC<Double>() {
            @Override
            public void onDataChanged(IrrigationControllerSmartObjectResource<Double> resource, Double updatedValue) {
                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Battery Level Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    }

}
