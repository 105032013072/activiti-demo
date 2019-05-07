package com.bosssoft.platform.activiti.test;

import java.io.InputStream;
import java.util.List;

import org.activiti.engine.BpmnxTool;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.consign.ConsignDetail;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.repository.Category;
import org.activiti.engine.repository.Model;
import org.activiti.engine.spi.identity.OMService;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bosssoft.platform.activiti.NotificationUtil;


public class MainTest {
	public static Logger logger = LoggerFactory.getLogger(MainTest.class);
	public ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

	public TaskService taskService = processEngine.getTaskService();
	public RuntimeService runtimeService = processEngine.getRuntimeService();
	public HistoryService histroyService = processEngine.getHistoryService();
	public RepositoryService repositoryService = processEngine.getRepositoryService();
	public IdentityService identityService = processEngine.getIdentityService();
	public ManagementService managementService = processEngine.getManagementService();
	public FormService formService=processEngine.getFormService();
	public BpmnxTool bpmnxTool=processEngine.getBpmnxTool();
	public OMService omservice;

	
	@Before
	public void setUp(){
		
		ProcessEngineConfigurationImpl processEngineConfiguration=(ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
		omservice=processEngineConfiguration.getSpiConfiguration().getOmService();
		
		
		
		//初始化流程类别
		repositoryService.saveCategory(null, "商务", null, false);
	}
	
	@After
	//清除数据
	public void tearDown() throws Exception{
	    //删除model
		List<Model> list=repositoryService.createModelQuery().list();
		for (Model model : list) {
			repositoryService.deleteModel(model.getId(), true);
		}
		
		//删除类别
		List<Category> caList=repositoryService.createCategoryQuery().list();
		for (Category category : caList) {
			repositoryService.deleteCategory(category.getId());
		}
	
		//删除代理关系
		List<ConsignDetail> consignDeflist=	repositoryService.createConsignDefineQuery().list();
		for (ConsignDetail consignDetail : consignDeflist) {
			repositoryService.deleteConsignDefinition(consignDetail.getConsignDefId());
		}
		
		NotificationUtil.cleanNotificationLog();
		
	
		
	}
	
	/**
	 * 创建model 并且部署
	 * @param classpathResource
	 * @throws Exception
	 */
	public Model createAndDeployModel(String classpathResource) throws Exception{
		String result=readAsString(classpathResource);
		return repositoryService.saveModelAndDeploy(null, result, getCategoryId());
	}
	
	public String readAsString(String classpathResource)throws Exception{
		InputStream  inputStream =this.getClass().getClassLoader().getResourceAsStream(classpathResource);
		/*String result = new BufferedReader(new InputStreamReader(inputStream))
		        .lines().collect(Collectors.joining(System.lineSeparator()));*/
		StringBuffer out = new StringBuffer();
	     byte[] b = new byte[4096];
	     for (int n; (n = inputStream.read(b)) != -1;) {
	          out.append(new String(b, 0, n));
	     }
		return  out.toString();
	}
	
	
	private String getCategoryId() throws Exception{
		List<Category> list=repositoryService.createCategoryQuery().categoryName("商务").list();
		if(list.isEmpty()){
			throw new Exception("无法找到类别Id");
		}
		
		return list.get(0).getId();
	}
	

   
	
}

