package com.jasonlat.state.factory;

import com.jasonlat.design.framework.state.AbstractState;
import com.jasonlat.design.framework.state.factory.StateFactory;
import com.jasonlat.design.framework.state.machine.StateMachineBuilder;
import com.jasonlat.design.framework.state.transition.StateTransition;
import com.jasonlat.state.context.OrderContext;
import com.jasonlat.state.event.OrderEvent;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 仅用于生成状态机Mermaid图的时候用到
 */
@Getter
@Service
public class OrderStatusMermaidDiagramFactory {

    /**
     * 状态机构建器
     */
    private final StateMachineBuilder<OrderContext, OrderEvent, String> builder;

    public OrderStatusMermaidDiagramFactory(Map<String, AbstractState<OrderContext, OrderEvent, String>> states) {
        String stateMachineIdPrefix = "order-status-machine-";
        this.builder = StateMachineBuilder.newBuilder(stateMachineIdPrefix + UUID.randomUUID());
        addStates(states);

        initializeStateMachine();
    }

    public StateFactory<OrderContext, OrderEvent, String> stateFactory() {
        return builder.getStateFactory();
    }

    public List<StateTransition<OrderContext, OrderEvent, String>> transitions() {
        return builder.getTransitions();
    }


    /**
     * 初始化状态机配置
     * <p>
     * 初始化状态机构建器，注册所有状态，配置状态转换规则，
     * 设置初始状态并启用状态转换日志记录。
     * </p>
     */
    private void initializeStateMachine() {
        System.out.println("\n=== 配置状态转换规则 ===");

        // 待支付状态的转换
        builder.addTransition("PENDING_PAYMENT", "PAID", OrderEvent.PAY)
                .addTransition("PENDING_PAYMENT", "STOPPED_PAYMENT", OrderEvent.STOP_PAYMENT)
                .addTransition("PENDING_PAYMENT", "CANCELLED", OrderEvent.CANCEL);

        // 已支付状态的转换
        builder.addTransition("PAID", "REFUNDED", OrderEvent.REFUND)
                .addTransition("PAID", "PENDING_PAYMENT", OrderEvent.REFUND); // 部分退款回到待支付

        // 止付状态的转换
        builder.addTransition("STOPPED_PAYMENT", "PENDING_PAYMENT", OrderEvent.RESUME_PAYMENT)
                .addTransition("STOPPED_PAYMENT", "PAID", OrderEvent.PAY)
                .addTransition("STOPPED_PAYMENT", "CANCELLED", OrderEvent.CANCEL);

        // 已退款状态的转换（终态）

        // 设置初始状态
        builder.initialState("PENDING_PAYMENT");

        // 启用状态转换日志
        builder.enableTransitionLogging(true);

        System.out.println("=== 订单状态机初始化完成 ===");
    }

    private void addStates(Map<String, AbstractState<OrderContext, OrderEvent, String>> states) {
        System.out.println("\n=== 开始注册订单状态 状态列表, size:" + states.size() + " ===");
        states.forEach((stateName, state) -> System.out.println(stateName));
        // 便利状态列表，注册所有状态
        states.forEach(builder::addState);
    }
}
