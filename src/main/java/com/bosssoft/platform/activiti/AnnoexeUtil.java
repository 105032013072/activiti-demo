package com.bosssoft.platform.activiti;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class AnnoexeUtil {
	public static  void  recordAnnoexe(Object eventEntity,String activitiEventType)throws Exception{
		 String logFilePath=  Thread.currentThread().getContextClassLoader().getResource("annoexe.log").getPath();
	     File logFile=new File(logFilePath);
	     
	     if(!logFile.exists()){
	    	 throw new Exception("notification.log 不存在");
	     }
	     
	     String jsonStr=JSON.toJSONString(eventEntity);
	     JSONObject  jsonObj=(JSONObject) JSON.parse(jsonStr);
	     if(activitiEventType!=null){
	    	 jsonObj.put("eventType", activitiEventType);
	     }
	     jsonStr=jsonObj.toString();
	    
	     FileWriter fw=new FileWriter(logFile, true);
	     BufferedWriter  bw=new BufferedWriter(fw);
	     bw.write(jsonStr);
	     bw.newLine();
	     bw.close();
	     fw.close();
	 }
	
	public static Map<String,List<Object>> getAnnoexes()throws Exception{
		 String logFilePath=  Thread.currentThread().getContextClassLoader().getResource("annoexe.log").getPath();
	     File logFile=new File(logFilePath);
	     
	     if(!logFile.exists()){
	    	 throw new Exception("annoexe.log 不存在");
	     }
	     
	     Map<String,List<Object>> map=new HashMap<String, List<Object>>();
	     List<Object> list=new ArrayList<Object>();
		 String jsonStr=null;	
	     BufferedReader reader=new BufferedReader(new FileReader(logFile));
	     while((jsonStr=reader.readLine())!=null){
	    	 if(StringUtils.isNotEmpty(jsonStr)){
	    		 JSONObject  jsonObj=(JSONObject) JSON.parse(jsonStr);
	    		 String eventType=jsonObj.get("eventType").toString();
	    		 jsonObj.remove("eventType");
	    		 List<Object> executionList=new ArrayList<Object>();
	    		 if(map.get(eventType)==null){
	    			 map.put(eventType, executionList);
	    		 }
	    	     executionList=map.get(eventType);
	    		 executionList.add(JSON.parseObject(jsonStr, Object.class));
	    		
	    	 }	
		 }
       return map;
	}
	
	public static  void cleanAnnoexeLog() throws Exception{
		String logFilePath=  Thread.currentThread().getContextClassLoader().getResource("annoexe.log").getPath();
	     File logFile=new File(logFilePath);
	     
	     if(!logFile.exists()){
	    	 throw new Exception("annoexe.log 不存在");
	     }
	     
	     FileWriter fileWriter =new FileWriter(logFile);
         fileWriter.write("");
         fileWriter.flush();
         fileWriter.close();
	}
}
