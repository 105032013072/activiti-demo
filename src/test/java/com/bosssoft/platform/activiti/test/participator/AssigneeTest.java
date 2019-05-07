package com.bosssoft.platform.activiti.test.participator;

import com.bosssoft.platform.activiti.test.MainTest;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.identity.IdentityEnum;
import org.activiti.engine.spi.identity.Participator;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

/**
 * 任务办理人不同来源的测试
 * @author huangxw
 *
 */
public class AssigneeTest extends MainTest{
   
	/**
	 * 默认的办理人
	 * @throws Exception
	 */
	@Test
	public void  defaultAssigneeTest() throws Exception {
    Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/participator/process_assignee_default.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		Task task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "填写申请", task.getName());
		assertEquals("错误的任务办理人", "u02", task.getAssignee());
		
		taskService.completCascade(task.getId(), null);
		
		//进入第二个节点
		task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "上级审核", task.getName());
		assertEquals("错误的任务办理人", "u02", task.getAssignee());
		
		taskService.completCascade(task.getId(), null);
		
		//进入第三个节点
		task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "终审", task.getName());
		assertEquals("错误的任务办理人", "u02", task.getAssignee());
	}
	
	/**
	 * 办理人来源与流程发起者
	 * @throws Exception
	 */
	@Test
	public void  processStarterAssigneeTest() throws Exception {
    Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/participator/process_assignee_starter.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u05")
                .start();
		
		Task task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "填写申请", task.getName());
		assertEquals("错误的任务办理人", "u05", task.getAssignee());
		
		taskService.completCascade(task.getId(), null);
		
		//进入第二个节点
		task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "上级审核", task.getName());
		assertEquals("错误的任务办理人", "u05", task.getAssignee());
		
		taskService.completCascade(task.getId(), null);
		
		//进入第三个节点
		task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "终审", task.getName());
		assertEquals("错误的任务办理人", "u05", task.getAssignee());
	}
	
	/**
	 * 与指定任务办理人相同
	 * @throws Exception
	 */
	@Test
	public void  sameActAssigneeTest() throws Exception {
    Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/participator/process_assignee_sameAct.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u04")
                .start();
		
		//第一个节点办理人指定为u03
		Task task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "填写申请", task.getName());
		assertEquals("错误的任务办理人", "u03", task.getAssignee());
		
		taskService.completCascade(task.getId(), null);
		
		//进入第二个节点  办理人与第一个节点办理人相同
		task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "上级审核", task.getName());
		assertEquals("错误的任务办理人", "u03", task.getAssignee());
		
		taskService.completCascade(task.getId(), null);
		
		//进入第三个节点  办理人与第二个节点办理人相同
		task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "终审", task.getName());
		assertEquals("错误的任务办理人", "u03", task.getAssignee());
	}
	
	/**
	 * 来源于流程变量  以及自定义接口
	 * @throws Exception
	 */
	@Test
	public void  processVarAndCustomAssigneeTest() throws Exception {
    Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/participator/process_assignee_varAndCustom.bpmnx");

		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u04")
                .start();
		
		//第一个节点办理人来自于自定义接口
		Task task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "填写申请", task.getName());
		assertEquals("错误的任务办理人", "u01", task.getAssignee());
		
		Map<String,Object> vars=new HashedMap();
		vars.put("two", new Participator("1111", "111_name", IdentityEnum.USER, null));
		taskService.completCascade(task.getId(), null,vars);
		
		//进入第二个节点  来源流程变量
		 vars=new HashedMap();
		 
		vars.put("three", new Participator("222", "222_name", IdentityEnum.USER, null));
		task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "上级审核", task.getName());
		assertEquals("错误的任务办理人", "1111", task.getAssignee());
		
		taskService.completCascade(task.getId(), null,vars);
		
		//进入第三个节点  办理人与第二个节点办理人相同
		task=  taskService.createTaskQuery().processInstanceId(inst.getId()).singleResult();
		assertEquals("任务名称不匹配", "终审", task.getName());
		assertEquals("错误的任务办理人", "222", task.getAssignee());
	}
	
	
}
