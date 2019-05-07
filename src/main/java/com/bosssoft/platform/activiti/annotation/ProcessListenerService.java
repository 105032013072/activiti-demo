package com.bosssoft.platform.activiti.annotation;

import org.activiti.engine.spi.execution.annotation.event.ProcessEvent;
import org.activiti.engine.spi.execution.annotation.event.ProcessEventType;
import org.activiti.engine.spi.execution.annotation.event.ProcessListener;
import org.activiti.engine.spi.execution.core.ExecutionContext;

@ProcessListener(id="process-listener-1",description = "流程事件监听")
public interface ProcessListenerService {

    @ProcessEvent(value = ProcessEventType.START,description = "测试流程开始方法")
    void start(ExecutionContext executionContext);

    @ProcessEvent(value = ProcessEventType.SUSPEND)
    void supspend(ExecutionContext executionContext);

    @ProcessEvent(value = ProcessEventType.ACTIVATE)
    void activate(ExecutionContext executionContext);

    @ProcessEvent(value = ProcessEventType.END, description = "测试流程结束监听")
    void end(ExecutionContext executionContext);

}
