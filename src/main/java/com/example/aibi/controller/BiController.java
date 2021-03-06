package com.example.aibi.controller;

import com.example.aibi.dao.Neo4jDao;
import com.example.aibi.dao.RedisDao;
import com.example.aibi.entity.*;
import com.example.aibi.service.RuleService;
import com.example.aibi.util.NodeUtils;
import com.example.aibi.util.TransNodeEntity;
import org.neo4j.driver.internal.value.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.*;

@RestController
@RequestMapping("/api/neo4j")
public class BiController {
    @Autowired
    private RedisDao redisDao;
    @Autowired
    private Neo4jDao neo4jDao;
    @Autowired
    private RuleService ruleService;

    //转换nodevalue到nodeentity
    private List<NodeEntity> valueListToEntityList(List<NodeValue> nodeValues) {
        List<NodeEntity> nodeEntityList = new ArrayList<>();
        for (NodeValue nodeValue : nodeValues) {
            NodeEntity nodeEntity = TransNodeEntity.NodeValueToEntity(nodeValue);
            redisDao.set((nodeEntity.id.toString()), nodeEntity);
            nodeEntityList.add(nodeEntity);
        }
        return nodeEntityList;
    }

    //relationvalue到entity
    private List<RelationshipEntity> shipListToEntityList(List<RelationshipValue> relationshipValues) {
        List<RelationshipEntity> relationshipEntities = new ArrayList<>();
        for (RelationshipValue relationshipValue : relationshipValues) {
            RelationshipEntity relationshipEntity = TransNodeEntity.RelationshipValueToEntity(relationshipValue);
            redisDao.set(relationshipEntity.id.toString(), relationshipEntity);
            relationshipEntities.add(relationshipEntity);
        }
        return relationshipEntities;
    }

    private int getRankPoint(List<NodeValue> director, List<NodeValue> office) {
        List<NodeEntity> dR = valueListToEntityList(director);
        List<NodeEntity> oR = valueListToEntityList(office);
        int sum = 0;
        for (NodeEntity nodeEntity : dR) {
            if (nodeEntity.getProperties().containsKey("ns8__rank")) {
                String s = (String) nodeEntity.getProperties().get("ns8__rank");
                s=s.replace("\"","");
                int rank=Integer.parseInt(s);
                sum += 100-rank;
            }
        }
        for (NodeEntity nodeEntity : oR) {
            if (nodeEntity.getProperties().containsKey("ns8__rank")) {
                String s = (String) nodeEntity.getProperties().get("ns8__rank");
                s=s.replace("\"","");
                int rank=Integer.parseInt(s);
                sum += 100-rank;
            }
        }
        return sum;
    }

    //获得分数
    private double getScore(NodeEntity nodeEntity) throws UnsupportedEncodingException {
        String type = "";
        for (String label : nodeEntity.labels) {
            if (label.equals("ns8__Person")) {
                type = "ns8__Person";
                break;
            }
            if (label.equals("ns4__Organization")) {
                type = "ns4__Organization";
                break;
            }
        }
        if (type.equals("ns8__Person")) {
            Person person = new Person();
            person.setCountAca(neo4jDao.queryPersonAQ(nodeEntity.getId()));
            person.setCountDirec(neo4jDao.queryPersonDirectorship(nodeEntity.getId()));
            person.setCountTenure(neo4jDao.queryPersonTenure(nodeEntity.getId()));
            person.setCountOfficer(neo4jDao.queryPersonOfficership(nodeEntity.getId()));
            List<NodeValue> directorRank = neo4jDao.queryDirectorRank(nodeEntity.getId());
            List<NodeValue> officeRank = neo4jDao.queryOfficeRank(nodeEntity.getId());
            person.setRank(getRankPoint(directorRank, officeRank));
            ruleService.rule(person);
            return person.getPoint();
        } else if (type.equals("ns4__Organization")) {
            Company company = new Company();
            company.setPersonScale(neo4jDao.queryCompanyScale(nodeEntity.getId()));
            ruleService.rule(company);
            return company.getPoint();
        } else return 0;
    }

