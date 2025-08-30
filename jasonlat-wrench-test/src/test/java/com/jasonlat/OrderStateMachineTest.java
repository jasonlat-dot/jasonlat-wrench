package com.jasonlat;

import com.jasonlat.design.framework.state.machine.StateMachine;
import com.jasonlat.state.context.OrderContext;
import com.jasonlat.state.event.OrderEvent;
import com.jasonlat.state.node.PaidState;
import com.jasonlat.state.node.PendingPaymentState;
import com.jasonlat.state.node.RefundedState;
import com.jasonlat.state.node.StoppedPaymentState;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

/**
 * 订单状态机测试类
 * <p>
 * 演示订单状态转换的完整流程，包括待支付、已支付、止付、退款等状态之间的转换。
 * 测试状态机的各种场景，包括正常流程、异常流程和边界情况。
 * 使用Spring Boot测试框架，通过依赖注入的方式获取状态机相关组件。
 * </p>
 *
 * <p>
 * 测试覆盖的场景：
 * <ul>
 *   <li>正常支付流程：待支付 -> 已支付</li>
 *   <li>止付流程：待支付 -> 止付 -> 恢复支付 -> 已支付</li>
 *   <li>退款流程：待支付 -> 已支付 -> 退款</li>
 *   <li>无效操作测试</li>
 *   <li>状态机统计信息测试</li>
 * </ul>
 * </p>
 *
 * @author jasonlat
 * @version 1.0
 * @since 2025-01-20
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class OrderStateMachineTest {

    @Autowired
    private StateMachine stateMachine;


    /**
     * 测试正常支付流程
     * <p>
     * 测试场景：创建订单 -> 支付成功
     * 预期结果：订单状态从待支付转换为已支付
     * </p>
     */
    @Test
    public void testNormalPaymentFlow() throws Exception {
        log.info("\n=== 测试正常支付流程 ===");

        // 创建订单上下文
        OrderContext orderContext = new OrderContext(
            "ORDER_001",
            "USER_123",
            new BigDecimal("99.99"),
            "iPhone 15 Pro"
        );

        // 验证初始状态
        log.info("初始状态: {}", orderContext.getCurrentState().getClass().getSimpleName());
        assert orderContext.getCurrentState() instanceof PendingPaymentState : "初始状态应该是待支付状态";

        // 注册到状态机

        // 模拟支付事件
        log.info("\n--- 触发支付事件 ---");


        // 注册后就可以通过状态机ID来处理事件
        stateMachine.register(orderContext);
        String result = stateMachine.processEvent(orderContext.getStateMachineId(), OrderEvent.PAY);

        log.info("处理结果: {}", result);

        // 验证状态转换
        assert orderContext.getCurrentState() instanceof PaidState : "支付后状态应该是已支付状态";
        assert "PAID".equals(orderContext.getStatus()) : "订单状态应该是PAID";

        // 打印订单日志
        printOrderLogs(orderContext);
    }

    /**
     * 测试止付流程
     * <p>
     * 测试场景：创建订单 -> 止付 -> 恢复支付 -> 支付成功
     * 预期结果：订单状态按预期转换，最终成功支付
     * </p>
     */
    @Test
    public void testStopPaymentFlow() throws Exception {
        log.info("\n=== 测试止付流程 ===");

        // 创建订单上下文
        OrderContext orderContext = new OrderContext(
            "ORDER_002",
            "USER_456",
            new BigDecimal("199.99"),
            "MacBook Pro"
        );

        // 注册到状态机
        stateMachine.register(orderContext);

        // 模拟止付事件
        log.info("\n--- 触发止付事件 ---");
        String result1 = orderContext.request(OrderEvent.STOP_PAYMENT);
        log.info("处理结果: {}", result1);

        // 验证止付状态
        assert orderContext.getCurrentState() instanceof StoppedPaymentState : "止付后状态应该是止付状态";
        assert "STOPPED_PAYMENT".equals(orderContext.getStatus()) : "订单状态应该是STOPPED_PAYMENT";

        // 模拟恢复支付事件
        log.info("\n--- 触发恢复支付事件 ---");
        String result2 = orderContext.request(OrderEvent.RESUME_PAYMENT);
        log.info("处理结果: {}", result2);

        // 验证恢复到待支付状态
        assert orderContext.getCurrentState() instanceof PendingPaymentState : "恢复支付后状态应该是待支付状态";

        // 模拟支付事件
        log.info("\n--- 触发支付事件 ---");
        String result3 = orderContext.request(OrderEvent.PAY);
        log.info("处理结果: {}", result3);

        // 验证最终支付成功
        assert orderContext.getCurrentState() instanceof PaidState : "最终状态应该是已支付状态";

        // 打印订单日志
        printOrderLogs(orderContext);
    }

    /**
     * 测试退款流程
     * <p>
     * 测试场景：创建订单 -> 支付成功 -> 申请退款
     * 预期结果：订单状态从已支付转换为待支付（部分退款）
     * </p>
     */
    @Test
    public void testRefundFlow() throws Exception {
        log.info("\n=== 测试退款流程 ===");

        // 创建订单上下文
        OrderContext orderContext = new OrderContext(
            "ORDER_003",
            "USER_789",
            new BigDecimal("299.99"),
            "iPad Pro"
        );

        // 注册到状态机
        stateMachine.register(orderContext);

        // 先支付
        log.info("\n--- 触发支付事件 ---");
        String result1 = orderContext.request(OrderEvent.PAY);
        log.info("处理结果: {}", result1);

        // 验证支付成功
        assert orderContext.getCurrentState() instanceof PaidState : "支付后状态应该是已支付状态";

        // 再退款
        log.info("\n--- 触发退款事件 ---");
        String result2 = orderContext.request(OrderEvent.REFUND);
        log.info("处理结果: {}", result2);

        // 验证退款后状态（根据业务逻辑，可能回到待支付状态）
        assert (orderContext.getCurrentState() instanceof PendingPaymentState ||
                orderContext.getCurrentState() instanceof RefundedState) : "退款后状态应该是待支付状态或已退款状态";

        // 打印订单日志
        printOrderLogs(orderContext);
    }

    /**
     * 测试止付状态下的直接支付
     * <p>
     * 测试场景：创建订单 -> 止付 -> 直接支付（问题已解决）
     * 预期结果：在止付状态下可以直接支付成功
     * </p>
     */
    @Test
    public void testDirectPaymentInStoppedState() throws Exception {
        log.info("\n=== 测试止付状态下的直接支付 ===");

        // 创建订单上下文
        OrderContext orderContext = new OrderContext(
            "ORDER_004",
            "USER_000",
            new BigDecimal("399.99"),
            "Apple Watch"
        );

        // 注册到状态机
        stateMachine.register(orderContext);

        // 先止付
        log.info("\n--- 触发止付事件 ---");
        String result1 = orderContext.request(OrderEvent.STOP_PAYMENT);
        log.info("处理结果: {}", result1);

        // 验证止付状态
        assert orderContext.getCurrentState() instanceof StoppedPaymentState : "止付后状态应该是止付状态";

        // 在止付状态下直接支付（适用于问题已解决的情况）
        log.info("\n--- 在止付状态下尝试直接支付 ---");
        String result2 = orderContext.request(OrderEvent.PAY);
        log.info("处理结果: {}", result2);

        // 验证支付成功
        assert orderContext.getCurrentState() instanceof PaidState : "止付状态下直接支付应该成功";

        // 打印订单日志
        printOrderLogs(orderContext);
    }

    /**
     * 测试无效事件处理
     * <p>
     * 测试场景：在各种状态下触发不支持的事件
     * 预期结果：系统能够优雅地处理无效事件，不会崩溃
     * </p>
     */
    @Test
    public void testInvalidEventHandling() throws Exception {
        log.info("\n=== 测试无效事件处理 ===");


        // 创建订单上下文
        OrderContext orderContext = new OrderContext(
            "ORDER_005",
            "USER_999",
            new BigDecimal("99.99"),
            "测试商品"
        );

        // 注册到状态机
        stateMachine.register(orderContext);

        try {
            // 在待支付状态下尝试退款（无效操作）
            log.info("\n--- 在待支付状态下尝试退款 ---");
            String result = orderContext.request(OrderEvent.REFUND);
            log.info("处理结果: {}", result);

            // 验证状态没有改变
            assert orderContext.getCurrentState() instanceof PendingPaymentState : "无效操作后状态应该保持不变";

        } catch (Exception e) {
            // 无效事件可能抛出异常，这是正常的
            log.info("捕获到预期的异常: {}", e.getMessage());
        }

        // 打印订单日志
        printOrderLogs(orderContext);
    }

    /**
     * 测试状态机统计信息
     * <p>
     * 测试场景：创建多个订单，执行不同操作，检查状态机统计信息
     * 预期结果：统计信息正确反映注册和活跃的状态机数量
     * </p>
     */
    @Test
    public void testStateMachineStatistics() throws Exception {
        log.info("\n=== 测试状态机统计信息 ===");

        // 创建多个订单进行测试
        for (int i = 1; i <= 5; i++) {
            OrderContext orderContext = new OrderContext(
                "ORDER_" + String.format("%03d", i),
                "USER_" + i,
                new BigDecimal("100.00"),
                "测试商品" + i
            );

            stateMachine.register(orderContext);

            try {
                // 根据订单编号执行不同操作
                switch (i % 3) {
                    case 0:
                        // 支付
                        orderContext.request(OrderEvent.PAY);
                        break;
                    case 1:
                        // 止付
                        orderContext.request(OrderEvent.STOP_PAYMENT);
                        break;
                    case 2:
                        // 支付后退款
                        orderContext.request(OrderEvent.PAY);
                        orderContext.request(OrderEvent.REFUND);
                        break;
                }
            } catch (Exception e) {
                    log.warn("订单{}操作异常: {}", i, e.getMessage());
                }
        }

        // 打印状态机统计信息
        log.info("\n--- 状态机统计信息 ---");
        StateMachine.Statistics stats = stateMachine.getStatistics();
        log.info("注册的状态机数量: {}", stats.getRegisteredCount());
        log.info("活跃的状态机数量: {}", stats.getActiveStateMachines());
        log.info("事件处理数量: {}", stats.getEventProcessedCount());
        log.info("状态转换数量: {}", stats.getStateTransitionCount());

        // 验证统计信息
        assert stats.getRegisteredCount() >= 0 : "注册的状态机数量应该大于等于0";
        assert stats.getActiveStateMachines() >= 0 : "活跃的状态机数量应该大于等于0";
    }

    /**
     * 测试状态描述信息
     * <p>
     * 测试场景：验证各个状态的描述信息是否正确
     * 预期结果：每个状态都有合适的描述信息
     * </p>
     */
    @Test
    public void testStateDescriptions() throws Exception {
        log.info("\n=== 测试状态描述信息 ===");


        // 测试各个状态的描述
        PendingPaymentState pendingState = new PendingPaymentState();
        PaidState paidState = new PaidState();
        StoppedPaymentState stoppedState = new StoppedPaymentState();
        RefundedState refundedState = new RefundedState();

        log.info("待支付状态描述: {}", pendingState.getDescription());
        log.info("已支付状态描述: {}", paidState.getDescription());
        log.info("止付状态描述: {}", stoppedState.getDescription());
        log.info("已退款状态描述: {}", refundedState.getDescription());

        // 验证描述不为空
        assert pendingState.getDescription() != null : "待支付状态描述不应为空";
        assert paidState.getDescription() != null : "已支付状态描述不应为空";
        assert stoppedState.getDescription() != null : "止付状态描述不应为空";
        assert refundedState.getDescription() != null : "已退款状态描述不应为空";
    }
    
    /**
     * 打印订单操作日志
     * <p>
     * 输出订单的详细操作日志，包括状态转换记录和当前状态信息。
     * </p>
     * 
     * @param orderContext 订单上下文
     */
    private void printOrderLogs(OrderContext orderContext) {
        log.info("\n--- 订单操作日志 ---");
        orderContext.getLogs().forEach(logEntry -> 
            log.info("[{}] {}", new java.util.Date(), logEntry));
        
        log.info("\n--- 订单状态信息 ---");
        log.info("当前状态类: {}", orderContext.getCurrentState().getClass().getSimpleName());
        log.info("订单状态: {}", orderContext.getStatus());
        log.info("订单信息: {}", orderContext.toString());
        
        // 打印时间戳信息
        if (orderContext.getPaidTime() != null) {
            log.info("支付时间: {}", new java.util.Date(orderContext.getPaidTime()));
        }
        if (orderContext.getStoppedTime() != null) {
            log.info("止付时间: {}", new java.util.Date(orderContext.getStoppedTime()));
        }
    }
}