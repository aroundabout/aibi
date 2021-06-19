package com.example.aibi.controller;

import com.example.aibi.dao.Neo4jDao;
import com.example.aibi.dao.RedisDao;
import com.example.aibi.entity.NodeARelationship;
import com.example.aibi.entity.NodeEntity;
import com.example.aibi.entity.RelationshipEntity;
import com.example.aibi.entity.Result;
import com.example.aibi.util.TransNodeEntity;
import org.neo4j.driver.internal.value.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/neo4j")
public class BiController {
    @Autowired
    private RedisDao redisDao;
    @Autowired
    private Neo4jDao neo4jDao;

    private List<NodeEntity> valueListToEntityList(List<NodeValue> nodeValues) {
        List<NodeEntity> nodeEntityList = new ArrayList<>();
        for (NodeValue nodeValue : nodeValues) {
            NodeEntity nodeEntity = TransNodeEntity.NodeValueToEntity(nodeValue);
            redisDao.set((nodeEntity.id.toString()), nodeEntity);
            nodeEntityList.add(nodeEntity);
        }
        return nodeEntityList;
    }

    private List<RelationshipEntity> shipListToEntityList(List<RelationshipValue> relationshipValues) {
        List<RelationshipEntity> relationshipEntities = new ArrayList<>();
        for (RelationshipValue relationshipValue : relationshipValues) {
            RelationshipEntity relationshipEntity = TransNodeEntity.RelationshipValueToEntity(relationshipValue);
            redisDao.set(relationshipEntity.id.toString(), relationshipEntity);
            relationshipEntities.add(relationshipEntity);
        }
        return relationshipEntities;
    }

    private List<NodeEntity> redisGetNode(Long id) {
        if (redisDao.hasKey(id.toString())) {
            List<NodeEntity> list = new ArrayList<>();
            list.add((NodeEntity) redisDao.get(id.toString()));
            return list;
        } else {
            List<NodeValue> list = neo4jDao.getEntityById(id);
            return valueListToEntityList(list);
        }
    }

    private List<RelationshipEntity> redisGetRelationship(Long id) {
        if (redisDao.hasKey(id.toString())) {
            List<RelationshipEntity> list = new ArrayList<>();
            list.add((RelationshipEntity) redisDao.get(id.toString()));
            return list;
        } else {
            List<RelationshipValue> list = neo4jDao.getRelationshipById(id);
            return shipListToEntityList(list);
        }
    }

    //测试用api 基本不用
    @RequestMapping(value = "/redis/hello/{id}", method = RequestMethod.GET)
    public Result<Object> hello(@PathVariable String id) {
        String ss = "haha";
        redisDao.set(id, ss);
        String s = (String) redisDao.get(id);
        return new Result().builder().code(200).data(s).msg("hello").build();
    }

    //初始化
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public Result<Object> initNeo4j() {
        List<NodeValue> nodeValues = neo4jDao.getInit();
        List<NodeEntity> nodeEntityList = valueListToEntityList(nodeValues);
        return new Result().builder().code(200).data(nodeEntityList).msg("hello").build();
    }

    // 通过id得到organization和person
    // organization
    @RequestMapping(value = "/organization/type/{type}/{id}", method = RequestMethod.GET)
    public Result<Object> getOrganizationById(@PathVariable String id, @PathVariable String type) {
        if (redisDao.get(id.toString()) != null) {
            NodeEntity nodeEntity = (NodeEntity) redisDao.get(id.toString());
            return new Result().builder().code(200).data(nodeEntity).msg("getOrganizationCacheFromRedis").build();
        }
        List<NodeValue> nodeValues;
        if (type.equals("Id")) {
            nodeValues = neo4jDao.getOrganizationById(Long.valueOf(id));
        } else if (type.equals("name")) {
            nodeValues = neo4jDao.getOrganizationByName(id);
        } else {
            nodeValues = neo4jDao.getOrganizationByPermId(id);

        }
        if (nodeValues.size() == 0) {
            return new Result().builder().code(404).data(null).msg("no resource").build();

        }
        List<NodeEntity> nodeEntityList = valueListToEntityList(nodeValues);
        return new Result().builder().code(200).data(nodeEntityList).msg("organization").build();
    }

    //person
    @RequestMapping(value = "/person/{type}/{id}", method = RequestMethod.GET)
    public Result<Object> getPersonById(@PathVariable String id, @PathVariable String type) {
        if (redisDao.get(id.toString()) != null) {
            NodeEntity nodeEntity = (NodeEntity) redisDao.get(id.toString());
            return new Result().builder().code(200).data(nodeEntity).msg("getPersonCacheFromRedis").build();
        }
        List<NodeValue> nodeValues;
        if (type.equals("Id")) {
            nodeValues = neo4jDao.getPersonById(Long.valueOf(id));
        } else if (type.equals("name")) {
            nodeValues = neo4jDao.getPersonByName(id);
        } else {
            nodeValues = neo4jDao.getPersonByPermId(id);
        }
        if (nodeValues.size() == 0) {
            return new Result().builder().code(404).data(null).msg("no resource").build();
        }
        List<NodeEntity> nodeEntityList = valueListToEntityList(nodeValues);
        return new Result().builder().code(200).data(nodeEntityList).msg("person").build();
    }

