package com.jasonlat.state.node;

import com.jasonlat.design.framework.state.AbstractState;
import com.jasonlat.state.context.OrderContext;
import com.jasonlat.state.event.OrderEvent;
import org.springframework.stereotype.Component;

/**
 * 待支付状态类
 * <p>
 * 表示订单处于等待支付的状态，用户可以进行支付、取消或止付操作。
 * 该状态是订单创建后的初始状态，等待用户完成支付流程。
 * </p>
 * 
 * <p>
 * 支持的事件转换：
 * <ul>
 *   <li>PAY -> PaidState（支付成功）</li>
 *   <li>CANCEL -> 订单取消</li>
 *   <li>STOP_PAYMENT -> StoppedPaymentState（止付）</li>
 * </ul>
 * </p>
 * 
 * @author jasonlat
 * @version 1.0
 * @since 2025-01-20
 */
@Component("PENDING_PAYMENT")
public class PendingPaymentState extends AbstractState<OrderContext, OrderEvent, String> {
    
    /**
     * 状态名称常量
     */
    private static final String STATE_NAME = "待支付";
    
    /**
     * 构造函数
     * 初始化待支付状态
     */
    public PendingPaymentState() {
        super(STATE_NAME);
    }
    
    /**
     * 进入状态时的处理逻辑
     * <p>
     * 当订单转换到待支付状态时执行的操作，包括：
     * <ul>
     *   <li>更新订单状态为待支付</li>
     *   <li>记录状态转换日志</li>
     *   <li>输出状态转换信息</li>
     * </ul>
     * </p>
     * 
     * @param context 订单上下文，包含订单的所有信息
     * @throws Exception 状态转换异常
     */
    @Override
    protected void doOnEnter(OrderContext context) throws Exception {
        System.out.printf("订单[%s]进入待支付状态，等待用户支付...%n", context.getOrderId());
        
        // 设置订单状态为待支付
        context.setStatus("PENDING_PAYMENT");
        context.addLog("订单进入待支付状态");
        
        logger.info("订单[{}]进入待支付状态", context.getOrderId());
    }
    
    /**
     * 退出状态时的处理逻辑
     * <p>
     * 当订单从待支付状态转换到其他状态时执行的清理操作。
     * </p>
     * 
     * @param context 订单上下文
     * @throws Exception 状态转换异常
     */
    @Override
    protected void doOnExit(OrderContext context) throws Exception {
        System.out.printf("订单[%s]离开待支付状态%n", context.getOrderId());
        context.addLog("订单离开待支付状态");
        
        logger.info("订单[{}]离开待支付状态", context.getOrderId());
    }
    
    /**
     * 处理订单事件的核心逻辑
     * <p>
     * 根据不同的事件类型执行相应的业务逻辑：
     * <ul>
     *   <li>PAY：处理支付事件，转换到已支付状态</li>
     *   <li>CANCEL：处理取消事件，取消订单</li>
     *   <li>STOP_PAYMENT：处理止付事件，转换到止付状态</li>
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
            case PAY:
                // 支付事件：转换到已支付状态
                context.setState(new PaidState());
                return "支付成功，订单已完成支付";
                
            case CANCEL:
                // 取消事件：取消订单
                context.setStatus("CANCELLED");
                context.addLog("订单已取消");
                return "订单已取消";
                
            case STOP_PAYMENT:
                // 止付事件：转换到止付状态
                context.setState(new StoppedPaymentState());
                return "订单支付已被停止";
                
            default:
                return handleUnsupportedEvent(context, event);
        }
    }
    
    /**
     * 检查当前状态是否可以处理指定事件
     * <p>
     * 待支付状态支持的事件包括：支付、取消、止付。
     * </p>
     * 
     * @param event 要检查的事件
     * @return 如果可以处理返回true，否则返回false
     */
    @Override
    public boolean canHandle(OrderEvent event) {
        return event == OrderEvent.PAY || 
               event == OrderEvent.CANCEL || 
               event == OrderEvent.STOP_PAYMENT;
    }
    
    /**
     * 获取状态描述信息
     * 
     * @return 状态的详细描述
     */
    public String getDescription() {
        return "订单待支付状态，等待用户完成支付操作";
    }
}