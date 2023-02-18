package it.unimore.fum.iot.smartIrrigation.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class PeopleCounterSmartObjectResource<T> {

    private static final Logger logger = LoggerFactory.getLogger(PeopleCounterSmartObjectResource.class);

    protected List<ResourceDataListenerPC<T>> resourceListenerListPC;

    private String id;

    private String type;

    public PeopleCounterSmartObjectResource() {
        this.resourceListenerListPC = new ArrayList<>();
    }

    public PeopleCounterSmartObjectResource(String id, String type) {
        this.id = id;
        this.type = type;
        this.resourceListenerListPC = new ArrayList<>();
    }

    public abstract T loadUpdatedValuePC();

    public void addDataListenerPC(ResourceDataListenerPC<T> resourceDataListenerPC){
        if(this.resourceListenerListPC != null)
            this.resourceListenerListPC.add(resourceDataListenerPC);
    }

    public void removeDataListenerPC(ResourceDataListenerPC<T> resourceDataListenerPC){
        if(this.resourceListenerListPC != null && this.resourceListenerListPC.contains(resourceDataListenerPC))
            this.resourceListenerListPC.remove(resourceDataListenerPC);
    }

    protected void notifyUpdatePC(T updatedValuePC){
        if(this.resourceListenerListPC != null && this.resourceListenerListPC.size() > 0)
            this.resourceListenerListPC.forEach(resourceDataListenerPC -> {
                if(resourceDataListenerPC != null)
                    resourceDataListenerPC.onDataChanged(this, updatedValuePC);
            });
        else
            logger.error("Empty or Null Resource Data Listener ! Nothing to notify ...");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PeopleCounterSmartObjectResource{");
        sb.append("id='").append(id).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}