package com.jasonlat.design.framework.link.singleton;

/**
 * 责任链装配接口
 * <p>
 * 该接口定义了责任链的基本装配能力，提供了责任链节点之间的连接和导航功能。
 * 通过此接口，可以构建复杂的责任链结构，实现请求的有序传递和处理。
 * </p>
 * 
 * <p>
 * 主要功能：
 * <ul>
 *   <li>获取责任链中的下一个节点</li>
 *   <li>向责任链中追加新的节点</li>
 *   <li>支持链式调用，便于构建复杂的责任链</li>
 * </ul>
 * </p>
 * 
 * <p>
 * 设计理念：
 * <ul>
 *   <li>单一职责：专注于责任链的结构管理</li>
 *   <li>开闭原则：对扩展开放，对修改封闭</li>
 *   <li>里氏替换：所有实现类都可以互相替换</li>
 * </ul>
 * </p>
 * 
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * // 创建责任链节点
 * ILogicChainArmory<String, Context, Result> node1 = new FirstNode();
 * ILogicChainArmory<String, Context, Result> node2 = new SecondNode();
 * ILogicChainArmory<String, Context, Result> node3 = new ThirdNode();
 * 
 * // 构建责任链
 * node1.appendNext(node2).appendNext(node3);
 * 
 * // 获取下一个节点
 * ILogicLink<String, Context, Result> nextNode = node1.next();
 * }
 * </pre>
 * </p>
 *
 * @param <T> 请求参数类型，表示在责任链中传递的请求数据类型
 * @param <D> 动态上下文类型，表示在责任链执行过程中共享的上下文信息类型
 * @param <R> 返回结果类型，表示责任链处理完成后的返回值类型
 * 
 * @author Jasonlat
 * @version 1.0
 * @since 2025-08-28
 * @see ILogicLink 逻辑责任链接口
 * @see AbstractLogicLink 抽象责任链实现
 */
public interface ILogicChainArmory<T, D, R> {

    /**
     * 获取责任链中的下一个节点
     * <p>
     * 该方法用于获取当前节点在责任链中的下一个处理节点。
     * 如果当前节点是责任链的最后一个节点，则返回 null。
     * </p>
     * 
     * <p>
     * 使用场景：
     * <ul>
     *   <li>在节点处理逻辑中判断是否存在下一个节点</li>
     *   <li>遍历整个责任链结构</li>
     *   <li>实现条件性的请求传递</li>
     * </ul>
     * </p>
     */
    ILogicLink<T, D, R> next();

    /**
     * 向责任链中追加下一个节点
     * <p>
     * 该方法用于在当前节点后面追加一个新的责任链节点，实现责任链的动态构建。
     * 方法返回新追加的节点，支持链式调用，便于构建复杂的责任链结构。
     * </p>
     * 
     * <p>
     * 设计特点：
     * <ul>
     *   <li>支持链式调用：返回追加的节点，可以继续追加</li>
     *   <li>动态构建：可以在运行时动态调整责任链结构</li>
     *   <li>类型安全：通过泛型确保节点类型的一致性</li>
     * </ul>
     * </p>
     * 
     * <p>
     * 注意事项：
     * <ul>
     *   <li>如果当前节点已经有下一个节点，新节点将替换原有的下一个节点</li>
     *   <li>追加的节点应该与当前责任链的泛型类型保持一致</li>
     *   <li>避免创建循环引用，可能导致无限循环</li>
     * </ul>
     * </p>
     *
     * @return 追加的节点，支持链式调用
     */
    ILogicLink<T, D, R> appendNext(ILogicLink<T, D, R> next);

}
