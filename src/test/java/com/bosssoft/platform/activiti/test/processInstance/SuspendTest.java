package com.bosssoft.platform.activiti.test.processInstance;

import java.util.List;

import org.activiti.engine.consign.ConsignDefType;
import org.activiti.engine.impl.TaskExt;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.identity.IdentityEnum;
import org.activiti.engine.spi.identity.Participator;
import org.activiti.engine.task.Task;
import static org.junit.Assert.*;
import org.junit.Test;

public class SuspendTest extends AbsProcessInstanceTest{
     
	@Test
	public void activeForCountersign() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/processInstance/process_activeForCountersign.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		suspendProcess(inst.getProcessInstanceId());
		checkActiveForCountersign(false);
		activeProcess(inst.getProcessInstanceId());
		runtimeService.setVariable(inst.getProcessInstanceId(), "act_countersignature_result", true);
		checkActiveForCountersign(true);
		
		//撤销任务的领取
		
	}
	
	private void checkActiveForCountersign(Boolean enable ){
		List<Task> blist=taskService.createTaskQuery().taskDefinitionKey("B").list();
		assertEquals(4, blist.size());
		for (Task task : blist) {
			verifyTaskComplete(task.getId(), null, enable);
		}
		
		List<Task> clist=taskService.createTaskQuery().taskDefinitionKey("C").list();
		assertEquals(4, clist.size());
		verifyTaskClaim(clist.get(0).getId(), null, "u02", enable);
	}
	
	@Test
	public void activeForDelegate() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/processInstance/process_activeForDelegate.bpmnx");

		repositoryService.createProcessConsignDefinition()
        .consignor("u01")
        .addConsignee(new Participator("u02", "小雨", IdentityEnum.USER, null))
        .consignDefType(ConsignDefType.ALL)
        .startTime("2018-06-11")
    	.endTime("2019-9-22")
    	.save();
		
		repositoryService.createProcessConsignDefinition()
        .consignor("u03")
        .addConsignee(new Participator("p03", "研发", IdentityEnum.POSITION, null))
        .consignDefType(ConsignDefType.ALL)
        .startTime("2018-06-11")
    	.endTime("2019-9-22")
    	.save();
		
		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		//TaskExt taskExt=	taskService.createTaskExtQuery().taskAssigneeOrCandidate("u06").singleResult();
		suspendProcess(inst.getProcessInstanceId());
		checkActiveForDelegate(false);
		activeProcess(inst.getProcessInstanceId());
		checkActiveForDelegate(true);
		
		//测试撤销任务的领取
		suspendProcess(inst.getProcessInstanceId());
		checkUnclaimForDelegate(false);
		activeProcess(inst.getProcessInstanceId());
		checkUnclaimForDelegate(true);
		
	}
	
	private void checkActiveForDelegate(Boolean enable ){
		TaskExt taskExt=	taskService.createTaskExtQuery().delegateTaskAssigneeOrCandidate("u02").singleResult();
	    verifyTaskComplete(taskExt.getTaskId(), taskExt.getConsigInstId(), enable);
	    taskExt=	taskService.createTaskExtQuery().delegateTaskAssigneeOrCandidate("u06").singleResult();
		verifyTaskClaim(taskExt.getTaskId(), taskExt.getConsigInstId(), "u06", enable);
	}
	
	private void checkUnclaimForDelegate(Boolean enable){
		TaskExt taskExt=	taskService.createTaskExtQuery().delegateTaskAssigneeOrCandidate("u06").singleResult();
		assertEquals("u06", taskExt.getAssigneeId());
		verifyUnClaim(taskExt.getTaskId(), taskExt.getConsigInstId(), enable);
	}
	
	@Test
	public void subsitutionAndCooperation() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/processInstance/process_activeForDelegate.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		//创建协办,接受协办，办理协办任务
		Task btask=taskService.createTaskQuery().taskDefinitionKey("B").singleResult();
		String bconsignInstId=taskService.createTaskCooperationInst(btask.getAssignee(), "u02", IdentityEnum.USER, btask.getId(), null);
		taskService.claimCascade(btask.getId(), bconsignInstId, "u02");
		taskService.completCascade(btask.getId(), bconsignInstId);
		
		//创建代办
		Task ctask=taskService.createTaskQuery().taskDefinitionKey("C").singleResult();
		taskService.createTaskSubstitutionInst(ctask.getAssignee(), "u04", IdentityEnum.USER, ctask.getId(), null);
		
		
		suspendProcess(inst.getProcessInstanceId());
		checkRedoAndReject(bconsignInstId, btask.getId(), false);
		activeProcess(inst.getProcessInstanceId());
		checkRedoAndReject(bconsignInstId, btask.getId(), true);
		
		//测试收回代办
	   String consignInstId=taskService.createTaskSubstitutionInst(ctask.getAssignee(), "u05", IdentityEnum.USER, ctask.getId(), null);
		suspendProcess(inst.getProcessInstanceId());
	    verifyWithdrawConsign(ctask.getId(), consignInstId, false, "u03");
	    activeProcess(inst.getProcessInstanceId());
	    verifyWithdrawConsign(ctask.getId(), consignInstId, true, "u03");
	}
	
	private void checkRedoAndReject(String redoConsignId,String redoTaskId,Boolean enable){
		verifyRedo(redoTaskId, redoConsignId, enable, "u02");
		TaskExt task=taskService.createTaskExtQuery().substitutionTaskAssignee("u04").singleResult();
		verifyRejectConsign(task.getTaskId(), task.getConsigInstId(), enable, "u03");
		
	}
	
	@Test
	public void taskOp() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/processInstance/process_taskOp.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		Task atask=taskService.createTaskQuery().taskDefinitionKey("A").singleResult();
		taskService.completCascade(atask.getId(), null);
		
		//退回上一步
		suspendProcess(inst.getId());
		verifyBackToLast("B", "A", false);
		activeProcess(inst.getId());
		verifyBackToLast("B", "A", true);
		
		
		atask=taskService.createTaskQuery().taskDefinitionKey("A").singleResult();
		taskService.completCascade(atask.getId(), null);
		
		Task btask=taskService.createTaskQuery().taskDefinitionKey("B").singleResult();
		taskService.completCascade(btask.getId(), null);
		
		//退回指定节点
		suspendProcess(inst.getId());
		verifyBackToAct("C", "A", false);
		activeProcess(inst.getId());
		verifyBackToAct("C", "A", true);
		
		atask=taskService.createTaskQuery().taskDefinitionKey("A").singleResult();
		taskService.completCascade(atask.getId(), null);
		
		//退回重填
		suspendProcess(inst.getId());
		verifyBackToReaplly("B", "apply", false);
		activeProcess(inst.getId());
		verifyBackToReaplly("B", "apply", true);
		
		
		Task applyTask=taskService.createTaskQuery().taskDefinitionKey("apply").singleResult();
		taskService.completCascade(applyTask.getId(), null);
			
		suspendProcess(inst.getId());
		verifyWithdrawTask("apply", inst.getId(), false);
		activeProcess(inst.getId());
		verifyWithdrawTask("apply", inst.getId(), true);
		
		applyTask=taskService.createTaskQuery().taskDefinitionKey("apply").singleResult();
		taskService.completCascade(applyTask.getId(), null);
		
		
		//加签	
		suspendProcess(inst.getId());
		verifyInsert("A", "u01", false);
		activeProcess(inst.getId());
		verifyInsert("A", "u01", true);

	}
	
	
	
	
}
