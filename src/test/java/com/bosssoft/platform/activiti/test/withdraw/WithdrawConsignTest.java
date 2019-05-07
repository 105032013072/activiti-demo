package com.bosssoft.platform.activiti.test.withdraw;

import org.activiti.engine.consign.ConsignInstState;
import org.activiti.engine.impl.TaskExt;
import org.activiti.engine.impl.task.TaskBusinessState;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.identity.IdentityEnum;
import org.junit.Test;

import com.bosssoft.platform.activiti.test.MainTest;
import com.bosssoft.platform.activiti.test.TaskVerifyEntity;
import com.bosssoft.platform.activiti.test.task.AbstractTaskTest;
import com.bosssoft.platform.activiti.test.task.AbstractTaskTest.TestTaskClassify;

public class WithdrawConsignTest extends AbstractTaskTest{
  
	@Test
	public void test() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/withdraw/process_commontask.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		TaskExt task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u02").singleResult();
		String consignInstId= taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u10", IdentityEnum.USER, task.getTaskId(), null);
		
		//收回代办
		taskService.withdrawConsign(consignInstId);
		
		//重新委托
		taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u03", IdentityEnum.USER, task.getTaskId(), null);
		taskService.withdrawConsign(getConsignInstIdForWithdraw(task.getTaskId(), "u02"));
		
		taskService.completCascade(task.getTaskId(), consignInstId);
		TaskVerifyEntity verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.WITHDRAW.toString(), ConsignInstState.WITHDRAW.toString(), "u02", false,true,null);
		verifyFinishedTask(TestTaskClassify.OWNNER, "u02", 1, verifyEntity);
	
		
	} 
}
