package com.bosssoft.platform.activiti.test.processInstance;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import com.bosssoft.platform.activiti.test.MainTest;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.containsString;

public class AbsProcessInstanceTest extends MainTest{
	
	public void suspendProcess(String processInstanceId){
		runtimeService.suspendProcessInstanceById(processInstanceId);
		
		long suspendedCount= runtimeService.createProcessInstanceQuery().suspended().count();
		assertEquals(1, suspendedCount);
		
		long activeCount= runtimeService.createProcessInstanceQuery().active().count();
		assertEquals(0, activeCount);
		
	   ExecutionEntity pi=	(ExecutionEntity) runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
	   assertEquals(SuspensionState.SUSPENDED.getStateCode(), pi.getSuspensionState());
	}
	
	public void activeProcess(String processInstanceId){
        runtimeService.activateProcessInstanceById(processInstanceId);
		
		long suspendedCount= runtimeService.createProcessInstanceQuery().suspended().count();
		assertEquals(0, suspendedCount);
		
		long activeCount= runtimeService.createProcessInstanceQuery().active().count();
		assertEquals(1, activeCount);
		
	   ExecutionEntity pi=	(ExecutionEntity) runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
	   assertEquals(SuspensionState.ACTIVE.getStateCode(), pi.getSuspensionState());
	}
	
	public void terminatProcess(String processInstanceId){
		runtimeService.terminateProcess(processInstanceId);
		
		long suspendedCount= runtimeService.createProcessInstanceQuery().termination().count();
		assertEquals(1, suspendedCount);
		
		long activeCount= runtimeService.createProcessInstanceQuery().active().count();
		assertEquals(0, activeCount);
		
		ExecutionEntity pi=	(ExecutionEntity) runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		assertEquals(SuspensionState.TERMINATION.getStateCode(), pi.getSuspensionState());
	}
	
	public void revokeProcess(String processInstanceId){
		runtimeService.revokeProcess(processInstanceId,"u01");
		
		long suspendedCount= runtimeService.createProcessInstanceQuery().termination().count();
		assertEquals(1, suspendedCount);
		
		long activeCount= runtimeService.createProcessInstanceQuery().active().count();
		assertEquals(0, activeCount);
		
		ExecutionEntity pi=	(ExecutionEntity) runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		assertEquals(SuspensionState.TERMINATION.getStateCode(), pi.getSuspensionState());
	}
	
	/**
	 * 完成任务
	 * @param taskId
	 * @param consignInstId
	 * @param enable
	 */
	public void verifyTaskComplete(String taskId,String consignInstId,Boolean enable){
		String errorString=null;
		try{
			taskService.completCascade(taskId, consignInstId);
		}catch(ActivitiException e){
			errorString=e.getMessage();
		}finally {
			if(enable){
				assertNull(errorString);
				HistoricTaskInstance historicTaskInstance=histroyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
			    assertNotNull(historicTaskInstance.getEndTime());
			}else{
				assertThat(errorString, containsString("Cannot complete a suspended task"));
			}
		}
	}
	
	/**
	 * 任务领取
	 * @param taskId
	 * @param consignInstId
	 * @param userId
	 * @param enable
	 */
	public void verifyTaskClaim(String taskId,String consignInstId,String userId,Boolean enable){
		String errorString=null;
		try{
			taskService.claimCascade(taskId, consignInstId, userId);
		}catch(ActivitiException e){
			errorString=e.getMessage();
		}finally {
			if(enable){
				assertNull(errorString);
			    Task task=	taskService.createTaskQuery().taskId(taskId).singleResult();
			    assertEquals(userId, task.getAssignee());
			}else{
				assertThat(errorString, containsString("Cannot execute operation: task is suspended"));
			}
		}
	}
	
	/**
	 * 撤销任务的领取
	 * @param taskId
	 * @param consignInstId
	 * @param enable
	 */
	public void verifyUnClaim(String taskId,String consignInstId,Boolean enable){
		String errorString=null;
		try{
			taskService.unclaimCascade(taskId, consignInstId);
		}catch(ActivitiException e){
			errorString=e.getMessage();
		}finally {
			if(enable){
				assertNull(errorString);
			    Task task=	taskService.createTaskQuery().taskId(taskId).singleResult();
			    assertNull(task.getAssignee());
			}else{
				assertThat(errorString, containsString("Cannot execute operation: task is suspended"));
			}
		}
	}
	
	/**
	 * 拒绝代理
	 * @param taskId
	 * @param consignInstId
	 * @param enable
	 */
	public void verifyRejectConsign(String taskId,String consignInstId,Boolean enable,String consignor){
		String errorString=null;
		try{
			taskService.rejectTask(consignInstId);;
		}catch(ActivitiException e){
			errorString=e.getMessage();
		}finally {
			if(enable){
				assertNull(errorString);
			    Task task=	taskService.createTaskQuery().taskId(taskId).singleResult();
			    assertEquals(consignor, task.getAssignee());
			}else{
				assertThat(errorString, containsString("Cannot execute operation: task is suspended"));
			}
		}
	}
	