    //pathvalue到Node and relation
    private NodeARelationship pathToReuslt(List<PathValue> list, String nodeKey, String relaKey) throws UnsupportedEncodingException {
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
                hashMap.put("score", getScore(nodeEntity));
                nodeEntity.setProperties(hashMap);
                redisDao.set(nodeEntity.getId().toString(), nodeEntity);
                redisDao.setAdd(nodeKey, nodeEntity.getId().toString());
                nodeEntities.add(nodeEntity);
            }
            for (Relationship relationship : relationships) {
                RelationshipEntity relationshipEntity = new RelationshipEntity();
                relationshipEntity.setId(relationship.id());
                relationshipEntity.setStartId(relationship.startNodeId());
                relationshipEntity.setEndId(relationship.endNodeId());
                relationshipEntity.setType(relationship.type());
                redisDao.set(String.valueOf(relationship.id()), relationshipEntity);
                redisDao.setAdd(relaKey, String.valueOf(relationship.id()));
                relationshipEntities.add(relationshipEntity);
            }
        }
        return new NodeARelationship(new ArrayList<>(nodeEntities), new ArrayList<>(relationshipEntities));
    }

    private NodeARelationship getFromCache(String nodeKey, String relaKey) {
        List<NodeEntity> nodeEntities = new ArrayList<>();
        List<RelationshipEntity> relationshipEntities = new ArrayList<>();

        if (redisDao.hasKey(nodeKey)) {
            Set<Object> members = redisDao.setMembers(nodeKey);
            for (Object member : members) {
                Long id = Long.valueOf((String) member);
                if (redisDao.hasKey(id.toString())) {
                    nodeEntities.add((NodeEntity) redisDao.get(id.toString()));
                }
            }
        }
        if (redisDao.hasKey(relaKey)) {
            Set<Object> members = redisDao.setMembers(relaKey);
            for (Object member : members) {
                Long id = Long.valueOf((String) member);
                if (redisDao.hasKey(id.toString())) {
                    relationshipEntities.add((RelationshipEntity) redisDao.get(id.toString()));
                }
            }
        }
        return new NodeARelationship(nodeEntities, relationshipEntities);
    }

    //缓存中读取
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

    //read in cache
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

    // personPoint


    //query0
    @RequestMapping(value = "/query0/node/{label}/{id}")
    public Result query0(@PathVariable Long id, @PathVariable String label) throws UnsupportedEncodingException {
        String commmon = "/query0/node/" + label + "/" + id.toString();
        String keyNode = "node" + commmon;
        String keyRela = "relationship" + commmon;

        NodeARelationship cache = getFromCache(keyNode, keyRela);
        if (cache.getNodeEntities().size() + cache.getRelationshipEntities().size() > 0) {
            return new Result().builder().code(200).msg("get data from cache").data(cache).build();
        }


        List<PathValue> list;
        if (label.equals(NodeUtils.labels.get(0))) {
            list = neo4jDao.query0person(id);
        } else if (label.equals(NodeUtils.labels.get(1))) {
            list = neo4jDao.query0Organization(id);
        } else if (label.equals(NodeUtils.labels.get(2))) {
            list = neo4jDao.query0TenureInOrganization(id);
        } else if (label.equals(NodeUtils.labels.get(3))) {
            list = neo4jDao.query0Officership(id);
        } else if (label.equals(NodeUtils.labels.get(4))) {
            list = neo4jDao.query0Directorship(id);
        } else {
            return new Result().builder().code(404).msg("no resource").data(null).build();
        }
        NodeARelationship nodeARelationship;
        if (list.size() == 0) {
            nodeARelationship = new NodeARelationship();
        } else {
            nodeARelationship = pathToReuslt(list, keyNode, keyRela);
        }
        return new Result().builder().code(200).msg("get data").data(nodeARelationship).build();
    }

    //query1
    @RequestMapping(value = "/query1/{nodeType}/type/{type}/{typeValue}/limit/{limit}")
    public Result query1(@PathVariable int limit,
                         @PathVariable String nodeType,
                         @PathVariable String type,
                         @PathVariable String typeValue) throws UnsupportedEncodingException {
        String commmon = "/query1/" + nodeType + "/type/" + type + "/" + typeValue;
        String keyNode = "node" + commmon;
        String keyRela = "relationship" + commmon;

        NodeARelationship cache = getFromCache(keyNode, keyRela);
        if (cache.getNodeEntities().size() + cache.getRelationshipEntities().size() > 0) {
            return new Result().builder().code(200).msg("get data from cache").data(cache).build();
        }

        limit = Math.min(100, limit);
        List<PathValue> list = null;
        if (nodeType.equals("ns8__Person")) {
            if (type.equals("permId")) {
                list = neo4jDao.query1PersonPermId(typeValue, limit);
            } else if (type.equals("name")) {
                list = neo4jDao.query1PersonName(typeValue, limit);
            }
        } else if (nodeType.equals("ns4__Organization")) {
            if (type.equals("permId")) {
                list = neo4jDao.query1OrganizationPermId(typeValue, limit);
            } else if (type.equals("name")) {
                list = neo4jDao.query1OrganizationName(typeValue, limit);
            }
        }
        NodeARelationship nodeARelationship;
        assert list != null;
        if (list.size() == 0) {
            nodeARelationship = new NodeARelationship();
        } else {
            nodeARelationship = pathToReuslt(list, keyNode, keyRela);
        }
        return new Result().builder().code(200).msg("get data").data(nodeARelationship).build();
    }

    @RequestMapping(value = "/query2/node1/{type1}/{typeValue1}/node2/{type2}/{typeValue2}/limit/{limit}")
    public Result query2(@PathVariable int limit,
                         @PathVariable String type1,
                         @PathVariable String type2,
                         @PathVariable String typeValue1,
                         @PathVariable String typeValue2) throws UnsupportedEncodingException {
        String commmon = "/query2/node1" + type1 + "/" + typeValue1 + "/node2/" + type2 + "/" + typeValue2;
        String keyNode = "node" + commmon;
        String keyRela = "relationship" + commmon;

        NodeARelationship cache = getFromCache(keyNode, keyRela);
        if (cache.getNodeEntities().size() + cache.getRelationshipEntities().size() > 0) {
            return new Result().builder().code(200).msg("get data from cache").data(cache).build();
        }


        limit = Math.min(100, limit);
        List<PathValue> list;
        if (type1.equals(type2)) {
            if (type1.equals("ns8__Person")) {
                list = neo4jDao.query2SamePerson(typeValue1, typeValue2, limit);
            } else {
                list = neo4jDao.query2SameOrganization(typeValue1, typeValue2, limit);
            }
        } else {
            if (type1.equals("ns8__Person")) {
                list = neo4jDao.query2Diff(typeValue2, typeValue1, limit);
            } else {
                list = neo4jDao.query2Diff(typeValue1, typeValue2, limit);
            }
        }
        NodeARelationship nodeARelationship;
        assert list != null;
        if (list.size() == 0) {
            nodeARelationship = new NodeARelationship();
        } else {
            nodeARelationship = pathToReuslt(list, keyNode, keyRela);
        }
        return new Result().builder().code(200).msg("get data").data(nodeARelationship).build();
    }

    @RequestMapping(value = "/query3/node1/{type1}/{typeValue1}/node2/{type2}/{typeValue2}")
    public Result query3(@PathVariable String type1,
                         @PathVariable String type2,
                         @PathVariable String typeValue1,
                         @PathVariable String typeValue2) throws UnsupportedEncodingException {
        String commmon = "/query2/node1" + type1 + "/" + typeValue1 + "/node2/" + type2 + "/" + typeValue2;
        String keyNode = "node" + commmon;
        String keyRela = "relationship" + commmon;

        NodeARelationship cache = getFromCache(keyNode, keyRela);
        if (cache.getNodeEntities().size() + cache.getRelationshipEntities().size() > 0) {
            return new Result().builder().code(200).msg("get data from cache").data(cache).build();
        }


        if ((type1.equals("ns4__Organization") || type1.equals("ns8__Person"))
                && (type2.equals("ns4__Organization") || type2.equals("ns8__Person"))) {
            List<PathValue> list = null;
            if (type1.equals(type2)) {
                if (type1.equals("ns8__Person")) {
                    list = neo4jDao.query3ShortestPathPerson(typeValue1, typeValue2);
                } else {
                    list = neo4jDao.query3ShortestPathOrganization(typeValue1, typeValue2);
                }
            } else {
                if (type1.equals("ns8__Person")) {
                    list = neo4jDao.query3ShortestPath(typeValue1, typeValue2);
                } else {
                    list = neo4jDao.query3ShortestPath(typeValue2, typeValue1);
                }
            }
            NodeARelationship nodeARelationship = pathToReuslt(list, keyNode, keyRela);
            return new Result().builder().code(200).msg("get data").data(nodeARelationship).build();
        }
        return new Result().builder().code(400).msg("不允许的输入").data(null).build();

    }


    //Deprecated
    //Deprecated
    //Deprecated
    //Deprecated
    //Deprecated

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
            nodeARelationship.setRelationshipEntities(returnRelationshipEntities);
        }
        if (nodeARelationship.getRelationshipEntities().size() + nodeARelationship.getNodeEntities().size() != 0) {
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
        nodeARelationship.setRelationshipEntities(relationshipEntities);
        nodeARelationship.setNodeEntities(nodeEntities);
        return new Result().builder().code(200).msg("return relationships and nodes").data(nodeARelationship).build();
    }

    //跳跃查询
