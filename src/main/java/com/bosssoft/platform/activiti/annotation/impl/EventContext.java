package com.bosssoft.platform.activiti.annotation.impl;

import java.io.Serializable;

public class EventContext implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String listernerId;
	
	private int excutorOrder;
	
	private Object eventEntity;

	public String getListernerId() {
		return listernerId;
	}

	public void setListernerId(String listernerId) {
		this.listernerId = listernerId;
	}

	

	public int getExcutorOrder() {
		return excutorOrder;
	}

	public void setExcutorOrder(int excutorOrder) {
		this.excutorOrder = excutorOrder;
	}

	public Object getEventEntity() {
		return eventEntity;
	}

	public void setEventEntity(Object eventEntity) {
		this.eventEntity = eventEntity;
	}
	
	
} 
