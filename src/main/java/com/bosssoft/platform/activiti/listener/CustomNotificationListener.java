package com.bosssoft.platform.activiti.listener;

import org.activiti.engine.spi.notification.NotificationListener;
import org.activiti.engine.spi.notification.event.NotificationEvent;

import com.bosssoft.platform.activiti.NotificationUtil;

public class CustomNotificationListener implements NotificationListener{

	public void execute(NotificationEvent noticeEvent) {
		try {
			NotificationUtil.recordNotification(noticeEvent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