//    @RequestMapping(value = "/step/{permId}/{step}", method = RequestMethod.GET)
//    public Result<Object> getStep(@PathVariable String permId, @PathVariable int step) throws UnsupportedEncodingException {
//        List<PathValue> list = neo4jDao.getStep(permId, step);
//        NodeARelationship nodeARelationship = pathToReuslt(list);
//        return new Result().builder().code(200).data(nodeARelationship).msg("yes").build();
//    }
//
//    @RequestMapping(value = "/organization/person/{permId}")
//    public Result<Object> getPersonToOrganization(@PathVariable String permId) throws UnsupportedEncodingException {
//        List<PathValue> list = neo4jDao.getPersonToOrganization(permId);
//        NodeARelationship nodeARelationship = pathToReuslt(list);
//        return new Result().builder().code(200).data(nodeARelationship).msg("yes").build();
//    }
//
//    @RequestMapping(value = "/person/organization/{permId}")
//    public Result<Object> getOrganizationToPerson(@PathVariable String permId) throws UnsupportedEncodingException {
//        List<PathValue> list = neo4jDao.getOrganizationToPerson(permId);
//        NodeARelationship nodeARelationship = pathToReuslt(list);
//        return new Result().builder().code(200).data(nodeARelationship).msg("yes").build();
//    }


}
