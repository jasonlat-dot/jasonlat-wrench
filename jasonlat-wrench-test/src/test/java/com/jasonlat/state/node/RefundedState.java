package com.jasonlat.state.node;

import com.jasonlat.design.framework.state.AbstractState;
import com.jasonlat.state.context.OrderContext;
import com.jasonlat.state.event.OrderEvent;
import org.springframework.stereotype.Component;

/**
 * 已退款状态类
 * <p>
 * 表示订单已完成退款操作，资金已返还给客户。
 * 这是订单生命周期的一个终态，通常不会再发生状态转换。
 * </p>
 * 
 * <p>
 * 支持的事件转换：
 * <ul>
 *   <li>QUERY -> 查询退款详情</li>
 * </ul>
 * </p>
 * 
 * @author jasonlat
 * @version 1.0
 * @since 2025-01-20
 */
@Component("REFUNDED")
public class RefundedState extends AbstractState<OrderContext, OrderEvent, String> {
    
    /**
     * 状态名称常量
     */
    private static final String STATE_NAME = "已退款";
    
    /**
     * 构造函数
     * 初始化已退款状态
     */
    public RefundedState() {
        super(STATE_NAME);
    }
    
    /**
     * 进入状态时的处理逻辑
     * <p>
     * 当订单转换到已退款状态时执行的操作，包括：
     * <ul>
     *   <li>更新订单状态为已退款</li>
     *   <li>记录退款完成时间</li>
     *   <li>记录状态转换日志</li>
     *   <li>发送退款完成通知</li>
     * </ul>
     * </p>
     * 
     * @param context 订单上下文，包含订单的所有信息
     * @throws Exception 状态转换异常
     */
    @Override
    protected void doOnEnter(OrderContext context) throws Exception {
        System.out.printf("订单[%s]进入已退款状态，退款已完成%n",
            context.getOrderId());
        
        // 设置订单状态为已退款
        context.setStatus("REFUNDED");
        context.addLog("订单进入已退款状态，退款已完成");
        
        // 记录退款完成时间
        context.setAttribute("refundedTime", System.currentTimeMillis());
        
        // 发送退款完成通知（模拟）
        System.out.printf("已向客户发送退款完成通知，订单号：%s%n",
            context.getOrderId());
        
        logger.info("订单[{}]退款已完成，资金已返还给客户", context.getOrderId());
    }
    
    /**
     * 退出状态时的处理逻辑
     * <p>
     * 当订单从已退款状态转换到其他状态时执行的清理操作。
     * 注意：已退款状态通常是终态，很少会发生状态转换。
     * </p>
     * 
     * @param context 订单上下文
     * @throws Exception 状态转换异常
     */
    @Override
    protected void doOnExit(OrderContext context) throws Exception {
        System.out.printf("订单[%s]离开已退款状态%n", context.getOrderId());
        context.addLog("订单离开已退款状态");
        
        logger.info("订单[{}]离开已退款状态", context.getOrderId());
    }
    
    /**
     * 处理订单事件的核心逻辑
     * <p>
     * 根据不同的事件类型执行相应的业务逻辑：
     * <ul>
     *   <li>QUERY：查询退款详情</li>
     * </ul>
     * </p>
     * 
     * @param context 订单上下文
     * @param event 触发的事件
     * @return 处理结果描述
     * @throws Exception 事件处理异常
     */
    @Override
    protected String doHandle(OrderContext context, OrderEvent event) throws Exception {
        switch (event) {
            case QUERY:
                // 查询事件：返回退款详情
                System.out.printf("查询订单[%s]退款详情%n", context.getOrderId());

                context.addLog("查询退款详情");

                Long refundedTime = (Long) context.getAttribute("refundedTime");
                return String.format("订单[%s]退款详情：状态=%s，退款时间=%s",
                    context.getOrderId(),
                    context.getStatus(),
                    refundedTime != null ?
                        new java.util.Date(refundedTime).toString() : "未知");

            default:
                return handleUnsupportedEvent(context, event);
        }
    }
    
    /**
     * 检查当前状态是否可以处理指定事件
     * <p>
     * 已退款状态支持的事件包括：查询。
     * </p>
     * 
     * @param event 要检查的事件
     * @return 如果可以处理返回true，否则返回false
     */
    @Override
    public boolean canHandle(OrderEvent event) {
        return event == OrderEvent.QUERY;
    }
    
    /**
     * 获取状态描述信息
     * 
     * @return 状态的详细描述
     */
    public String getDescription() {
        return "订单已退款状态，退款已完成，资金已返还给客户";
    }
}