    //拓展查询
    @RequestMapping(value = "/extend/{id}", method = RequestMethod.GET)
    public Result<Object> getExtend(@PathVariable Long id) {
        NodeARelationship nodeARelationship = new NodeARelationship();
        if (redisDao.hasKey("node" + id.toString())) {
            Set<Object> idList = redisDao.setMembers("node" + id.toString());
            List<NodeEntity> returnNodeEntities = new ArrayList<>();
            for (Object o : idList) {
                List<NodeEntity> nodeEntities = redisGetNode(Long.valueOf((String) o));
                returnNodeEntities.addAll(nodeEntities);
            }
            nodeARelationship.setNodeEntities(returnNodeEntities);
        }
        if (redisDao.hasKey("relationship" + id.toString())) {
            Set<Object> idList = redisDao.setMembers("relationship" + id.toString());
            List<RelationshipEntity> returnRelationshipEntities = new ArrayList<>();
            for (Object o : idList) {
                List<RelationshipEntity> relationshipEntities = redisGetRelationship(Long.valueOf((String) o));
                returnRelationshipEntities.addAll(relationshipEntities);
            }
            nodeARelationship.setRelationships(returnRelationshipEntities);
        }
        if (nodeARelationship.getRelationships().size() + nodeARelationship.getNodeEntities().size() != 0) {
            return new Result().builder()
                    .code(200)
                    .msg("return return relationships and nodes for redis")
                    .data(nodeARelationship)
                    .build();
        }
        List<NodeValue> nodeValues = neo4jDao.getExtendN(id);
        List<RelationshipValue> relationshipValues = neo4jDao.getExtendR(id);
        List<NodeEntity> nodeEntities = valueListToEntityList(nodeValues);
        List<RelationshipEntity> relationshipEntities = shipListToEntityList(relationshipValues);
        //缓存
        for (NodeEntity nodeEntity : nodeEntities) {
            redisDao.setAdd("node" + id.toString(), nodeEntity.getId().toString());
        }
        for (RelationshipEntity relationshipEntity : relationshipEntities) {
            redisDao.setAdd("relationship" + id.toString(), relationshipEntity.getId().toString());
        }
        nodeARelationship.setRelationships(relationshipEntities);
        nodeARelationship.setNodeEntities(nodeEntities);
        return new Result().builder().code(200).msg("return relationships and nodes").data(nodeARelationship).build();
    }

    //跳跃查询
    @RequestMapping(value = "/step/{permId}/{step}", method = RequestMethod.GET)
    public Result<Object> getStep(@PathVariable String permId, @PathVariable int step) {
        List<PathValue> list = neo4jDao.getStep(permId, step);
        Set<NodeEntity> nodeEntities = new HashSet<>();
        Set<RelationshipEntity> relationshipEntities = new HashSet<>();
        for (PathValue pathValue : list) {
            Iterable<Node> nodes = pathValue.asPath().nodes();
            Iterable<Relationship> relationships = pathValue.asPath().relationships();
            for (Node node : nodes) {
                NodeEntity nodeEntity = new NodeEntity();
                nodeEntity.setId(node.id());
                nodeEntity.setLabels((ArrayList<String>) node.labels());
                var keys = node.keys();
                HashMap<String, Object> hashMap = new HashMap<>();
                for (String s : keys) {
                    hashMap.put(s, node.get(s).toString());
                }
                nodeEntity.setProperties(hashMap);
                nodeEntities.add(nodeEntity);
            }
            for (Relationship relationship : relationships) {
                RelationshipEntity relationshipEntity = new RelationshipEntity();
                relationshipEntity.setId(relationship.id());
                relationshipEntity.setStartId(relationship.startNodeId());
                relationshipEntity.setEndId(relationship.endNodeId());
                relationshipEntity.setType(relationship.type());
                relationshipEntities.add(relationshipEntity);
            }
        }

//        Set<NodeEntity> tempset = new HashSet<>(nodeEntities);
//        Set<RelationshipEntity> tempset2=new HashSet<>(relationshipEntities);

        NodeARelationship nodeARelationship =
                new NodeARelationship(new ArrayList<>(nodeEntities), new ArrayList<>(relationshipEntities));
        return new Result().builder().code(200).data(nodeARelationship).msg("yes").build();
    }


}
