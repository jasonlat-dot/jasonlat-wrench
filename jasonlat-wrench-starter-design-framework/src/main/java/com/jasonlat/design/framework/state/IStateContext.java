package com.jasonlat.design.framework.state;

/**
 * 状态上下文接口
 * <p>
 * 该接口定义了状态机的上下文环境，负责管理当前状态、状态转换和相关数据。
 * 状态上下文是状态模式中的核心组件，它维护着状态机的当前状态，并提供状态转换的能力。
 * </p>
 *
 * @param <C> 状态上下文类型，通常是实现类自身的类型
 * @param <E> 事件类型，表示触发状态转换的事件
 * @param <R> 返回结果类型，表示状态处理后的返回值
 * 
 * @author Jasonlat
 * @version 1.0
 * @since 2025-08-30
 * @see IState 状态接口
 * @see AbstractStateContext 抽象状态上下文实现
 */
public interface IStateContext<C extends IStateContext<C, E, R>, E, R> {

    /**
     * 设置当前状态
     * <p>
     * 该方法用于更新状态机的当前状态。在状态转换时，会调用此方法来切换到新的状态。
     * 实现时需要考虑状态转换的生命周期管理，包括旧状态的退出和新状态的进入。
     * </p>
     * 
     * <p>
     * 状态转换流程：
     * <ol>
     *   <li>调用当前状态的onExit方法</li>
     *   <li>更新状态引用</li>
     *   <li>调用新状态的onEnter方法</li>
     *   <li>记录状态转换日志（可选）</li>
     * </ol>
     * </p>
     *
     * @param state 新的状态实例
     * @throws Exception 当状态转换过程中发生异常时抛出
     */
    void setState(IState<C, E, R> state) throws Exception;

    /**
     * 获取当前状态
     * <p>
     * 返回状态机当前的状态实例。可以用于状态检查、日志记录或调试目的。
     * </p>
     *
     * @return 当前状态实例
     */
    IState<C, E, R> getCurrentState();

    /**
     * 处理请求事件
     * <p>
     * 该方法是状态机的主要入口点，用于处理外部传入的事件。
     * 它会将事件委托给当前状态进行处理，并返回处理结果。
     * </p>
     * 
     * <p>
     * 处理流程：
     * <ol>
     *   <li>检查当前状态是否可以处理该事件</li>
     *   <li>调用当前状态的handle方法</li>
     *   <li>处理可能的状态转换</li>
     *   <li>返回处理结果</li>
     * </ol>
     * </p>
     *
     * @param event 要处理的事件
     * @return 处理结果
     * @throws Exception 当处理过程中发生异常时抛出
     */
    R request(E event) throws Exception;

    /**
     * 获取上下文数据
     * <p>
     * 返回状态机上下文中存储的数据。这些数据在状态转换过程中保持持久化，
     * 可以被不同的状态访问和修改。
     * </p>
     * 
     * <p>
     * 数据类型可以是：
     * <ul>
     *   <li>业务实体对象</li>
     *   <li>配置参数</li>
     *   <li>临时计算结果</li>
     *   <li>状态转换历史</li>
     * </ul>
     * </p>
     *
     * @param key 数据键
     * @param <T> 数据类型
     * @return 对应的数据值，如果不存在则返回null
     */
    <T> T getData(String key);

    /**
     * 设置上下文数据
     * <p>
     * 在状态机上下文中存储数据。这些数据可以在状态转换过程中被访问和修改，
     * 为状态之间的数据共享提供支持。
     * </p>
     *
     * @param key 数据键
     * @param value 数据值
     * @param <T> 数据类型
     */
    <T> void setData(String key, T value);

    /**
     * 移除上下文数据
     * <p>
     * 从状态机上下文中移除指定的数据。用于清理不再需要的数据，
     * 避免内存泄漏和数据污染。
     * </p>
     *
     * @param key 要移除的数据键
     * @return 被移除的数据值，如果不存在则返回null
     */
    <T> T removeData(String key);

    /**
     * 清空所有上下文数据
     * <p>
     * 清除状态机上下文中的所有数据。通常在状态机重置或销毁时调用。
     * </p>
     */
    void clearData();

    /**
     * 获取状态机标识
     * <p>
     * 返回状态机的唯一标识符，用于区分不同的状态机实例。
     * 在日志记录、监控和调试时非常有用。
     * </p>
     *
     * @return 状态机标识符
     */
    String getStateMachineId();

    /**
     * 检查状态机是否处于终止状态
     * <p>
     * 判断状态机是否已经到达终止状态。终止状态表示状态机的生命周期结束，
     * 不再接受新的事件处理。
     * </p>
     *
     * @return 如果处于终止状态返回true，否则返回false
     */
    default boolean isTerminated() {
        return false; // 默认实现返回false，子类可以根据需要重写
    }

    /**
     * 重置状态机
     * <p>
     * 将状态机重置到初始状态，清除所有上下文数据。
     * 用于状态机的重新初始化或错误恢复。
     * </p>
     *
     * @throws Exception 当重置过程中发生异常时抛出
     */
    default void reset() throws Exception {
        clearData();
        // 子类可以重写此方法来实现具体的重置逻辑
    }
}