	public void verifyRedo(String taskId,String consignInstId,Boolean enable,String consignee){
		String errorString=null;
		try{
			taskService.redo(consignInstId);
		}catch(ActivitiException e){
			errorString=e.getMessage();
		}finally {
			if(enable){
				assertNull(errorString);
			    Task task=	taskService.createTaskQuery().taskId(taskId).singleResult();
			    assertEquals(consignee, task.getAssignee());
			}else{
				assertThat(errorString, containsString("Cannot execute operation: task is suspended"));
			}
		}
	}
	
	/**
	 * 收回委托
	 * @param taskId
	 * @param consignInstId
	 * @param enable
	 * @param consignor
	 */
   public void verifyWithdrawConsign(String taskId,String consignInstId,Boolean enable,String consignor){
	   String errorString=null;
		try{
			taskService.withdrawConsign(consignInstId);
		}catch(ActivitiException e){
			errorString=e.getMessage();
		}finally {
			if(enable){
				assertNull(errorString);
			    Task task=	taskService.createTaskQuery().taskId(taskId).singleResult();
			    assertEquals(consignor, task.getAssignee());
			}else{
				assertThat(errorString, containsString("Cannot execute operation: task is suspended"));
			}
		}
   }
   
   /**
    * 退回到指定节点
    * @param currentTaskKey
    * @param expectActKey
    * @param enable
    */
   public void verifyBackToAct(String currentTaskKey, String expectActKey,Boolean enable){
	   String errorString=null;
	    Task currentTask=taskService.createTaskQuery().taskDefinitionKey(currentTaskKey).singleResult();
	   
	   try{
			taskService.backToActivity(currentTask.getId(), expectActKey, true);
		}catch(ActivitiException e){
			errorString=e.getMessage();
		}finally {
			if(enable){
				assertNull(errorString);
			    Task task=	taskService.createTaskQuery().executionId(currentTask.getExecutionId()).singleResult();
			    assertEquals(expectActKey, task.getTaskDefinitionKey());
			}else{
				assertThat(errorString, containsString("Cannot execute operation: task is suspended"));
			}
		}
   }
	
   /**
    * 退回上一步
    * @param currentTaskKey
    * @param expectActKey
    * @param enable
    */
   public void verifyBackToLast(String currentTaskKey, String expectActKey,Boolean enable){
	   String errorString=null;
	    Task currentTask=taskService.createTaskQuery().taskDefinitionKey(currentTaskKey).singleResult();
	   try{
			taskService.backToLast(currentTask.getId(), true);
		}catch(ActivitiException e){
			errorString=e.getMessage();
		}finally {
			if(enable){
				assertNull(errorString);
			    Task task=	taskService.createTaskQuery().executionId(currentTask.getExecutionId()).singleResult();
			    assertEquals(expectActKey, task.getTaskDefinitionKey());
			}else{
				assertThat(errorString, containsString("Cannot execute operation: task is suspended"));
			}
		}
   }
   
   /**
    * 
    * 退回重填
    * @param currentTaskKey
    * @param expectActKey
    * @param enable
    */
   public void verifyBackToReaplly(String currentTaskKey, String expectActKey,Boolean enable){
	   String errorString=null;
	    Task currentTask=taskService.createTaskQuery().taskDefinitionKey(currentTaskKey).singleResult();
	   try{
			taskService.backToReapply(currentTask.getId());
		}catch(ActivitiException e){
			errorString=e.getMessage();
		}finally {
			if(enable){
				assertNull(errorString);
			    Task task=	taskService.createTaskQuery().executionId(currentTask.getExecutionId()).singleResult();
			    assertEquals(expectActKey, task.getTaskDefinitionKey());
			}else{
				assertThat(errorString, containsString("Cannot execute operation: task is suspended"));
			}
		}
   }
   
   /**
    * 发起者回收任务
    * @param expectActKey
    * @param processInstId
    * @param enable
    */
   public void verifyWithdrawTask(String expectActKey,String processInstId, Boolean enable){
	   String errorString=null;
	   try{
			taskService.withdrawByStarter(processInstId);
		}catch(ActivitiException e){
			errorString=e.getMessage();
		}finally {
			if(enable){
				assertNull(errorString);
			    Task task=	taskService.createTaskQuery().processInstanceId(processInstId).singleResult();
			    assertEquals(expectActKey, task.getTaskDefinitionKey());
			}else{
				assertThat(errorString, containsString("Cannot execute operation: process is suspended"));
			}
		}
   }
   
   /**
    * 加签（前加签为例）
    * @param currentTaskKey
    * @param insertAssigneeId
    * @param enable
    */
   public void verifyInsert(String currentTaskKey,String insertAssigneeId,Boolean enable){
	   String errorString=null;
	   Task currentTask=taskService.createTaskQuery().taskDefinitionKey(currentTaskKey).singleResult();
	   try{
			taskService.insertBefore(currentTask.getId(), insertAssigneeId);
		}catch(ActivitiException e){
			errorString=e.getMessage();
		}finally {
			if(enable){
				assertNull(errorString);
			    Task task=	taskService.createTaskQuery().executionId(currentTask.getExecutionId()).singleResult();
			    assertEquals(insertAssigneeId, task.getAssignee());
			}else{
				assertThat(errorString, containsString("Cannot execute operation: task is suspended"));
			}
		}
   }
   
   
}
