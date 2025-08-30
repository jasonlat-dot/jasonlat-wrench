package com.jasonlat.state.node;

import com.jasonlat.design.framework.state.AbstractState;
import com.jasonlat.state.context.OrderContext;
import com.jasonlat.state.event.OrderEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 已支付状态类
 * <p>
 * 表示订单已完成支付，用户已成功付款。在此状态下，订单可以进行退款操作，
 * 但不能再次支付或取消。该状态标志着订单支付流程的成功完成。
 * </p>
 * 
 * <p>
 * 支持的事件转换：
 * <ul>
 *   <li>REFUND -> PendingPaymentState（退款后回到待支付状态）</li>
 * </ul>
 * </p>
 * 
 * @author jasonlat
 * @version 1.0
 * @since 2025-08-30
 */
@Component("PAID")
public class PaidState extends AbstractState<OrderContext, OrderEvent, String> {
    /**
     * 状态名称常量
     */
    private static final String STATE_NAME = "已支付";
    
    /**
     * 构造函数
     * 初始化已支付状态
     */
    public PaidState() {
        super(STATE_NAME);
    }
    
    /**
     * 进入状态时的处理逻辑
     * <p>
     * 当订单转换到已支付状态时执行的操作，包括：
     * <ul>
     *   <li>更新订单状态为已支付</li>
     *   <li>记录支付完成时间</li>
     *   <li>记录状态转换日志</li>
     *   <li>输出支付成功信息</li>
     * </ul>
     * </p>
     * 
     * @param context 订单上下文，包含订单的所有信息
     * @throws Exception 状态转换异常
     */
    @Override
    protected void doOnEnter(OrderContext context) throws Exception {
        System.out.printf("订单[%s]进入已支付状态，支付完成！%n",
            context.getOrderId());
        
        // 设置订单状态为已支付
        context.setStatus("PAID");
        context.addLog("订单支付完成，进入已支付状态");
        
        // 记录支付时间
        context.setPaidTime(System.currentTimeMillis());
        
        logger.info("订单[{}]支付完成，金额：{}", context.getOrderId(), context.getAmount());
    }
    
    /**
     * 退出状态时的处理逻辑
     * <p>
     * 当订单从已支付状态转换到其他状态时执行的清理操作。
     * </p>
     * 
     * @param context 订单上下文
     * @throws Exception 状态转换异常
     */
    @Override
    protected void doOnExit(OrderContext context) throws Exception {
        System.out.printf("订单[%s]离开已支付状态%n", context.getOrderId());
        context.addLog("订单离开已支付状态");
        
        logger.info("订单[{}]离开已支付状态", context.getOrderId());
    }
    
    /**
     * 处理订单事件的核心逻辑
     * <p>
     * 根据不同的事件类型执行相应的业务逻辑：
     * <ul>
     *   <li>REFUND：处理退款事件，执行退款操作并转换到待支付状态</li>
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
            case REFUND:
                // 退款事件：执行退款逻辑并转换到已退款状态
                System.out.printf("订单[%s]开始退款，金额：%s%n",
                    context.getOrderId(), context.getAmount());
                
                // 记录退款信息
                context.addLog(String.format("订单退款，金额：%s", context.getAmount()));
                
                // 转换到已退款状态
                context.setState(new RefundedState());
                
                return String.format("退款成功，金额：%s，订单已转换为已退款状态", context.getAmount());
                
            default:
                return handleUnsupportedEvent(context, event);
        }
    }
    
    /**
     * 检查当前状态是否可以处理指定事件
     * <p>
     * 已支付状态仅支持退款事件。
     * </p>
     * 
     * @param event 要检查的事件
     * @return 如果可以处理返回true，否则返回false
     */
    @Override
    public boolean canHandle(OrderEvent event) {
        return event == OrderEvent.REFUND;
    }

    /**
     * 获取状态描述信息
     * 
     * @return 状态的详细描述
     */
    public String getDescription() {
        return "订单已支付状态，支付已完成，可以进行退款操作";
    }
}