package com.bosssoft.platform.activiti.test.countersign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;
import static org.junit.Assert.*;
import com.bosssoft.platform.activiti.test.MainTest;
import com.bosssoft.platform.activiti.test.TestUtil;

/**
 * 会签的参与者来源测试
 * @author huangxw
 *
 */
public class CountersignSourceTest extends MainTest{
    
	@Test
	public void test() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/countersign/source/process_countersignsource.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		
		List<Task> taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		Map<String,Object> vars=new HashedMap();
		vars.put("act_countersignature_result", true);
		taskService.completCascade(taskList.get(0).getId(), null,vars);//通过
		
		//第二个节点  (自定义接口)
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
	    assertEquals(2, taskList.size());
	    for (Task task : taskList) {
	    	assertEquals("B", task.getTaskDefinitionKey());
		}
	    String[] assigneeArray=getTaskAssignee(taskList);
	    String[] expectArray=new String[]{"u03","u04"};
		Arrays.sort(expectArray);
		assertArrayEquals(assigneeArray, expectArray);
		taskService.completCascade(taskList.get(0).getId(), null,vars);//通过
		
	    //第三个节点 （与第一个节点的参与者相同） 按参与者个数会签   
	    taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
	    expectArray=new String[]{"u06","r02","r01"};
		String[] targetArray=new String[3];
		int i=0;
		for (Task task : taskList) {
			assertEquals("C", task.getTaskDefinitionKey());
			assertNull(task.getAssignee());
			String[] candidateArray=TestUtil.getCandidates(task.getId(), taskService);
			assertEquals(1, candidateArray.length);
			targetArray[i++]=candidateArray[0];
		}
		Arrays.sort(expectArray);
		Arrays.sort(targetArray);
		assertArrayEquals(expectArray, targetArray);
		
		//办理第三个节点任务
		taskService.claimCascade(taskList.get(0).getId(),null, "u06");
		taskService.completCascade(taskList.get(0).getId(), null,vars);
		
