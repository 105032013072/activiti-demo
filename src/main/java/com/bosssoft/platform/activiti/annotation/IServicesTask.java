package com.bosssoft.platform.activiti.annotation;

import org.activiti.engine.spi.execution.annotation.service.ServiceTask;
import org.activiti.engine.spi.execution.annotation.service.ServiceTaskMethod;

@ServiceTask(id="testService", description = "服务任务执行接口")
public interface IServicesTask {
    @ServiceTaskMethod
    void exe();
}
