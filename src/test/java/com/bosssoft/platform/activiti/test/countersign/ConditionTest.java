package com.bosssoft.platform.activiti.test.countersign;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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

public class ConditionTest extends MainTest{
	@Test
	public void test() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/countersign/sourcecandition/process_condition.bpmnx");
	
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
			if(total==1){
				System.out.println("total==1");
			}
			
			
			//第一个节点
			List<Task> taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
			assertEquals(5, taskList.size());
			for (Task task : taskList) {
		    	assertEquals("A", task.getTaskDefinitionKey());
			}
		    String[] assigneeArray=getTaskAssignee(taskList);
		    String[] expectArray=new String[]{"u06","u01","u04","u02","u05"};
			Arrays.sort(expectArray);
			assertArrayEquals(assigneeArray, expectArray);
			
			Map<String,Object> vars=new HashedMap();
			vars.put("act_countersignature_result", true);
			for (Task task : taskList) {
				if(task.getAssignee().equals("u06")){
					taskService.completCascade(task.getId(), null,vars);//通过
					break;
				}
			}
			
			if(total==1){
				taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
				assertEquals(2, taskList.size());
				for (Task task : taskList) {
			    	assertEquals("B", task.getTaskDefinitionKey());
				}
			     assigneeArray=getTaskAssignee(taskList);
			    expectArray=new String[]{"u01","u02"};
				Arrays.sort(expectArray);
				assertArrayEquals(assigneeArray, expectArray);
				//taskService.completCascade(taskList.get(0).getId(), null,vars);//通过
				
			}else if(total==2){
				taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
				assertEquals(1, taskList.size());
				for (Task task : taskList) {
			    	assertEquals("B", task.getTaskDefinitionKey());
				}
			     assigneeArray=getTaskAssignee(taskList);
			    expectArray=new String[]{"u02"};
				Arrays.sort(expectArray);
				assertArrayEquals(assigneeArray, expectArray);
				
				
			}else {
				taskList=taskService.createTaskQuery().processInstanceId(inst.getId()).list();
				assertEquals(3, taskList.size());
				for (Task task : taskList) {
			    	assertEquals("B", task.getTaskDefinitionKey());
				}
			     assigneeArray=getTaskAssignee(taskList);
			    expectArray=new String[]{"u01","u06","u04"};
				Arrays.sort(expectArray);
				assertArrayEquals(assigneeArray, expectArray);
				
				
			}
			
			
		}
	
	
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
