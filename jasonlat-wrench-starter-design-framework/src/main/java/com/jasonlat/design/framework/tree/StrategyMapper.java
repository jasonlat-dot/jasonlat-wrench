package com.jasonlat.design.framework.tree;

/**
 * 策略映射器接口
 * <p>
 * 该接口定义了策略映射的核心功能，负责根据请求参数和上下文选择合适的策略处理器。
 * 通过策略映射，可以实现动态的策略选择和路由功能。
 * </p>
 * 
 * <p>主要特性：</p>
 * <ul>
 *   <li>支持动态策略选择</li>
 *   <li>泛型设计，支持多种数据类型</li>
 *   <li>基于请求参数和上下文的智能映射</li>
 *   <li>支持异常处理机制</li>
 * </ul>
 *
 * @param <T> 请求参数类型
 * @param <D> 动态上下文类型
 * @param <R> 返回结果类型
 * @author Jasonlat
 * @description 策略映射器
 * @create 2025-08-30
 */
public interface StrategyMapper<T, D, R> {

    /**
     * 获取待执行的策略处理器
     * <p>
     * 根据请求参数和动态上下文的内容，选择并返回合适的策略处理器。
     * 实现类需要根据具体的业务规则来实现策略选择逻辑。
     * </p>
     *
     * @param requestParameter 请求参数，用于确定策略选择的依据
     * @param dynamicContext 动态上下文，包含策略选择过程中需要的额外信息
     * @return 选择的策略处理器，如果没有找到合适的策略则返回null
     * @throws Exception 策略选择过程中可能抛出的异常
     */
    default StrategyHandler<T, D, R> get(T requestParameter, D dynamicContext) throws Exception {
        return null;
    }

}
