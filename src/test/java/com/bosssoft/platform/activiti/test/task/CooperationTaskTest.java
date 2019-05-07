package com.bosssoft.platform.activiti.test.task;

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

public class CooperationTaskTest extends AbstractTaskTest{
     
	@Test
	public void commonTask() throws Exception{
		 Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_cooperation_commonTask.bpmnx");
	
		 ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		 
		 //创建协办
		 TaskExt task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u01").singleResult();
		 String ataskId=task.getTaskId();
		 String aConsignStr=taskService.createTaskCooperationInst(task.getAssigneeId(), "u02", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		
		 task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u03").singleResult();
		 String btaskId=task.getTaskId();
		 String bConsignStr= taskService.createTaskCooperationInst(task.getAssigneeId(), "u04", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		 
		 TaskVerifyEntity verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNRECEIVE.toString(), null, false,true,null);
	     verifyUnfinishTask(TestTaskClassify.COOPERATION, 1, "u02", verifyEntity);
	     verifyDelegateTask(TestTaskClassify.COOPERATION, "u01", 1, verifyEntity);
	     
	     verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNRECEIVE.toString(), null, false,true,null);
	     verifyUnfinishTask(TestTaskClassify.COOPERATION, 1, "u04", verifyEntity);
	     verifyDelegateTask(TestTaskClassify.COOPERATION, "u03", 1, verifyEntity);
	     
	     //领取任务
	     taskService.claimCascade(ataskId, aConsignStr, "u02");
	     taskService.claimCascade(btaskId, bConsignStr, "u04");
	     
	     verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNFINISH.toString(), "u02", false,true,"u02");
	     verifyUnfinishTask(TestTaskClassify.COOPERATION, 1, "u02", verifyEntity);
	     verifyDelegateTask(TestTaskClassify.COOPERATION, "u01", 1, verifyEntity);
	     
