package com.bosssoft.platform.activiti.test.notification;

import com.bosssoft.platform.activiti.NotificationUtil;
import com.bosssoft.platform.activiti.test.MainTest;
import com.bosssoft.platform.activiti.test.TestUtil;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

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

/**
 * 流程撤销通知
 * @author huangxw
 *
 */
public class ProcessRevokeTest extends MainTest{
    
	/**
	 * 流程当前活动接节点为个人任务，组任务（包括代理情况下的任务）
	 * @throws Exception 
	 */
	@Test
	public void personalAndGroupTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/notification/process_personalAndGroupTest.bpmnx");

		//创建委托
		repositoryService.createProcessConsignDefinition()
        .consignor("u02")
        .addConsignee(new Participator("u06", "王五", IdentityEnum.USER, null))
        .consignDefType(ConsignDefType.ALL)
        .startTime("2018-06-11")
    	 .endTime("2019-9-22")
    	 .save();
		
		repositoryService.createProcessConsignDefinition()
        .consignor("u03")
        .addConsignee(new Participator("r05", "高级测试工程师", IdentityEnum.ROLE, null))
        .consignDefType(ConsignDefType.ALL)
        .startTime("2018-06-11")
    	 .endTime("2019-9-22")
    	 .save();
		
		
		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		NotificationUtil.cleanNotificationLog();
		
		runtimeService.revokeProcess(inst.getId(), "撤销", "u03");
		
		//校验通知信息
		List<NotificationEvent> notificationList=NotificationUtil.getNotifications();
		assertEquals(4, notificationList.size());
		
		List<Participator> areceiver=new ArrayList<Participator>();
		List<Participator> breceiver=new ArrayList<Participator>();
		List<Participator> creceiver=new ArrayList<Participator>();
		List<Participator> dreceiver=new ArrayList<Participator>();
		for (NotificationEvent notificationEvent : notificationList) {
			assertEquals(NotificationCategory.TASKNOTIFICATION, notificationEvent.getNotificationCategory());
			
			NotificationContext notificationContext=(NotificationContext)notificationEvent.getNotificationEventContext();
			
			assertEquals(NotificationType.PROCESSREVOKE, notificationContext.getNotificationType());
			
			if("A".equals(notificationContext.getTaskName())){
				areceiver.addAll(notificationContext.getReceivers());
			}else if("B".equals(notificationContext.getTaskName())){//B
				breceiver.addAll(notificationContext.getReceivers());
			}else if("C".equals(notificationContext.getTaskName())){
				creceiver.addAll(notificationContext.getReceivers());
			}else {
				dreceiver.addAll(notificationContext.getReceivers());
			}
		}
		
