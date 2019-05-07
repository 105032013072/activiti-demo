package com.bosssoft.platform.activiti.test.participator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.identity.IdentityEnum;
import org.activiti.engine.spi.identity.Participator;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.bosssoft.platform.activiti.test.MainTest;
import com.bosssoft.platform.activiti.test.TestUtil;

/**
 * 任务候选人不同来源的测试
 * @author huangxw
 *
 */
public class CandidateTest extends MainTest{

	/**
	 * 默认的候选人来源
	 * @throws Exception 
	 */
	@Test
	public void defaultCandidateTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/participator/process_candidate_default.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		//第一个节点
		Task task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "填写申请", task.getName());
	    String[] taskCandidates=TestUtil. getCandidates(task.getId(),taskService);
	    String[] targetArray=new String[]{"r02","r01"};
	    Arrays.sort(targetArray);
	    assertNull(task.getAssignee());
	    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
	    
		taskService.claimCascade(task.getId(), null, "u01");
		taskService.completCascade(task.getId(), null);
		
		//第二个节点
		task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "上级审核", task.getName());
	    taskCandidates=TestUtil. getCandidates(task.getId(),taskService);
	    targetArray=new String[]{"r02","r01"};
	    Arrays.sort(targetArray);
	   
	    assertNull(task.getAssignee());
	    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
	    
	    taskService.claimCascade(task.getId(), null, "u06");
		taskService.completCascade(task.getId(), null);
		
		//第三个节点
		task = taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "终审", task.getName());
		taskCandidates =TestUtil.  getCandidates(task.getId(),taskService);
		targetArray = new String[] { "r03"};
		Arrays.sort(targetArray);

		assertNull(task.getAssignee());
		assertArrayEquals("候选人不匹配", targetArray, taskCandidates);		
	}
	
	
	/**
	 * 与指定任务候选人相同
	 * @throws Exception 
	 */
	@Test
	public void sameActCandidateTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/participator/process_candidate_sameAct.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		//第一个节点
		Task task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "填写申请", task.getName());
	    String[] taskCandidates=TestUtil. getCandidates(task.getId(),taskService);
	    String[] targetArray=new String[]{"r01"};
	    Arrays.sort(targetArray);
	    assertNull(task.getAssignee());
	    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
	    
		taskService.claimCascade(task.getId(), null, "u06");
		taskService.completCascade(task.getId(), null);
		
		//第二个节点  与第一节点候选人相同
		task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "上级审核", task.getName());
	    taskCandidates=TestUtil. getCandidates(task.getId(),taskService);
	    targetArray=new String[]{"r01"};
	    Arrays.sort(targetArray);
	   
	    assertNull(task.getAssignee());
	    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
	    
	    taskService.claimCascade(task.getId(), null, "u06");
		taskService.completCascade(task.getId(), null);
		
		//第三个节点 与第二节点候选人相同
		task = taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "终审", task.getName());
		taskCandidates =TestUtil.  getCandidates(task.getId(),taskService);
		targetArray = new String[] { "r01"};
		Arrays.sort(targetArray);

		assertNull(task.getAssignee());
		assertArrayEquals("候选人不匹配", targetArray, taskCandidates);		
	}
	
	
	/**
	 * 与指定活动候选人相同但排除办理人
	 * @throws Exception 
	 */
	@Test
	public void mutexActCandidateTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/participator/process_candidate_mutexAct.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		//第一个节点
		Task task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "填写申请", task.getName());
	    String[] taskCandidates=TestUtil.getCandidates(task.getId(),taskService);
	    String[] targetArray=new String[]{"r01","r02"};
	    Arrays.sort(targetArray);
	    assertNull(task.getAssignee());
	    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
	    
		taskService.claimCascade(task.getId(), null, "u06");
		taskService.completCascade(task.getId(), null);
		
		//第二个节点  排除第一个节点的办理人
		task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "上级审核", task.getName());
	    taskCandidates=TestUtil.getCandidates(task.getId(),taskService);
	    targetArray=new String[]{"u01","u04","u02","u05"};
	    Arrays.sort(targetArray);
	   
	    assertNull(task.getAssignee());
	    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
	    
	    taskService.claimCascade(task.getId(), null, "u05");
		taskService.completCascade(task.getId(), null);
		
		//第三个节点  排除第二个节点的办理人
		task = taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "终审", task.getName());
		taskCandidates =TestUtil. getCandidates(task.getId(),taskService);
		targetArray=new String[]{"u01","u04","u02"};
		Arrays.sort(targetArray);

		assertNull(task.getAssignee());
		assertArrayEquals("候选人不匹配", targetArray, taskCandidates);		
	}
	
	/**
	 * 所选择的参与者中排除指定活动的办理人
	 * @throws Exception 
	 */
	@Test
	public void mutexDafaultCandidateTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/participator/process_candidate_mutexDefault.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		//第一个节点
		Task task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "填写申请", task.getName());
	    String[] taskCandidates=TestUtil.getCandidates(task.getId(),taskService);
	    String[] targetArray=new String[]{"r01","r02"};
	    Arrays.sort(targetArray);
	    assertNull(task.getAssignee());
	    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
	    
		taskService.claimCascade(task.getId(), null, "u06");
		taskService.completCascade(task.getId(), null);
		
		//第二个节点  默认中排除第一个节点的办理人
		task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "上级审核", task.getName());
	    taskCandidates=TestUtil.getCandidates(task.getId(),taskService);
	    targetArray=new String[]{"u01"};
	    Arrays.sort(targetArray);
	   
	    assertNull(task.getAssignee());
	    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
	    
	    taskService.claimCascade(task.getId(), null, "u04");
		taskService.completCascade(task.getId(), null);
		
		//第三个节点  默认中排除第二个节点的办理人
		task = taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "终审", task.getName());
		taskCandidates =TestUtil. getCandidates(task.getId(),taskService);
		targetArray=new String[]{"u02","u05"};
		Arrays.sort(targetArray);

		assertNull(task.getAssignee());
		assertArrayEquals("候选人不匹配", targetArray, taskCandidates);		
	}
	
	/**
	 * 流程变量   自定义接口
	 * @throws Exception 
	 */
	@Test
	public void varAndCustomCandidateTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/participator/process_candidate_varAndCustom.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		//第一个节点 自定义接口
		Task task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "填写申请", task.getName());
	    String[] taskCandidates=TestUtil.getCandidates(task.getId(),taskService);
	    String[] targetArray=new String[]{"u03","u04"};
	    Arrays.sort(targetArray);
	    assertNull(task.getAssignee());
	    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
	    
	    Map<String,Object> var=new HashedMap();
	    List<Participator> list=new ArrayList<Participator>();
	    list.add(new Participator("u05", "李四", IdentityEnum.USER, null));
	    var.put("two", list);
		taskService.claimCascade(task.getId(), null, "u03");
		taskService.completCascade(task.getId(), null,var);
		
		//第二个节点  流程变量
		task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "上级审核", task.getName());
	    taskCandidates=TestUtil.getCandidates(task.getId(),taskService);
	    targetArray=new String[]{"u05"};
	    Arrays.sort(targetArray);
	   
	    assertNull(task.getAssignee());
	    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
	    
	    list.add(new Participator("u888", "888_name", IdentityEnum.USER, null));
	    var=new HashedMap();
	    var.put("three", list);
	    taskService.claimCascade(task.getId(), null, "u04");
		taskService.completCascade(task.getId(), null,var);
		
		//第三个节点  流程变量
		task = taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "终审", task.getName());
		taskCandidates =TestUtil. getCandidates(task.getId(),taskService);
		targetArray=new String[]{"u888","u05"};
		Arrays.sort(targetArray);

		assertNull(task.getAssignee());
		assertArrayEquals("候选人不匹配", targetArray, taskCandidates);		
	}
	
	
	
	
	
	
	
	
	
}
