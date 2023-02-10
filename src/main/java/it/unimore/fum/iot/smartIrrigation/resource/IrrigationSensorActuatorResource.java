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

    private String accens;

    private String policy;

    private String livelloIrr;

    private String tipologiaIrr;

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
            accens = "OFF";
            policy = "Week Day";
            livelloIrr = "Medium";
            tipologiaIrr = "Rotation ON";

            updatedIrrigationControllerDescriptor.setPolicyConfiguration(accens);

            updatedIrrigationControllerDescriptor.setPolicyConfiguration(policy);

            updatedIrrigationControllerDescriptor.setLivelloIrrigazione(livelloIrr);

            updatedIrrigationControllerDescriptor.setTipologiaIrrigazione(tipologiaIrr);

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
                    accens = "OFF";
                    policy = "Week Day";
                    livelloIrr = "Medium";
                    tipologiaIrr = "Rotation ON";
                    updatedIrrigationControllerDescriptor.setAccensione(accens);
                    updatedIrrigationControllerDescriptor.setPolicyConfiguration(policy);
                    updatedIrrigationControllerDescriptor.setLivelloIrrigazione(livelloIrr);
                    updatedIrrigationControllerDescriptor.setTipologiaIrrigazione(tipologiaIrr);

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


