package com.bosssoft.platform.activiti;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.spi.notification.NotificationContext;
import org.activiti.engine.spi.notification.TimeEffectiveContext;
import org.activiti.engine.spi.notification.event.NotificationCategory;
import org.activiti.engine.spi.notification.event.NotificationEvent;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class NotificationUtil {
	
	public static  void  recordNotification(NotificationEvent notificationEvent)throws Exception{
		 String logFilePath=  Thread.currentThread().getContextClassLoader().getResource("notification.log").getPath();
	     File logFile=new File(logFilePath);
	     
	     if(!logFile.exists()){
	    	 throw new Exception("notification.log 不存在");
	     }
	     
	     String jsonStr=JSON.toJSONString(notificationEvent.getNotificationEventContext());
	     JSONObject  jsonObj=(JSONObject) JSON.parse(jsonStr);
	     jsonObj.put("notificationCategory", notificationEvent.getNotificationCategory());
	     jsonStr=jsonObj.toString();
	    
	     FileWriter fw=new FileWriter(logFile, true);
	     BufferedWriter  bw=new BufferedWriter(fw);
	     bw.write(jsonStr);
	     bw.newLine();
	     bw.close();
	     fw.close();
	 }
	
	public static List<NotificationEvent> getNotifications()throws Exception{
		 String logFilePath=  Thread.currentThread().getContextClassLoader().getResource("notification.log").getPath();
	     File logFile=new File(logFilePath);
	     
	     if(!logFile.exists()){
	    	 throw new Exception("notification.log 不存在");
	     }
	     
	     List<NotificationEvent> list=new ArrayList<NotificationEvent>();
		 String jsonStr=null;	
	     BufferedReader reader=new BufferedReader(new FileReader(logFile));
	     while((jsonStr=reader.readLine())!=null){
	    	 if(StringUtils.isNotEmpty(jsonStr)){
	    		 JSONObject  jsonObj=(JSONObject) JSON.parse(jsonStr);
	    		 
	    		 NotificationCategory category=NotificationCategory.valueOf(jsonObj.getString("notificationCategory"));
	    		 if(NotificationCategory.TASKNOTIFICATION.equals(category)){
	    			 NotificationContext notificationContext=JSON.parseObject(jsonStr, NotificationContext.class);
	    			 NotificationEvent event=new NotificationEvent(NotificationUtil.class, category, notificationContext);
	    			 list.add(event);
	    		 }else {
	    			 TimeEffectiveContext timeEffectiveContext=JSON.parseObject(jsonStr, TimeEffectiveContext.class);
	    			 NotificationEvent event=new NotificationEvent(NotificationUtil.class, category, timeEffectiveContext);
	    			 list.add(event);
	    		 }
	    	 }	
		 }

        return list;
	}
	
	public static  void cleanNotificationLog() throws Exception{
		String logFilePath=  Thread.currentThread().getContextClassLoader().getResource("notification.log").getPath();
	     File logFile=new File(logFilePath);
	     
	     if(!logFile.exists()){
	    	 throw new Exception("notification.log 不存在");
	     }
	     
	     FileWriter fileWriter =new FileWriter(logFile);
         fileWriter.write("");
         fileWriter.flush();
         fileWriter.close();
	}
	
}
