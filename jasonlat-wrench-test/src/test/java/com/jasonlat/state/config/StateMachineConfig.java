package com.jasonlat.state.config;

import com.jasonlat.design.framework.state.machine.StateMachine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class StateMachineConfig {
    @Bean(name = "stateMachine")
    public StateMachine stateMachine(List<StateMachine.StateTransitionListener> listeners) {
        StateMachine instance = StateMachine.getInstance();
        listeners.forEach(instance::addStateTransitionListener);
        return instance;
    }

}
