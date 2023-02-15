package it.unimore.fum.iot.smartIrrigation.resource;

import it.unimore.fum.iot.smartIrrigation.model.IrrigationControllerDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class IrrigationSensorActuatorResource extends IrrigationControllerSmartObjectResource<IrrigationControllerDescriptor> {

    private static final Logger logger = LoggerFactory.getLogger(IrrigationSensorActuatorResource.class);

    private IrrigationControllerDescriptor updatedIrrigationControllerDescriptor = new IrrigationControllerDescriptor();

    private static final long UPDATE_PERIOD = 30000; //5 Seconds

    private static final long TASK_DELAY_TIME = 5000; //Seconds before starting the periodic update task

    public static final String RESOURCE_TYPE = "iot:env-irr:status";

    public static final IrrigationControllerDescriptor UpdatedIrrigationControllerDescriptor = null;

    private Timer updateTimer = null;

    private Boolean accensione = false;

    private String policyConfiguration = "Day";

    private String livelloIrrigazione = "LOW";

    private Boolean tipologiaIrrigazioneRotazione = false;

    public IrrigationSensorActuatorResource() {
        super(UUID.randomUUID().toString(), IrrigationSensorActuatorResource.RESOURCE_TYPE);
        init();
    }

    public IrrigationSensorActuatorResource(String id, String type) {
        super(id, type);
        init();
    }

    private void init() {
        try {

/*            accensione = true;
            policyConfiguration = "Week Day";
            livelloIrrigazione = "Medium";
            tipologiaIrrigazioneRotazione = "Rotation ON";

            updatedIrrigationControllerDescriptor.setPolicyConfiguration(accensione);

            updatedIrrigationControllerDescriptor.setPolicyConfiguration(policyConfiguration);

            updatedIrrigationControllerDescriptor.setLivelloIrrigazione(livelloIrrigazione);

            updatedIrrigationControllerDescriptor.setTipologiaIrrigazione(tipologiaIrrigazioneRotazione);
*/
            logger.info("Configuration automatic policy correctly loaded !");

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
                    accensione = false;
                    policyConfiguration = "Week Day";
                    livelloIrrigazione = "Medium";
                    tipologiaIrrigazioneRotazione = false;
                    updatedIrrigationControllerDescriptor.setAccensione(accensione);
                    updatedIrrigationControllerDescriptor.setPolicyConfiguration(policyConfiguration);
                    updatedIrrigationControllerDescriptor.setLivelloIrrigazione(livelloIrrigazione);
                    updatedIrrigationControllerDescriptor.setTipologiaIrrigazione(tipologiaIrrigazioneRotazione);

                    notifyUpdateIC(updatedIrrigationControllerDescriptor);
                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);


        }catch (Exception e) {
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public IrrigationControllerDescriptor loadUpdatedValueIC() {
        return this.updatedIrrigationControllerDescriptor;
    }

    public static void main(String[] args) {
        IrrigationSensorActuatorResource irrigationSensorActuatorResource = new IrrigationSensorActuatorResource();

        logger.info("New {} Resource Created with Id: {} ! Updated Value: {}",
                irrigationSensorActuatorResource.getType(),
                irrigationSensorActuatorResource.getId());
//                presenceSensorResource.loadUpdatedValuePC()



        irrigationSensorActuatorResource.addDataListenerIC(new ResourceDataListenerIC<IrrigationControllerDescriptor>() {
            @Override
            public void onDataChanged(IrrigationControllerSmartObjectResource<IrrigationControllerDescriptor> resource, IrrigationControllerDescriptor updatedValue) {
                if (resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });
    }
}


