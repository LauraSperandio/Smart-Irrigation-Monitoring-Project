package it.unimore.fum.iot.smartIrrigation.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class RainSensorResource extends EnvironmentalMonitoringSmartObjectResource<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(RainSensorResource.class);

    private static final long UPDATE_PERIOD = 60000; //1 minutes

    private static final long TASK_DELAY_TIME = 5000; //Seconds before starting the periodic update task

    public static final String RESOURCE_TYPE = "iot:environmentalsensor:rain";

    public static boolean updatedRain;

    private boolean randombool;

    private Timer updateTimer = null;

    public RainSensorResource() {
        super(UUID.randomUUID().toString(), RainSensorResource.RESOURCE_TYPE);
        init();
    }

    public RainSensorResource(String id, String type) {
        super(id, type);
        init();
    }

    /**
     * Init internal random Rain
     */
    private void init(){

        try{

            Random random = new Random();

            randombool = random.nextBoolean();

            updatedRain = randombool;

            startPeriodicEventValueUpdateTask();

        }catch (Exception e){
            logger.error("Error init Rain Resource Object ! Msg: {}", e.getLocalizedMessage());
        }

    }

    private void startPeriodicEventValueUpdateTask(){

        try{

            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            this.updateTimer = new Timer();
            this.updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    Random random = new Random();

                    randombool = random.nextBoolean();

                    updatedRain = randombool;

                    notifyUpdate(updatedRain);

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Boolean loadUpdatedValue() {
        return this.updatedRain;
    }

    public static void main(String[] args) {

        RainSensorResource rainSensorResource = new RainSensorResource();
        logger.info("New {} Resource Created with Id: {} ! Rain: {}",
                rainSensorResource.getType(),
                rainSensorResource.getId(),
                rainSensorResource.loadUpdatedValue());

        //Add Resource Listener
        rainSensorResource.addDataListener(new ResourceDataListenerEM<Boolean>() {
            @Override
            public void onDataChanged(EnvironmentalMonitoringSmartObjectResource<Boolean> resource, Boolean updatedValue) {
                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Rain Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    }

}
