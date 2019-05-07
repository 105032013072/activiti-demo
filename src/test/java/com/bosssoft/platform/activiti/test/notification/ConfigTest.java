package com.bosssoft.platform.activiti.test.notification;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.notification.NotificationContext;
import org.activiti.engine.spi.notification.NotificationType;
import org.activiti.engine.spi.notification.event.NotificationEvent;
import org.junit.Test;

import com.bosssoft.platform.activiti.NotificationUtil;
import com.bosssoft.platform.activiti.test.MainTest;

public class ConfigTest extends MainTest{
  
	@Test
	public void emailAndSmsTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/notification/process_sms_email.bpmnx");

		
		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		
	}
	
	//局部的任务通知
	@Test
	public void localNotiification() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/notification/process_localNotiification.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		List<NotificationEvent> notificationList=NotificationUtil.getNotifications();
		assertEquals(1, notificationList.size());
		NotificationEvent event=notificationList.get(0);
		NotificationContext notificationContext=(NotificationContext)event.getNotificationEventContext();
		assertEquals(NotificationType.NEWTASK, notificationContext.getNotificationType());
		assertEquals("A", notificationContext.getTaskName());
		
		NotificationUtil.cleanNotificationLog();
		runtimeService.revokeProcess(inst.getId(), "撤销", "u03");
		notificationList=NotificationUtil.getNotifications();
		assertEquals(1, notificationList.size());
		 event=notificationList.get(0);
		 notificationContext=(NotificationContext)event.getNotificationEventContext();
		assertEquals(NotificationType.PROCESSREVOKE, notificationContext.getNotificationType());
		assertEquals("B", notificationContext.getTaskName());
		
	}
	
}
