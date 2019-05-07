package com.bosssoft.platform.activiti.annotation.impl;


import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.spi.execution.core.ExecutionContext;
import org.springframework.stereotype.Service;

import com.bosssoft.platform.activiti.AnnoexeUtil;
import com.bosssoft.platform.activiti.annotation.ProcessListenerService;

@Service
public class PrcessListenerImpl implements ProcessListenerService {
    
	
    public void start(ExecutionContext executionContext) {
    	saveEventInfo(executionContext, 0);
    }

    
    public void supspend(ExecutionContext executionContext) {
    	saveEventInfo(executionContext, 0);
    }

    
    public void activate(ExecutionContext executionContext) {
    	saveEventInfo(executionContext, 0);

    }

    
    public void end(ExecutionContext executionContext) {
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
