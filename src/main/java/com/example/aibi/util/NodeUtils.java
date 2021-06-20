package com.example.aibi.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class NodeUtils {
    public static Set<String> nodeProperty;
    public static ArrayList<String> labels=new ArrayList<>(){{
        add("ns8__Person");
        add("ns4__Organization");
        add("ns8__TenureInOrganization");
        add("ns8__Officership");
        add("ns8__Directorship");
    }};

    public static Set<String> relationshipName;

}
