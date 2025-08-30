package com.jasonlat.state.event;

import lombok.Getter;

/**
 * 订单状态转换事件枚举
 * 定义了订单在不同状态之间转换时触发的事件类型
 * 
 * @author jasonlat
 * @version 1.0
 * @since 2024-01-20
 */
@Getter
public enum OrderEvent {
    
    /**
     * 支付事件 - 用户完成支付操作
     */
    PAY("支付", "用户完成支付操作"),
    
    /**
     * 止付事件 - 停止支付，可能由于风控或其他原因
     */
    STOP_PAYMENT("止付", "停止支付操作"),
    
    /**
     * 取消事件 - 取消订单
     */
    CANCEL("取消", "取消订单"),
    
    /**
     * 退款事件 - 申请退款
     */
    REFUND("退款", "申请退款操作"),
    
    /**
     * 恢复支付事件 - 恢复被止付的订单
     */
    RESUME_PAYMENT("恢复支付", "恢复被止付的订单支付"),
    
    /**
     * 查询事件 - 查询订单状态和详情
     */
    QUERY("查询", "查询订单状态和详情信息");
    
    /**
     * 事件名称
     * -- GETTER --
     *  获取事件名称
     */
    private final String name;
    
    /**
     * 事件描述
     * -- GETTER --
     *  获取事件描述
     */
    private final String description;
    
    /**
     * 构造函数
     * 
     * @param name 事件名称
     * @param description 事件描述
     */
    OrderEvent(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * 重写toString方法
     * 
     * @return 事件的字符串表示
     */
    @Override
    public String toString() {
        return String.format("%s(%s)", name, description);
    }
}