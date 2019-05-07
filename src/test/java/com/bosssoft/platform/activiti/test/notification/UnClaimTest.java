package com.bosssoft.platform.activiti.test.notification;
import static org.junit.Assert.*;
import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.consign.ConsignDefType;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.identity.IdentityEnum;
import org.activiti.engine.spi.identity.Participator;
import org.activiti.engine.spi.notification.NotificationContext;
import org.activiti.engine.spi.notification.NotificationType;
import org.activiti.engine.spi.notification.event.NotificationEvent;
import org.activiti.engine.task.Task;
import org.junit.Test;

import com.bosssoft.platform.activiti.NotificationUtil;
import com.bosssoft.platform.activiti.test.MainTest;
import com.bosssoft.platform.activiti.test.TestUtil;

public class UnClaimTest extends MainTest{

	@Test
	public void candidateAndDelegate() throws Exception  {
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/notification/process_unclaim_candidateAndDelegate.bpmnx");

		//创建委托
		repositoryService.createProcessConsignDefinition().consignor("u01")
				.addConsignee(new Participator("u02", "小雨", IdentityEnum.USER, null))
				.addConsignee(new Participator("u03", "小王", IdentityEnum.USER, null)).consignDefType(ConsignDefType.ALL)
				.startTime("2018-06-11").endTime("2019-9-22").save();
		
		
		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		//领取任务
		Task aTask=taskService.createTaskQuery().taskName("A").singleResult();
		Task bTask=taskService.createTaskQuery().taskName("B").singleResult();
		taskService.claimCascade(aTask.getId(), null, "u06");
		String delegateString=TestUtil.getConsignInstId(bTask.getId(), "u02", taskService);
		taskService.claimCascade(bTask.getId(), null, "u02");
		
		NotificationUtil.cleanNotificationLog();
		taskService.unclaimCascade(aTask.getId(), null);
		taskService.unclaimCascade(bTask.getId(), delegateString);
		
		List<NotificationEvent> notificationList=NotificationUtil.getNotifications();
		
		assertEquals(1, notificationList.size());
		NotificationContext notificationContext=(NotificationContext)notificationList.get(0).getNotificationEventContext();
		assertEquals(NotificationType.CLAIMCANCEL, notificationContext.getNotificationType());
		TestUtil.checkReceiver(notificationContext.getReceivers(), new String[]{"u01"});
		
	}
}