		//第四个节点  (与第三个节点相同，但是排除办理人)
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals("会签实例个数错误", 4,taskList.size());
		 assigneeArray=getTaskAssignee(taskList);
		 expectArray=new String[]{"u02","u05","u01","u04"};
		Arrays.sort(expectArray);
		assertArrayEquals(assigneeArray, expectArray);
	}
	
	/**
	 * 与指定活动候选人相同   指定的活动节点：并行   操作员模式会签
	 * @throws Exception 
	 */
	@Test
	public void sameActParallelOp() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/countersign/source/process_sameActParallelOp.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		
		List<Task> taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		Map<String,Object> vars=new HashedMap();
		vars.put("act_countersignature_result", true);
		taskService.completCascade(taskList.get(0).getId(), null,vars);//通过
		
		//第二个节点
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		String[] expectArray=new String[]{"u06","r02","r01"};
		verifyTaskCandidate(expectArray, taskList, "B");
		
	}
	
	//-----------与指定活动候选人相同
	/**
	 * 与指定活动候选人相同   指定的活动节点  并行  参与者模式会签
	 * @throws Exception 
	 */
	@Test
	public void sameActParallelCandidate() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/countersign/source/process_sameActParallelCandidate.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		
		List<Task> taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		Map<String,Object> vars=new HashedMap();
		vars.put("act_countersignature_result", true);
		taskService.claimCascade(taskList.get(0).getId(),null, "u06");
		taskService.completCascade(taskList.get(0).getId(), null,vars);//通过
		
		//第二个节点
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		String[] expectArray=new String[]{"u06","r02","r01"};
		verifyTaskCandidate(expectArray, taskList, "B");
		
	}
	
	
	/**
	 * 与指定活动候选人相同   指定的活动节点：串行   操作员模式会签
	 * @throws Exception 
	 */
	@Test
	public void sameActSequentialOp() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/countersign/source/process_sameActSequentialOp.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		
		List<Task> taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(1, taskList.size());
		Map<String,Object> vars=new HashedMap();
		vars.put("act_countersignature_result", true);
		taskService.completCascade(taskList.get(0).getId(), null,vars);//通过
		
		//第二个节点
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		String[] expectArray=new String[]{"r01","r02"};
		verifyTaskCandidate(expectArray, taskList, "B");
		
	}
	
	
	/**
	 * 与指定活动候选人相同   指定的活动节点  串行  参与者模式会签
	 * @throws Exception 
	 */
	@Test
	public void sameActSequentialCandidate() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/countersign/source/process_sameActSequentialCandidate.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		
		List<Task> taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(1, taskList.size());
		Map<String,Object> vars=new HashedMap();
		vars.put("act_countersignature_result", true);
		taskService.claimCascade(taskList.get(0).getId(),null, "u01");
		taskService.completCascade(taskList.get(0).getId(), null,vars);//通过
		
		//第二个节点
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		String[] expectArray=new String[]{"r02","r01"};
		verifyTaskCandidate(expectArray, taskList, "B");
		
	}
	
	//---------与指定活动候选人相同但排除办理人
	/**
	 * 与指定活动候选人相同但排除办理人   指定的活动节点  并行  操作员模式会签
	 * @throws Exception 
	 */
	@Test
	public void actMutexParallelOp() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/countersign/source/process_actMutexParallelOp.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		
		List<Task> taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(5, taskList.size());
		Map<String,Object> vars=new HashedMap();
		vars.put("act_countersignature_result", true);
		for (Task task : taskList) {
			if("u01".equals(task.getAssignee())){
				taskService.completCascade(task.getId(), null,vars);//通过
				break;
			}
		}
		
		//第二个节点
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		String[] expectArray=new String[]{"u02","u04","u05","u06"};
		verifyTaskCandidate(expectArray, taskList, "B");
	}
	
	/**
	 * 与指定活动候选人相同但排除办理人   指定的活动节点  并行  参与者模式会签
	 * @throws Exception 
	 */
	@Test
	public void actMutexParallelCandidate() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/countersign/source/process_actMutexParallelCandidate.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		
		List<Task> taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(2, taskList.size());
		Map<String,Object> vars=new HashedMap();
		vars.put("act_countersignature_result", true);
		taskService.claimCascade(taskList.get(0).getId(), null, "u06");
		taskService.completCascade(taskList.get(0).getId(), null,vars);
		
		//第二个节点
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		String[] expectArray=new String[]{"u02","u04","u05","u01"};
		verifyTaskCandidate(expectArray, taskList, "B");
	}
	
	/**
	 * 与指定活动候选人相同但排除办理人   指定的活动节点  串行    操作员模式会签
	 * @throws Exception 
	 */
	@Test
	public void actMutexSequentialOp() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/countersign/source/process_actMutexSequentialOp.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		
		List<Task> taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(1, taskList.size());
		Map<String,Object> vars=new HashedMap();
		vars.put("act_countersignature_result", true);
		taskService.completCascade(taskList.get(0).getId(), null,vars);
		String assignee=taskList.get(0).getAssignee();
		
		//第二个节点
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		List<String> assigneeList = new ArrayList<String>(Arrays.asList("u02","u04","u05","u06","u01"));
		assigneeList.remove(assignee);
		String[] expectArray=new String[4];
		assigneeList.toArray(expectArray);
		verifyTaskCandidate(expectArray, taskList, "B");
	}
	
	
	/**
	 * 与指定活动候选人相同但排除办理人   指定的活动节点  串行    参与者个数模式会签
	 * @throws Exception 
	 */
	@Test
	public void actMutexSequentialCandidate() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/countersign/source/process_actMutexSequentialCandidate.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		
		List<Task> taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(1, taskList.size());
		Map<String,Object> vars=new HashedMap();
		vars.put("act_countersignature_result", true);
		Task task=taskList.get(0);
		taskService.claimCascade(task.getId(), null, "u01");
		taskService.completCascade(task.getId(), null,vars);
		
		
		//第二个节点
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		String[] expectArray=new String[]{"u02","u04","u05","u06"};
		verifyTaskCandidate(expectArray, taskList, "B");
	}
	
	/**
	 * 默认中排除指定活动办理人
	 * @throws Exception
	 */
	@Test
	public void defaultMutex()throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/countersign/source/process_defaultMutex.bpmnx");

		ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		
		List<Task> taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		assertEquals(1, taskList.size());
		Map<String,Object> vars=new HashedMap();
		vars.put("act_countersignature_result", true);
		Task task=taskList.get(0);
		taskService.claimCascade(task.getId(), null, "u01");
		taskService.completCascade(task.getId(), null,vars);
		
		taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
		String[] expectArray=new String[]{"u06"};
		verifyTaskCandidate(expectArray, taskList, "B");
		
	}
	
	
	private void  verifyTaskCandidate(String[] expectArray,List<Task> taskList,String taskDefKey){
		assertEquals(expectArray.length, taskList.size());
		
		String[] targetArray=new String[taskList.size()];
		int i=0;
		for (Task task : taskList) {
			assertEquals(taskDefKey, task.getTaskDefinitionKey());
			assertNull(task.getAssignee());
			String[] candidateArray=TestUtil.getCandidates(task.getId(), taskService);
			assertEquals(1, candidateArray.length);
			targetArray[i++]=candidateArray[0];
		}
		Arrays.sort(expectArray);
		Arrays.sort(targetArray);
		assertArrayEquals(expectArray, targetArray);
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
