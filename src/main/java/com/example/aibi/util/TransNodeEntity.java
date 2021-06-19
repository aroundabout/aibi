package com.example.aibi.util;

import com.example.aibi.entity.NodeEntity;
import com.example.aibi.entity.RelationshipEntity;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.internal.value.RelationshipValue;

import java.util.ArrayList;
import java.util.HashMap;

public class TransNodeEntity {
    public static NodeEntity NodeValueToEntity(NodeValue nodeValue) {
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setLabels((ArrayList<String>) nodeValue.asNode().labels());
        HashMap<String, Object> hashMap = new HashMap<>();
        for (var key : nodeValue.keys()) {
            hashMap.put(key, nodeValue.get(key).toString());
        }
        nodeEntity.setId(nodeValue.asNode().id());
        nodeEntity.setProperties(hashMap);
        return nodeEntity;
    }

    public static RelationshipEntity RelationshipValueToEntity(RelationshipValue relationshipValue){
        RelationshipEntity relationshipEntity =new RelationshipEntity();
        relationshipEntity.setId(relationshipValue.asRelationship().id());
        relationshipEntity.setStartId(relationshipValue.asRelationship().startNodeId());
        relationshipEntity.setEndId(relationshipValue.asRelationship().endNodeId());
        relationshipEntity.setType(relationshipValue.asRelationship().type().toString());
        return relationshipEntity;
    }
}
