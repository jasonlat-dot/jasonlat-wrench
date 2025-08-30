package com.jasonlat.design.framework.tree;

/**
 * 策略处理器接口
 * <p>
 * 该接口定义了策略处理的核心方法，用于处理特定的业务逻辑。
 * 通过泛型设计，支持不同类型的请求参数、上下文和返回值。
 * </p>
 * 
 * <p>主要特性：</p>
 * <ul>
 *   <li>泛型设计，支持多种数据类型</li>
 *   <li>提供默认实现，返回null</li>
 *   <li>支持异常处理机制</li>
 *   <li>函数式接口设计，支持Lambda表达式</li>
 * </ul>
 *
 * @param <T> 请求参数类型
 * @param <D> 动态上下文类型
 * @param <R> 返回结果类型
 * @author Jasonlat
 * @description 受理策略处理
 * @create 2025-08-30
 */
public interface StrategyHandler<T, D, R> {

    /**
     * 默认策略处理器
     * <p>
     * 提供一个默认的策略处理器实现，当没有找到合适的策略时使用。
     * 该默认实现返回null，表示没有处理结果。
     * </p>
     */
    StrategyHandler DEFAULT = (T, D) -> null;

    /**
     * 策略处理方法
     * <p>
     * 根据请求参数和动态上下文执行具体的业务逻辑处理。
     * 实现类需要根据具体的业务需求来实现此方法。
     * </p>
     *
     * @param requestParameter 请求参数，包含处理所需的输入数据
     * @param dynamicContext 动态上下文，包含处理过程中需要的额外信息和状态
     * @return 处理结果，可以是任意类型的返回值
     * @throws Exception 处理过程中可能抛出的异常
     */
    R apply(T requestParameter, D dynamicContext) throws Exception;

}