package com.bosssoft.platform.activiti.test.consign;

import java.util.List;

import org.activiti.engine.consign.ConsignDefType;
import org.activiti.engine.consign.ConsignInstState;
import org.activiti.engine.impl.TaskExt;
import org.activiti.engine.impl.task.TaskBusinessState;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.identity.IdentityEnum;
import org.activiti.engine.spi.identity.Participator;
import org.junit.Test;

import com.bosssoft.platform.activiti.test.TaskVerifyEntity;
import com.bosssoft.platform.activiti.test.task.AbstractTaskTest;
import com.bosssoft.platform.activiti.test.task.AbstractTaskTest.TestTaskClassify;

public class subsitutionTaskTest extends AbstractTaskTest{

	@Test
	public void acceptCommonAndDelegate() throws Exception{
       Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_commonAndDelegate.bpmnx");

		
		repositoryService.createProcessConsignDefinition()
        .consignor("u03")
        .addConsignee(new Participator("u04", "张三", IdentityEnum.USER, null))
        .consignDefType(ConsignDefType.ALL)
        .startTime("2018-06-11")
    	.endTime("2019-9-22")
    	.save(); 
		
		
		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		//创建代办任务
		TaskExt task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u01").singleResult();
		taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u02", IdentityEnum.USER, task.getTaskId(), null);
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u04").singleResult();
		taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u05", IdentityEnum.USER, task.getTaskId(), null);
	
		//待办任务
		TaskVerifyEntity verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNRECEIVE.toString(), null, false,true,null);
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u02", verifyEntity);
		verifyDelegateTask(TestTaskClassify.SUBSTITUTION, "u01", 1, verifyEntity);
	
		verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNRECEIVE.toString(), null, true,true,null);
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u05", verifyEntity);
		verifyDelegateTask(TestTaskClassify.SUBSTITUTION, "u04", 1, verifyEntity);
		
		verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNFINISH.toString(), null, true,false,"u04");
	    verifyUnfinishTask(TestTaskClassify.DELEGATE, 1, "u04", verifyEntity);
	    verifyDelegateTask(TestTaskClassify.DELEGATE, "u03", 1, verifyEntity);
	    
	    //接受代办任务
	    task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u02").singleResult();
	  //  taskService.claimCascade(task.getTaskId(), task.getConsigInstId(), "u02");
	    taskService.claimCascade(task.getTaskId(), getConsignInstIdForClaim(task.getTaskId(), "u02"), "u02");
	    task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u05").singleResult();
	  //  taskService.claimCascade(task.getTaskId(), task.getConsigInstId(), "u05");
	    taskService.claimCascade(task.getTaskId(), getConsignInstIdForClaim(task.getTaskId(), "u05"), "u05");
	    
	    
	    verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), "u02", false,true,"u02");
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u02", verifyEntity);
		verifyDelegateTask(TestTaskClassify.SUBSTITUTION, "u01", 1, verifyEntity);
	
		verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), "u05", true,true,"u05");
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u05", verifyEntity);
		verifyDelegateTask(TestTaskClassify.SUBSTITUTION, "u04", 1, verifyEntity);
		
		verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNFINISH.toString(), "u05", true,false,"u04");
	    verifyUnfinishTask(TestTaskClassify.DELEGATE, 1, "u04", verifyEntity);
	    verifyDelegateTask(TestTaskClassify.DELEGATE, "u03", 1, verifyEntity);
	    
	    //办理人代办任务
	    task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u02").singleResult();
	  //  taskService.completCascade(task.getTaskId(), task.getConsigInstId());
	    taskService.completCascade(task.getTaskId(), getConsignInstIdForComplete(task.getTaskId()));
	    task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u05").singleResult();
	 //   taskService.completCascade(task.getTaskId(), task.getConsigInstId());
	    taskService.completCascade(task.getTaskId(), getConsignInstIdForComplete(task.getTaskId()));
	    
	    //已办任务
	    verifyUnfinishTask(TestTaskClassify.ALL, 0, "u02", null);
	    verifyUnfinishTask(TestTaskClassify.ALL, 0, "u05", null);
	    verifyUnfinishTask(TestTaskClassify.ALL, 0, "u04", null);
	    
	    verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.FINISHED.toString(), "u02", false,true,"u02");
	    verifyDelegateTask(TestTaskClassify.SUBSTITUTION, "u01", 1, verifyEntity);
	    verifyFinishedTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
	    
	    verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.FINISHED.toString(), "u05", false,true,"u05");
	    verifyDelegateTask(TestTaskClassify.SUBSTITUTION, "u04", 1, verifyEntity);
	    verifyFinishedTask(TestTaskClassify.ALL, "u05", 1, verifyEntity);
	    
	    verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.DELEGATE.toString(), ConsignInstState.FINISHED.toString(), "u05", false,true,"u04");
	    verifyDelegateTask(TestTaskClassify.DELEGATE, "u03", 1, verifyEntity);
	    verifyFinishedTask(TestTaskClassify.ALL, "u04", 1, verifyEntity);
	    
	}
	
	
	
	@Test
	public void rejectCommonAndDelegate() throws Exception{
       Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_commonAndDelegate.bpmnx");

		
		repositoryService.createProcessConsignDefinition()
        .consignor("u03")
        .addConsignee(new Participator("u04", "张三", IdentityEnum.USER, null))
        .consignDefType(ConsignDefType.ALL)
        .startTime("2018-06-11")
    	.endTime("2019-9-22")
    	.save(); 
		
		
		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		//创建代办任务
		TaskExt task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u01").singleResult();
		taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u02", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u04").singleResult();
		taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u05", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		    
	    //拒绝代办任务
	    task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u02").singleResult();
	   // taskService.rejectTask(task.getConsigInstId());
	    taskService.rejectTask(getConsignInstIdForReject(task.getTaskId()));
	    task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u05").singleResult();
	    //taskService.rejectTask(task.getConsigInstId());
	    taskService.rejectTask(getConsignInstIdForReject(task.getTaskId()));
	    
	    verifyUnfinishTask(TestTaskClassify.ALL, 0, "u02", null);
	    verifyUnfinishTask(TestTaskClassify.ALL, 0, "u05", null);
	    
	    verifyDelegateTask(TestTaskClassify.ALL, "u01", 0, null);
	    verifyDelegateTask(TestTaskClassify.ALL, "u04", 0, null);
	    
	    TaskVerifyEntity verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.REJECTION.toString(), ConsignInstState.REJECTION.toString(), "u01", false,true,null);
	    verifyUnfinishTask(TestTaskClassify.OWNNER, 1, "u01", verifyEntity);
	    
	    verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNFINISH.toString(), "u04", false,true,"u04");
	    verifyUnfinishTask(TestTaskClassify.ALL, 1, "u04", verifyEntity);
	    
	    //B 自己完成
	    task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u01").singleResult();
	    //taskService.completCascade(task.getTaskId(),task.getConsigInstId());
	   taskService.completCascade(task.getTaskId(),getConsignInstIdForComplete(task.getTaskId()));
	    
	    //C 重新代办给别人
	    task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u04").singleResult();
	    taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u06", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		
		 verifyDelegateTask(TestTaskClassify.ALL, "u01", 0, null);
		 
		 verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNRECEIVE.toString(), null, true,true,null);
		 verifyDelegateTask(TestTaskClassify.ALL, "u04", 1, verifyEntity);
		 verifyUnfinishTask(TestTaskClassify.ALL, 1, "u06", verifyEntity);
		 
		 verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNFINISH.toString(), null, true,false,"u04");
		 verifyUnfinishTask(TestTaskClassify.ALL, 1, "u04", verifyEntity);
		
		 verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.REJECTION.toString(),ConsignInstState.REJECTION.toString(), "u01", false,true,null);
		// verifyUnfinishTask(TestTaskClassify.OWNNER, 1, "u01", verifyEntity);
		 verifyFinishedTask(TestTaskClassify.OWNNER, "u01", 1, verifyEntity);
	}
	
	/**
	 * 来源于代办任务，没有撤销任务的领取
	 * @throws Exception 
	 */
	@Test
	public void sourceSubsitutionNotCancel() throws Exception{
       Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_commonAndDelegate.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		TaskExt task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u01").singleResult();
		taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u02", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u03").singleResult();
		taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u04", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		
		//接受代办任务
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u02").singleResult();
		//taskService.claimCascade(task.getTaskId(), task.getConsigInstId(), "u02");
		taskService.claimCascade(task.getTaskId(), getConsignInstIdForClaim(task.getTaskId(), "u02"), "u02");
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u04").singleResult();
		//taskService.claimCascade(task.getTaskId(), task.getConsigInstId(), "u04");
		taskService.claimCascade(task.getTaskId(), getConsignInstIdForClaim(task.getTaskId(), "u04"), "u04");
		
		//再次创建代办
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u02").singleResult();
		taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u06", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u04").singleResult();
		taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u05", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		
		 TaskVerifyEntity verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNRECEIVE.toString(), null, true,true,null);
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u06", verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
		
		 verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNRECEIVE.toString(), null, true,true,null);
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u05", verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u04", 1, verifyEntity);
		
		
		verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), null, true,false,"u02");
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u02", verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u01", 1, verifyEntity);
		
		verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), null, true,false,"u04");
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u04", verifyEntity);
		verifyDelegateTask(TestTaskClassify.SUBSTITUTION, "u03", 1, verifyEntity);
		
		//B接受并且办理  C拒绝
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u06").singleResult();
		//taskService.claimCascade(task.getTaskId(), task.getConsigInstId(), "u06");
       // taskService.completCascade(task.getTaskId(), task.getConsigInstId());
		
		taskService.claimCascade(task.getTaskId(), getConsignInstIdForClaim(task.getTaskId(), "u06"), "u06");
	    taskService.completCascade(task.getTaskId(), getConsignInstIdForComplete(task.getTaskId()));
		
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u05").singleResult();
		//taskService.rejectTask(task.getConsigInstId());
		taskService.rejectTask(getConsignInstIdForReject(task.getTaskId()));
		
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u01", null);
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u02", null);
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u06", null);
		
		verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.FINISHED.toString(), "u06", true,false,"u06");
		verifyFinishedTask(TestTaskClassify.ALL, "u06", 1, verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
		
		verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.FINISHED.toString(), "u06", true,false,"u02");
		verifyFinishedTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u01", 1, verifyEntity);
		
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u05", null);
		
		verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), "u04", false,true,"u04");
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u04", null);
		verifyDelegateTask(TestTaskClassify.ALL, "u03", 1, verifyEntity);
	}
	
	@Test
	public void sourceSubsitutionCancel() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_sourceCooperationRedo.bpmnx");	
		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		TaskExt task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u02").singleResult();
	    String consignInstId1=	taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u03", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		//taskService.claimCascade(task.getTaskId(), consignInstId1, "u03");
	    taskService.claimCascade(task.getTaskId(), getConsignInstIdForClaim(task.getTaskId(), "u03"), "u03");
		
		String consignInstId2=	taskService.createTaskSubstitutionInst("u03", "u04", IdentityEnum.USER, task.getTaskId(), consignInstId1);
		//taskService.claimCascade(task.getTaskId(), consignInstId2, "u04");
		taskService.claimCascade(task.getTaskId(), getConsignInstIdForClaim(task.getTaskId(), "u04"), "u04");
		
		TaskVerifyEntity verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), "u04", true,true,"u04");
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u04", verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u03", 1, verifyEntity);
		
		verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), "u04", true,false,"u03");
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u03", verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
		
		//u03  撤销任务的领取
		//taskService.unclaimCascade(task.getTaskId(), consignInstId1);
		taskService.unclaimCascade(task.getTaskId(), getConsignInstIdForUmClaim(task.getTaskId(), "u03"));
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u04", null);
		
		verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNRECEIVE.toString(), null, false,true,null);
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u03", verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
	}
	
	@Test
	public void sourceCooperationNotCancel()throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_commonAndDelegate.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		TaskExt task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u01").singleResult();
		taskService.createTaskCooperationInst(task.getAssigneeId(), "u02", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u03").singleResult();
		taskService.createTaskCooperationInst(task.getAssigneeId(), "u04", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		
		//接受协办任务
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u02").singleResult();
		//taskService.claimCascade(task.getTaskId(), task.getConsigInstId(), "u02");
		 taskService.claimCascade(task.getTaskId(), getConsignInstIdForClaim(task.getTaskId(), "u02"), "u02");
		
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u04").singleResult();
		//taskService.claimCascade(task.getTaskId(), task.getConsigInstId(), "u04");
		 taskService.claimCascade(task.getTaskId(), getConsignInstIdForClaim(task.getTaskId(), "u04"), "u04");
		
		//再次创建代办
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u02").singleResult();
		taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u06", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u04").singleResult();
		taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u05", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		
		 TaskVerifyEntity verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNRECEIVE.toString(), null, true,true,null);
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u06", verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
		
		 verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNRECEIVE.toString(), null, true,true,null);
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u05", verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u04", 1, verifyEntity);
		
		
		verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNFINISH.toString(), null, true,false,"u02");
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u02", verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u01", 1, verifyEntity);
		
		verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNFINISH.toString(), null, true,false,"u04");
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u04", verifyEntity);
		verifyDelegateTask(TestTaskClassify.COOPERATION, "u03", 1, verifyEntity);
		
		//B接受并且办理  C拒绝
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u06").singleResult();
		//taskService.claimCascade(task.getTaskId(), task.getConsigInstId(), "u06");
       // taskService.completCascade(task.getTaskId(), task.getConsigInstId());
		
		taskService.claimCascade(task.getTaskId(), getConsignInstIdForClaim(task.getTaskId(), "u06"), "u06");
	    taskService.completCascade(task.getTaskId(), getConsignInstIdForComplete(task.getTaskId()));
		
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u05").singleResult();
		//taskService.rejectTask(task.getConsigInstId());
		taskService.rejectTask(getConsignInstIdForReject(task.getTaskId()));
		
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u06", null);
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u05", null);
		
		verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.FINISHED.toString(), "u01", true,false,"u06");
		verifyFinishedTask(TestTaskClassify.ALL, "u06", 1, verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
		
		verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.UNCONFIRM.toString(), ConsignInstState.UNCONFIRM.toString(), "u01", true,true,"u02");
		verifyDelegateTask(TestTaskClassify.ALL, "u01", 1, verifyEntity);
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u01", verifyEntity);
		verifyFinishedTask(TestTaskClassify.COOPERATION, "u02", 1, verifyEntity);
		
		
		verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNFINISH.toString(), "u04", false,true,"u04");
		verifyDelegateTask(TestTaskClassify.ALL, "u03", 1, verifyEntity);
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u04", verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u04", 0, null);
		
		//B 节点确认任务   C节点自己办理
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u01").singleResult();
      //  taskService.completCascade(task.getTaskId(), task.getConsigInstId());
		taskService.completCascade(task.getTaskId(), getConsignInstIdForComplete(task.getTaskId()));
        
        task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u04").singleResult();
       // taskService.completCascade(task.getTaskId(), task.getConsigInstId());
        taskService.completCascade(task.getTaskId(), getConsignInstIdForComplete(task.getTaskId()));
		
         verifyUnfinishTask(TestTaskClassify.ALL, 0, "u01", null);
         verifyUnfinishTask(TestTaskClassify.ALL, 0, "u04", null);
         
         verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.FINISHED.toString(), "u01", true,false,"u06");
         verifyFinishedTask(TestTaskClassify.ALL, "u06", 1, verifyEntity);
         verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
         
         verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.NORMAL.toString(), ConsignInstState.FINISHED.toString(), "u01", true,false,"u02");
         verifyFinishedTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
         verifyFinishedTask(TestTaskClassify.ALL, "u01", 1, verifyEntity);
         verifyDelegateTask(TestTaskClassify.ALL, "u01", 1, verifyEntity);
         
         verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.UNCONFIRM.toString(), ConsignInstState.UNCONFIRM.toString(), "u03", false,false,"u04");
         verifyFinishedTask(TestTaskClassify.ALL, "u04", 1, verifyEntity);
         verifyDelegateTask(TestTaskClassify.COOPERATION, "u03", 1, verifyEntity);
         
         verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.UNCONFIRM.toString(), ConsignInstState.UNCONFIRM.toString(), "u03", false,true,"u04");
         verifyUnfinishTask(TestTaskClassify.ALL, 1, "u03", verifyEntity);
	}
	
	
	@Test
	public void sourceCooperationCancelAndRedo() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_commonAndDelegate.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		TaskExt task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u01").singleResult();
		taskService.createTaskCooperationInst(task.getAssigneeId(), "u02", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u03").singleResult();
		taskService.createTaskCooperationInst(task.getAssigneeId(), "u04", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		
		//接受协办任务
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u02").singleResult();
		taskService.claimCascade(task.getTaskId(), task.getConsigInstId(), "u02");
		TaskExt task2=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u04").singleResult();
		taskService.claimCascade(task2.getTaskId(), task2.getConsigInstId(), "u04");
		
		//再次创建代办
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u02").singleResult();
		taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u06", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u04").singleResult();
		taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u05", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		
		//B接受并且办理  C接受
		task = taskService.createTaskExtQuery().taskAssigneeOrCandidate("u06").singleResult();
		taskService.claimCascade(task.getTaskId(), task.getConsigInstId(), "u06");
		taskService.completCascade(task.getTaskId(), task.getConsigInstId());

		task = taskService.createTaskExtQuery().taskAssigneeOrCandidate("u05").singleResult();
		taskService.claimCascade(task.getTaskId(), task.getConsigInstId(), "u05");
		
		//u01 打回重做,u04撤销任务的领取
		task = taskService.createTaskExtQuery().taskAssigneeOrCandidate("u01").singleResult();
		taskService.redo(task.getConsigInstId());
		
		taskService.unclaimCascade(task2.getTaskId(), task2.getConsigInstId());
		
		TaskVerifyEntity verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), "u06", true,true,"u06");
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u06", verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
		verifyFinishedTask(TestTaskClassify.ALL, "u06", 0, null);
		verifyFinishedTask(TestTaskClassify.ALL, "u02", 0, null);
		
		verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.REDO.toString(), ConsignInstState.REDO.toString(), "u06", true,false,"u02");
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u02", verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u01", 1, verifyEntity);
		
		verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNRECEIVE.toString(), null, false,true,null);
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u05", null);
		verifyDelegateTask(TestTaskClassify.ALL, "u03", 1, verifyEntity);
		
	}
}
