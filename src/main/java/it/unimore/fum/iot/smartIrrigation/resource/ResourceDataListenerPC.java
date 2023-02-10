package it.unimore.fum.iot.smartIrrigation.resource;




public interface ResourceDataListenerPC<K> {

    public void onDataChanged(PeopleCounterSmartObjectResource<K> resource, K updatedValue);

}
