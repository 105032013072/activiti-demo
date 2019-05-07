package com.bosssoft.platform.activiti.test.notification;

import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.identity.Participator;
import org.activiti.engine.spi.notification.NotificationContext;
import org.activiti.engine.spi.notification.NotificationType;
import org.activiti.engine.spi.notification.event.NotificationCategory;
import org.activiti.engine.spi.notification.event.NotificationEvent;
import org.activiti.engine.spi.notification.impl.EmailNotificationListener;
import org.activiti.engine.task.Task;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.bosssoft.platform.activiti.NotificationUtil;
import com.bosssoft.platform.activiti.test.MainTest;
import com.bosssoft.platform.activiti.test.TestUtil;

/**
 * 任务到达的通知测试
 * @author huangxw
 *
 */
public class TaskArriveTest extends MainTest{
   
	@Test
	public void testNewTask() throws Exception{
		
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/notification/process_newtask.bpmnx");

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
			
			assertEquals(NotificationType.NEWTASK, notificationContext.getNotificationType());
			
			if("A".equals(notificationContext.getTaskName())){
				 TestUtil.checkReceiver(notificationContext.getReceivers(), new String[]{"u01"});
			}else{//B
				 TestUtil.checkReceiver(notificationContext.getReceivers(), new String[]{"u02","u04","u05"});
			}
		}
		
		 Arrays.sort(targetActArray);
		 assertArrayEquals(expectActArray, targetActArray);
	}
	
	
	@Test
	public void countersignNewTask() throws Exception{
		
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/notification/process_countersign_newTask.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		List<NotificationEvent> notificationList=NotificationUtil.getNotifications();

        assertEquals(3, notificationList.size());
        int i=0;
		List<Participator> areceiver=new ArrayList<Participator>();
		List<Participator> breceiver=new ArrayList<Participator>();
        for (NotificationEvent notificationEvent : notificationList) {
			assertEquals(NotificationCategory.TASKNOTIFICATION, notificationEvent.getNotificationCategory());
			
			NotificationContext notificationContext=(NotificationContext)notificationEvent.getNotificationEventContext();
			
			assertEquals(NotificationType.NEWTASK, notificationContext.getNotificationType());
			
			if("A".equals(notificationContext.getTaskName())){
				areceiver.addAll(notificationContext.getReceivers());
			}else{//B
			
				breceiver.addAll(notificationContext.getReceivers());
			}
		}
        
        TestUtil. checkReceiver(areceiver, new String[]{"u01","u06"});
        TestUtil. checkReceiver(breceiver, new String[]{"u02","u04","u05"});
	}
}
