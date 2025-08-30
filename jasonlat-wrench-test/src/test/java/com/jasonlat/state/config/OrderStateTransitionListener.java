package com.jasonlat.state.config;

import com.jasonlat.design.framework.state.IStateContext;
import com.jasonlat.design.framework.state.machine.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderStateTransitionListener implements StateMachine.StateTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(OrderStateTransitionListener.class);

    @Override
    public void onStateMachineRegistered(String stateMachineId, IStateContext<?, ?, ?> context) {
        log.info("OrderStateTransitionListener 状态机已注册: [{}]", stateMachineId);
    }

    @Override
    public void onStateMachineUnregistered(String stateMachineId, IStateContext<?, ?, ?> context) {
        log.info("OrderStateTransitionListener 状态机已注销: [{}]", stateMachineId);
    }

    @Override
    public void onStateTransition(String stateMachineId, String fromState, String toState, Object event) {
        log.info("OrderStateTransitionListener 状态机已转换: [{}] --[{}]--> {}", stateMachineId, fromState, toState);
    }

    @Override
    public void onEventProcessingError(String stateMachineId, Object event, Exception exception) {
        log.error("OrderStateTransitionListener 状态机处理事件出错: {}", stateMachineId, exception);
    }

    @Override
    public void onAllStateMachinesCleared(int count) {
        log.info("OrderStateTransitionListener 状态机已全部清除: {}", count);
    }
}
