package com.jasonlat.state.node;

import com.jasonlat.design.framework.state.AbstractState;
import com.jasonlat.state.context.OrderContext;
import com.jasonlat.state.event.OrderEvent;
import org.springframework.stereotype.Component;

/**
 * 止付状态类
 * <p>
 * 表示订单支付被停止，可能由于风控、资金不足或其他原因导致无法继续支付。
 * 在此状态下，订单可以恢复支付、取消订单，或在某些情况下直接支付（如果问题已解决）。
 * </p>
 * 
 * <p>
 * 支持的事件转换：
 * <ul>
 *   <li>RESUME_PAYMENT -> PendingPaymentState（恢复支付）</li>
 *   <li>CANCEL -> 订单取消</li>
 *   <li>PAY -> PaidState（直接支付，适用于问题已解决的情况）</li>
 * </ul>
 * </p>
 * 
 * @author jasonlat
 * @version 1.0
 * @since 2025-01-20
 */
@Component("STOPPED_PAYMENT")
public class StoppedPaymentState extends AbstractState<OrderContext, OrderEvent, String> {
    
    /**
     * 状态名称常量
     */
    private static final String STATE_NAME = "止付";
    
    /**
     * 构造函数
     * 初始化止付状态
     */
    public StoppedPaymentState() {
        super(STATE_NAME);
    }
    
    /**
     * 进入状态时的处理逻辑
     * <p>
     * 当订单转换到止付状态时执行的操作，包括：
     * <ul>
     *   <li>更新订单状态为止付</li>
     *   <li>记录止付时间</li>
     *   <li>记录状态转换日志</li>
     *   <li>输出止付警告信息</li>
     * </ul>
     * </p>
     * 
     * @param context 订单上下文，包含订单的所有信息
     * @throws Exception 状态转换异常
     */
    @Override
    protected void doOnEnter(OrderContext context) throws Exception {
        System.out.printf("订单[%s]进入止付状态，支付已被停止%n",
            context.getOrderId());
        
        // 设置订单状态为止付
        context.setStatus("STOPPED_PAYMENT");
        context.addLog("订单进入止付状态，支付被停止");
        
        // 记录止付时间
        context.setStoppedTime(System.currentTimeMillis());
        
        logger.warn("订单[{}]进入止付状态，原因可能为风控或资金问题", context.getOrderId());
    }
    
    /**
     * 退出状态时的处理逻辑
     * <p>
     * 当订单从止付状态转换到其他状态时执行的清理操作。
     * </p>
     * 
     * @param context 订单上下文
     * @throws Exception 状态转换异常
     */
    @Override
    protected void doOnExit(OrderContext context) throws Exception {
        System.out.println(String.format("订单[%s]离开止付状态", context.getOrderId()));
        context.addLog("订单离开止付状态");
        
        logger.info("订单[{}]离开止付状态", context.getOrderId());
    }
    
    /**
     * 处理订单事件的核心逻辑
     * <p>
     * 根据不同的事件类型执行相应的业务逻辑：
     * <ul>
     *   <li>RESUME_PAYMENT：恢复支付，转换到待支付状态</li>
     *   <li>CANCEL：取消订单</li>
     *   <li>PAY：直接支付（适用于问题已解决的情况）</li>
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
            case RESUME_PAYMENT:
                // 恢复支付事件：转换到待支付状态
                System.out.printf("订单[%s]恢复支付，转换到待支付状态%n",
                    context.getOrderId());
                
                context.addLog("订单恢复支付");
                context.setState(new PendingPaymentState());
                
                return "支付已恢复，订单状态已转换为待支付";
                
            case CANCEL:
                // 取消事件：取消订单
                System.out.printf("订单[%s]在止付状态下被取消%n",
                    context.getOrderId());
                
                context.setStatus("CANCELLED");
                context.addLog("订单在止付状态下被取消");
                
                return "订单已取消";
                
            case PAY:
                // 直接支付事件：适用于问题已解决的情况
                System.out.printf("订单[%s]在止付状态下直接支付成功%n",
                    context.getOrderId());
                
                context.addLog("订单在止付状态下直接支付成功");
                context.setState(new PaidState());
                
                return "支付成功，订单状态已转换为已支付";
                
            default:
                return handleUnsupportedEvent(context, event);
        }
    }
    
    /**
     * 检查当前状态是否可以处理指定事件
     * <p>
     * 止付状态支持的事件包括：恢复支付、取消、直接支付。
     * </p>
     * 
     * @param event 要检查的事件
     * @return 如果可以处理返回true，否则返回false
     */
    @Override
    public boolean canHandle(OrderEvent event) {
        return event == OrderEvent.RESUME_PAYMENT || 
               event == OrderEvent.CANCEL || 
               event == OrderEvent.PAY;
    }
    
    /**
     * 获取状态描述信息
     * 
     * @return 状态的详细描述
     */
    public String getDescription() {
        return "订单止付状态，支付被停止，可以恢复支付、取消订单或直接支付";
    }
}