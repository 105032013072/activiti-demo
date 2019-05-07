package com.bosssoft.platform.activiti.test.notification;

import org.junit.Test;

import com.bosssoft.platform.activiti.NotificationUtil;
import com.bosssoft.platform.activiti.test.MainTest;
import com.bosssoft.platform.activiti.test.TestUtil;

import static org.junit.Assert.*;

import java.util.List;

import org.activiti.engine.consign.ConsignInstType;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.identity.IdentityEnum;
import org.activiti.engine.spi.notification.NotificationContext;
import org.activiti.engine.spi.notification.NotificationType;
import org.activiti.engine.spi.notification.event.NotificationCategory;
import org.activiti.engine.spi.notification.event.NotificationEvent;
import org.activiti.engine.task.Task;


public class RejectConsignTest extends MainTest{
    
	/**
	 * 拒绝代办任务
	 */
	@Test
	public void rejectSubstitution()throws Exception{
		rejectConsignTest(ConsignInstType.SUBSTITUTION);

	} 
	
	/**
	 * 拒绝协办任务
	 */
	@Test
	public void rejectcooperation()throws Exception{
		rejectConsignTest(ConsignInstType.COOPERATION);

	} 
	
	private void rejectConsignTest(ConsignInstType consignInstType) throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/notification/process_substitution_copperation.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		Task task=taskService.createTaskQuery().processInstanceId(inst.getId()).taskName("A").singleResult();
		if(ConsignInstType.SUBSTITUTION.equals(consignInstType)){
			taskService.createTaskSubstitutionInst(task.getAssignee(), "u02", IdentityEnum.USER, task.getId(), null);
		}else if(ConsignInstType.COOPERATION.equals(consignInstType)){
			taskService.createTaskCooperationInst(task.getAssignee(), "u02", IdentityEnum.USER, task.getId(), null);
		}
		
		NotificationUtil.cleanNotificationLog();
		String consignInstId=TestUtil.getConsignInstId(task.getId(), "u02", taskService);
		taskService.rejectTask(consignInstId);
		
		List<NotificationEvent> notificationList=NotificationUtil.getNotifications();
		assertEquals(1, notificationList.size());
		NotificationEvent event=notificationList.get(0);
		assertEquals(NotificationCategory.TASKNOTIFICATION, event.getNotificationCategory());
		NotificationContext notificationContext=(NotificationContext)event.getNotificationEventContext();
		assertEquals(NotificationType.REJECT, notificationContext.getNotificationType());
		TestUtil.checkReceiver(notificationContext.getReceivers(), new String[]{"u01"});
		
		
	}
}
