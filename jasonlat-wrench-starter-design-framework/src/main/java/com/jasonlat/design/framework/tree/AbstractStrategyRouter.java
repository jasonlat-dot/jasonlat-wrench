package com.jasonlat.design.framework.tree;

import lombok.Getter;
import lombok.Setter;

/**
 * 策略路由抽象类
 * <p>
 * 该类提供了基础的策略路由功能，实现了策略映射器和策略处理器接口。
 * 通过策略模式，可以根据不同的请求参数和上下文动态选择合适的处理策略。
 * </p>
 * 
 * <p>主要特性：</p>
 * <ul>
 *   <li>支持策略动态路由和选择</li>
 *   <li>提供默认策略处理器机制</li>
 *   <li>泛型设计，支持不同类型的请求参数、上下文和返回值</li>
 *   <li>简洁的策略路由实现</li>
 * </ul>
 *
 * @param <T> 请求参数类型
 * @param <D> 动态上下文类型
 * @param <R> 返回结果类型
 * @author Jasonlat
 * @description 策略路由抽象类
 * @create 2025-08-30
 */
public abstract class AbstractStrategyRouter<T, D, R> implements StrategyMapper<T, D, R>, StrategyHandler<T, D, R> {

    /**
     * 默认策略处理器
     * <p>
     * 当无法找到合适的策略处理器时，将使用此默认处理器进行处理。
     * 默认值为 StrategyHandler.DEFAULT，返回 null。
     * </p>
     */
    @Getter
    @Setter
    @SuppressWarnings("unchecked")
    protected StrategyHandler<T, D, R> defaultStrategyHandler = StrategyHandler.DEFAULT;

    /**
     * 策略路由方法
     * <p>
     * 根据请求参数和动态上下文获取合适的策略处理器进行处理。
     * 如果找不到合适的策略处理器，则使用默认策略处理器。
     * </p>
     *
     * @param requestParameter 请求参数，用于确定使用哪个策略
     * @param dynamicContext 动态上下文，包含处理过程中需要的额外信息
     * @return 策略处理的结果
     * @throws Exception 处理过程中可能抛出的异常
     */
    public R router(T requestParameter, D dynamicContext) throws Exception {
        StrategyHandler<T, D, R> strategyHandler = get(requestParameter, dynamicContext);
        if(null != strategyHandler) return strategyHandler.apply(requestParameter, dynamicContext);
        return defaultStrategyHandler.apply(requestParameter, dynamicContext);
    }

}
