package com.example.aibi.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NodeEntity implements Serializable {
    public HashMap<String, Object> properties;
    public ArrayList<String> labels;
    public Long id;

    public NodeEntity() {
        properties = new HashMap<>();
        labels = new ArrayList<>();
    }

    public NodeEntity(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    public NodeEntity(HashMap<String, Object> properties, ArrayList<String> labels, Long id) {
        this.properties = properties;
        this.labels = labels;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public ArrayList<String> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<String> labels) {
        this.labels = labels;
    }


    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    public Object get(String key) {
        return properties.get(key);
    }

    public void put(String key, Object o) {
        properties.put(key, o);
    }

    public void putAll(Map<String, Object> map) {
        properties.putAll(map);
    }

    @Override
    public String toString() {
        return getProperties().toString();
    }

    /**
     * NodeEntity的相等需要Node的id相等即可，用于在List中添加时过滤相同节点
     *
     * @param object 比较对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        else if (!(object instanceof NodeEntity)) return false;
        return getId().equals(((NodeEntity) object).getId());
    }
    @Override
    public int hashCode(){
        return getId().hashCode();
    }
}
