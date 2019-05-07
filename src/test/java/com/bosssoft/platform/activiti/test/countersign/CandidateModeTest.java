package com.bosssoft.platform.activiti.test.countersign;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

import com.bosssoft.platform.activiti.test.MainTest;
import com.bosssoft.platform.activiti.test.TestUtil;

import static org.hamcrest.CoreMatchers.*;

/**
 * 参与者个数会签
 * @author huangxw
 *
 */
public class CandidateModeTest extends MainTest{
    
	@Test
	public void zeroTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/countersign/process_candidatemode_zero.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		
       //第一个会签节点  （并行）
		List<Task> taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals("会签实例个数错误", 3,taskList.size());
		String[] expectArray=new String[]{"u06","r03","r01"};
		String[] targetArray=new String[3];
		int i=0;
		for (Task task : taskList) {
			assertNull(task.getAssignee());
			String[] candidateArray=TestUtil.getCandidates(task.getId(), taskService);
			assertEquals(1, candidateArray.length);
			targetArray[i++]=candidateArray[0];
		}
		Arrays.sort(expectArray);
		Arrays.sort(targetArray);
		assertArrayEquals(expectArray, targetArray);
		
		//领取任务 并且完成第一个任务
		Task task0=taskList.get(0);
		Task task1=taskList.get(1);
		taskService.claimCascade(task0.getId(),null, "u06");
		taskService.claimCascade(task1.getId(), null, "u03");
		Map<String,Object> vars=new HashedMap();
		vars.put("act_countersignature_result", true);
		taskService.completCascade(task0.getId(), null,vars);
		
		//完成的百分比为0，所以 该会签任务结束
		for( i=0;i<taskList.size();i++){
			Task t=taskList.get(i);
			HistoricTaskInstance  hisTask=	histroyService.createHistoricTaskInstanceQuery().taskId(t.getId()).singleResult();
			if(t.getId().equals(task0.getId())){
				assertEquals("completed", hisTask.getDeleteReason());
				assertEquals("u06", hisTask.getAssignee());
			}else if(t.getId().equals(task1.getId())){
				assertEquals("deleted", hisTask.getDeleteReason());
				assertEquals("u03", hisTask.getAssignee());
			}else{
				assertEquals("deleted", hisTask.getDeleteReason());
				assertNull(hisTask.getAssignee());
			}
		}
		
		//进入第二个节点（串行）
		taskList = taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(1, taskList.size());
		Task task = taskList.get(0);
		assertNull(task.getAssignee());
		assertEquals("B", task.getTaskDefinitionKey());
		String[] candidateArray=TestUtil.getCandidates(task.getId(), taskService);
		assertEquals(1, candidateArray.length);
		assertEquals("p02", candidateArray[0]);
		taskService.claimCascade(task.getId(),null, "u04");
		taskService.completCascade(task.getId(), null,vars);
		
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(0, taskList.size());
	}
	
	
	@Test
	public void allTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/countersign/process_candidatemode_all.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		
       //第一个会签节点  （并行）
		List<Task> taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals("会签实例个数错误", 3,taskList.size());
		String[] expectArray=new String[]{"u06","r03","r01"};
		String[] targetArray=new String[3];
		int i=0;
		for (Task task : taskList) {
			assertNull(task.getAssignee());
			String[] candidateArray=TestUtil.getCandidates(task.getId(), taskService);
			assertEquals(1, candidateArray.length);
			targetArray[i++]=candidateArray[0];
		}
		Arrays.sort(expectArray);
		Arrays.sort(targetArray);
		assertArrayEquals(expectArray, targetArray);
		
		
		//领取任务 并且完成任务
		Task task0 = taskList.get(0);
		Task task1 = taskList.get(1);
		Task task2=taskList.get(2);
		taskService.claimCascade(task0.getId(), null, "u06");
		taskService.claimCascade(task1.getId(), null, "u03");
		taskService.claimCascade(task2.getId(), null, "u01");
		Map<String, Object> vars = new HashedMap();
		vars.put("act_countersignature_result", true);
		taskService.completCascade(task0.getId(), null, vars);
		taskService.completCascade(task1.getId(), null, vars);
		taskService.completCascade(task2.getId(), null, vars);
		
		//进入第二个节点（串行）
		taskList = taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(1, taskList.size());
		Task task = taskList.get(0);
		assertNull(task.getAssignee());
		assertEquals("B", task.getTaskDefinitionKey());
		String[] candidateArray = TestUtil.getCandidates(task.getId(), taskService);
		assertEquals(1, candidateArray.length);
		assertEquals("p02", candidateArray[0]);
		taskService.claimCascade(task.getId(), null, "u04");
		taskService.completCascade(task.getId(), null, vars);
		
		taskList = taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(1, taskList.size());
	     task = taskList.get(0);
	     assertNull(task.getAssignee());
		assertEquals("B", task.getTaskDefinitionKey());
		 candidateArray = TestUtil.getCandidates(task.getId(), taskService);
		assertEquals(1, candidateArray.length);
		assertEquals("p04", candidateArray[0]);
		taskService.claimCascade(task.getId(), null, "u05");
		taskService.completCascade(task.getId(), null, vars);
		
		//流程完成
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(0, taskList.size());
		
		List<HistoricTaskInstance> bhisTaskList=histroyService.createHistoricTaskInstanceQuery().processInstanceId(inst.getId()).taskDefinitionKey("B").list();
		assertEquals(2, bhisTaskList.size());
		
	}

	
}
