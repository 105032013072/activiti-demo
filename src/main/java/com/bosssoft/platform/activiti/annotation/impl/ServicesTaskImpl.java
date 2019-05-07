package com.bosssoft.platform.activiti.annotation.impl;

import org.springframework.stereotype.Service;

import com.bosssoft.platform.activiti.annotation.IServicesTask;


@Service
public class ServicesTaskImpl implements IServicesTask {
    
    public void exe() {
        System.out.println("ServicesTaskImpl execution");
    }
}
