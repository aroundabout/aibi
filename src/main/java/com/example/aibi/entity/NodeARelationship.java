package com.example.aibi.entity;

import java.util.ArrayList;
import java.util.List;

public class NodeARelationship {
    public List<NodeEntity> nodeEntities;
    public List<RelationshipEntity> relationshipEntities;

    public NodeARelationship() {
        nodeEntities = new ArrayList<>();
        relationshipEntities = new ArrayList<>();
    }

    public NodeARelationship(List<NodeEntity> e, List<RelationshipEntity> r) {
        nodeEntities = e;
        relationshipEntities = r;
    }

    public void setNodeEntities(List<NodeEntity> nodeEntities) {
        this.nodeEntities = nodeEntities;
    }

    public void setRelationships(List<RelationshipEntity> relationshipEntities) {
        this.relationshipEntities = relationshipEntities;
    }

    public List<NodeEntity> getNodeEntities() {
        return nodeEntities;
    }

    public List<RelationshipEntity> getRelationships() {
        return relationshipEntities;
    }
}
