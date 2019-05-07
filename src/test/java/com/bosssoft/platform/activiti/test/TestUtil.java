package com.bosssoft.platform.activiti.test;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.impl.TaskExt;
import org.activiti.engine.spi.identity.Participator;
import org.activiti.engine.spi.notification.event.NotificationEvent;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;

public class TestUtil {
	public static String[] getCandidates(String taskId,TaskService taskService){
		List<String> result=new ArrayList<String>();
		List<IdentityLink> list=	taskService.getIdentityLinksForTask(taskId);
		for (IdentityLink identityLink : list) {
			if(IdentityLinkType.CANDIDATE.equals(identityLink.getType())){
				if(StringUtils.isNoneEmpty(identityLink.getGroupId())){
					result.add(identityLink.getGroupId());
				}else{
					result.add(identityLink.getUserId());
				}
			}
		}
		String[] array=new String[result.size()];
		result.toArray(array);
		Arrays.sort(array);
		return array;
	}
	
	public static void checkReceiver(List<Participator> receiverList,String[] excepetReceiver){
	    String [] receiverArray=new String[receiverList.size()];
	    int i=0;
	    for (Participator participator : receiverList) {
			receiverArray[i++]=participator.getParticipatorId();
		}
	    
	    Arrays.sort(receiverArray);
	    Arrays.sort(excepetReceiver);
	    assertArrayEquals(excepetReceiver, receiverArray);
	}
	
	/**
	 * 获取受托人关于某个任务对应的委托实例ID
	 * @param taskId
	 * @param consigneeId
	 * @param taskService
	 * @return
	 */
	public static  String getConsignInstId(String taskId,String consigneeId,TaskService taskService){
		String consignInstId=null;
		List<TaskExt> list=	taskService.createTaskExtQuery().taskAssigneeOrCandidate(consigneeId).list();
		for (TaskExt taskExt : list) {
			if(taskExt.getTaskId().equals(taskId)){
				consignInstId=taskExt.getConsigInstId();
			}
		}
		return consignInstId;
	}
}
