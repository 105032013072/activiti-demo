package com.bosssoft.platform.activiti.test.task;

import com.bosssoft.platform.activiti.test.MainTest;
import com.bosssoft.platform.activiti.test.TaskVerifyEntity;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.consign.ConsignInstState;
import org.activiti.engine.consign.ConsignInstType;
import org.activiti.engine.consign.ConsignTask;
import org.activiti.engine.impl.TaskExt;
import org.activiti.engine.task.ConsignInst;
import org.activiti.engine.task.Task;

public abstract class AbstractTaskTest extends MainTest{
	
	public String TASKCLASSIFY_OWNER="owner";
	
	
  /**
   * 验证没有代理的任务
   * @param userId
   * @param isFinished true 已办     false  待办
   */
	private void  verifyNoneDelegateDate(String userId,Boolean isFinished){
		List<TaskExt> list=new ArrayList<TaskExt>();
		if(isFinished){
			list=histroyService.createHistoricTaskExtQuery().completeDelegateTaskAssignee(userId).list();
	       
		}else{
		   list=taskService.createTaskExtQuery().delegateTaskAssigneeOrCandidate(userId).list();
	    }
       assertEquals(0, list.size());
   }
	
	/**
	 * 验证没有代办任务
	 * @param userId
	 * @param isFinished
	 */
   private  void  verifyNoneSubsitutionDate(String userId,Boolean isFinished){
		List<TaskExt> list=new ArrayList<TaskExt>();
		if(isFinished){
			
	       
		}else{
		   list=taskService.createTaskExtQuery().substitutionTaskAssignee(userId).list();
	    }
       assertEquals(0, list.size());
   }
   
   /**
    * 验证没有协办任务
    * @param userId
    * @param isFinished
    */
   private void verifyNoneCooperationDate(String userId,Boolean isFinished){
	   List<TaskExt> list=new ArrayList<TaskExt>();
		if(isFinished){
			list=histroyService.createHistoricTaskExtQuery().completeCooperationTaskAssignee(userId).list();
	       
		}else{
		   list=taskService.createTaskExtQuery().cooperationTaskAssignee(userId).list();
	    }
      assertEquals(0, list.size());
   }
   
   /**
    * 验证没有自己的任务
    * @param userId
    * @param isFinished
    */
   private void verifyNoneOwnDate(String userId,Boolean isFinished){
	   List<TaskExt> list=new ArrayList<TaskExt>();
		if(isFinished){
			list=histroyService.createHistoricTaskExtQuery().completeOwnerTaskAssignee(userId).list();
	       
		}else{
		   list=taskService.createTaskExtQuery().ownerTaskAssigneeOrCandidate(userId).list();
	    }
      assertEquals(0, list.size());
   }
   
   
   public void verifyUnfinishTask(TestTaskClassify taskClassify,int dataSize,String userId,TaskVerifyEntity... taskVerifyEntityArray){
	   List<TaskExt> list=new ArrayList<TaskExt>();
	  
	  if(TestTaskClassify.OWNNER.equals(taskClassify)){
		  list=taskService.createTaskExtQuery().ownerTaskAssigneeOrCandidate(userId).orderByTaskCreateTime().desc().list();
	  }else if(TestTaskClassify.DELEGATE.equals(taskClassify)){
		  list=taskService.createTaskExtQuery().delegateTaskAssigneeOrCandidate(userId).orderByTaskCreateTime().desc().list();
	  }else if(TestTaskClassify.SUBSTITUTION.equals(taskClassify)){
		  list=taskService.createTaskExtQuery().substitutionTaskAssignee(userId).orderByTaskCreateTime().desc().list();
	  }else if(TestTaskClassify.COOPERATION.equals(taskClassify)){
		  list=taskService.createTaskExtQuery().cooperationTaskAssignee(userId).orderByTaskCreateTime().desc().list();
	  }else if(TestTaskClassify.ALL.equals(taskClassify)){
		  list=taskService.createTaskExtQuery().taskAssigneeOrCandidate(userId).orderByTaskCreateTime().desc().list();
	  }
	  
	  verify(false,list, dataSize, userId, taskVerifyEntityArray);
   }
   
