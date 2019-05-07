package com.bosssoft.platform.activiti.test;

import org.activiti.engine.consign.ConsignInstState;
import org.activiti.engine.impl.task.TaskBusinessState;

public class TaskVerifyEntity {

	public String expextTaskDefKey;
	
	public String expectBusinessState;
	
	public String expectConsigState;
	
	public String expectAssignee;
	
	public Boolean expectIsArriveConsignLimit;
	
	public Boolean editable;
	
	public String expectHolder;
	
	public TaskVerifyEntity(String expextTaskDefKey,String expectBusinessState
			,String expectConsigState,String expectAssignee,Boolean expectIsArriveConsignLimit,Boolean editable){
		this.expextTaskDefKey=expextTaskDefKey;
		this.expectBusinessState=expectBusinessState;
		this.expectConsigState=expectConsigState;
		this.expectAssignee=expectAssignee;
		this.expectIsArriveConsignLimit=expectIsArriveConsignLimit;
		this.editable=editable;
		
	}
	
	public TaskVerifyEntity(String expextTaskDefKey,String expectBusinessState
			,String expectConsigState,String expectAssignee,Boolean expectIsArriveConsignLimit,Boolean editable,String expectHolder){
		this.expextTaskDefKey=expextTaskDefKey;
		this.expectBusinessState=expectBusinessState;
		this.expectConsigState=expectConsigState;
		this.expectAssignee=expectAssignee;
		this.expectIsArriveConsignLimit=expectIsArriveConsignLimit;
		this.editable=editable;
		this.expectHolder=expectHolder;
	}
	

	public String getExpextTaskDefKey() {
		return expextTaskDefKey;
	}

	public void setExpextTaskDefKey(String expextTaskDefKey) {
		this.expextTaskDefKey = expextTaskDefKey;
	}

	
	
	public String getExpectBusinessState() {
		return expectBusinessState;
	}

	public void setExpectBusinessState(String expectBusinessState) {
		this.expectBusinessState = expectBusinessState;
	}

	public String getExpectConsigState() {
		return expectConsigState;
	}

	public void setExpectConsigState(String expectConsigState) {
		this.expectConsigState = expectConsigState;
	}

	public String getExpectAssignee() {
		return expectAssignee;
	}

	public void setExpectAssignee(String expectAssignee) {
		this.expectAssignee = expectAssignee;
	}

	public Boolean getExpectIsArriveConsignLimit() {
		return expectIsArriveConsignLimit;
	}

	public void setExpectIsArriveConsignLimit(Boolean expectIsArriveConsignLimit) {
		this.expectIsArriveConsignLimit = expectIsArriveConsignLimit;
	}

	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	public String getExpectHolder() {
		return expectHolder;
	}

	public void setExpectHolder(String expectHolder) {
		this.expectHolder = expectHolder;
	}
	
	
	
}
