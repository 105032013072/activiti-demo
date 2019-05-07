package com.bosssoft.platform.activiti.test.processstarter;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.identity.IdentityEnum;
import org.activiti.engine.spi.identity.Participator;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.containsString;


import com.bosssoft.platform.activiti.test.MainTest;

/**
 * 流程发起者测试
 * @author huangxw
 *
 */
public class ProcessStarterTest extends MainTest{

	//任意人员均可发起
	@Test
	public void testProcessStarterForArbitrary() throws Exception {
		Model model = super.createAndDeployModel("com/bosssoft/platform/activiti/test/processstarter/processStarter_arbitrary.bpmnx");
		verifyArbitrary(model);
	}
	
	
	private void verifyArbitrary(Model model){
		// 任意用户均可查看到该流程实例
				List<Participator> participatorList = new ArrayList<Participator>();
				participatorList.add(new Participator("u01", "小明", IdentityEnum.USER, null));
				participatorList.add(new Participator("u06", "王五", IdentityEnum.USER, null));
				participatorList.add(new Participator("u10", "小雪", IdentityEnum.USER, null));

				for (Participator participator : participatorList) {
				  ProcessDefinition processDefinition=	repositoryService
						                                .createProcessDefinitionQuery()
						                                .allowStartByUser(participator.getParticipatorId())
						                                .singleResult();
				  
				   assertEquals("用户："+participator.getParticipatorId()+" 无法查看到流程"+model.getProcessDefinitionId(), 
						   model.getProcessDefinitionId(), processDefinition.getId());
				
				}
				
				
				//任意用户均可启动流程实例
				for (Participator participator : participatorList) {
				   ProcessInstance inst= runtimeService
					                   .createProcessInstanceBuilder()
					                   .processDefinitionId(model.getProcessDefinitionId())
					                   .processStarter(participator.getParticipatorId())
					                   .start();
				
				   assertNotNull(participator.getParticipatorId()+"无法启动流程", inst);
				}
	}
	
	
	
	
	//指定人员启动
	@Test
	public void testProcessStarterForDesignated() throws Exception{
		//限制u01 和r01 允许发起流程
		Model model = super.createAndDeployModel("com/bosssoft/platform/activiti/test/processstarter/processStarter_designated.bpmnx");
		verifyDesignated(model);
	}
	
	private void verifyDesignated(Model model){
		//指定用户可以查看到流程
				List<Participator> participatorList = new ArrayList<Participator>();
				participatorList.add(new Participator("u01", "小明", IdentityEnum.USER, null));
				participatorList.add(new Participator("u06", "王五", IdentityEnum.USER, null));
				participatorList.add(new Participator("u10", "小雪", IdentityEnum.USER, null));
				participatorList.add(new Participator("u04", "张三", IdentityEnum.USER, null));
				
				for (Participator participator : participatorList) {
					  ProcessDefinition processDefinition=	repositoryService
							                                .createProcessDefinitionQuery()
							                                .allowStartByUser(participator.getParticipatorId())
							                                .singleResult();
					  
					  if("u10".equals(participator.getParticipatorId())||"u04".equals(participator.getParticipatorId())){
						  //无法查看到流程
						  assertNull("用户："+participator.getParticipatorId()+"没有被授权但是 可以查看到流程"+model.getProcessDefinitionId(), processDefinition);
					  }else{
						  //可以查看到流程
						  assertEquals("用户："+participator.getParticipatorId()+" 无法查看到流程"+model.getProcessDefinitionId(), 
								   model.getProcessDefinitionId(), processDefinition.getId());
					  }

				}
				
				//指定用户才可以发起流程
				for (Participator participator : participatorList) {
					ProcessInstance inst=null;
					try{
						inst= runtimeService
				                   .createProcessInstanceBuilder()
				                   .processDefinitionId(model.getProcessDefinitionId())
				                   .processStarter(participator.getParticipatorId())
				                   .start();
					
					}catch(ActivitiException e){
						if("u10".equals(participator.getParticipatorId())||"u04".equals(participator.getParticipatorId())){
							 //验证异常信息
							assertThat(e.getMessage(), containsString(participator.getParticipatorId()+" has no authorization to start processes"));
						}
					}finally {
						if("u10".equals(participator.getParticipatorId())||"u04".equals(participator.getParticipatorId())){
							  //无法发起
							assertNull("用户："+participator.getParticipatorId()+"没有被授权但是 可以启动流程"+model.getProcessDefinitionId(), inst);
						  }else{
							  //可以发起流程
							  assertNotNull(participator.getParticipatorId()+"无法启动流程", inst);
						  }
					}

				}
	}
	

	//测试发起者权限的修改
	@Test
	public void testProcessStarterForEdite() throws Exception{
		//任意人员启动
		Model model = super.createAndDeployModel("com/bosssoft/platform/activiti/test/processstarter/processStarter_arbitrary.bpmnx");
		verifyArbitrary(model);
		
		//修改为指定人员启动，强制更新
		String updateStr=super.readAsString("com/bosssoft/platform/activiti/test/processstarter/processStarter_designated.bpmnx");
		repositoryService.saveModelAndUpdateProcessDefinition(model.getId(), updateStr);
		verifyDesignated(model);
	}
	
	
}
