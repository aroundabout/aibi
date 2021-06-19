package com.example.aibi.util;

import java.util.Set;
import java.util.Stack;

public class NodeUtils {
    public static Set<String> nodeProperty;
    public static Set<String> labels;
    public static Set<String> relationshipName;

    public NodeUtils(){
        setLabels();
        setNodeProperty();
        setRelationshipName();
    }

    public static void setLabels(){
        labels.add("Resource");
        labels.add("_GraphConfig");
        labels.add("_NsPrefDef");
        labels.add("ns0__Organization");
        labels.add("ns6__AcademicDegree");
        labels.add("ns6__AcademicQualification");
        labels.add("ns6__DirectorRole");
        labels.add("ns6__Directorship");
        labels.add("ns6__OfficerRole");
        labels.add("ns6__Officership");
        labels.add("ns6__Person");
        labels.add("ns6__TenureInOrganization");
    }

    public static void setNodeProperty(){
        nodeProperty.add("uri");
        nodeProperty.add("ns0__hasLEI");
        nodeProperty.add("ns0__hasHeadquartersPhoneNumber");
        nodeProperty.add("ns0__hasLatestOrganizationFoundedDate");
        nodeProperty.add("ns0__hasHeadquartersFaxNumber");
        nodeProperty.add("ns1__hasPermId");
        nodeProperty.add("ns2__HeadquartersAddress");
        nodeProperty.add("ns2__RegisteredAddress");
        nodeProperty.add("ns3__organization-name");
    }
    public static void setRelationshipName(){
        relationshipName.add("ns0__hasActivityStatus");
        relationshipName.add("ns0__hasHoldingClassification");
        relationshipName.add("ns0__hasPrimaryBusinessSector");
        relationshipName.add("ns0__hasPrimaryEconomicSector");
        relationshipName.add("ns0__hasPrimaryIndustryGroup");
        relationshipName.add("ns0__isIncorporatedIn");
        relationshipName.add("ns1__hasPublicationStatus");
        relationshipName.add("ns3__hasGender");
        relationshipName.add("ns3__hasURL");
        relationshipName.add("ns4__isDomiciledIn");
        relationshipName.add("ns5__hasOrganizationPrimaryQuote");
        relationshipName.add("ns5__hasPrimaryInstrument");
        relationshipName.add("ns6__hasHolder");
        relationshipName.add("ns6__hasPositionType");
        relationshipName.add("ns6__hasTenureInOrganization");
        relationshipName.add("ns6__holdsPosition");
        relationshipName.add("ns6__inSubject");
        relationshipName.add("ns6__isPositionIn");
        relationshipName.add("ns6__isTenureIn");
        relationshipName.add("ns6__withDegree");
    }
}
