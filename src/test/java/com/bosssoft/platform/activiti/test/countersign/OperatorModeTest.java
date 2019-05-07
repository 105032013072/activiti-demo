package com.bosssoft.platform.activiti.test.countersign;

import com.bosssoft.platform.activiti.test.MainTest;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;
/**
 * 按操作员个数会签
 * @author huangxw
 *
 */
public class OperatorModeTest extends MainTest{
   
	/**
	 * 完成百分比为0的测试
	 * @throws Exception
	 */
	@Test
	public void zeroTest() throws Exception{
		 Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/countersign/process_operatormode_zero.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .businessKey("0123")
	                .start();
		
       //第一个会签节点  （并行）
		List<Task> taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals("会签实例个数错误", 3,taskList.size());
		String[] assigneeArray=getTaskAssignee(taskList);
		String[] expectArray=new String[]{"u01","u03","u06"};
		Arrays.sort(expectArray);
		assertArrayEquals(assigneeArray, expectArray);
		
		//办理会签任务
		Map<String,Object> vars=new HashedMap();
		vars.put("act_countersignature_result", false);
		Task task=taskList.get(0);
		taskService.completCascade(task.getId(), null,vars);
		
		//完成的百分比为0，所以 该会签任务结束
		for(int i=0;i<taskList.size();i++){
			Task t=taskList.get(i);
			HistoricTaskInstance  hisTask=	histroyService.createHistoricTaskInstanceQuery().taskId(t.getId()).singleResult();
			if(i==0){
				assertEquals("completed", hisTask.getDeleteReason());
			}else{
				assertEquals("deleted", hisTask.getDeleteReason());
			}
		}
		
		//进入第二个节点（串行）
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(1, taskList.size());
		task=taskList.get(0);
		expectArray=new String[]{"u01","u06"};
		List<String> expectList=new ArrayList<String>(Arrays.asList(expectArray));
		assertEquals("B", task.getTaskDefinitionKey());
		assertThat(expectList, hasItem(task.getAssignee()));
		
		//完成任务
		taskService.completCascade(task.getId(), null,vars);
		
		//完成的百分比为0,所以流程结束
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(0, taskList.size());
		
		List<HistoricTaskInstance> bhisTaskList=histroyService.createHistoricTaskInstanceQuery().processInstanceId(inst.getId()).taskDefinitionKey("B").list();
		assertEquals(1, bhisTaskList.size());

	}
	

	/**
	 * 完成百分比为100的测试
	 * @throws Exception 
	 */
	@Test
	public void allTest() throws Exception{
		Model model = super.createAndDeployModel(
				"com/bosssoft/platform/activiti/test/countersign/process_operatormode_all.bpmnx");

		ProcessInstance inst = runtimeService.createProcessInstanceBuilder()
				.processDefinitionId(model.getProcessDefinitionId()).processStarter("u03").start();

		// 第一个会签节点 （并行）
		List<Task> taskList = taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals("会签实例个数错误", 3, taskList.size());
		String[] assigneeArray = getTaskAssignee(taskList);
		String[] expectArray = new String[] { "u01", "u03", "u06" };
		Arrays.sort(expectArray);
		assertArrayEquals(assigneeArray, expectArray);
		
	    //需要完成所有所有任务实例
		Map<String,Object> vars=new HashedMap();
		vars.put("act_countersignature_result", false);
		for (Task task : taskList) {
			taskService.completCascade(task.getId(), null,vars);
		}
		
		// 进入第二个节点（串行）
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(1, taskList.size());
		Task task=taskList.get(0);
		expectArray=new String[]{"u01","u06"};
		List<String> expectList=new ArrayList<String>(Arrays.asList(expectArray));
		assertEquals("B", task.getTaskDefinitionKey());
		assertThat(expectList, hasItem(task.getAssignee()));
		
		taskService.completCascade(task.getId(), null,vars);
		
		//第二个实例
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(1, taskList.size());
		task=taskList.get(0);
		assertEquals("B", task.getTaskDefinitionKey());
		assertThat(expectList, hasItem(task.getAssignee()));
		taskService.completCascade(task.getId(), null,vars);
		
		//流程结束
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(0, taskList.size());
	}
	
	/**
	 * 50%的通过率
	 * @throws Exception
	 */
	@Test
	public void rateTest() throws Exception {
		Model model = super.createAndDeployModel(
				"com/bosssoft/platform/activiti/test/countersign/process_operatormode_50.bpmnx");

		ProcessInstance inst = runtimeService.createProcessInstanceBuilder()
				.processDefinitionId(model.getProcessDefinitionId()).processStarter("u03").start();
		
		List<Task> taskList = taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		
		Map<String,Object> vars=new HashedMap();
		vars.put("act_countersignature_result", false);
		taskService.completCascade(taskList.get(0).getId(), null,vars);//不通过
		vars.put("act_countersignature_result", true);
		taskService.completCascade(taskList.get(1).getId(), null,vars);//不通过
		
		//通过率没有达到0.5，会签未完成
		 taskList = taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(2, taskList.size());
		vars.put("act_countersignature_result", true);
		taskService.completCascade(taskList.get(0).getId(), null,vars);//通过
		
		//第一个节点的会签完成，进入第二个节点（串行）
		taskList = taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(1, taskList.size());
		Task task = taskList.get(0);
		assertEquals("B", task.getTaskDefinitionKey());
		
		vars.put("act_countersignature_result", false);
		taskService.completCascade(task.getId(), null,vars);//不通过
		
		task=taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("B", task.getTaskDefinitionKey());
		vars.put("act_countersignature_result", true);
		taskService.completCascade(task.getId(), null,vars);//通过
		
		task=taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("B", task.getTaskDefinitionKey());
		vars.put("act_countersignature_result", true);
		taskService.completCascade(task.getId(), null,vars);//通过
		
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(0, taskList.size());
	}
	
	
	
	
	private String[] getTaskAssignee(List<Task> taskList){
		List<String> list=new ArrayList<String>();
		for (Task task : taskList) {
			list.add(task.getAssignee());
		}
		String[] result=list.toArray(new String[list.size()]);
		Arrays.sort(result);
		return result;
	}
}
