package com.jasonlat.design.framework.link.singleton;

/**
 * 逻辑责任链接口
 * <p>
 * 该接口是责任链模式的核心接口，定义了责任链中每个节点的基本行为。
 * 通过泛型设计，支持不同类型的请求参数、动态上下文和返回结果。
 * </p>
 * 
 * <p>
 * 责任链模式的优势：
 * <ul>
 *   <li>降低耦合度：请求发送者和接收者解耦</li>
 *   <li>增强灵活性：可以动态地增加或删除责任</li>
 *   <li>简化对象：对象不需要知道链的结构</li>
 * </ul>
 * </p>
 * 
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * // 创建责任链节点
 * ILogicLink<String, Map<String, Object>, Boolean> validator = new ValidationLink();
 * ILogicLink<String, Map<String, Object>, Boolean> processor = new ProcessLink();
 * 
 * // 构建责任链
 * validator.appendNext(processor);
 * 
 * // 执行责任链
 * Boolean result = validator.apply("request", context);
 * }
 * </pre>
 * </p>
 *
 * @param <T> 请求参数类型，表示传入责任链的请求数据
 * @param <D> 动态上下文类型，表示在责任链执行过程中传递的上下文信息
 * @param <R> 返回结果类型，表示责任链执行后的返回值
 * 
 * @author Jasonlat
 * @version 1.0
 * @since 2025-08-28
 * @see ILogicChainArmory 责任链装配接口
 * @see AbstractLogicLink 抽象责任链实现
 */
public interface ILogicLink<T, D, R> extends ILogicChainArmory<T, D, R> {

    /**
     * 执行当前责任链节点的逻辑处理
     * <p>
     * 该方法是责任链模式的核心方法，每个具体的责任链节点都需要实现此方法。
     * 在实现时，节点可以选择：
     * <ul>
     *   <li>处理请求并返回结果</li>
     *   <li>将请求传递给下一个节点</li>
     *   <li>处理请求后继续传递给下一个节点</li>
     * </ul>
     * </p>
     * 
     * <p>
     * 注意事项：
     * <ul>
     *   <li>实现类应该根据业务逻辑决定是否继续传递请求</li>
     *   <li>如果需要传递给下一个节点，应调用 next() 方法</li>
     *   <li>异常处理应该在实现类中妥善处理</li>
     * </ul>
     * </p>
     *
     * @param requestParameter 请求参数，包含需要处理的业务数据
     * @param dynamicContext 动态上下文，用于在责任链节点间传递状态信息
     * @return 处理结果，可能是当前节点的处理结果或下游节点的处理结果
     * @throws Exception 当处理过程中发生异常时抛出
     */
    R apply(T requestParameter, D dynamicContext) throws Exception;

}
