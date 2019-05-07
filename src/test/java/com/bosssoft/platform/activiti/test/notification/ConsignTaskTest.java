package com.bosssoft.platform.activiti.test.notification;

import java.util.List;
import static org.junit.Assert.*;

import org.activiti.engine.consign.ConsignDefType;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.identity.IdentityEnum;
import org.activiti.engine.spi.identity.Participator;
import org.activiti.engine.spi.notification.NotificationContext;
import org.activiti.engine.spi.notification.NotificationType;
import org.activiti.engine.spi.notification.event.NotificationCategory;
import org.activiti.engine.spi.notification.event.NotificationEvent;
import org.activiti.engine.task.Task;
import org.junit.Test;

import com.bosssoft.platform.activiti.NotificationUtil;
import com.bosssoft.platform.activiti.test.MainTest;
import com.bosssoft.platform.activiti.test.TestUtil;

public class ConsignTaskTest extends MainTest{
   
	/**
	 * 代办任务测试
	 * @throws Exception
	 */
	@Test
	public void substitutionTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/notification/process_substitution_copperation.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		List<NotificationEvent> notificationList=NotificationUtil.getNotifications();
		assertEquals(1, notificationList.size());
		NotificationEvent event=notificationList.get(0);
		verifyNotification(event, NotificationType.NEWTASK, new String[]{"u01"});
		
		NotificationUtil.cleanNotificationLog();
		
		//创建代办
		 Task task=taskService.createTaskQuery().processInstanceId(inst.getId()).taskName("A").singleResult();
		 taskService.createTaskSubstitutionInst(task.getAssignee(), "u02", IdentityEnum.USER, task.getId(), null);
		
