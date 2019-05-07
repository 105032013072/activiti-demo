package com.bosssoft.platform.activiti.test.carboncopy;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.persistence.entity.NotificationEntity;
import org.activiti.engine.impl.persistence.entity.NotificationReceiverEntity;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.identity.IdentityEnum;
import org.activiti.engine.spi.identity.Participator;
import org.activiti.engine.spi.notification.NotificationType;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

import com.bosssoft.platform.activiti.test.MainTest;
import com.bosssoft.platform.common.lang.data.Page;

/**
 * 知会通知创建测试
 * @author huangxw
 *
 */
public class CarbonCopyCreateTest extends MainTest{
  
	@Test
	public void defineTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/carboncopy/process_define.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		
		Task task=taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("A", task.getTaskDefinitionKey());
		taskService.completCascade(task.getId(), null);
		String[] expectReceiver=new String[]{"u09","u02","u04","u05"};
		verifyCarbonCopy(task.getId(), inst.getId(), expectReceiver);
		
		//第二个节点   (配置+手动)
		task=taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("B", task.getTaskDefinitionKey());
		taskService.completeInformCascade(task.getId(), null, "finish", new Participator("u01", "小明", IdentityEnum.USER, null));
		expectReceiver=new String[]{"u01","u08"};
		verifyCarbonCopy(task.getId(), inst.getId(), expectReceiver);
	}
	
	
	@Test
	public void manualTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/carboncopy/process_manual.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		
		Task task=taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("A", task.getTaskDefinitionKey());
		taskService.completCascade(task.getId(), null);
		String[] expectReceiver=new String[]{};
		verifyCarbonCopy(task.getId(), inst.getId(), expectReceiver);
		
		//第二个节点   (配置+手动)
		task=taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("B", task.getTaskDefinitionKey());
		taskService.completeInformCascade(task.getId(), null, "finish", new Participator("u01", "小明", IdentityEnum.USER, null));
		expectReceiver=new String[]{"u01"};
		verifyCarbonCopy(task.getId(), inst.getId(), expectReceiver);
	}
	
	
	
	private void verifyCarbonCopy(String taskId,String processInstanceId,String[] expectReceiver){
		Page<Participator> pageResult=new Page<Participator>(1, 20);
		
		List<Participator> allusers=omservice.getUsers(pageResult, null);
		for (Participator participator : allusers) {
			List<NotificationEntity> list=runtimeService
				      .createNotificationListQuery()
				      .taskId(taskId)
				      .receiverId(participator.getParticipatorId())
				      .processInstanceId(processInstanceId)
				      .notificationType(NotificationType.CARBONCOPY)
				      .list();
			if(ArrayUtils.contains(expectReceiver, participator.getParticipatorId())){
				assertEquals(1, list.size());
			}else{
				assertEquals(0, list.size());
			}
			
		}
	}
}
