package com.bosssoft.platform.activiti.test.carboncopy;
import static org.junit.Assert.*;

import java.util.List;

import org.activiti.engine.impl.persistence.entity.NotificationEntity;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.notification.NotificationType;
import org.activiti.engine.task.Task;
import org.junit.Test;

import com.bosssoft.platform.activiti.test.MainTest;

/**
 * 知会通知操作测试
 * @author huangxw
 *
 */
public class CarbonCopyOpTest extends MainTest {

	@Test
	public void test() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/carboncopy/process_op.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		
		Task task=taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("A", task.getTaskDefinitionKey());
		taskService.completCascade(task.getId(), null);
		
		task=taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("B", task.getTaskDefinitionKey());
		taskService.completCascade(task.getId(), null);
		
		String[] expectReceiver=new String[]{"u01","u06"};
		
		
		List<NotificationEntity> list=runtimeService
			      .createNotificationListQuery()
			      .receiverId("u01")
			      .notificationType(NotificationType.CARBONCOPY)
			      .unread()
			      .list();
		assertEquals(2, list.size());
		//已阅
		runtimeService.handleNotification(list.get(0).getId(), "u01", true);
		
		
		//查询已阅
		for (String receiverId : expectReceiver) {
			list = runtimeService.createNotificationListQuery().receiverId(receiverId)
					.notificationType(NotificationType.CARBONCOPY).alreadyRead().list();
			if (receiverId.equals("u01")) {
				assertEquals(1, list.size());
			} else {
				assertEquals(0, list.size());
			}
		}

		// 查询未阅
		for (String receiverId : expectReceiver) {
			list = runtimeService.createNotificationListQuery().receiverId(receiverId)
					.notificationType(NotificationType.CARBONCOPY).unread().list();
			if (receiverId.equals("u01")) {
				assertEquals(1, list.size());
			} else {
				assertEquals(2, list.size());
			}
		}
	}
}
