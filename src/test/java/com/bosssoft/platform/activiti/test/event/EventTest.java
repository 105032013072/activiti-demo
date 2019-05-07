package com.bosssoft.platform.activiti.test.event;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.spi.execution.core.ExecutionConfiguration;
import org.junit.Test;

import com.bosssoft.platform.activiti.AnnoexeUtil;
import com.bosssoft.platform.activiti.test.MainTest;

/**
 * 事件触发测试
 * @author huangxw
 *
 */
public class EventTest extends MainTest{
   
	
	private void initCache() throws IOException{
		ExecutionConfiguration executionConfiguration=new ExecutionConfiguration();
		executionConfiguration.getExecutionCache();
	}
	
	
	
	@Test
	public void test() throws Exception{
		initCache();
		
		Model model=super.createAndDeployModel("com/bosssoft/platform/activiti/test/event/process_event.bpmnx");
	 
		ProcessInstance inst= runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(model.getProcessDefinitionId())
                .processStarter("u03")
                .start();
		
		Map<String, List<Object>> map=AnnoexeUtil.getAnnoexes();
		for (Entry<String, List<Object>> entry : map.entrySet()) {
			System.out.println(entry.getKey());
			for (Object object : entry.getValue()) {
				System.out.println(object);
			}
		}
		
		
	}
}
