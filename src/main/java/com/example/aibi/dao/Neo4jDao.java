package com.example.aibi.dao;

import com.example.aibi.entity.NodeEntity;
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
    //query0 查询person周围的节点
    @Query("match data=(p:ns8__Person)-[]-(m)-[]-(o:ns4__Organization) " +
            "where(id(p)=$id and (m:ns8__TenureInOrganization or m:ns8__Officership or m:ns8__Directorship)) " +
            "return data  limit 100")
    List<PathValue> query0person(Long id);
    //query0 查询organization
    @Query("match data=(p:ns8__Person)-[]-(m)-[]-(o:ns4__Organization) " +
            "where(id(o)=$id and (m:ns8__TenureInOrganization or m:ns8__Officership or m:ns8__Directorship)) " +
            "return data  limit 100")
    List<PathValue> query0Organization(Long id);
    @Query("match  data=(n:ns8__TenureInOrganization)-[]-(m) " +
            "where (id(n)=$id and (m:ns8__Person or m:ns4__Organization)) " +
            "return data limit 100")
    List<PathValue> query0TenureInOrganization(Long id);
    @Query("match  data=(n:ns8__Officership)-[]-(m) " +
            "where (id(n)=$id and (m:ns8__Person or m:ns4__Organization or m:ns8__OfficerRole)) " +
            "return data limit 100 ")
    List<PathValue> query0Officership(Long id);
    @Query("match  data=(n:ns8__Directorship)-[]-(m)" +
            " where (id(n)=$id and (m:ns8__Person or m:ns4__Organization or m:ns8__DirectorRole))" +
            " return data limit 100")
    List<PathValue> query0Directorship(Long id);

    //query1
    //查询person to organizaition
    //permId
    @Query("match data=(n:ns8__Person{ns1__hasPermId:$permId})-[]-(m)-[]-(p:ns4__Organization) " +
            "where(m:ns8__TenureInOrganization or m:ns8__Officership or m:ns8__Directorship) " +
            "and apoc.coll.duplicates(NODES(data)) = [] " +
            "return data limit $limit")
    List<PathValue> query1PersonPermId(String permId,int limit);
    //name
    @Query("match data=(n:ns8__Person)-[]-(m)-[]-(p:ns4__Organization) " +
            "where((n.`ns6__family-name` contains $name or n.`ns6__given-name` contains $name) " +
            "and( m:ns8__TenureInOrganization or m:ns8__Officership or m:ns8__Directorship)) " +
            "and apoc.coll.duplicates(NODES(data)) = [] " +
            "return data limit $limit")
    List<PathValue> query1PersonName(String name,int limit);
    //查询 organizaition to person
    //permId
    @Query("match data=(n:ns8__Person)-[]-(m)-[]-(p:ns4__Organization{ns1__hasPermId:$permId}) " +
            "where(m:ns8__TenureInOrganization or m:ns8__Officership or m:ns8__Directorship) " +
            "and apoc.coll.duplicates(NODES(data)) = [] " +
            "return data limit $limit")
    List<PathValue> query1OrganizationPermId(String permId,int limit);
    //name
    @Query("match data=(n:ns8__Person)-[]-(m)-[]-(p:ns4__Organization) " +
            "where( (p.`ns6__organization-name` contains $name ) " +
            "and( m:ns8__TenureInOrganization or m:ns8__Officership or m:ns8__Directorship)) " +
            "and apoc.coll.duplicates(NODES(data)) = [] " +
            "return data limit $limit")
    List<PathValue> query1OrganizationName(String name,int limit);

    //query2
    //查询
    @Query("match data=(p1:ns8__Person)-[]-(o1)-[]-(o:ns4__Organization)-[]-(o2)-[]-(p2:ns8__Person) " +
            "where p1.ns1__hasPermId=$permId1 and p2.ns1__hasPermId=$permId2 " +
            "and (o1:ns8__TenureInOrganization or o1:ns8__Officership or o1:ns8__Directorship) " +
            "and (o2:ns8__TenureInOrganization or o2:ns8__Officership or o2:ns8__Directorship) " +
            "and apoc.coll.duplicates(NODES(data)) = []" +
            "return data limit $limit")
    List<PathValue> query2SamePerson(String permId1,String permId2, int limit);
    @Query("match data=(p1:ns4__Organization)-[]-(o1)-[]-(o:ns8__Person)-[]-(o2)-[]-(p2:ns4__Organization) " +
            "where p1.ns1__hasPermId=$permId1 and p2.ns1__hasPermId=$permId2 and " +
            "and (o1:ns8__TenureInOrganization or o1:ns8__Officership or o1:ns8__Directorship) " +
            "and (o2:ns8__TenureInOrganization or o2:ns8__Officership or o2:ns8__Directorship) " +
            "and apoc.coll.duplicates(NODES(data)) = []" +
            "return data limit $limit")
    List<PathValue> query2SameOrganization(String permId1,String permId2, int limit);
    @Query("match data=(o1:ns4__Organization)-[]-(m)-[]-(o2:ns8__Person) " +
            "where o1.ns1__hasPermId=$permId1 and o2.ns1__hasPermId=$permId2 " +
            "and (m:ns8__TenureInOrganization or m:ns8__Officership or m:ns8__Directorship) " +
            "and apoc.coll.duplicates(NODES(data)) = [] " +
            "return data limit $limit")
    List<PathValue> query2Diff(String permId1,String permId2,int limit);


    //query3
    //两个same 一个diff
    @Query("match (p1),(p2),p=shortestpath((p1)-[*..10]-(p2)) " +
            "where p1: ns8__Person and p1.ns1__hasPermId=$permId1 " +
            "and p2: ns8__Person and p2.ns1__hasPermId=$permId2 " +
            "and all (x in nodes(p) where x:ns8__Person or x:ns4__Organization or x:ns8__TenureInOrganization " +
            "or x:ns8__Directorship or x:ns8__Officership) " +
            "return p")
    List<PathValue> query3ShortestPathPerson(String permId1, String permId2);

    @Query("match (p1),(p2),p=shortestpath((p1)-[*..10]-(p2)) " +
            "where p1: ns4__Organization and p1.ns1__hasPermId=$permId1 " +
            "and p2: ns4__Organization and p2.ns1__hasPermId=$permId2 " +
            "and all (x in nodes(p) where x:ns8__Person or x:ns4__Organization or x:ns8__TenureInOrganization " +
            "or x:ns8__Directorship or x:ns8__Officership) " +
            "return p")
    List<PathValue> query3ShortestPathOrganization(String permId1, String permId2);

    @Query("match (p1),(p2),p=shortestpath((p1)-[*..10]-(p2)) " +
            "where p1: ns8__Person and p1.ns1__hasPermId=$permId1 " +
            "and p2: ns4__Organization and p2.ns1__hasPermId=$permId2 " +
            "and all (x in nodes(p) where x:ns8__Person or x:ns4__Organization or x:ns8__TenureInOrganization " +
            "or x:ns8__Directorship or x:ns8__Officership) " +
            "return p")
    List<PathValue> query3ShortestPath(String permId1, String permId2);

    //query for score
    //person
    //aq
    @Query("MATCH data=(n:ns8__Person)-[]-(p:ns8__AcademicQualification) " +
            "where id(n)=$id " +
            "RETURN count(data)")
    int queryPersonAQ(Long id);

    @Query("MATCH (n:ns8__Person)-[]->(p:ns8__Directorship) " +
            "where id(n)=$id " +
            "RETURN count(p)")
    int queryPersonDirectorship(Long id);

    @Query("MATCH (n:ns8__Person)-[]->(p:ns8__TenureInOrganization) " +
            "where id(n)=$id " +
            "RETURN count(p)")
    int queryPersonTenure(Long id);

    @Query("MATCH (n:ns8__Person)-[]->(p:ns8__Officership) " +
            "where id(n)=$id " +
            "RETURN count(p)")
    int queryPersonOfficership(Long id);

    @Query("match data=(n:ns8__Person)-[]-(:ns8__Directorship)-[]-(r:ns8__DirectorRole) " +
            "where id(n)=$id " +
            "return r limit 10")
    List<NodeValue> queryDirectorRank(Long id);

    @Query("match data=(n:ns8__Person)-[]-(:ns8__Directorship)-[]-(r:ns8__DirectorRole) " +
            "where id(n)=$id " +
            "return r limit 10")
    List<NodeValue> queryOfficeRank(Long id);
    //company
    //scale
    @Query("match (n:ns4__Organization)-[]-(o)-[]-(p:ns8__Person) " +
            "where id(n)=$id and " +
            "(o:ns8__TenureInOrganization or o:ns8__Directorship or o:ns8__Officership) " +
            "return count(p)")
    int queryCompanyScale(Long id);


    //Deprecated
    //Deprecated
    //Deprecated
    //Deprecated
    //Deprecated

    //初始化
    @Query("match (n:ns4__Organization) return n limit 25")
    List<NodeValue> getInit();

    //通用查询 通过id查询节点
    @Query("match (n) where id(n)=$id return n")
    List<NodeValue> getEntityById(Long id);

    //查询relationship
    @Query("match [n] where id(n)=$id return n")
    List<RelationshipValue> getRelationshipById(Long id);

    //查询organization
    //id
    @Query("match (n:ns4__Organization) where id(n)=$id return n")
    List<NodeValue> getOrganizationById(Long id);

    //permId
    @Query("match (n:ns4__Organization) where n.ns1__hasPermId=$permId return n")
    List<NodeValue> getOrganizationByPermId(String permId);

    //name
    @Query("match(n:ns4__Organization) where n.`ns6__organization-name` contains $name return n limit 25")
    List<NodeValue> getOrganizationByName(String name);

    //查询person
    //id
    @Query("match (n:ns8__Person) where id(n)=$id return n")
    List<NodeValue> getPersonById(Long id);

    //permId
    @Query("match (n:ns8__Person) where n.ns1__hasPermId=$permId return n")
    List<NodeValue> getPersonByPermId(String permId);

    //name
    @Query("match (n:ns8__Person) where n.`ns6__family-name` contains $name or n.`ns6__given-name` contains $name return n limit 25")
    List<NodeValue> getPersonByName(String name);

    //拓展查询
    //节点
    @Query("match (n)-[r]-(p) where id(n)=$id return p limit 25")
    List<NodeValue> getExtendN(Long id);

    //关系
    @Query("match (n)-[r]-(p) where id(n)=$id return r limit 25")
    List<RelationshipValue> getExtendR(Long id);

    //step查询
    @Query("match data=(p:ns8__Person{ns1__hasPermId:$permId})-[*1..2]-(e) WHERE apoc.coll.duplicates(NODES(data)) = [] return data limit 25")
    List<PathValue> getStep(String permId, int step);

    //查询person到organization
    @Query("match data=(n:ns8__Person{ns1__hasPermId:$permId})-[]-(m)-[]-(p:ns4__Organization) where(m:ns8__TenureInOrganization or m:ns8__Officership or m:ns8__Directorship) return data limit 10")
    List<PathValue> getPersonToOrganization(String permId);

    //查询person到organization
    @Query("match data=(n:ns8__Person)-[]-(m)-[]-(p:ns4__Organization{ns1__hasPermId:$permId}) where(m:ns8__TenureInOrganization or m:ns8__Officership or m:ns8__Directorship) return data limit 10")
    List<PathValue> getOrganizationToPerson(String permId);
}
