package com.bosssoft.platform.activiti;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.spi.identity.IdentityEnum;
import org.activiti.engine.spi.identity.Participator;

public class CustomParticipatorService implements Serializable{
	 
	private static final long serialVersionUID = 1L;
	
   public List<Participator> getApplicantForCandidate(){
	   List<Participator> list=new ArrayList<Participator>();
	   Participator applicant=new Participator("u03", "小王", IdentityEnum.USER, null);
	   Participator u4=new Participator("u04", "张三", IdentityEnum.USER, null);
	   list.add(applicant);
	   list.add(u4);
	   return list;
   }
   
   public Participator getApplicantForAssignee(){

	   Participator applicant=new Participator("u01", "小王", IdentityEnum.USER, null);
	   return applicant;
   }

   public Participator getTwo(){
	   Participator u4=new Participator("u04", "张三", IdentityEnum.USER, null);
	   return u4;
   }
   
   public Participator getThree(){
	   Participator u5=new Participator("u05", "李四", IdentityEnum.USER, null);
	   return u5;
   }
}
