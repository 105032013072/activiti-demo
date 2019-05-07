package com.bosssoft.platform.activiti.test.task.claimAndHandle;

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

import org.activiti.engine.consign.ConsignInstState;
import org.activiti.engine.impl.TaskExt;
import org.activiti.engine.impl.TaskExt.TaskClassify;
import org.activiti.engine.impl.task.TaskBusinessState;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;

import com.bosssoft.platform.activiti.test.MainTest;
import com.bosssoft.platform.activiti.test.TaskVerifyEntity;
import com.bosssoft.platform.activiti.test.task.AbstractTaskTest;
import com.mysql.fabric.xmlrpc.base.Array;

public class CommonTaskTest extends AbstractTaskTest{
  
	@Test
	public void test() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_commontask.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
	    //A节点任务已自动完成
		TaskVerifyEntity averifyEntity=new TaskVerifyEntity("A", TaskBusinessState.NORMAL.toString(), null, "u03",false,true);
		verifyFinishedTask(TestTaskClassify.OWNNER, "u03", 1, averifyEntity);
		verifyFinishedTask(TestTaskClassify.DELEGATE, "u03", 0, null);
		verifyFinishedTask(TestTaskClassify.SUBSTITUTION, "u03", 0, null);
		verifyFinishedTask(TestTaskClassify.DELEGATE, "u03", 0, null);
		
		//B,C节点
		TaskVerifyEntity b1verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.NORMAL.toString(), null, "u01",false,true);
		TaskVerifyEntity b2verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.NORMAL.toString(), null, null,false,true);
		verifyUnfinishTask(TestTaskClassify.OWNNER, 2, "u01", b1verifyEntity,b2verifyEntity);
		
		TaskVerifyEntity cverifyEntity=new TaskVerifyEntity("C", TaskBusinessState.NORMAL.toString(), null, null,false,true);
		verifyUnfinishTask(TestTaskClassify.OWNNER, 1, "u06", cverifyEntity);
		
		
		List<TaskExt> list=taskService.createTaskExtQuery().ownerTaskAssigneeOrCandidate("u06").list();
		TaskExt ctask=list.get(0);
		//u06 领取同时办理C节点任务
		taskService.claimAndExecution(list.get(0).getTaskId(), "u06", null, null);
		//taskService.claimCascade(list.get(0).getTaskId(), null, "u06");
		b1verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.NORMAL.toString(), null, "u01",false,true);
		verifyUnfinishTask(TestTaskClassify.OWNNER, 1, "u01", b1verifyEntity);
		
	    TaskExt btask=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u01").singleResult();
	    
		//完成B，C任务
	   // taskService.completCascade(ctask.getTaskId(), null); 
	   // taskService.completCascade(btask.getTaskId(), null);
	    taskService.claimAndExecution(btask.getTaskId(), "u01", null, null);;
	    
	    //已办任务查询
	    verifyUnfinishTask(TestTaskClassify.ALL, 0, "u01", null);
	    verifyUnfinishTask(TestTaskClassify.ALL, 0, "u06", null);
	    
	    TaskVerifyEntity verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.NORMAL.toString(), null, "u01",false,true);
	    verifyFinishedTask(TestTaskClassify.ALL, "u01", 1, verifyEntity);
	     verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.NORMAL.toString(), null, "u06",false,true);
	    verifyFinishedTask(TestTaskClassify.ALL, "u06", 1, verifyEntity);
	}
	
	
	
}
