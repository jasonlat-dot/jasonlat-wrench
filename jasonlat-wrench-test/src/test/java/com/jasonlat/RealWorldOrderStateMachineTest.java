package com.jasonlat;

import com.jasonlat.design.framework.state.machine.StateMachine;
import com.jasonlat.state.context.OrderContext;
import com.jasonlat.state.event.OrderEvent;
import com.jasonlat.state.node.PaidState;
import com.jasonlat.state.node.PendingPaymentState;
import com.jasonlat.state.node.RefundedState;
import com.jasonlat.state.node.StoppedPaymentState;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 真实业务场景订单状态机测试类
 * <p>
 * 模拟真实的业务场景：
 * 1. 订单创建请求 - 创建待支付订单，返回支付二维码
 * 2. 支付回调请求 - 独立的HTTP请求，通过订单ID获取状态机进行状态更新
 * 3. 查询订单状态请求 - 独立的HTTP请求，查询当前订单状态
 * 4. 退款请求 - 独立的HTTP请求，处理退款逻辑
 * </p>
 * 
 * @author jasonlat
 * @since 1.0.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class RealWorldOrderStateMachineTest {
    
    /**
     * 状态机实例
     */
    @Autowired
    private StateMachine stateMachine;
    
    /**
     * 模拟订单存储（实际业务中是数据库）
     */
    private final Map<String, OrderContext> orderStorage = new HashMap<>();
    

    
    /**
     * 测试后清理
     */
    @After
    public void tearDown() {
        log.info("\n=== 清理测试环境 ===");
        orderStorage.clear();
        log.info("测试环境清理完成");
    }
    
    /**
     * 模拟订单创建HTTP请求
     * <p>
     * 业务场景：用户下单，创建待支付订单，返回支付二维码
     * HTTP接口：POST /api/orders
     * </p>
     */
    @Test
    public void testCreateOrderRequest() throws Exception {
        log.info("\n=== 模拟订单创建HTTP请求 ===");
        
        // 模拟HTTP请求参数
        String userId = "USER_123";
        BigDecimal amount = new BigDecimal("99.99");
        String productName = "iPhone 15 Pro";
        
        // 业务逻辑：创建订单
        String orderId = createOrder(userId, amount, productName);
        
        // 验证订单创建结果
        OrderContext orderContext = getOrderFromStorage(orderId);
        assert orderContext != null : "订单应该创建成功";
        assert orderContext.getCurrentState() instanceof PendingPaymentState : "新订单应该是待支付状态";
        
        log.info("订单创建成功，订单ID: {}, 状态: {}", 
                orderId, orderContext.getCurrentState().getClass().getSimpleName());
        
        // 模拟返回给前端的响应
        String qrCode = generatePaymentQRCode(orderId);
        log.info("返回支付二维码: {}", qrCode);
        
        printOrderInfo(orderContext);
    }
    
    /**
     * 模拟支付回调HTTP请求
     * <p>
     * 业务场景：支付网关异步回调，通知订单支付成功
     * HTTP接口：POST /api/payment/callback
     * </p>
     */
    @Test
    public void testPaymentCallbackRequest() throws Exception {
        log.info("\n=== 模拟支付回调HTTP请求 ===");
        
        // 前置条件：先创建一个待支付订单
        String orderId = createOrder("USER_456", new BigDecimal("199.99"), "MacBook Pro");
        log.info("前置条件：订单 {} 已创建", orderId);
        
        // 模拟支付网关回调参数
        String paymentId = "PAY_" + System.currentTimeMillis();
        String callbackStatus = "SUCCESS";
        
        // 业务逻辑：处理支付回调
        boolean callbackResult = handlePaymentCallback(orderId, paymentId, callbackStatus);
        
        // 验证回调处理结果
        assert callbackResult : "支付回调应该处理成功";
        
        // 重新从存储中获取订单（模拟从数据库查询）
        OrderContext orderContext = getOrderFromStorage(orderId);
        assert orderContext.getCurrentState() instanceof PaidState : "回调后订单应该是已支付状态";
        assert orderContext.getPaidTime() != null : "支付时间应该被记录";
        
        log.info("支付回调处理成功，订单 {} 状态更新为: {}", 
                orderId, orderContext.getCurrentState().getClass().getSimpleName());
        
        printOrderInfo(orderContext);
    }
    
    /**
     * 模拟查询订单状态HTTP请求
     * <p>
     * 业务场景：前端轮询查询订单支付状态
     * HTTP接口：GET /api/orders/{orderId}/status
     * </p>
     */
    @Test
    public void testQueryOrderStatusRequest() throws Exception {
        log.info("\n=== 模拟查询订单状态HTTP请求 ===");
        
        // 前置条件：创建订单并完成支付
        String orderId = createOrder("USER_789", new BigDecimal("299.99"), "iPad Pro");
        handlePaymentCallback(orderId, "PAY_TEST", "SUCCESS");
        
        // 模拟前端查询请求
        OrderStatusResponse statusResponse = queryOrderStatus(orderId);
        
        // 验证查询结果
        assert statusResponse != null : "应该返回订单状态";
        assert "PAID".equals(statusResponse.getStatus()) : "订单状态应该是PAID";
        assert statusResponse.getPaidTime() != null : "应该返回支付时间";
        
        log.info("订单状态查询成功: {}", statusResponse);
    }
    
    /**
     * 模拟退款HTTP请求
     * <p>
     * 业务场景：用户申请退款，系统处理退款请求
     * HTTP接口：POST /api/orders/{orderId}/refund
     * </p>
     */
    @Test
    public void testRefundRequest() throws Exception {
        log.info("\n=== 模拟退款HTTP请求 ===");
        
        // 前置条件：创建已支付订单
        String orderId = createOrder("USER_999", new BigDecimal("399.99"), "Apple Watch");
        handlePaymentCallback(orderId, "PAY_REFUND_TEST", "SUCCESS");
        
        // 模拟退款请求参数
        String refundReason = "用户主动申请退款";
        BigDecimal refundAmount = new BigDecimal("399.99");
        
        // 业务逻辑：处理退款请求
        boolean refundResult = handleRefundRequest(orderId, refundAmount, refundReason);
        
        // 验证退款处理结果
        assert refundResult : "退款请求应该处理成功";
        
        // 重新查询订单状态
        OrderContext orderContext = getOrderFromStorage(orderId);
        assert orderContext.getCurrentState() instanceof RefundedState : "退款后订单应该是已退款状态, 当前状态：" + orderContext.getCurrentState().getClass().getSimpleName();
        
        log.info("退款处理成功，订单 {} 状态更新为: {}", 
                orderId, orderContext.getCurrentState().getClass().getSimpleName());
        
        printOrderInfo(orderContext);
    }
    
    /**
     * 模拟风控止付HTTP请求
     * <p>
     * 业务场景：风控系统检测到异常，触发订单止付
     * HTTP接口：POST /api/risk/stop-payment
     * </p>
     */
    @Test
    public void testRiskStopPaymentRequest() throws Exception {
        log.info("\n=== 模拟风控止付HTTP请求 ===");
        
        // 前置条件：创建待支付订单
        String orderId = createOrder("USER_RISK", new BigDecimal("9999.99"), "高风险商品");
        
        // 模拟风控系统参数
        String riskReason = "检测到异常支付行为";
        String riskLevel = "HIGH";
        
        // 业务逻辑：处理风控止付
        boolean stopResult = handleRiskStopPayment(orderId, riskReason, riskLevel);
        
        // 验证止付处理结果
        assert stopResult : "风控止付应该处理成功";
        
        // 重新查询订单状态
        OrderContext orderContext = getOrderFromStorage(orderId);
        assert orderContext.getCurrentState() instanceof StoppedPaymentState : "止付后订单应该是止付状态, 当前状态：" + orderContext.getCurrentState().getClass().getSimpleName();
        assert orderContext.getStoppedTime() != null : "止付时间应该被记录";
        
        log.info("风控止付处理成功，订单 {} 状态更新为: {}", 
                orderId, orderContext.getCurrentState().getClass().getSimpleName());
        
        printOrderInfo(orderContext);
    }
    
    /**
     * 模拟完整的订单生命周期
     * <p>
     * 业务场景：订单创建 -> 风控止付 -> 风险解除 -> 支付成功 -> 申请退款
     * </p>
     */
    @Test
    public void testCompleteOrderLifecycle() throws Exception {
        log.info("\n=== 模拟完整订单生命周期 ===");
        
        // 步骤1：创建订单
        String orderId = createOrder("USER_LIFECYCLE", new BigDecimal("599.99"), "完整流程测试商品");
        log.info("步骤1：订单创建完成，订单ID: {}", orderId);
        
        // 步骤2：风控止付
        handleRiskStopPayment(orderId, "初始风控检查", "MEDIUM");
        log.info("步骤2：风控止付完成");
        
        // 步骤3：风险解除
        handleRiskResumePayment(orderId, "风险解除，恢复支付");
        log.info("步骤3：风险解除完成");
        
        // 步骤4：支付成功
        handlePaymentCallback(orderId, "PAY_LIFECYCLE", "SUCCESS");
        log.info("步骤4：支付成功完成");
        
        // 步骤5：申请退款
        handleRefundRequest(orderId, new BigDecimal("599.99"), "完整流程测试退款");
        log.info("步骤5：退款处理完成");
        
        // 验证最终状态
        OrderContext orderContext = getOrderFromStorage(orderId);
        assert orderContext.getCurrentState() instanceof RefundedState : "最终状态应该是已退款, 当前状态：" + orderContext.getCurrentState().getClass().getSimpleName();;
        
        log.info("完整订单生命周期测试完成，最终状态: {}", 
                orderContext.getCurrentState().getClass().getSimpleName());
        
        printOrderInfo(orderContext);
    }
    
    // ==================== 业务方法模拟 ====================
    
    /**
     * 创建订单（模拟订单创建业务逻辑）
     * 
     * @param userId 用户ID
     * @param amount 订单金额
     * @param productName 商品名称
     * @return 订单ID
     */
    private String createOrder(String userId, BigDecimal amount, String productName) {
        // 生成订单ID
        String orderId = "ORDER_" + System.currentTimeMillis();
        
        // 创建订单上下文
        OrderContext orderContext = new OrderContext(orderId, userId, amount, productName);
        
        // 注册到状态机
        stateMachine.register(orderContext);

        // 保存到存储（模拟数据库保存）
        orderStorage.put(orderId, orderContext);
        
        orderContext.addLog("订单创建成功");
        
        return orderId;
    }
    
    /**
     * 处理支付回调（模拟支付回调业务逻辑）
     * 
     * @param orderId 订单ID
     * @param paymentId 支付ID
     * @param status 支付状态
     * @return 处理结果
     */
    private boolean handlePaymentCallback(String orderId, String paymentId, String status) {
        try {
            // 从存储中获取订单（模拟从数据库查询）
            OrderContext orderContext = getOrderFromStorage(orderId);
            if (orderContext == null) {
                log.error("订单不存在: {}", orderId);
                return false;
            }
            
            if ("SUCCESS".equals(status)) {
                // 触发支付成功事件
                String result = orderContext.request(OrderEvent.PAY);
                orderContext.setPaidTime(System.currentTimeMillis());
                orderContext.addLog("支付回调成功，支付ID: " + paymentId);
                
                // 更新存储（模拟数据库更新）
                orderStorage.put(orderId, orderContext);
                
                log.info("支付回调处理成功: {}", result);
                return true;
            } else {
                orderContext.addLog("支付回调失败，支付ID: " + paymentId + ", 状态: " + status);
                log.warn("支付回调失败，订单: {}, 状态: {}", orderId, status);
                return false;
            }
        } catch (Exception e) {
            log.error("支付回调处理异常，订单: {}", orderId, e);
            return false;
        }
    }
    
    /**
     * 查询订单状态（模拟订单状态查询业务逻辑）
     * 
     * @param orderId 订单ID
     * @return 订单状态响应
     */
    private OrderStatusResponse queryOrderStatus(String orderId) {
        OrderContext orderContext = getOrderFromStorage(orderId);
        if (orderContext == null) {
            return null;
        }
        
        return new OrderStatusResponse(
                orderId,
                orderContext.getStatus(),
                orderContext.getCurrentState().getClass().getSimpleName(),
                orderContext.getPaidTime(),
                orderContext.getStoppedTime()
        );
    }
    
    /**
     * 处理退款请求（模拟退款业务逻辑）
     * 
     * @param orderId 订单ID
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 处理结果
     */
    private boolean handleRefundRequest(String orderId, BigDecimal refundAmount, String refundReason) {
        try {
            OrderContext orderContext = getOrderFromStorage(orderId);
            if (orderContext == null) {
                log.error("订单不存在: {}", orderId);
                return false;
            }
            
            // 触发退款事件
            String result = orderContext.request(OrderEvent.REFUND);
            orderContext.addLog("退款申请成功，原因: " + refundReason + ", 金额: " + refundAmount);
            
            // 更新存储
            orderStorage.put(orderId, orderContext);
            
            log.info("退款处理成功: {}", result);
            return true;
        } catch (Exception e) {
            log.error("退款处理异常，订单: {}", orderId, e);
            return false;
        }
    }
    
    /**
     * 处理风控止付（模拟风控止付业务逻辑）
     * 
     * @param orderId 订单ID
     * @param riskReason 风险原因
     * @param riskLevel 风险等级
     * @return 处理结果
     */
    private boolean handleRiskStopPayment(String orderId, String riskReason, String riskLevel) {
        try {
            OrderContext orderContext = getOrderFromStorage(orderId);
            if (orderContext == null) {
                log.error("订单不存在: {}", orderId);
                return false;
            }
            
            // 触发止付事件
            String result = orderContext.request(OrderEvent.STOP_PAYMENT);
            orderContext.setStoppedTime(System.currentTimeMillis());
            orderContext.addLog("风控止付，原因: " + riskReason + ", 等级: " + riskLevel);
            
            // 更新存储
            orderStorage.put(orderId, orderContext);
            
            log.info("风控止付处理成功: {}", result);
            return true;
        } catch (Exception e) {
            log.error("风控止付处理异常，订单: {}", orderId, e);
            return false;
        }
    }
    
    /**
     * 处理风险解除（模拟风险解除业务逻辑）
     *
     * @param orderId      订单ID
     * @param resumeReason 解除原因
     */
    private void handleRiskResumePayment(String orderId, String resumeReason) {
        try {
            OrderContext orderContext = getOrderFromStorage(orderId);
            if (orderContext == null) {
                log.error("订单不存在: {}", orderId);
                return;
            }
            
            // 触发恢复支付事件
            String result = orderContext.request(OrderEvent.RESUME_PAYMENT);
            orderContext.addLog("风险解除，恢复支付: " + resumeReason);
            
            // 更新存储
            orderStorage.put(orderId, orderContext);
            
            log.info("风险解除处理成功: {}", result);
        } catch (Exception e) {
            log.error("风险解除处理异常，订单: {}", orderId, e);
        }
    }
    
    /**
     * 从存储中获取订单（模拟从数据库查询）
     * 
     * @param orderId 订单ID
     * @return 订单上下文
     */
    private OrderContext getOrderFromStorage(String orderId) {
        return orderStorage.get(orderId);
    }
    
    /**
     * 生成支付二维码（模拟支付二维码生成）
     * 
     * @param orderId 订单ID
     * @return 二维码内容
     */
    private String generatePaymentQRCode(String orderId) {
        return "https://pay.example.com/qr?orderId=" + orderId + "&timestamp=" + System.currentTimeMillis();
    }
    
    /**
     * 打印订单信息
     * 
     * @param orderContext 订单上下文
     */
    private void printOrderInfo(OrderContext orderContext) {
        log.info("\n--- 订单信息 ---");
        log.info("订单ID: {}", orderContext.getOrderId());
        log.info("当前状态: {}", orderContext.getCurrentState().getClass().getSimpleName());
        log.info("订单状态: {}", orderContext.getStatus());
        log.info("用户ID: {}", orderContext.getUserId());
        log.info("订单金额: {}", orderContext.getAmount());
        log.info("商品名称: {}", orderContext.getProductInfo());
        
        if (orderContext.getPaidTime() != null) {
            log.info("支付时间: {}", new java.util.Date(orderContext.getPaidTime()));
        }
        if (orderContext.getStoppedTime() != null) {
            log.info("止付时间: {}", new java.util.Date(orderContext.getStoppedTime()));
        }
        
        log.info("操作日志:");
        orderContext.getLogs().forEach(logEntry -> log.info("  - {}", logEntry));
    }
    
    /**
     * 订单状态响应类
     */
    public static class OrderStatusResponse {
        private String orderId;
        private String status;
        private String stateName;
        private Long paidTime;
        private Long stoppedTime;
        
        public OrderStatusResponse(String orderId, String status, String stateName, Long paidTime, Long stoppedTime) {
            this.orderId = orderId;
            this.status = status;
            this.stateName = stateName;
            this.paidTime = paidTime;
            this.stoppedTime = stoppedTime;
        }
        
        // Getters
        public String getOrderId() { return orderId; }
        public String getStatus() { return status; }
        public String getStateName() { return stateName; }
        public Long getPaidTime() { return paidTime; }
        public Long getStoppedTime() { return stoppedTime; }
        
        @Override
        public String toString() {
            return String.format("OrderStatusResponse{orderId='%s', status='%s', stateName='%s', paidTime=%s, stoppedTime=%s}",
                    orderId, status, stateName, paidTime, stoppedTime);
        }
    }
}