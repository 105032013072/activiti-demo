package com.bosssoft.platform.activiti.annotation.impl;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.spi.execution.core.ExecutionContext;
import org.springframework.stereotype.Service;

import com.bosssoft.platform.activiti.AnnoexeUtil;
import com.bosssoft.platform.activiti.annotation.TaskListenerService;

@Service
public class TaskListenerImpl implements TaskListenerService {
    
    public void start(ExecutionContext executionContext) {
    	saveEventInfo(executionContext, 0);
    }

    
    public void end(ExecutionContext executionContext) {
    	saveEventInfo(executionContext, 0);
    }

    
    public void activate(ExecutionContext executionContext) {
    	saveEventInfo(executionContext, 0);
    }

   
    public void assign(ExecutionContext executionContext) {
    	saveEventInfo(executionContext, 0);

    }

    
    public void unclaim(ExecutionContext executionContext) {
    	saveEventInfo(executionContext, 0);

    }

    
    public void claim(ExecutionContext executionContext) {
    	saveEventInfo(executionContext, 0);

    }

    
    public void back(ExecutionContext executionContext) {
    	saveEventInfo(executionContext, 0);

    }

    
    public void redo(ExecutionContext executionContext) {
    	saveEventInfo(executionContext, 0);

    }

    
    public void withdraw(ExecutionContext executionContext) {
    	saveEventInfo(executionContext, 0);

    }

   
    public void confirm(ExecutionContext executionContext) {
    	saveEventInfo(executionContext, 0);

    }
    
    private void saveEventInfo(ExecutionContext executionContext,int order){
    	EventContext eventContext=new EventContext();
    	eventContext.setListernerId("process-listener-1");
    	eventContext.setExcutorOrder(order);
    	eventContext.setEventEntity(executionContext);
    	
    	try {
			AnnoexeUtil.recordAnnoexe(eventContext, executionContext.getEventName());
		} catch (Exception e) {
			throw new ActivitiException(e.toString());
		}
    }
}
