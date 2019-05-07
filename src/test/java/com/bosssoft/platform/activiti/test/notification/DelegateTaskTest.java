package com.bosssoft.platform.activiti.test.notification;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.activiti.engine.consign.ConsignDefType;
import org.activiti.engine.impl.TaskExt;
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


public class DelegateTaskTest extends MainTest{

	@Test
	public void newTaskDelegate() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/notification/process_newTaskDelegate.bpmnx");

		//创建委托
		repositoryService.createProcessConsignDefinition()
		                  .consignor("u01")
		                  .addConsignee(new Participator("u02", "小雨", IdentityEnum.USER, null))
		                  .addConsignee(new Participator("u03", "小王", IdentityEnum.USER, null))
		                  .consignDefType(ConsignDefType.ALL)
		                  .startTime("2018-06-11")
		              	  .endTime("2019-9-22")
		              	  .save();
		
		
		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();

		List<NotificationEvent> notificationList=NotificationUtil.getNotifications();
		 String [] expectActArray=new String[]{"A","B"};
	      String [] targetActArray=new String[2];
	      Arrays.sort(expectActArray);
		
		
        assertEquals(2, notificationList.size());
        int i=0;
        for (NotificationEvent notificationEvent : notificationList) {
			assertEquals(NotificationCategory.TASKNOTIFICATION, notificationEvent.getNotificationCategory());
			
			NotificationContext notificationContext=(NotificationContext)notificationEvent.getNotificationEventContext();
			targetActArray[i++]=notificationContext.getTaskName();
			
			if("A".equals(notificationContext.getTaskName())){
				assertEquals(NotificationType.DELEGATE, notificationContext.getNotificationType());
				TestUtil.checkReceiver(notificationContext.getReceivers(), new String[]{"u02","u03"});
			}else{//B
				assertEquals(NotificationType.NEWTASK, notificationContext.getNotificationType());
				TestUtil.checkReceiver(notificationContext.getReceivers(), new String[]{"u01","u06"});
			}
		}
        Arrays.sort(targetActArray);
		assertArrayEquals(expectActArray, targetActArray);
		
		
		NotificationUtil.cleanNotificationLog();//清空通知日志
		
		//查询B节点的任务，并且领取
		Task Btask=taskService.createTaskQuery().processInstanceId(inst.getId()).taskName("B").singleResult();
		taskService.claimCascade(Btask.getId(), null, "u01");
		
