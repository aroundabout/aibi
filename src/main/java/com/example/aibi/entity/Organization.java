package com.example.aibi.entity;

import lombok.Builder;
import lombok.Data;
import org.neo4j.ogm.annotation.NodeEntity;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;

@NodeEntity(label = "dept")
@Data
@Builder
public class Organization {
    @Id
    @GeneratedValue
    private Long id;
}
