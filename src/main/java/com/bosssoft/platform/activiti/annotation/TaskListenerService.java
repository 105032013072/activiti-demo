package com.bosssoft.platform.activiti.annotation;

import org.activiti.engine.spi.execution.core.ExecutionContext;
import org.activiti.engine.spi.execution.annotation.event.*;

@TaskListener(id="task-listener-1",description = "任务事件监听测试接口")
public interface TaskListenerService {
    @TaskEvent(value = TaskEventType.START, description = "监听开始事件")
    void start(ExecutionContext executionContext);

    @TaskEvent(value = TaskEventType.END, description = "监听事结束件")
    void end(ExecutionContext executionContext);

    @TaskEvent(value = TaskEventType.ACTIVATE,description = "监听激活事件")
    void activate(ExecutionContext executionContext);

    @TaskEvent(value = TaskEventType.ASSIGN,description = "监听用户任务设置办理人事件")
    void assign(ExecutionContext executionContext);

    @TaskEvent(value = TaskEventType.UNCLAIM,description = "监听任务拒绝领取事件")
    void unclaim(ExecutionContext executionContext);

    @TaskEvent(value = TaskEventType.CLAIM,description = "监听任务领取事件")
    void claim(ExecutionContext executionContext);

    @TaskEvent(value = TaskEventType.BACK,description = "监听回退事件")
    void back(ExecutionContext executionContext);

    @TaskEvent(value = TaskEventType.REDO,description = "监听重做事件")
    void redo(ExecutionContext executionContext);

    @TaskEvent(value = TaskEventType.WITHDRAW,description = "监听收回事件")
    void withdraw(ExecutionContext executionContext);

    @TaskEvent(value = TaskEventType.CONFIRM,description = "监听确认事件")
    void confirm(ExecutionContext executionContext);
}
