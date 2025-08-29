package com.jasonlat.design.framework.link.prototype;

import com.jasonlat.design.framework.link.prototype.chain.BusinessLinkedList;
import com.jasonlat.design.framework.link.prototype.handler.ILogicHandler;

/**
 * 链路装配器
 * 用于构建和管理业务逻辑处理链路，将多个逻辑处理器按顺序组装成一个完整的处理链
 * 
 * @param <T> 请求参数类型
 * @param <D> 动态上下文类型，必须继承自DynamicContext
 * @param <R> 返回结果类型
 * @author Jasonlat
 * @description 链路装配
 * @create 2025-08-29
 */
public class LinkArmory<T, D extends DynamicContext, R> {

    /**
     * 业务逻辑链路实例
     */
    private final BusinessLinkedList<T, D, R> logicLink;

    /**
     * 构造函数，创建链路装配器并初始化逻辑处理链
     * 
     * @param linkName 链路名称，用于标识和调试
     * @param logicHandlers 逻辑处理器数组，按传入顺序依次执行
     */
    @SafeVarargs
    public LinkArmory(String linkName, ILogicHandler<T, D, R>... logicHandlers) {
        logicLink = new BusinessLinkedList<>(linkName);
        for (ILogicHandler<T, D, R> logicHandler: logicHandlers){
            logicLink.add(logicHandler);
        }
    }

    /**
     * 获取业务逻辑链路实例
     * 
     * @return 业务逻辑链路对象
     */
    public BusinessLinkedList<T, D, R> getLogicLink() {
        return logicLink;
    }

}
