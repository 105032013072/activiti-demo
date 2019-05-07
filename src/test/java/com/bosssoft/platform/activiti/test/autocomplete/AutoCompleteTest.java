package com.bosssoft.platform.activiti.test.autocomplete;

import java.util.Date;
import java.util.List;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import static org.junit.Assert.*;
import com.bosssoft.platform.activiti.test.MainTest;

/**
 * 测试任务的自动完成
 * @author huangxw
 *
 */
public class AutoCompleteTest extends MainTest{

	@Test
	public void testAutoComplete() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/autocomplete/process-autocomplete.bpmnx");
		
		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u02")
                .start();
		
		
		//自动完成所有任务节点，流程已经结束
	    List<Task> activeTaskList=   taskService.createTaskQuery().processInstanceId(inst.getId()).list();
	    assertEquals("流程存在正在运行的任务", 0, activeTaskList.size());
	    Date endtime=histroyService
	    		    .createHistoricProcessInstanceQuery()
	    		    .processInstanceId(inst.getId())
	    		    .singleResult()
	    		    .getEndTime();
	    assertNotNull("流程结束时间为空，流程未结束", endtime);
	    
	    //验证已完成任务节点的办理人
	    HistoricTaskInstanceQuery query= histroyService
                                        .createHistoricTaskInstanceQuery()
                                        .processInstanceId(inst.getId());
	      
	    //第一个节点为发起者
	    HistoricTaskInstance hisTask= query
	    		                   .taskDefinitionKey("A")
	    		                   .singleResult();
	    assertEquals("第一个任务的办理人不是发起者","u02" , hisTask.getAssignee());
	    
	    //第二个节点  办理人为u01
	     hisTask= query
                .taskDefinitionKey("B")
                .singleResult();
         assertEquals("第二个任务的办理人不是u01","u01" , hisTask.getAssignee());
	    
       //第三个节点  办理人为空   默认为发起者
         hisTask= query
                 .taskDefinitionKey("C")
                 .singleResult();
         assertEquals("第三个任务的办理人不是发起者","u02" , hisTask.getAssignee());
	}
	
}
