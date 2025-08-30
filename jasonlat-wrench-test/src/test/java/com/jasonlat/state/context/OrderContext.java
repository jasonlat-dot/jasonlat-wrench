package com.jasonlat.state.context;

import com.jasonlat.design.framework.state.AbstractStateContext;
import com.jasonlat.design.framework.state.IState;
import com.jasonlat.state.node.PendingPaymentState;
import com.jasonlat.state.event.OrderEvent;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单上下文类
 * 继承AbstractStateContext，简化了状态上下文的实现
 * 只需要关注业务逻辑，状态管理由父类处理
 * 
 * @author jasonlat
 * @version 1.0
 * @since 2024-01-20
 */
@Getter
@Setter
public class OrderContext extends AbstractStateContext<OrderContext, OrderEvent, String> {
    
    /**
     * 订单ID
     */
    private final String orderId;
    
    /**
     * 订单状态
     */
    private String status;
    
    /**
     * 订单金额
     */
    private final BigDecimal amount;
    
    /**
     * 用户ID
     */
    private final String userId;
    
    /**
     * 商品信息
     */
    private final String productInfo;
    
    /**
     * 创建时间
     */
    private final long createTime;
    
    /**
     * 支付时间
     */
    private Long paidTime;
    
    /**
     * 止付时间
     */
    private Long stoppedTime;
    
    /**
     * 操作日志列表
     */
    private final List<String> logs;
    
    /**
     * 扩展属性
     */
    private final Map<String, Object> attributes;
    
    /**
     * 构造函数
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param amount 订单金额
     * @param productInfo 商品信息
     */
    public OrderContext(String orderId, String userId, BigDecimal amount, String productInfo) {
        super("ORDER_" + orderId); // 调用父类构造函数
        
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.productInfo = productInfo;
        this.createTime = System.currentTimeMillis();
        this.status = "CREATED";
        this.logs = new ArrayList<>();
        this.attributes = new ConcurrentHashMap<>();

        // 记录订单创建日志
        addLog(String.format("订单创建 - 用户:%s, 金额:%s, 商品:%s", userId, amount, productInfo));
        
        // 设置初始状态为待支付
        try {
            setInitialState(new PendingPaymentState());

        } catch (Exception e) {
            throw new RuntimeException("初始化订单状态失败", e);
        }
    }
    
    /**
     * 添加操作日志
     * 
     * @param message 日志消息
     */
    public void addLog(String message) {
        String logEntry = String.format("[%d] %s", System.currentTimeMillis(), message);
        logs.add(logEntry);
        System.out.printf("订单[%s] %s%n", orderId, logEntry);
    }
    
    /**
     * 设置扩展属性
     * 
     * @param key 属性键
     * @param value 属性值
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    /**
     * 获取扩展属性
     * 
     * @param key 属性键
     * @return 属性值
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    /**
     * 获取操作日志列表
     * 
     * @return 操作日志列表
     */
    public List<String> getLogs() {
        return new ArrayList<>(logs);
    }
    
    /**
     * 获取所有扩展属性
     * 
     * @return 扩展属性映射
     */
    public Map<String, Object> getAttributes() {
        return new ConcurrentHashMap<>(attributes);
    }
    
    /**
     * 创建初始状态
     * AbstractStateContext要求实现的方法
     * 
     * @return 初始状态实例
     */
    @Override
    protected IState<OrderContext, OrderEvent, String> createInitialState() {
        return new PendingPaymentState();
    }
    
    /**
     * 获取上下文数据（重写父类方法以返回自身）
     * 
     * @return 订单上下文自身
     */
    public OrderContext getContextData() {
        return this;
    }
    
    /**
     * 重写toString方法
     * 
     * @return 订单上下文的字符串表示
     */
    @Override
    public String toString() {
        return String.format("OrderContext{orderId='%s', status='%s', amount=%s, userId='%s', productInfo='%s'}",
                orderId, status, amount, userId, productInfo);
    }
}