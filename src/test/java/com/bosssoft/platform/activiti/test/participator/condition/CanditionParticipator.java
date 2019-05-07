package com.bosssoft.platform.activiti.test.participator.condition;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.identity.IdentityEnum;
import org.activiti.engine.spi.identity.Participator;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

import com.bosssoft.platform.activiti.test.MainTest;
import com.bosssoft.platform.activiti.test.TestUtil;

public class CanditionParticipator extends MainTest{

	@Test
	public void candidateTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/participator/candition/process_participator_condition.bpmnx");
		
		for(int total=0;total<=2;total++){
			Map<String,Object> var=new HashedMap();
		    List<Participator> list=new ArrayList<Participator>();
		    list.add(new Participator("u05", "李四", IdentityEnum.USER, null));
		    var.put("two", list);
		    var.put("total", total);
			
			
			ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .setVariables(var)
	                .processStarter("u03")
	                .start();
			if(total==2){
				System.out.println("totaol==2");
			}
			
			
			//第一个节点
			Task task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
			String[] taskCandidates=TestUtil.getCandidates(task.getId(),taskService);
		    String[] targetArray=new String[]{"r01","r02"};
		    Arrays.sort(targetArray);
		    Arrays.sort(taskCandidates);
		    assertNull(task.getAssignee());
		    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
			
			taskService.claim(task.getId(), "u06");
			taskService.complete(task.getId());
			
			//第二个节点
			if(total==0){
				task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
				assertEquals("任务名称不匹配", "上级审核", task.getName());
				
				 taskCandidates=TestUtil.getCandidates(task.getId(),taskService);
			     targetArray=new String[]{"u03","u04"};
			    Arrays.sort(targetArray);
			    Arrays.sort(taskCandidates);
			    assertNull(task.getAssignee());
			    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
				
			}else if(total==1){
				task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
				assertEquals("任务名称不匹配", "上级审核", task.getName());
				
				 taskCandidates=TestUtil.getCandidates(task.getId(),taskService);
			     targetArray=new String[]{"u01"};
			    Arrays.sort(targetArray);
			    Arrays.sort(taskCandidates);
			    assertNull(task.getAssignee());
			    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
				
			}else if(total==2){
				
				task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
				assertEquals("任务名称不匹配", "上级审核", task.getName());
				
				 taskCandidates=TestUtil.getCandidates(task.getId(),taskService);
			     targetArray=new String[]{"u02"};
			    Arrays.sort(targetArray);
			    Arrays.sort(taskCandidates);
			    assertNull(task.getAssignee());
			    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
				
			    
			}else{
				throw new Exception("error");
			}
		}
		
		
	}
	
	
	@Test
	public void candidateQLTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/participator/candition/process_participator_condition_QL.bpmnx");
		
		for(int total=0;total<=2;total++){
			Map<String,Object> var=new HashedMap();
		    List<Participator> list=new ArrayList<Participator>();
		    list.add(new Participator("u05", "李四", IdentityEnum.USER, null));
		    var.put("two", list);
		    var.put("total", total);
			
			
			ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .setVariables(var)
	                .processStarter("u03")
	                .start();
			if(total==2){
				System.out.println("totaol==2");
			}
			
			
			//第一个节点
			Task task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
			String[] taskCandidates=TestUtil.getCandidates(task.getId(),taskService);
		    String[] targetArray=new String[]{"r01","r02"};
		    Arrays.sort(targetArray);
		    Arrays.sort(taskCandidates);
		    assertNull(task.getAssignee());
		    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
			
			taskService.claim(task.getId(), "u06");
			taskService.complete(task.getId());
			
			//第二个节点
			if(total==0){
				task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
				assertEquals("任务名称不匹配", "上级审核", task.getName());
				
				 taskCandidates=TestUtil.getCandidates(task.getId(),taskService);
			     targetArray=new String[]{"u03","u04"};
			    Arrays.sort(targetArray);
			    Arrays.sort(taskCandidates);
			    assertNull(task.getAssignee());
			    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
				
			}else if(total==1){
				task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
				assertEquals("任务名称不匹配", "上级审核", task.getName());
				
				 taskCandidates=TestUtil.getCandidates(task.getId(),taskService);
			     targetArray=new String[]{"u01"};
			    Arrays.sort(targetArray);
			    Arrays.sort(taskCandidates);
			    assertNull(task.getAssignee());
			    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
				
			}else if(total==2){
				
				task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
				assertEquals("任务名称不匹配", "上级审核", task.getName());
				
				 taskCandidates=TestUtil.getCandidates(task.getId(),taskService);
			     targetArray=new String[]{"u02"};
			    Arrays.sort(targetArray);
			    Arrays.sort(taskCandidates);
			    assertNull(task.getAssignee());
			    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
				
			    
			}else{
				throw new Exception("error");
			}
		}
		
		
	}
	
	
	
	
	
	
	@Test
	public void assigneeTest() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/participator/candition/process_participator_condition_assignee.bpmnx");
		
		for(int total=0;total<=2;total++){
			Map<String,Object> var=new HashedMap();
		    List<Participator> list=new ArrayList<Participator>();
		    list.add(new Participator("u05", "李四", IdentityEnum.USER, null));
		    var.put("two", list);
		    var.put("total", total);
			
			
			ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .setVariables(var)
	                .processStarter("u03")
	                .start();
			if(total==2){
				System.out.println("totaol==2");
			}
			
			
			//第一个节点
			Task task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
			String[] taskCandidates=TestUtil.getCandidates(task.getId(),taskService);
		    String[] targetArray=new String[]{"r01","r02"};
		    Arrays.sort(targetArray);
		    Arrays.sort(taskCandidates);
		    assertNull(task.getAssignee());
		    assertArrayEquals("候选人不匹配", targetArray, taskCandidates);
			
			taskService.claim(task.getId(), "u06");
			taskService.complete(task.getId());
			
			//第二个节点
			if(total==0){
				task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
				assertEquals("任务名称不匹配", "上级审核", task.getName());
				assertEquals("错误的任务办理人", "u03", task.getAssignee());
				
			}else if(total==1){
				task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
				assertEquals("任务名称不匹配", "上级审核", task.getName());
				assertEquals("错误的任务办理人", "u01", task.getAssignee());
				
			}else if(total==2){
				
				task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
				assertEquals("任务名称不匹配", "上级审核", task.getName());
				assertEquals("错误的任务办理人", "u02", task.getAssignee());
				
			    
			}else{
				throw new Exception("error");
			}
		}
		
		
	}
}
