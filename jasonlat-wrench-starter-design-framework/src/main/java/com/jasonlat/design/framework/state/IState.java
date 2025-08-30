package com.jasonlat.design.framework.state;

/**
 * 状态接口
 * <p>
 * 该接口是状态模式的核心接口，定义了状态的基本行为和生命周期方法。
 * 每个具体状态类都需要实现此接口，提供状态特定的处理逻辑。
 * </p>
 * 
 * <p>
 * 状态模式的优势：
 * <ul>
 *   <li>封装状态转换逻辑：将状态转换规则封装在状态类内部</li>
 *   <li>消除条件分支：避免大量的if-else或switch-case语句</li>
 *   <li>提高可维护性：新增状态只需添加新的状态类</li>
 *   <li>符合开闭原则：对扩展开放，对修改封闭</li>
 * </ul>
 * </p>
 * 
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * // 创建具体状态实现
 * public class IdleState implements IState<OrderContext, OrderEvent, OrderResult> {
 *     @Override
 *     public OrderResult handle(OrderContext context, OrderEvent event) {
 *         if (event == OrderEvent.START_PROCESS) {
 *             context.setState(new ProcessingState());
 *             return OrderResult.success("订单开始处理");
 *         }
 *         return OrderResult.error("无效的事件");
 *     }
 * }
 * }
 * </pre>
 * </p>
 *
 * @param <C> 状态上下文类型，表示状态机的上下文环境
 * @param <E> 事件类型，表示触发状态转换的事件
 * @param <R> 返回结果类型，表示状态处理后的返回值
 * 
 * @author Jasonlat
 * @version 1.0
 * @since 2025-08-30
 * @see IStateContext 状态上下文接口
 * @see AbstractState 抽象状态实现
 */
public interface IState<C, E, R> {

    /**
     * 处理状态事件
     * <p>
     * 该方法是状态处理的核心方法，负责处理传入的事件并执行相应的业务逻辑。
     * 根据事件类型和当前状态，可能会触发状态转换或执行特定的业务操作。
     * </p>
     * 
     * <p>
     * 实现要点：
     * <ul>
     *   <li>根据事件类型执行相应的处理逻辑</li>
     *   <li>必要时更新状态上下文中的状态</li>
     *   <li>返回处理结果，包含成功/失败信息</li>
     *   <li>处理异常情况，确保状态机的稳定性</li>
     * </ul>
     * </p>
     *
     * @param context 状态上下文，包含状态机的当前状态和相关数据
     * @param event 触发的事件，用于决定执行哪种处理逻辑
     * @return 处理结果，包含操作是否成功以及相关信息
     * @throws Exception 当处理过程中发生异常时抛出
     */
    R handle(C context, E event) throws Exception;

    /**
     * 进入状态时的回调方法
     * <p>
     * 当状态机转换到当前状态时，会自动调用此方法。
     * 可以在此方法中执行状态初始化、资源准备等操作。
     * </p>
     * 
     * <p>
     * 典型用途：
     * <ul>
     *   <li>初始化状态相关的数据</li>
     *   <li>启动定时器或后台任务</li>
     *   <li>记录状态转换日志</li>
     *   <li>发送状态变更通知</li>
     * </ul>
     * </p>
     *
     * @param context 状态上下文
     * @throws Exception 当初始化过程中发生异常时抛出
     */
    default void onEnter(C context) throws Exception {
        // 默认实现为空，子类可以根据需要重写
    }

    /**
     * 退出状态时的回调方法
     * <p>
     * 当状态机从当前状态转换到其他状态时，会自动调用此方法。
     * 可以在此方法中执行资源清理、数据保存等操作。
     * </p>
     * 
     * <p>
     * 典型用途：
     * <ul>
     *   <li>清理状态相关的资源</li>
     *   <li>停止定时器或后台任务</li>
     *   <li>保存状态数据</li>
     *   <li>发送状态退出通知</li>
     * </ul>
     * </p>
     *
     * @param context 状态上下文
     * @throws Exception 当清理过程中发生异常时抛出
     */
    default void onExit(C context) throws Exception {
        // 默认实现为空，子类可以根据需要重写
    }

    /**
     * 获取状态名称
     * <p>
     * 返回当前状态的唯一标识名称，用于日志记录、调试和状态机可视化。
     * 建议使用有意义的名称，便于理解和维护。
     * </p>
     *
     * @return 状态名称
     */
    String getStateName();

    /**
     * 检查是否可以处理指定事件
     * <p>
     * 在执行handle方法之前，可以通过此方法检查当前状态是否支持处理指定的事件。
     * 这有助于提前发现不合法的状态转换，提高系统的健壮性。
     * </p>
     *
     * @param event 要检查的事件
     * @return 如果当前状态可以处理该事件返回true，否则返回false
     */
    default boolean canHandle(E event) {
        return true; // 默认实现允许处理所有事件，子类可以根据需要重写
    }
}