		TestUtil. checkReceiver(areceiver, new String[]{"u01"});
        TestUtil. checkReceiver(breceiver, new String[]{"u04","u05"});
        TestUtil. checkReceiver(creceiver, new String[]{"u06"});
        TestUtil. checkReceiver(dreceiver, new String[]{"u09","u10"});
		
	}
	
	/**
	 * 流程当前活动接节点为代办任务（待接受和已经接受）以及会签
	 * @throws Exception 
	 */
	@Test
	public void subsitutionAndCounterSign() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/notification/process_subsitutionAndCounterSign.bpmnx");
	    
		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		//创建代办
		Task Atask=taskService.createTaskQuery().processInstanceId(inst.getId()).taskName("A").singleResult();
		Task Btask=taskService.createTaskQuery().processInstanceId(inst.getId()).taskName("B").singleResult();
		
		taskService.createTaskSubstitutionInst(Atask.getAssignee(), "u03", IdentityEnum.USER, Atask.getId(), null);
		taskService.createTaskSubstitutionInst(Btask.getAssignee(), "u04", IdentityEnum.USER, Btask.getId(), null);
		
		String bConsignInstId=TestUtil.getConsignInstId(Btask.getId(), "u04", taskService);
		taskService.claimCascade(Btask.getId(), bConsignInstId, "u04");
		assertNull(taskService.createTaskQuery().taskId(Atask.getId()).singleResult().getAssignee());
		assertEquals("u04", taskService.createTaskQuery().taskId(Btask.getId()).singleResult().getAssignee());
		
        NotificationUtil.cleanNotificationLog();
		
		runtimeService.revokeProcess(inst.getId(), "撤销", "u03");
		
		
		
		
		
		//校验通知信息
		List<NotificationEvent> notificationList=NotificationUtil.getNotifications();
		assertEquals(4, notificationList.size());
		
		List<Participator> areceiver=new ArrayList<Participator>();
		List<Participator> breceiver=new ArrayList<Participator>();
		List<Participator> creceiver=new ArrayList<Participator>();
		for (NotificationEvent notificationEvent : notificationList) {
			assertEquals(NotificationCategory.TASKNOTIFICATION, notificationEvent.getNotificationCategory());
			
			NotificationContext notificationContext=(NotificationContext)notificationEvent.getNotificationEventContext();
			
			assertEquals(NotificationType.PROCESSREVOKE, notificationContext.getNotificationType());
			
			if("A".equals(notificationContext.getTaskName())){
				areceiver.addAll(notificationContext.getReceivers());
			}else if("B".equals(notificationContext.getTaskName())){//B
				breceiver.addAll(notificationContext.getReceivers());
			}else {
				creceiver.addAll(notificationContext.getReceivers());
			}
		}
		
		TestUtil. checkReceiver(areceiver, new String[]{"u03"});
        TestUtil. checkReceiver(breceiver, new String[]{"u04"});
        TestUtil. checkReceiver(creceiver, new String[]{"u04","u05","u06","u07","u08"});
		
	}
	
	@Test
	public void cooperationTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/notification/process_cooperation.bpmnx");
	    
		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		//创建代办
		Task Atask=taskService.createTaskQuery().processInstanceId(inst.getId()).taskName("A").singleResult();
		Task Btask=taskService.createTaskQuery().processInstanceId(inst.getId()).taskName("B").singleResult();
		
		taskService.createTaskCooperationInst(Atask.getAssignee(), "u03", IdentityEnum.USER, Atask.getId(), null);
		taskService.createTaskCooperationInst(Btask.getAssignee(), "u04", IdentityEnum.USER, Btask.getId(), null);
		
		String aConsignInstId=TestUtil.getConsignInstId(Atask.getId(), "u03", taskService);
		String bConsignInstId=TestUtil.getConsignInstId(Btask.getId(), "u04", taskService);
		taskService.claimCascade(Atask.getId(), aConsignInstId, "u03");
		taskService.claimCascade(Btask.getId(), bConsignInstId, "u04");
		
		taskService.completCascade(Btask.getId(), bConsignInstId);
		
         NotificationUtil.cleanNotificationLog();
		
		runtimeService.revokeProcess(inst.getId(), "撤销", "u03");
		
		
		// 校验通知信息
		List<NotificationEvent> notificationList = NotificationUtil.getNotifications();
		assertEquals(2, notificationList.size());

		List<Participator> areceiver = new ArrayList<Participator>();
		List<Participator> breceiver = new ArrayList<Participator>();
		for (NotificationEvent notificationEvent : notificationList) {
			assertEquals(NotificationCategory.TASKNOTIFICATION, notificationEvent.getNotificationCategory());

			NotificationContext notificationContext = (NotificationContext) notificationEvent
					.getNotificationEventContext();

			assertEquals(NotificationType.PROCESSREVOKE, notificationContext.getNotificationType());

			if ("A".equals(notificationContext.getTaskName())) {
				areceiver.addAll(notificationContext.getReceivers());
			} else {// B
				breceiver.addAll(notificationContext.getReceivers());
			} 
		}

		TestUtil.checkReceiver(areceiver, new String[] { "u03" });
		TestUtil.checkReceiver(breceiver, new String[] { "u01" });
	
	}
	

}
