package com.example.aibi.dao;

import com.example.aibi.entity.Organization;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.internal.value.PathValue;
import org.neo4j.driver.internal.value.RelationshipValue;
import org.neo4j.driver.internal.value.ValueAdapter;
import org.neo4j.driver.util.Pair;
import org.springframework.cglib.core.Converter;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Neo4jDao extends Neo4jRepository<Organization, Long> {
    //初始化
    @Query("match (n:ns0__Organization) return n limit 25")
    List<NodeValue> getInit();

    //通用查询 通过id查询节点
    @Query("match (n) where id(n)=$id return n")
    List<NodeValue> getEntityById(Long id);

    //查询relationship
    @Query("match [n] where id(n)=$id return n")
    List<RelationshipValue> getRelationshipById(Long id);

    //查询organization
    //id
    @Query("match (n:ns0__Organization) where id(n)=$id return n")
    List<NodeValue> getOrganizationById(Long id);

    //permId
    @Query("match (n:ns0__Organization) where n.ns1__hasPermId=$permId return n")
    List<NodeValue> getOrganizationByPermId(String permId);

    //name
    @Query("match(n:ns0__Organization) where n.`ns3__organization-name` contains $name return n limit 25")
    List<NodeValue> getOrganizationByName(String name);

    //查询person
    //id
    @Query("match (n:ns6__Person) where id(n)=$id return n")
    List<NodeValue> getPersonById(Long id);

    //permId
    @Query("match (n:ns6__Person) where n.ns1__hasPermId=$permId return n")
    List<NodeValue> getPersonByPermId(String permId);

    //name
    @Query("match (n:ns6__Person) where n.`ns3__family-name` contains $name or n.`ns3__given-name` contains $name return n limit 25")
    List<NodeValue> getPersonByName(String name);

    //拓展查询
    //节点
    @Query("match (n)-[r]-(p) where id(n)=$id return p limit 25")
    List<NodeValue> getExtendN(Long id);

    //关系
    @Query("match (n)-[r]-(p) where id(n)=$id return r limit 25")
    List<RelationshipValue> getExtendR(Long id);

    //
    @Query("match data=(p:ns6__Person{ns1__hasPermId:$permId})-[*1..3]-(e) WHERE apoc.coll.duplicates(NODES(data)) = [] return data limit 25")
    List<PathValue> getStep(String permId, int step);
}