   private  void  verify(Boolean isFinished, List<TaskExt> list,int dataSize,String userId,TaskVerifyEntity... taskVerifyEntityArray){
	   assertEquals(dataSize, list.size());
		if(taskVerifyEntityArray!=null){
			for (TaskVerifyEntity taskVerifyEntity : taskVerifyEntityArray) {
				  TaskExt taskExt=findTaskByExpectDefKey(taskVerifyEntity.getExpextTaskDefKey(), list);
				  assertNotNull(taskExt);
				  
				  assertEquals(taskVerifyEntity.getExpectBusinessState(), taskExt.getBusinessState());
				  assertEquals(taskVerifyEntity.getExpectConsigState(), taskExt.getConsigState());
				  if(!isFinished){
					  assertEquals(taskVerifyEntity.getExpectIsArriveConsignLimit(), taskExt.isArriveConsignLimit());
					  //判断该记录是否可编辑办理
					 assertEquals(taskVerifyEntity.getEditable(), editable(taskExt,userId));
					  
				  }else{
					  assertEquals(taskVerifyEntity.getExpectHolder(), taskExt.getHolder());
				  }
				 
				  if(taskVerifyEntity.getExpectAssignee()==null){
					  assertNull(taskExt.getAssigneeId());
				  }else{
					  assertEquals(taskVerifyEntity.getExpectAssignee(), taskExt.getAssigneeId());
				  }
			} 
		}  
	     
   }
   
   private Boolean editable(TaskExt taskExt,String userId){
	   if(taskExt.getAssigneeId()!=null){
		   return taskExt.getAssigneeId().equals(userId);
	   }else{
		   if(taskExt.getHolder()==null) return true;
		   else return false;
	   }
	   
   }
   
   
	private TaskExt findTaskByExpectDefKey(String taskdefKey, List<TaskExt> list) {
		TaskExt result = null;
		for (TaskExt taskExt : list) {
			if (taskExt.getTaskDefKey().equals(taskdefKey)) {
				result = taskExt;
				break;
			}
		}
		return result;
	}
	
	private ConsignTask findConsignTaskByExpectDefKey(String taskdefKey, List<ConsignTask> list){
		ConsignTask result=null;
		for (ConsignTask consignTask : list) {
			if (consignTask.getTaskDefKey().equals(taskdefKey)) {
				result = consignTask;
				break;
			}
		}
		return result;
	}
   
   public void verifyFinishedTask(TestTaskClassify taskClassify,String userId,int dataSize,TaskVerifyEntity... taskVerifyEntityArray){
	   List<TaskExt> list=new ArrayList<TaskExt>();
		  
		  if(TestTaskClassify.OWNNER.equals(taskClassify)){
			  list=histroyService.createHistoricTaskExtQuery().completeOwnerTaskAssignee(userId).orderByTaskCreateTime().desc().list();
		  }else if(TestTaskClassify.DELEGATE.equals(taskClassify)){
			  list=histroyService.createHistoricTaskExtQuery().completeDelegateTaskAssignee(userId).orderByTaskCreateTime().desc().list();
		  }else if(TestTaskClassify.SUBSTITUTION.equals(taskClassify)){
			  list=histroyService.createHistoricTaskExtQuery().completeSubstitutionTaskAssignee(userId).orderByTaskCreateTime().desc().list();
		  }else if(TestTaskClassify.COOPERATION.equals(taskClassify)){
			  list=histroyService.createHistoricTaskExtQuery().completeCooperationTaskAssignee(userId).orderByTaskCreateTime().desc().list();
		  }else if(TestTaskClassify.ALL.equals(taskClassify)){
			  list=histroyService.createHistoricTaskExtQuery().completetaskAssignee(userId).orderByTaskCreateTime().desc().list();
		  }
		  
		  verify(true,list, dataSize, userId, taskVerifyEntityArray);
   }
   
   public void verifyDelegateTask(TestTaskClassify taskClassify,String userId,int dataSize,TaskVerifyEntity... taskVerifyEntityArray){
	   List<ConsignTask> list=new ArrayList<ConsignTask>();
	   
	    if(TestTaskClassify.DELEGATE.equals(taskClassify)){
			  list=histroyService.createHistoricConsignTaskQuery().delegate().consignor(userId).orderByTaskCreateTime().desc().list();
		  }else if(TestTaskClassify.SUBSTITUTION.equals(taskClassify)){
			  list=histroyService.createHistoricConsignTaskQuery().substitution().consignor(userId).orderByTaskCreateTime().desc().list();
		  }else if(TestTaskClassify.COOPERATION.equals(taskClassify)){
			  list=histroyService.createHistoricConsignTaskQuery().cooperation().consignor(userId).orderByTaskCreateTime().desc().list();
		  }else if(TestTaskClassify.ALL.equals(taskClassify)){
			  list=histroyService.createHistoricConsignTaskQuery().consignor(userId).orderByTaskCreateTime().desc().list();
		  }
	    
	    assertEquals(dataSize, list.size());
		if(taskVerifyEntityArray!=null){
			for (TaskVerifyEntity taskVerifyEntity : taskVerifyEntityArray) {
				  ConsignTask consignTask=findConsignTaskByExpectDefKey(taskVerifyEntity.getExpextTaskDefKey(), list);
				  assertNotNull(consignTask);
				  
				  assertEquals(taskVerifyEntity.getExpectBusinessState(), consignTask.getBusinessState());
				  assertEquals(taskVerifyEntity.getExpectConsigState(), consignTask.getConsigState());
				  if(taskVerifyEntity.getExpectAssignee()==null){
					  assertNull(consignTask.getAssigneeId());
				  }else{
					  assertEquals(taskVerifyEntity.getExpectAssignee(), consignTask.getAssigneeId());
				  }
				  
				  assertEquals(taskVerifyEntity.getExpectHolder(), consignTask.getHolder());
			} 
		}  
	    
   }
   
   
   
