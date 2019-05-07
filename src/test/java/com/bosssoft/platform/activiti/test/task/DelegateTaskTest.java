package com.bosssoft.platform.activiti.test.task;

import java.util.List;
import static org.junit.Assert.*;
import org.activiti.engine.consign.ConsignDefType;
import org.activiti.engine.consign.ConsignInstState;
import org.activiti.engine.impl.TaskExt;
import org.activiti.engine.impl.task.TaskBusinessState;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.identity.IdentityEnum;
import org.activiti.engine.spi.identity.Participator;
import org.activiti.engine.task.ConsignInst;
import org.junit.Test;

import com.bosssoft.platform.activiti.test.TaskVerifyEntity;

public class DelegateTaskTest extends AbstractTaskTest{

	@Test
	public void sourceCommon() throws Exception{
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_delegate_common.bpmnx");

		
		repositoryService.createProcessConsignDefinition()
        .consignor("u01")
        .addConsignee(new Participator("p02", "商务", IdentityEnum.POSITION, null))
        .addConsignee(new Participator("u04", "张三", IdentityEnum.USER, null))
        .consignDefType(ConsignDefType.ALL)
        .startTime("2018-06-11")
    	.endTime("2019-9-22")
    	.save();
		
		repositoryService.createProcessConsignDefinition()
        .consignor("u06")
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
		
		TaskVerifyEntity verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.NORMAL.toString(), null, null, false,true);
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u01", verifyEntity);
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u06", verifyEntity);
		
		verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNCLAIM.toString(), null, false,true);
		verifyDelegateTask(TestTaskClassify.ALL, "u01", 1, verifyEntity);
	
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u04", verifyEntity);
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u05", verifyEntity);
		
		
		//领取B，C任务
		TaskExt btask=taskService.createTaskExtQuery().delegateTaskAssigneeOrCandidate("u04").singleResult();
		taskService.claimCascade(btask.getTaskId(), btask.getConsigInstId(), "u04");
		TaskExt ctask=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u06").singleResult();
		taskService.claimCascade(ctask.getTaskId(), ctask.getConsigInstId(), "u06");
		
		verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNFINISH.toString(), "u04", false,true,"u04");
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u01", null);
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u06", null);
		verifyDelegateTask(TestTaskClassify.ALL, "u06", 1, verifyEntity);
		
		TaskVerifyEntity verifyEntity2=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNFINISH.toString(), "u04", false,true,"u04");
		verifyDelegateTask(TestTaskClassify.ALL, "u01", 1, verifyEntity2);
	
		verifyUnfinishTask(TestTaskClassify.ALL, 2, "u04", verifyEntity2,verifyEntity);
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u05", null);
		
		
		//完成任务
		List<TaskExt> list=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u04").list();
		assertEquals(2, list.size());
		for (TaskExt taskExt : list) {
			taskService.completCascade(taskExt.getTaskId(), taskExt.getConsigInstId());
		}
		
		//已经完成的任务
		verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.DELEGATE.toString(), ConsignInstState.FINISHED.toString(), "u04", false,true,"u04");
		verifyEntity2=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.FINISHED.toString(), "u04", false,true,"u04");
		verifyFinishedTask(TestTaskClassify.ALL, "u04", 2, verifyEntity,verifyEntity2);
		
		verifyDelegateTask(TestTaskClassify.DELEGATE, "u01", 1, verifyEntity2);
		verifyDelegateTask(TestTaskClassify.DELEGATE, "u06", 1, verifyEntity);
		
		//D 节点的待办任务
		TaskExt  dtask=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u06").singleResult();
		taskService.claimCascade(dtask.getTaskId(), dtask.getConsigInstId(), "u06");
		
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u01", null);
		
		verifyEntity=new TaskVerifyEntity("D", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNFINISH.toString(), "u04", false,true,"u04");
		verifyEntity2=new TaskVerifyEntity("C", TaskBusinessState.DELEGATE.toString(), ConsignInstState.FINISHED.toString(), "u04", false,true,"u04");
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u04", verifyEntity);
		verifyDelegateTask(TestTaskClassify.DELEGATE, "u06", 2, verifyEntity,verifyEntity2);
		
		//u06 撤销任务的领取
		taskService.unclaimCascade(dtask.getTaskId(), null);
		verifyUnfinishTask(TestTaskClassify.ALL, 0, "u04", null);
		verifyDelegateTask(TestTaskClassify.DELEGATE, "u06", 1, verifyEntity2);
		
		verifyEntity=new TaskVerifyEntity("D", TaskBusinessState.NORMAL.toString(), null,null, false,true);
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u01", verifyEntity);
		verifyUnfinishTask(TestTaskClassify.ALL, 1, "u06", verifyEntity);
	
	}
	
	@Test
	public void sourceDelegate() throws Exception{
       Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_delegate_sourceDelegate.bpmnx");

       createConsignDef();
		
	   ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
	   
	   TaskExt ctask=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u10").singleResult();
	   taskService.claimCascade(ctask.getTaskId(), ctask.getConsigInstId(), "u10");
	   
	   //待办任务查询
	   TaskVerifyEntity verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNFINISH.toString(), null, true, true,"u02");
	   verifyDelegateTask(TestTaskClassify.DELEGATE, "u01", 1, verifyEntity);
	   
	    verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNCLAIM.toString(), null, true, true);
	   verifyDelegateTask(TestTaskClassify.DELEGATE, "u02", 1, verifyEntity);
	  
	   verifyUnfinishTask(TestTaskClassify.DELEGATE, 1, "u03", verifyEntity);
	   verifyUnfinishTask(TestTaskClassify.DELEGATE, 1, "u04", verifyEntity);
	   verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNFINISH.toString(), null, true, false);
	   verifyUnfinishTask(TestTaskClassify.DELEGATE, 1, "u02", verifyEntity);

	   verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNFINISH.toString(), "u08", true, false,"u08");
	   verifyDelegateTask(TestTaskClassify.DELEGATE, "u10", 1, verifyEntity);
	   verifyUnfinishTask(TestTaskClassify.ALL, 1, "u10", verifyEntity);
	   verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNFINISH.toString(), "u08", true, true);
	   verifyUnfinishTask(TestTaskClassify.ALL, 1, "u08", verifyEntity);
	   
	   TaskExt btask=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u04").singleResult();
	   taskService.claimCascade(btask.getTaskId(), btask.getConsigInstId(), "u04");
	   ctask=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u08").singleResult();
	   
	   
	   taskService.completCascade(ctask.getTaskId(), ctask.getConsigInstId());
	   taskService.completCascade(btask.getTaskId(), btask.getConsigInstId());
	   
	   //已完成
	   verifyUnfinishTask(TestTaskClassify.ALL, 0, "u03", null);
	   verifyUnfinishTask(TestTaskClassify.ALL, 0, "u04", null);
	   verifyUnfinishTask(TestTaskClassify.ALL, 0, "u08", null);
	   verifyUnfinishTask(TestTaskClassify.ALL, 0, "u02", null);
	   
	   verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.FINISHED.toString(), "u04", true, false,"u02");
	   verifyFinishedTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
	   verifyDelegateTask(TestTaskClassify.ALL, "u01", 1, verifyEntity);
	   verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.FINISHED.toString(), "u04", true, false,"u04");
	   verifyFinishedTask(TestTaskClassify.ALL, "u04", 1, verifyEntity);
	   verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
	   
	   verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.DELEGATE.toString(), ConsignInstState.FINISHED.toString(), "u08", true, false,"u08");
	   TaskVerifyEntity  verifyEntity2=new TaskVerifyEntity("D", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNCLAIM.toString(), null, false, true);
	   verifyFinishedTask(TestTaskClassify.ALL, "u08", 1, verifyEntity);
	   verifyDelegateTask(TestTaskClassify.ALL, "u10", 1, verifyEntity);
	   
	   verifyEntity=new TaskVerifyEntity("C", TaskBusinessState.DELEGATE.toString(), ConsignInstState.FINISHED.toString(), "u08", true, false,"u10");
	   verifyFinishedTask(TestTaskClassify.ALL, "u10", 1, verifyEntity);
	   verifyDelegateTask(TestTaskClassify.ALL, "u03", 2, verifyEntity,verifyEntity2);
	   
	   
	   //D 节点的待办任务
	   TaskExt dtask=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u10").singleResult();
	   taskService.claimCascade(dtask.getTaskId(), dtask.getConsigInstId(), "u10");
	   
	   verifyEntity=new TaskVerifyEntity("D", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNFINISH.toString(), "u08", true, false,"u08");
	    verifyEntity2=new TaskVerifyEntity("C", TaskBusinessState.DELEGATE.toString(), ConsignInstState.FINISHED.toString(), "u08", true, false,"u08");
	   verifyDelegateTask(TestTaskClassify.DELEGATE, "u10", 2, verifyEntity,verifyEntity2);
	   
	   //u10撤销领取
	   taskService.unclaimCascade(dtask.getTaskId(), dtask.getConsigInstId());
	   verifyUnfinishTask(TestTaskClassify.ALL, 0, "u08", null);
	   verifyDelegateTask(TestTaskClassify.ALL, "u10", 1, verifyEntity2);
	   
	   verifyEntity=new TaskVerifyEntity("D", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNCLAIM.toString(), null, false, true);
	   verifyUnfinishTask(TestTaskClassify.ALL, 1, "u09", verifyEntity);
	   verifyUnfinishTask(TestTaskClassify.ALL, 1, "u10", verifyEntity);
	}
	
	
	private  void createConsignDef(){
		repositoryService.createProcessConsignDefinition()
        .consignor("u01")
        .addConsignee(new Participator("u02", "小雨", IdentityEnum.USER, null))
        .consignDefType(ConsignDefType.ALL)
        .startTime("2018-06-11")
    	.endTime("2019-9-22")
    	.save();
		
		
		repositoryService.createProcessConsignDefinition()
        .consignor("u02")
        .addConsignee(new Participator("u03", "小王", IdentityEnum.USER, null))
        .addConsignee(new Participator("u04", "张三", IdentityEnum.USER, null))
        .consignDefType(ConsignDefType.ALL)
        .startTime("2018-06-11")
    	.endTime("2019-9-22")
    	.save();
		
		repositoryService.createProcessConsignDefinition()
        .consignor("u10")
        .addConsignee(new Participator("u08", "小红", IdentityEnum.USER, null))
        .consignDefType(ConsignDefType.ALL)
        .startTime("2018-06-11")
    	.endTime("2019-9-22")
    	.save();
		
		repositoryService.createProcessConsignDefinition()
        .consignor("u03")
        .addConsignee(new Participator("u10", "小雪", IdentityEnum.USER, null))
        .addConsignee(new Participator("p04", "测试", IdentityEnum.POSITION, null))
        .consignDefType(ConsignDefType.ALL)
        .startTime("2018-06-11")
    	.endTime("2019-9-22")
    	.save();
	}
	
	@Test
	public void sourceSubsitution() throws Exception{
       Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_sourceSubsitution.bpmnx");

		repositoryService.createProcessConsignDefinition()
        .consignor("u03")
        .addConsignee(new Participator("r01", "部门经理", IdentityEnum.ROLE, null))
        .addConsignee(new Participator("u01", "小明", IdentityEnum.USER, null))
        .consignDefType(ConsignDefType.ALL)
        .startTime("2018-06-11")
    	.endTime("2019-9-22")
    	.save();
		
		
		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		List<TaskExt> list=taskService.createTaskExtQuery().ownerTaskAssigneeOrCandidate("u02").list();
		assertEquals(2, list.size());

		String cConsignInstStr=null;
		String bconsignInstStr=null;
		String ctaskId=null;
		String btaskId=null;
		for (TaskExt taskExt : list) {
		 String instStr=taskService.createTaskSubstitutionInst(taskExt.getAssigneeId(),"u03", IdentityEnum.USER, taskExt.getTaskId(), null);
			if("B".equals(taskExt.getTaskDefKey())){
				bconsignInstStr=instStr;
				btaskId=taskExt.getTaskId();
			}else{
				cConsignInstStr=instStr;
				ctaskId=taskExt.getTaskId();
			}
		}
		//接受代办任务
		taskService.claimCascade(btaskId, bconsignInstStr, "u03");
		taskService.claimCascade(ctaskId, cConsignInstStr, "u03");
		
		TaskVerifyEntity verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), null, true, true,"u03");
		TaskVerifyEntity verifyEntity2=new TaskVerifyEntity("C", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), null, true, true,"u03");
		verifyDelegateTask(TestTaskClassify.SUBSTITUTION, "u02", 2, verifyEntity,verifyEntity2);
		
		 verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), null, true, false);
		 verifyEntity2=new TaskVerifyEntity("C", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), null, true, false);
		verifyUnfinishTask(TestTaskClassify.SUBSTITUTION, 2, "u03", verifyEntity,verifyEntity2);
		
		verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNCLAIM.toString(), null, true, true,null);
		 verifyEntity2=new TaskVerifyEntity("C", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNCLAIM.toString(), null, true, true,null);
		verifyDelegateTask(TestTaskClassify.ALL, "u03", 2, verifyEntity,verifyEntity2);
		verifyUnfinishTask(TestTaskClassify.ALL, 2,"u01", verifyEntity,verifyEntity2);
		verifyUnfinishTask(TestTaskClassify.ALL, 2,"u06", verifyEntity,verifyEntity2);
		
		//C节点撤销任务领取
		taskService.unclaimCascade(ctaskId, cConsignInstStr);
		
		//待办
		verifyUnfinishTask(TestTaskClassify.ALL, 1,"u01", verifyEntity);
		verifyUnfinishTask(TestTaskClassify.ALL, 1,"u06", verifyEntity);
		
		 verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNFINISH.toString(), null, true, false);
		 verifyEntity2=new TaskVerifyEntity("C", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.UNRECEIVE.toString(), null, false, true);
		 verifyUnfinishTask(TestTaskClassify.ALL, 2, "u03", verifyEntity,verifyEntity2);
		
		 verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNCLAIM.toString(), null, true, false,null);
		 verifyDelegateTask(TestTaskClassify.ALL, "u03", 1, verifyEntity);
		
		//B 办理任务
		 TaskExt atask=taskService.createTaskExtQuery().delegateTaskAssigneeOrCandidate("u01").singleResult();
		 taskService.claimCascade(atask.getTaskId(), atask.getConsigInstId(), "u01");
		 taskService.completCascade(atask.getTaskId(), atask.getConsigInstId());
		
		 
		 //已办任务
		  verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.FINISHED.toString(), "u01", true, true,"u03");
		  verifyDelegateTask(TestTaskClassify.ALL, "u02", 2, verifyEntity,verifyEntity2);
		  
		  verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.FINISHED.toString(), "u01", true, true,"u01");
		  verifyDelegateTask(TestTaskClassify.DELEGATE, "u03", 1, verifyEntity);
		  verifyFinishedTask(TestTaskClassify.ALL, "u01", 1, verifyEntity);
		  
		  verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.SUBSTITUTION.toString(), ConsignInstState.FINISHED.toString(), "u01", true, true,"u03");
		  verifyFinishedTask(TestTaskClassify.SUBSTITUTION, "u03", 1, verifyEntity);
	}
	
	
	@Test
	public void sourceCooperation() throws Exception{
       Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_sourceSubsitution.bpmnx");

		repositoryService.createProcessConsignDefinition()
        .consignor("u03")
        .addConsignee(new Participator("r01", "部门经理", IdentityEnum.ROLE, null))
        .addConsignee(new Participator("u01", "小明", IdentityEnum.USER, null))
        .consignDefType(ConsignDefType.ALL)
        .startTime("2018-06-11")
    	.endTime("2019-9-22")
    	.save();
		
		
		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		List<TaskExt> list=taskService.createTaskExtQuery().ownerTaskAssigneeOrCandidate("u02").list();
		assertEquals(2, list.size());

		String cConsignInstStr=null;
		String bconsignInstStr=null;
		String ctaskId=null;
		String btaskId=null;
		for (TaskExt taskExt : list) {
		 String instStr=taskService.createTaskCooperationInst(taskExt.getAssigneeId(),"u03", IdentityEnum.USER, taskExt.getTaskId(), null);
			if("B".equals(taskExt.getTaskDefKey())){
				bconsignInstStr=instStr;
				btaskId=taskExt.getTaskId();
			}else{
				cConsignInstStr=instStr;
				ctaskId=taskExt.getTaskId();
			}
		}
		//接受协办办任务
		taskService.claimCascade(btaskId, bconsignInstStr, "u03");
		taskService.claimCascade(ctaskId, cConsignInstStr, "u03");
		
		TaskVerifyEntity verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNFINISH.toString(), null, true, true,"u03");
		TaskVerifyEntity verifyEntity2=new TaskVerifyEntity("C", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNFINISH.toString(), null, true, true,"u03");
		verifyDelegateTask(TestTaskClassify.COOPERATION, "u02", 2, verifyEntity,verifyEntity2);
		
		 verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNFINISH.toString(), null, true, false);
		 verifyEntity2=new TaskVerifyEntity("C", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNFINISH.toString(), null, true, false);
		verifyUnfinishTask(TestTaskClassify.COOPERATION, 2, "u03", verifyEntity,verifyEntity2);
		
		verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNCLAIM.toString(), null, true, true,null);
		 verifyEntity2=new TaskVerifyEntity("C", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNCLAIM.toString(), null, true, true,null);
		verifyDelegateTask(TestTaskClassify.ALL, "u03", 2, verifyEntity,verifyEntity2);
		verifyUnfinishTask(TestTaskClassify.ALL, 2,"u01", verifyEntity,verifyEntity2);
		verifyUnfinishTask(TestTaskClassify.ALL, 2,"u06", verifyEntity,verifyEntity2);
		
		//C节点撤销协办任务领取
		taskService.unclaimCascade(ctaskId, cConsignInstStr);
		
		//待办
		verifyUnfinishTask(TestTaskClassify.ALL, 1,"u01", verifyEntity);
		verifyUnfinishTask(TestTaskClassify.ALL, 1,"u06", verifyEntity);
		
		 verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNFINISH.toString(), null, true, false);
		 verifyEntity2=new TaskVerifyEntity("C", TaskBusinessState.COOPERATION.toString(), ConsignInstState.UNRECEIVE.toString(), null, false, true);
		 verifyUnfinishTask(TestTaskClassify.ALL, 2, "u03", verifyEntity,verifyEntity2);
		
		 verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNCLAIM.toString(), null, true, false,null);
		 verifyDelegateTask(TestTaskClassify.ALL, "u03", 1, verifyEntity);
		
		//B 办理任务
		 TaskExt atask=taskService.createTaskExtQuery().delegateTaskAssigneeOrCandidate("u01").singleResult();
		 taskService.claimCascade(atask.getTaskId(), atask.getConsigInstId(), "u01");
		 taskService.completCascade(atask.getTaskId(), atask.getConsigInstId());
		
		 
		 //已办任务
		  verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.UNCONFIRM.toString(), ConsignInstState.UNCONFIRM.toString(), "u02", true, true,"u03");
		  verifyDelegateTask(TestTaskClassify.ALL, "u02", 2, verifyEntity,verifyEntity2);
		  
		  verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.FINISHED.toString(), "u02", true, true,"u01");
		  verifyDelegateTask(TestTaskClassify.DELEGATE, "u03", 1, verifyEntity);
		  verifyFinishedTask(TestTaskClassify.ALL, "u01", 1, verifyEntity);
		  
		  verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.UNCONFIRM.toString(), ConsignInstState.UNCONFIRM.toString(), "u02", true, true,"u03");
		  verifyFinishedTask(TestTaskClassify.COOPERATION, "u03", 1, verifyEntity);
		  
		  verifyUnfinishTask(TestTaskClassify.OWNNER, 1, "u02", verifyEntity);
		  
		  //确认协办任务
		  TaskExt task=taskService.createTaskExtQuery().taskAssigneeOrCandidate("u02").singleResult();
		  taskService.completCascade(task.getTaskId(), task.getConsigInstId());
		  
		  verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.NORMAL.toString(), ConsignInstState.FINISHED.toString(), "u02", true, true,"u03");
		  verifyDelegateTask(TestTaskClassify.COOPERATION, "u02", 2, verifyEntity,verifyEntity2);
		 
		  verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.NORMAL.toString(), ConsignInstState.FINISHED.toString(), "u02", true, true,"u03");
		  verifyFinishedTask(TestTaskClassify.OWNNER, "u02", 1, verifyEntity);
		  
		  verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.NORMAL.toString(), ConsignInstState.FINISHED.toString(), "u02", true, true,"u03");
		  verifyFinishedTask(TestTaskClassify.COOPERATION, "u03", 1, verifyEntity);
		  
	}
	
	@Test
	public void  sourceCooperationRedo() throws Exception{
		 Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/task/process_sourceCooperationRedo.bpmnx");

			repositoryService.createProcessConsignDefinition()
	        .consignor("u03")
	        .addConsignee(new Participator("r01", "部门经理", IdentityEnum.ROLE, null))
	        .addConsignee(new Participator("u01", "小明", IdentityEnum.USER, null))
	        .consignDefType(ConsignDefType.ALL)
	        .startTime("2018-06-11")
	    	.endTime("2019-9-22")
	    	.save();
			
			
			ProcessInstance inst= runtimeService
	                .createProcessInstanceBuilder()
	                .processDefinitionId(model.getProcessDefinitionId())
	                .processStarter("u03")
	                .start();
			
			//创建协办
			TaskExt task=taskService.createTaskExtQuery().ownerTaskAssigneeOrCandidate("u02").singleResult();
			String instStr=taskService.createTaskCooperationInst(task.getAssigneeId(),"u03", IdentityEnum.USER, task.getTaskId(), null);
			
			//接受协办办任务
			taskService.claimCascade(task.getTaskId(), instStr, "u03");
			
			//办理代理任务
			task=taskService.createTaskExtQuery().delegateTaskAssigneeOrCandidate("u01").singleResult();
			taskService.claimCascade(task.getTaskId(), task.getConsigInstId(), "u01");
			taskService.completCascade(task.getTaskId(), task.getConsigInstId());
			
			TaskVerifyEntity verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.FINISHED.toString(), "u02", true, true,"u01");
			verifyFinishedTask(TestTaskClassify.DELEGATE, "u01", 1, verifyEntity);
			verifyDelegateTask(TestTaskClassify.DELEGATE, "u03", 1, verifyEntity);
			
			verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.UNCONFIRM.toString(), ConsignInstState.UNCONFIRM.toString(), "u02", true, true,"u03");
			verifyFinishedTask(TestTaskClassify.COOPERATION, "u03", 1, verifyEntity);
			verifyDelegateTask(TestTaskClassify.COOPERATION, "u02", 1, verifyEntity);
			
			verifyUnfinishTask(TestTaskClassify.OWNNER, 1, "u02", verifyEntity);
			
			//u02 打回重做
			task=taskService.createTaskExtQuery().ownerTaskAssigneeOrCandidate("u02").singleResult();
		    assertNotNull(task);
		    taskService.redo(task.getConsigInstId());
		    
		    verifyUnfinishTask(TestTaskClassify.ALL, 0, "u02", null);
		    
		    verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.DELEGATE.toString(), ConsignInstState.UNFINISH.toString(), "u01", true, true,"u01");
			verifyFinishedTask(TestTaskClassify.ALL, "u01", 0, null);
			verifyUnfinishTask(TestTaskClassify.DELEGATE, 1, "u01", verifyEntity);
			verifyDelegateTask(TestTaskClassify.DELEGATE, "u03", 1, verifyEntity);
			
			verifyFinishedTask(TestTaskClassify.COOPERATION, "u03", 0, null);
			
			verifyEntity=new TaskVerifyEntity("B", TaskBusinessState.REDO.toString(), ConsignInstState.REDO.toString(), "u01", true, false,"u03");
			verifyUnfinishTask(TestTaskClassify.ALL, 1, "u03", verifyEntity);
			verifyDelegateTask(TestTaskClassify.ALL, "u02", 1, verifyEntity);
	}
	
	
	
}
