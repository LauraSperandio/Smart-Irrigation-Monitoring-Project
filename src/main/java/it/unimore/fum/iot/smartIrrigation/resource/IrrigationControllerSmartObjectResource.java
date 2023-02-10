package it.unimore.fum.iot.smartIrrigation.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class IrrigationControllerSmartObjectResource<T> {

    private static final Logger logger = LoggerFactory.getLogger(IrrigationControllerSmartObjectResource.class);

    protected List<ResourceDataListenerIC<T>> resourceListenerListIC;

    private String id;

    private String type;

    public IrrigationControllerSmartObjectResource() {
        this.resourceListenerListIC = new ArrayList<>();
    }

    public IrrigationControllerSmartObjectResource(String id, String type) {
        this.id = id;
        this.type = type;
        this.resourceListenerListIC = new ArrayList<>();
    }

    public abstract T loadUpdatedValueIC();

    public void addDataListenerIC(ResourceDataListenerIC<T> resourceDataListenerIC){
        if(this.resourceListenerListIC != null)
            this.resourceListenerListIC.add(resourceDataListenerIC);
    }

    public void removeDataListenerIC(ResourceDataListenerIC<T> resourceDataListenerIC){
        if(this.resourceListenerListIC != null && this.resourceListenerListIC.contains(resourceDataListenerIC))
            this.resourceListenerListIC.remove(resourceDataListenerIC);
    }

    protected void notifyUpdateIC(T updatedValueIC){
        if(this.resourceListenerListIC != null && this.resourceListenerListIC.size() > 0)
            this.resourceListenerListIC.forEach(resourceDataListenerIC -> {
                if(resourceDataListenerIC != null)
                    resourceDataListenerIC.onDataChanged(this, updatedValueIC);
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
        final StringBuffer sb = new StringBuffer("IrrigationControllerSmartObjectResource{");
        sb.append("id='").append(id).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
