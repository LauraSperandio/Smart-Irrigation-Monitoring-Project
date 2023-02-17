package it.unimore.fum.iot.smartIrrigation.resource;

//import it.unimore.fum.iot.smartIrrigation.model.PeopleCounterDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class PresenceSensorResource extends PeopleCounterSmartObjectResource<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(PresenceSensorResource.class);

//    private PeopleCounterDescriptor updatedPeopleCounterDescriptorPC = new PeopleCounterDescriptor();

    private static final long UPDATE_PERIOD = 5000; //5 Seconds

    private static final long TASK_DELAY_TIME = 5000; //Seconds before starting the periodic update task

    public static final String RESOURCE_TYPE = "iot:peoplecountersensor:presence";

    private Integer updatedPresenceData;

//    public static final PeopleCounterDescriptor UpdatedPeopleCounterDescriptor = null;

    private Timer updateTimer = null;

    private Integer randomIn;

    private Integer randomOut;

    private Integer personeDentro;

    public PresenceSensorResource() {
        super(UUID.randomUUID().toString(), PresenceSensorResource.RESOURCE_TYPE);
        init();
    }

    public PresenceSensorResource(String id, String type) {
        super(id, type);
        init();
    }



    private void init() {
        try {

            Random random1 = new Random();
            // genera numero casuale tra 0 e 3
            this.randomIn = random1.nextInt(4);
//            updatedPeopleCounterDescriptor.setIn(randomIn);

            Random random2 = new Random();
            // genera numero casuale tra 0 e 3
            this.randomOut = random2.nextInt(4);
//            updatedPeopleCounterDescriptor.setOut(randomOut);

            this.personeDentro = (this.randomIn - this.randomOut);

            startPeriodicEventValueUpdateTask();

        }catch (Exception e) {
            logger.error("Error init Presence Resource Object ! Msg: {}", e.getLocalizedMessage());
        }
    }

    private void startPeriodicEventValueUpdateTask() {
        try{

            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            this.updateTimer = new Timer();
            this.updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Random random1 = new Random();
                    // genera numero casuale tra 0 e 3
                    randomIn = (randomIn + random1.nextInt(4));
 //                   updatedPeopleCounterDescriptor.setIn(randomIn);

                    Random random2 = new Random();
                    // genera numero casuale tra 0 e 3
                    randomOut = (randomOut + random2.nextInt(4));
//                    updatedPeopleCounterDescriptor.setOut(randomOut);

                    if(randomIn>randomOut) {
                        personeDentro = (randomIn - randomOut);
                    }else{
                        personeDentro = (randomOut - randomIn);
                    }
                    updatedPresenceData = personeDentro;
//                    updatedPeopleCounterDescriptor.setDiffpers(personeDentro);

                    notifyUpdatePC(updatedPresenceData);
                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);


        }catch (Exception e) {
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }
    }

/*    @Override
    public PeopleCounterDescriptor loadUpdatedValuePC() {
        return this.updatedPeopleCounterDescriptor;
    }

    public static void main(String[] args) {
        PresenceSensorResource presenceSensorResource = new PresenceSensorResource();

        logger.info("New {} Resource Created with Id: {} ! Updated Value: {}",
                presenceSensorResource.getType(),
                presenceSensorResource.getId());
//                presenceSensorResource.loadUpdatedValuePC()


        presenceSensorResource.addDataListenerPC(new ResourceDataListenerPC<PeopleCounterDescriptor>() {
            @Override
            public void onDataChanged(PeopleCounterSmartObjectResource<PeopleCounterDescriptor> resource, PeopleCounterDescriptor updatedValue) {
                if (resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    } */

    @Override
    public Integer loadUpdatedValuePC() {
        return this.updatedPresenceData;
    }

    public static void main(String[] args) {

        PresenceSensorResource presenceSensorResource = new PresenceSensorResource();
        logger.info("New {} Resource Created with Id: {} ! Person in the area: {}",
                presenceSensorResource.getType(),
                presenceSensorResource.getId(),
                presenceSensorResource.loadUpdatedValuePC());

        //Add Resource Listener
        presenceSensorResource.addDataListenerPC(new ResourceDataListenerPC<Integer>() {
            @Override
            public void onDataChanged(PeopleCounterSmartObjectResource<Integer> resource, Integer updatedValuePC) {
                if(resource != null && updatedValuePC != null)
                    logger.info("Device: {} -> New Presence Data Received: {}", resource.getId(), updatedValuePC);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }

        });

    }
}


