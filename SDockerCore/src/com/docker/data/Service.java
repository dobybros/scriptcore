package com.docker.data;

import com.docker.storage.mongodb.CleanDocument;
import org.bson.Document;

public class Service {
    public static final String FIELD_SERVICE_SERVICE = "service";
    public static final String FIELD_SERVICE_VERSION = "version";
    public static final String FIELD_SERVICE_MINVERSION = "minVersion";
    private String service;
    private Integer version;
    private Integer minVersion;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(Integer minVersion) {
        this.minVersion = minVersion;
    }

    public void fromDocument(Document dbObj) {
        service = (String) dbObj.get(FIELD_SERVICE_SERVICE);
        version = dbObj.getInteger(FIELD_SERVICE_VERSION);
        minVersion = dbObj.getInteger(FIELD_SERVICE_MINVERSION);
    }

    public Document toDocument() {
        Document dbObj = new CleanDocument();
        dbObj.put(FIELD_SERVICE_SERVICE, service);
        dbObj.put(FIELD_SERVICE_MINVERSION, minVersion);
        dbObj.put(FIELD_SERVICE_VERSION, version);
        return dbObj;
    }
}
