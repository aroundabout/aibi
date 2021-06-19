package com.example.aibi.entity;

import java.io.Serializable;

public class RelationshipEntity implements Serializable {
    public Long id;
    public Long startId;
    public Long endId;
    public String type;

    public void setId(Long id) {
        this.id = id;
    }

    public void setEndId(Long endId) {
        this.endId = endId;
    }

    public void setStartId(Long startId) {
        this.startId = startId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public Long getEndId() {
        return endId;
    }

    public Long getStartId() {
        return startId;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object object){
        if(object==null) return false;
        else if(!(object instanceof RelationshipEntity)) return false;
        return getId().equals(((RelationshipEntity) object).getId());
    }
    @Override
    public int hashCode(){
        return getId().hashCode();
    }
}
