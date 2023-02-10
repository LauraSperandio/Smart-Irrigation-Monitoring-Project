package it.unimore.fum.iot.smartIrrigation.resource;




public interface ResourceDataListenerIC<K> {

    public void onDataChanged(IrrigationControllerSmartObjectResource<K> resource, K updatedValue);

}