	     verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNFINISH.toString(), "u04", false,true,"u04");
	     verifyUnfinishTask(TestTaskClassify.COOPERATION, 1, "u04", verifyEntity);
	     verifyDelegateTask(TestTaskClassify.COOPERATION, "u03", 1, verifyEntity);
	     
	     //办理任务
	     taskService.completCascade(ataskId, aConsignStr);
	     taskService.completCascade(btaskId, bConsignStr);
	     
	     verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.UNCONFIRM.toString(), ConsignInstState.UNCONFIRM.toString(), "u01", false,true,"u02");
	     verifyUnfinishTask(TestTaskClassify.COOPERATION, 0, "u02", null);
	     verifyUnfinishTask(TestTaskClassify.OWNNER, 1, "u01", verifyEntity);
	     verifyFinishedTask(TestTaskClassify.COOPERATION, "u02", 1, verifyEntity);
	     verifyDelegateTask(TestTaskClassify.COOPERATION, "u01", 1, verifyEntity);
	     
	     verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.UNCONFIRM.toString(), ConsignInstState.UNCONFIRM.toString(), "u03", false,true,"u04");
	     verifyUnfinishTask(TestTaskClassify.COOPERATION, 0, "u04", null);
	     verifyUnfinishTask(TestTaskClassify.OWNNER, 1, "u03", verifyEntity);
	     verifyFinishedTask(TestTaskClassify.COOPERATION, "u04", 1, verifyEntity);
	     verifyDelegateTask(TestTaskClassify.COOPERATION, "u03", 1, verifyEntity);
	     
	     //A确认任务  B重做
	     taskService.completCascade(ataskId, aConsignStr);
	     taskService.redo(bConsignStr);
	     
	     verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.NORMAL.toString(), ConsignInstState.FINISHED.toString(), "u01", false,true,"u02");
	     verifyUnfinishTask(TestTaskClassify.COOPERATION, 0, "u02", null);
	     verifyUnfinishTask(TestTaskClassify.ALL, 0, "u01", null);
	     verifyFinishedTask(TestTaskClassify.COOPERATION, "u02", 1, verifyEntity);
	     verifyFinishedTask(TestTaskClassify.OWNNER, "u01", 1, verifyEntity);
	     verifyDelegateTask(TestTaskClassify.COOPERATION, "u01", 1, verifyEntity);
	     
	     verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.REDO.toString(), ConsignInstState.REDO.toString(), "u04", false,true,"u04");
	     verifyUnfinishTask(TestTaskClassify.COOPERATION, 1, "u04", verifyEntity);
	     verifyUnfinishTask(TestTaskClassify.OWNNER, 0, "u03", null);
	     verifyFinishedTask(TestTaskClassify.COOPERATION, "u04", 0, null);
	     verifyDelegateTask(TestTaskClassify.COOPERATION, "u03", 1, verifyEntity);
	     
	     taskService.completCascade(btaskId, bConsignStr);
	     
	     verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.UNCONFIRM.toString(), ConsignInstState.UNCONFIRM.toString(), "u03", false,true,"u04");
	     verifyUnfinishTask(TestTaskClassify.COOPERATION, 0, "u04", null);
	     verifyUnfinishTask(TestTaskClassify.OWNNER, 1, "u03", verifyEntity);
	     verifyFinishedTask(TestTaskClassify.COOPERATION, "u04", 1, verifyEntity);
	     verifyDelegateTask(TestTaskClassify.COOPERATION, "u03", 1, verifyEntity);
	     
	     //B确认任务
	     taskService.completCascade(btaskId, bConsignStr);
	     
	     verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.NORMAL.toString(), ConsignInstState.FINISHED.toString(), "u03", false,true,"u04");
	     verifyUnfinishTask(TestTaskClassify.COOPERATION, 0, "u04", null);
	     verifyUnfinishTask(TestTaskClassify.ALL, 0, "u03", null);
	     verifyFinishedTask(TestTaskClassify.COOPERATION, "u04", 1, verifyEntity);
	     verifyFinishedTask(TestTaskClassify.OWNNER, "u03", 1, verifyEntity);
	     verifyDelegateTask(TestTaskClassify.COOPERATION, "u03", 1, verifyEntity);
	}
	
	@Test
	public void delegateTask() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_cooperation_sourcedelegate.bpmnx");
		
		
		repositoryService.createProcessConsignDefinition()
        .consignor("u01")
        .addConsignee(new Participator("u02", "小王", IdentityEnum.USER, null))
        .consignDefType(ConsignDefType.ALL)
        .startTime("2018-06-11")
    	.endTime("2019-9-22")
    	.save();
		
		 ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		 
		 TaskExt task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u02").singleResult();
		 String taskId=task.getTaskId();
		 String consignStr= taskService.createTaskCooperationInst(task.getAssigneeId(), "u03", IdentityEnum.USER, task.getTaskId(), task.getConsigInstId());
		 
		 TaskVerifyEntity verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNRECEIVE.toString(), null, true,true,null);
	     verifyUnfinishTask(TestTaskClassify.COOPERATION, 1, "u03", verifyEntity);
	     verifyDelegateTask(TestTaskClassify.COOPERATION, "u02", 1, verifyEntity);
		 
	     verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNFINISH.toString(), null, true,false,"u02");
	     verifyUnfinishTask(TestTaskClassify.DELEGATE, 1, "u02", verifyEntity);
	     verifyDelegateTask(TestTaskClassify.DELEGATE, "u01", 1, verifyEntity);
	     
	     //领取并且办理协办任务
	     taskService.claimCascade(taskId, consignStr, "u03");
	     taskService.completCascade(taskId, consignStr);
	     
	     verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.UNCONFIRM.toString(), ConsignInstState.UNCONFIRM.toString(), "u02", true,true,"u03");
	     verifyUnfinishTask(TestTaskClassify.ALL, 0, "u03", null);
	     verifyFinishedTask(TestTaskClassify.ALL, "u03", 1, verifyEntity);
	     verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
	     
	     verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNFINISH.toString(), "u02", true,true,"u02");
	     verifyUnfinishTask(TestTaskClassify.DELEGATE, 1, "u02", verifyEntity);
	     
	     //确认任务
	     taskService.completCascade(taskId, consignStr);
	     
	     verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.NORMAL.toString(), ConsignInstState.FINISHED.toString(), "u02", true,true,"u03");
	     verifyUnfinishTask(TestTaskClassify.ALL, 0, "u03", null);
	     verifyUnfinishTask(TestTaskClassify.ALL, 0, "u02", null);
	    
	     
	     verifyFinishedTask(TestTaskClassify.ALL, "u03", 1, verifyEntity);
	     verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
	     
	     
	     verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.DELEGATE.toString(), ConsignInstState.FINISHED.toString(), "u02", true,true,"u02");
	     verifyFinishedTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
	     verifyDelegateTask(TestTaskClassify.ALL, "u01", 1, verifyEntity);
	    
	}
	
	@Test
	public void subsitutionTask() throws Exception{
        Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_cooperation_subsitutionTask.bpmnx");
		
		 ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		 
		//创建代办任务
		TaskExt task = taskService.createTaskExtQuery().taskAssigneeOrCandidate("u01").singleResult();
		String aSubInstId=taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u02", IdentityEnum.USER, task.getTaskId(), null);
		String ataskId=task.getTaskId();
		
		task = taskService.createTaskExtQuery().taskAssigneeOrCandidate("u03").singleResult();
		String btaskId=task.getTaskId();
		String bSubInstId=taskService.createTaskSubstitutionInst(task.getAssigneeId(), "u05", IdentityEnum.USER, task.getTaskId(), null);
		
		//接受代办任务
		taskService.claimCascade(ataskId, aSubInstId,"u02");
		taskService.claimCascade(btaskId, bSubInstId,"u05");
		
		//创建协办
		String acooInstid=taskService.createTaskCooperationInst("u02", "u04", IdentityEnum.USER, ataskId, aSubInstId);
		String bcooInstid=	taskService.createTaskCooperationInst("u05", "u06", IdentityEnum.USER, btaskId, bSubInstId);
		
		TaskVerifyEntity verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNRECEIVE.toString(), null, true,true,null);
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u04", verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
		
		verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), null, true,false,"u02");
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u02", verifyEntity);
		verifyDelegateTask(TestTaskClassify.ALL, "u01", 1, verifyEntity);
		
		
		verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNRECEIVE.toString(), null, true,true,null);
		verifyUnfinishTask(TestTaskClassify.COOPERATION, 1, "u06", verifyEntity);
		verifyDelegateTask(TestTaskClassify.COOPERATION, "u05", 1, verifyEntity);
		
		verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), null, true,false,"u05");
		verifyUnfinishTask(TestTaskClassify.SUBSTITUTION, 1, "u05", verifyEntity);
		verifyDelegateTask(TestTaskClassify.SUBSTITUTION, "u03", 1, verifyEntity);
		
	    //领取协办任务
		taskService.claimCascade(ataskId, acooInstid,"u04");
		taskService.claimCascade(btaskId, bcooInstid,"u06");
		
		//A 办理协办任务   B撤销代办的领取
		taskService.completCascade(ataskId, acooInstid);
		taskService.unclaimCascade(btaskId, bSubInstId);
		
		verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.UNCONFIRM.toString(), ConsignInstState.UNCONFIRM.toString(), "u02", true,true,"u04");
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u04", null);
		verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
		
		verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), "u02", true,true,"u04");
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u02", verifyEntity);
		
		verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNRECEIVE.toString(), null, false,true,null);
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u06", null);
		verifyDelegateTask(TestTaskClassify.ALL, "u03", 1, verifyEntity);
		
		//A 确认协办任务
		taskService.completCascade(ataskId, acooInstid);
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u04", null);
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u02", null);
		
		verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.NORMAL.toString(), ConsignInstState.FINISHED.toString(), "u02", true,true,"u04");
		verifyFinishedTask(TestTaskClassify.COOPERATION, "u04", 1, verifyEntity);
		verifyDelegateTask(TestTaskClassify.COOPERATION, "u02", 1, verifyEntity);
		
		verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.FINISHED.toString(), "u02", true,true,"u02");
		verifyDelegateTask(TestTaskClassify.SUBSTITUTION, "u01", 1, verifyEntity);
		
	}
	
	@Test
	public void cooperationCancel() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_cooperation_subsitutionTask.bpmnx");
		
		 ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
		 
		    //创建代办任务
			TaskExt task = taskService.createTaskExtQuery().taskAssigneeOrCandidate("u01").singleResult();
			String acooInstidOne=taskService.createTaskCooperationInst(task.getAssigneeId(), "u02", IdentityEnum.USER, task.getTaskId(), null);
			String ataskId=task.getTaskId();
			
			task = taskService.createTaskExtQuery().taskAssigneeOrCandidate("u03").singleResult();
			String btaskId=task.getTaskId();
			String bcooInstidOne=taskService.createTaskCooperationInst(task.getAssigneeId(), "u05", IdentityEnum.USER, task.getTaskId(), null);
			
			//接受协办任务
			taskService.claimCascade(ataskId, acooInstidOne,"u02");
			taskService.claimCascade(btaskId, bcooInstidOne,"u05");
			
			//创建协办
			String acooInstid=taskService.createTaskCooperationInst("u02", "u04", IdentityEnum.USER, ataskId, acooInstidOne);
			String bcooInstid=taskService.createTaskCooperationInst("u05", "u06", IdentityEnum.USER, btaskId, bcooInstidOne);
			
			TaskVerifyEntity verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNRECEIVE.toString(), null, true,true,null);
			verifyUnfinishTask(TestTaskClassify.ALL, 1, "u04", verifyEntity);
			verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
			
			verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNFINISH.toString(), null, true,false,"u02");
			verifyUnfinishTask(TestTaskClassify.ALL, 1, "u02", verifyEntity);
			verifyDelegateTask(TestTaskClassify.ALL, "u01", 1, verifyEntity);
			
			
			verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNRECEIVE.toString(), null, true,true,null);
			verifyUnfinishTask(TestTaskClassify.COOPERATION, 1, "u06", verifyEntity);
			verifyDelegateTask(TestTaskClassify.COOPERATION, "u05", 1, verifyEntity);
			
			verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNFINISH.toString(), null, true,false,"u05");
			verifyUnfinishTask(TestTaskClassify.COOPERATION, 1, "u05", verifyEntity);
			verifyDelegateTask(TestTaskClassify.COOPERATION, "u03", 1, verifyEntity); 
			
		
			 //领取协办任务
			taskService.claimCascade(ataskId, acooInstid,"u04");
			taskService.claimCascade(btaskId, bcooInstid,"u06");
			
			//A 办理协办任务   B撤销协办的领取
			taskService.completCascade(ataskId, acooInstid);
			taskService.unclaimCascade(btaskId, bcooInstidOne);
			
			verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.UNCONFIRM.toString(), ConsignInstState.UNCONFIRM.toString(), "u02", true,true,"u04");
			verifyUnfinishTask(TestTaskClassify.ALL, 0, "u04", null);
			verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
			
			verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNFINISH.toString(), "u02", true,true,"u04");
			verifyUnfinishTask(TestTaskClassify.ALL, 1, "u02", verifyEntity);
			
			verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNRECEIVE.toString(), null, false,true,null);
			verifyUnfinishTask(TestTaskClassify.ALL, 0, "u06", null);
			verifyDelegateTask(TestTaskClassify.ALL, "u03", 1, verifyEntity);
			
			//A 最后一个层级确认任务
			taskService.completCascade(ataskId, acooInstid);
			verifyUnfinishTask(TestTaskClassify.ALL, 0, "u04", null);
			verifyUnfinishTask(TestTaskClassify.ALL, 0, "u02", null);
			
			verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.NORMAL.toString(), ConsignInstState.FINISHED.toString(), "u01", true,true,"u04");
			verifyFinishedTask(TestTaskClassify.COOPERATION, "u04", 1, verifyEntity);
			verifyDelegateTask(TestTaskClassify.COOPERATION, "u02", 1, verifyEntity);
			
			verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.UNCONFIRM.toString(), ConsignInstState.UNCONFIRM.toString(), "u01", true,true,"u02");
			verifyDelegateTask(TestTaskClassify.COOPERATION, "u01", 1, verifyEntity);
			verifyUnfinishTask(TestTaskClassify.OWNNER, 1, "u01", verifyEntity);
			verifyFinishedTask(TestTaskClassify.COOPERATION, "u02", 1, verifyEntity);
			
			//A 第一个协办层级 打回重做
            taskService.redo(acooInstidOne);
            
            verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.REDO.toString(), ConsignInstState.REDO.toString(), "u04", true,true,"u04");
            verifyUnfinishTask(TestTaskClassify.COOPERATION, 1, "u04", verifyEntity);
            verifyDelegateTask(TestTaskClassify.COOPERATION, "u02", 1, verifyEntity);
           
            verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.REDO.toString(), ConsignInstState.REDO.toString(), "u04", true,false,"u02");
            verifyUnfinishTask(TestTaskClassify.COOPERATION, 1, "u02", verifyEntity);
            verifyDelegateTask(TestTaskClassify.COOPERATION, "u01", 1, verifyEntity);
			
            //重做任务
            taskService.completCascade(ataskId, acooInstid);
            
            verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.UNCONFIRM.toString(), ConsignInstState.UNCONFIRM.toString(), "u02", true,true,"u04");
            verifyUnfinishTask(TestTaskClassify.ALL, 0, "u04", null);
            verifyDelegateTask(TestTaskClassify.COOPERATION, "u02", 1, verifyEntity);
            verifyFinishedTask(TestTaskClassify.ALL, "u04", 1,verifyEntity);
            
            verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.REDO.toString(), ConsignInstState.REDO.toString(), "u02", true,true,"u02");
            verifyUnfinishTask(TestTaskClassify.COOPERATION, 1, "u02", verifyEntity);
            verifyDelegateTask(TestTaskClassify.COOPERATION, "u01", 1, verifyEntity);
            
         //最后一个协办层级  确认任务
            taskService.completCascade(ataskId, acooInstid);
            
            verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.NORMAL.toString(), ConsignInstState.FINISHED.toString(), "u01", true,true,"u04");
            verifyUnfinishTask(TestTaskClassify.ALL, 0, "u04", null);
            verifyDelegateTask(TestTaskClassify.COOPERATION, "u02", 1, verifyEntity);
            verifyFinishedTask(TestTaskClassify.ALL, "u04", 1,verifyEntity);
           
            verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.UNCONFIRM.toString(), ConsignInstState.UNCONFIRM.toString(), "u01", true,true,"u02");
            verifyFinishedTask(TestTaskClassify.COOPERATION, "u02", 1,verifyEntity);
            verifyUnfinishTask(TestTaskClassify.OWNNER, 1, "u01", verifyEntity);
            verifyDelegateTask(TestTaskClassify.COOPERATION, "u01", 1, verifyEntity);
            
            
            //第一个层级的协办确认任务
            taskService.completCascade(ataskId, acooInstidOne);
            verifyEntity=new TaskVerifyEntity("A", TaskBusinessState.NORMAL.toString(), ConsignInstState.FINISHED.toString(), "u01", true,true,"u02");
            verifyFinishedTask(TestTaskClassify.COOPERATION, "u02", 1,verifyEntity);
            verifyFinishedTask(TestTaskClassify.OWNNER, "u01", 1,verifyEntity);
            verifyUnfinishTask(TestTaskClassify.OWNNER, 0, "u01", null);
            verifyDelegateTask(TestTaskClassify.COOPERATION, "u01", 1, verifyEntity);
	}
	
	
}
