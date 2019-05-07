package com.bosssoft.platform.activiti.test;

import static org.junit.Assert.assertEquals;

import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.identity.IdentityEnum;
import org.activiti.engine.task.Task;
import org.junit.Test;

public class MyTest extends MainTest{
	/**
	 * 默认的办理人
	 * @throws Exception
	 */
	@Test
	public void  defaultAssigneeTest() throws Exception {
    //Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/participator/process_assignee_default.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                //.processDefinitionId(model.getProcessDefinitionId())
                .processDefinitionId("Process_1:1:537506")
                .businessKey("business_01")
                .processStarter("u03")
                .start();
		
		Task task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "填写申请", task.getName());
		assertEquals("错误的任务办理人", "u02", task.getAssignee());
		
		taskService.completCascade(task.getId(), null);
		
		//进入第二个节点
		task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "上级审核", task.getName());
		assertEquals("错误的任务办理人", "u02", task.getAssignee());
		
		taskService.completCascade(task.getId(), null);
		
		//进入第三个节点
		task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "终审", task.getName());
		assertEquals("错误的任务办理人", "u02", task.getAssignee());
	}
	
	@Test
	public void consignee(){
		taskService.createTaskSubstitutionInst("u02", "u01", IdentityEnum.USER, "537514", null);
		taskService.createTaskSubstitutionInst("u02", "u01", IdentityEnum.USER, "542508", null);
		taskService.createTaskSubstitutionInst("u02", "u01", IdentityEnum.USER, "540008", null);
		System.out.println("dddd");
	}
	
	@Test
	public void complete(){
		taskService.claimCascade("540008", "545012", "u01");
		taskService.completCascade("540008", "545012");
		
		taskService.claimCascade("537514", "545002", "u01");
		taskService.completCascade("537514", "545002");
		
		taskService.claimCascade("542508", "545007", "u01");
		taskService.completCascade("542508", "545007");
		
		System.out.println("123");
		
		
	}
}