   public String getConsignInstIdForComplete(String taskId){
	   String consignInstId=null;
	   Task task=taskService.createTaskQuery().taskId(taskId).singleResult();
	   String assignee=task.getAssignee();
	   List<ConsignInst> list= taskService.createConsignInstQuery().taskId(taskId).assignee(assignee).list();
	   if(list!=null && list.isEmpty()==false){
		   List<ConsignInst> result=new ArrayList<ConsignInst>();
			for (ConsignInst consignInst : list) {
				if(!isParent(consignInst,list)) result.add(consignInst);
			}
		  
		   consignInstId=  result.get(0).getId();
	   }
	   return consignInstId;
   }
	

     private boolean isParent(ConsignInst targetConsignInst, List<ConsignInst> list) {
		for (ConsignInst consignInst : list) {
			if(targetConsignInst.getId().equals(consignInst.getConsignSourceId())) return true;
		}
    	 
		return false;
	}

public String getConsignInstIdForReject(String taskId){
	   String consignInstId=null;
	   List<ConsignInst> list= taskService.createConsignInstQuery().taskId(taskId).consignInstState(ConsignInstState.UNRECEIVE).list();
	   if(list!=null && list.isEmpty()==false){
		   consignInstId=  list.get(0).getId();
	   }
	   return consignInstId;
   }
   
   public String getConsignInstIdForWithdraw(String taskId,String userId){
	   String consignInstId=null;
	   List<ConsignInst> list= taskService.createConsignInstQuery().taskId(taskId)
			           .consignor(userId)
			           .consignInstTypeIn(ConsignInstType.SUBSTITUTION,ConsignInstType.COOPERATION)
			           .consignInstStateIn(ConsignInstState.UNRECEIVE,ConsignInstState.UNFINISH)
			           //.consignInstState(consignInstState)
			           .list();
	   if(list!=null && list.isEmpty()==false){
		   consignInstId=  list.get(0).getId();
	   }
	   return consignInstId;
   }
   
   
   public String getConsignInstIdForRedo(String taskId){
	   String consignInstId=null;
	   List<ConsignInst> list= taskService.createConsignInstQuery().taskId(taskId)
			           .consignInstState(ConsignInstState.UNCONFIRM)
			           .list();
	   if(list!=null && list.isEmpty()==false){
		   consignInstId=  list.get(0).getId();
	   }
	   return consignInstId;
   }
   
   public String getConsignInstIdForClaim(String taskId,String userId){
	   String consignInstId=null;
	   List<ConsignInst> list= taskService.createConsignInstQuery().taskId(taskId)
			           .candidate(userId)
			           .list();
	   if(list!=null && list.isEmpty()==false){
		   consignInstId=  list.get(0).getId();
	   }
	   return consignInstId;
   }
   
   public String getConsignInstIdForUmClaim(String taskId,String userId){
	   String consignInstId=null;
	   List<ConsignInst> list= taskService.createConsignInstQuery().taskId(taskId)
			           .holder(userId)
			           .list();
	   if(list!=null && list.isEmpty()==false){
		   consignInstId=  list.get(0).getId();
	   }
	   return consignInstId;
   }
   
   public enum TestTaskClassify{
		  DELEGATE("delegate"),
		  SUBSTITUTION("substitution"),
		  COOPERATION("cooperation"),
	      OWNNER("ownner"),
	      ALL("all");
	      String value;
	      TestTaskClassify(String value){
	          this.value=value;
	      }
	      
	      public String getValue() {
	          return value;
	      }
	      
	      public void setValue(String value) {
	          this.value = value;
	      }
	  }
}
