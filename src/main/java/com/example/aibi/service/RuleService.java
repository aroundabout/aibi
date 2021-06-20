package com.example.aibi.service;

import com.example.aibi.entity.Company;
import com.example.aibi.entity.Person;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * @ClassName RuleService
 * @Description //TODO
 * @Author poros
 * @Date 2021/5/8 20:00
 **/
@Service
public class RuleService {

    @Autowired
    private KieBase kieBase;

    public void rule(Person person) throws UnsupportedEncodingException {
        // commit branch
        KieSession kieSession = kieBase.newKieSession();
        kieSession.insert(person);
        kieSession.fireAllRules();
        kieSession.dispose();
    }

    public void rule(Company company) throws UnsupportedEncodingException {
        // commit branch
        KieSession kieSession = kieBase.newKieSession();
        kieSession.insert(company);
        kieSession.fireAllRules();
        kieSession.dispose();
    }

}