		  notificationList=NotificationUtil.getNotifications();
	     assertEquals(1, notificationList.size());
		 event=notificationList.get(0);
		 verifyNotification(event, NotificationType.SUBSTITUTION, new String[]{"u02"});
		
	}
	
	/**
	 * 协办任务测试  （包括重做，待确认）
	 * @throws Exception
	 */
	@Test
	public void cooperationTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/notification/process_substitution_copperation.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		List<NotificationEvent> notificationList=NotificationUtil.getNotifications();
		assertEquals(1, notificationList.size());
		NotificationEvent event=notificationList.get(0);
		verifyNotification(event, NotificationType.NEWTASK, new String[]{"u01"});
		
		NotificationUtil.cleanNotificationLog();
		
		//创建协办
		 Task task=taskService.createTaskQuery().processInstanceId(inst.getId()).taskName("A").singleResult();
		 taskService.createTaskCooperationInst(task.getAssignee(), "u02", IdentityEnum.USER, task.getId(), null);
		
		  notificationList=NotificationUtil.getNotifications();
	     assertEquals(1, notificationList.size());
		 event=notificationList.get(0);
		 verifyNotification(event, NotificationType.COOPERATION, new String[]{"u02"});
		 
		 String consignInstId= TestUtil.getConsignInstId(task.getId(), "u02", taskService);
		 NotificationUtil.cleanNotificationLog();
		 taskService.claimCascade(task.getId(),consignInstId, "u02");//接受协办任务
		 taskService.completCascade(task.getId(), consignInstId);
		 
		 notificationList=NotificationUtil.getNotifications();
	     assertEquals(1, notificationList.size());
		 event=notificationList.get(0);
		 verifyNotification(event, NotificationType.CONFIRM, new String[]{"u01"});
		 
		 NotificationUtil.cleanNotificationLog();
		 
		 //重做
		 taskService.redo(consignInstId);
		 
		 notificationList=NotificationUtil.getNotifications();
	     assertEquals(1, notificationList.size());
		 event=notificationList.get(0);
		 verifyNotification(event, NotificationType.REDO, new String[]{"u02"});
		
	}
	
	/**
	 * 包含代理的协办
	 * @throws Exception 
	 */
	@Test
	public void cooperationWithDelegateTest() throws Exception{
		Model model = super.createAndDeployModel(
				"com/bosssoft/platform/activiti/test/notification/process_substitution_copperation.bpmnx");

		// 创建委托
		repositoryService.createProcessConsignDefinition().consignor("u02")
		.addConsignee(new Participator("u03", "小王", IdentityEnum.USER, null)).consignDefType(ConsignDefType.ALL)
		.startTime("2018-06-11").endTime("2019-9-22").save();

		ProcessInstance inst = runtimeService.createProcessInstanceBuilder()
				.processDefinitionId(model.getProcessDefinitionId()).processStarter("u03").start();
		
        NotificationUtil.cleanNotificationLog();
		
		//创建协办
		 Task task=taskService.createTaskQuery().processInstanceId(inst.getId()).taskName("A").singleResult();
		 taskService.createTaskCooperationInst(task.getAssignee(), "u02", IdentityEnum.USER, task.getId(), null);
		 String cooperationInstId= TestUtil.getConsignInstId(task.getId(), "u02", taskService);
		 
		 //协办任务被代理给u03
		 taskService.claimCascade(task.getId(),cooperationInstId, "u02");//接受协办任务
		
		 NotificationUtil.cleanNotificationLog();
		 
		 String delegateInstId=TestUtil.getConsignInstId(task.getId(), "u03", taskService);
		 taskService.completCascade(task.getId(), delegateInstId);
		 
		 afterConsign(task, cooperationInstId, delegateInstId);
	}
	
	/**
	 * 包含代办的协办
	 * @throws Exception 
	 */
	@Test
	public void cooperationWithsubstitutionTest() throws Exception{
		Model model = super.createAndDeployModel(
				"com/bosssoft/platform/activiti/test/notification/process_substitution_copperation.bpmnx");

		ProcessInstance inst = runtimeService.createProcessInstanceBuilder()
				.processDefinitionId(model.getProcessDefinitionId()).processStarter("u03").start();
		
        NotificationUtil.cleanNotificationLog();
		
		//创建协办
		 Task task=taskService.createTaskQuery().processInstanceId(inst.getId()).taskName("A").singleResult();
		 taskService.createTaskCooperationInst(task.getAssignee(), "u02", IdentityEnum.USER, task.getId(), null);
		 String cooperationInstId= TestUtil.getConsignInstId(task.getId(), "u02", taskService);
		 
		 taskService.claimCascade(task.getId(),cooperationInstId, "u02");//接受协办任务
		 
		 //创建代办
		 taskService.createTaskSubstitutionInst("u02", "u03", IdentityEnum.USER, task.getId(), cooperationInstId);
		 String substitutionInstId=TestUtil.getConsignInstId(task.getId(), "u03", taskService);
		 taskService.claimCascade(task.getId(), substitutionInstId, "u03");
		 NotificationUtil.cleanNotificationLog();
		 
		 taskService.completCascade(task.getId(), substitutionInstId);
		 
		 afterConsign(task, cooperationInstId, substitutionInstId);
	}
	
	/**
	 * 包含多个层级的协办
	 * @throws Exception 
	 */
	@Test
	public void cooperationWithCooperationTest() throws Exception{
		Model model = super.createAndDeployModel(
				"com/bosssoft/platform/activiti/test/notification/process_substitution_copperation.bpmnx");

		ProcessInstance inst = runtimeService.createProcessInstanceBuilder()
				.processDefinitionId(model.getProcessDefinitionId()).processStarter("u03").start();
		
        NotificationUtil.cleanNotificationLog();
		
		//创建协办
		 Task task=taskService.createTaskQuery().processInstanceId(inst.getId()).taskName("A").singleResult();
		 taskService.createTaskCooperationInst(task.getAssignee(), "u02", IdentityEnum.USER, task.getId(), null);
		 String cooperationInstId= TestUtil.getConsignInstId(task.getId(), "u02", taskService);
		 
		 taskService.claimCascade(task.getId(),cooperationInstId, "u02");//接受协办任务
		 
		 //创建代办
		 taskService.createTaskCooperationInst("u02", "u03", IdentityEnum.USER, task.getId(), cooperationInstId);
		 String cooperatioInstId=TestUtil.getConsignInstId(task.getId(), "u03", taskService);
		 taskService.claimCascade(task.getId(), cooperatioInstId, "u03");//领取协办任务		 
		 taskService.completCascade(task.getId(), cooperatioInstId);
		 
		 NotificationUtil.cleanNotificationLog();
		 //u02确认协办
		 taskService.completCascade(task.getId(), cooperatioInstId);
		 
		
		 //u01 的待确认通知
		 List<NotificationEvent> notificationList=NotificationUtil.getNotifications();
		 assertEquals(1, notificationList.size());
		 NotificationEvent event=notificationList.get(0);
		 
		 verifyNotification(event, NotificationType.CONFIRM, new String[]{"u01"});
		 NotificationUtil.cleanNotificationLog();
		 //重做
		 taskService.redo(cooperationInstId);
		 
		 notificationList=NotificationUtil.getNotifications();
	     assertEquals(1, notificationList.size());
		 event=notificationList.get(0);
		 verifyNotification(event, NotificationType.REDO, new String[]{"u03"});

		
	}
	
	private void afterConsign(Task task,String cooperationInstId,String consignId) throws Exception{
		 
		
		 List<NotificationEvent> notificationList=NotificationUtil.getNotifications();
		 assertEquals(1, notificationList.size());
		 NotificationEvent event=notificationList.get(0);
		 
		 verifyNotification(event, NotificationType.CONFIRM, new String[]{"u01"});
		 NotificationUtil.cleanNotificationLog();
		 //重做
		 taskService.redo(cooperationInstId);
		 
		 notificationList=NotificationUtil.getNotifications();
	     assertEquals(1, notificationList.size());
		 event=notificationList.get(0);
		 verifyNotification(event, NotificationType.REDO, new String[]{"u03"});
		 NotificationUtil.cleanNotificationLog();
		
		 //重做后完成任务，待确认
		 taskService.completCascade(task.getId(), consignId);
		  notificationList=NotificationUtil.getNotifications();
		 assertEquals(1, notificationList.size());
		 event=notificationList.get(0);
		 
		 verifyNotification(event, NotificationType.CONFIRM, new String[]{"u01"});
	}

	private void verifyNotification(NotificationEvent notificationEvent,NotificationType expectType,String[] excepetReceiver){
       assertEquals(NotificationCategory.TASKNOTIFICATION, notificationEvent.getNotificationCategory());
		
		NotificationContext notificationContext=(NotificationContext)notificationEvent.getNotificationEventContext();
		assertEquals(expectType, notificationContext.getNotificationType());
		TestUtil.checkReceiver(notificationContext.getReceivers(), excepetReceiver);
	}
	
}