		notificationList=NotificationUtil.getNotifications();
		assertEquals(1, notificationList.size());
		NotificationEvent bNotificationEvent=notificationList.get(0);
		assertEquals(NotificationCategory.TASKNOTIFICATION, bNotificationEvent.getNotificationCategory());
		NotificationContext context=(NotificationContext)bNotificationEvent.getNotificationEventContext();
		assertEquals(NotificationType.DELEGATE, context.getNotificationType());
		TestUtil.checkReceiver(context.getReceivers(), new String[]{"u02","u03"});
		
	}
	
	@Test
	public void consignTaskDelegate() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/notification/process_consignTaskDelegate.bpmnx");

		// 创建委托
		repositoryService.createProcessConsignDefinition().consignor("u01")
				.addConsignee(new Participator("u02", "小雨", IdentityEnum.USER, null)).consignDefType(ConsignDefType.ALL)
				.startTime("2018-06-11").endTime("2019-9-22").save();

		repositoryService.createProcessConsignDefinition().consignor("u02")
				.addConsignee(new Participator("u03", "小王", IdentityEnum.USER, null)).consignDefType(ConsignDefType.ALL)
				.startTime("2018-06-11").endTime("2019-9-22").save();
		
		
		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		List<NotificationEvent> notificationList=NotificationUtil.getNotifications();
		 String [] expectActArray=new String[]{"A","B"};
	      String [] targetActArray=new String[2];
	      Arrays.sort(expectActArray);
		
		
       assertEquals(2, notificationList.size());
       int i=0;
       for (NotificationEvent notificationEvent : notificationList) {
			assertEquals(NotificationCategory.TASKNOTIFICATION, notificationEvent.getNotificationCategory());
			
			NotificationContext notificationContext=(NotificationContext)notificationEvent.getNotificationEventContext();
			targetActArray[i++]=notificationContext.getTaskName();
			
			if("A".equals(notificationContext.getTaskName())){
				assertEquals(NotificationType.DELEGATE, notificationContext.getNotificationType());
				TestUtil.checkReceiver(notificationContext.getReceivers(), new String[]{"u03"});
			}else{//B
				assertEquals(NotificationType.NEWTASK, notificationContext.getNotificationType());
				TestUtil.checkReceiver(notificationContext.getReceivers(), new String[]{"u05"});
			}
		}
       Arrays.sort(targetActArray);
	   assertArrayEquals(expectActArray, targetActArray);
		
	   
	   Task Atask=taskService.createTaskQuery().processInstanceId(inst.getId()).taskName("A").singleResult();
	   
	   //B节点创建代办
	   Task Btask=taskService.createTaskQuery().processInstanceId(inst.getId()).taskName("B").singleResult();
	   taskService.createTaskSubstitutionInst(Btask.getAssignee(), "u01", IdentityEnum.USER, Btask.getId(), null);
	   
	   NotificationUtil.cleanNotificationLog();//清空通知日志
	   
	   taskService.claimCascade(Btask.getId(), TestUtil.getConsignInstId(Btask.getId(), "u01",taskService), "u01");//接受代办任务
	   notificationList=NotificationUtil.getNotifications();
	   
	   assertEquals(1, notificationList.size());
		NotificationEvent bNotificationEvent=notificationList.get(0);
		assertEquals(NotificationCategory.TASKNOTIFICATION, bNotificationEvent.getNotificationCategory());
		NotificationContext context=(NotificationContext)bNotificationEvent.getNotificationEventContext();
		assertEquals(NotificationType.DELEGATE, context.getNotificationType());
	    TestUtil.checkReceiver(context.getReceivers(), new String[]{"u02"});
	    
	    NotificationUtil.cleanNotificationLog();//清空通知日志
	    
	    taskService.completCascade(Atask.getId(), TestUtil.getConsignInstId(Atask.getId(), Atask.getAssignee(),taskService));
	    taskService.completCascade(Btask.getId(), TestUtil.getConsignInstId(Btask.getId(), Btask.getAssignee(),taskService));
	    
	    //C 节点
	    notificationList=NotificationUtil.getNotifications();
		assertEquals(1, notificationList.size());
		NotificationEvent notificationEvent=notificationList.get(0);
		assertEquals(NotificationCategory.TASKNOTIFICATION, notificationEvent.getNotificationCategory());
		context=(NotificationContext)notificationEvent.getNotificationEventContext();
		assertEquals(NotificationType.NEWTASK, context.getNotificationType());
		TestUtil.checkReceiver(context.getReceivers(), new String[]{"u05"});
		
		Task Ctask=taskService.createTaskQuery().processInstanceId(inst.getId()).taskName("C").singleResult();
		taskService.createTaskCooperationInst(Ctask.getAssignee(), "u01", IdentityEnum.USER, Ctask.getId(), null);
		
		NotificationUtil.cleanNotificationLog();//清空通知日志
		
		taskService.claimCascade(Ctask.getId(), TestUtil.getConsignInstId(Ctask.getId(), "u01",taskService), "u01");//接受协办任务
		
		
		notificationList=NotificationUtil.getNotifications();
		assertEquals(1, notificationList.size());
		notificationEvent=notificationList.get(0);
		assertEquals(NotificationCategory.TASKNOTIFICATION, bNotificationEvent.getNotificationCategory());
		context=(NotificationContext)bNotificationEvent.getNotificationEventContext();
		assertEquals(NotificationType.DELEGATE, context.getNotificationType());
		TestUtil.checkReceiver(context.getReceivers(), new String[]{"u02"});
	}
	
	
}
