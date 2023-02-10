package it.unimore.fum.iot.smartIrrigation.resource;




public interface ResourceDataListenerEM<K> {

    public void onDataChanged(EnvironmentalMonitoringSmartObjectResource<K> resource, K updatedValue);

}
