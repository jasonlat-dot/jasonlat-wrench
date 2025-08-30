package com.jasonlat.design.framework.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象状态实现类
 * <p>
 * 该抽象类提供了状态模式的基础实现，封装了状态的通用行为和生命周期管理。
 * 具体的状态实现类只需要继承此类并实现 {@link #doHandle(IStateContext, Object)} 方法即可。
 * </p>
 * 
 * <p>
 * 核心特性：
 * <ul>
 *   <li>封装了状态的基本结构和行为</li>
 *   <li>提供了统一的日志记录机制</li>
 *   <li>实现了状态生命周期的模板方法</li>
 *   <li>支持状态转换的前置和后置处理</li>
 * </ul>
 * </p>
 * 
 * <p>
 * 设计模式应用：
 * <ul>
 *   <li>模板方法模式：定义了状态处理的基本框架</li>
 *   <li>状态模式：实现了状态的封装和转换机制</li>
 *   <li>策略模式：每个具体状态代表不同的处理策略</li>
 * </ul>
 * </p>
 * 
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * // 创建具体的状态实现
 * public class IdleState extends AbstractState<OrderContext, OrderEvent, OrderResult> {
 *     
 *     public IdleState() {
 *         super("IDLE");
 *     }
 *     
 *     @Override
 *     protected OrderResult doHandle(OrderContext context, OrderEvent event) throws Exception {
 *         switch (event) {
 *             case START_PROCESS:
 *                 context.setState(new ProcessingState());
 *                 return OrderResult.success("订单开始处理");
 *             default:
 *                 return OrderResult.error("无效的事件: " + event);
 *         }
 *     }
 *     
 *     @Override
 *     public boolean canHandle(OrderEvent event) {
 *         return event == OrderEvent.START_PROCESS;
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
 * @see IState 状态接口
 * @see IStateContext 状态上下文接口
 */
public abstract class AbstractState<C extends IStateContext<C, E, R>, E, R> implements IState<C, E, R> {

    /**
     * 日志记录器
     */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 状态名称
     */
    private final String stateName;

    /**
     * 构造函数
     *
     * @param stateName 状态名称
     */
    protected AbstractState(String stateName) {
        this.stateName = stateName;
    }

    /**
     * 处理状态事件的模板方法
     * <p>
     * 该方法实现了状态处理的标准流程，包括前置检查、实际处理和后置处理。
     * 子类只需要实现 {@link #doHandle(IStateContext, Object)} 方法即可。
     * </p>
     * 
     * <p>
     * 处理流程：
     * <ol>
     *   <li>记录处理开始日志</li>
     *   <li>执行前置处理</li>
     *   <li>检查事件是否可以处理</li>
     *   <li>调用具体的处理逻辑</li>
     *   <li>执行后置处理</li>
     *   <li>记录处理结束日志</li>
     * </ol>
     * </p>
     *
     * @param context 状态上下文
     * @param event 触发的事件
     * @return 处理结果
     * @throws Exception 当处理过程中发生异常时抛出
     */
    @Override
    public final R handle(C context, E event) throws Exception {
        logger.debug("状态 [{}] 开始处理事件 [{}]", stateName, event);
        
        try {
            // 前置处理
            beforeHandle(context, event);
            
            // 检查是否可以处理该事件
            if (!canHandle(event)) {
                logger.warn("状态 [{}] 无法处理事件 [{}]", stateName, event);
                return handleUnsupportedEvent(context, event);
            }
            
            // 执行具体的处理逻辑
            R result = doHandle(context, event);
            
            // 后置处理
            afterHandle(context, event, result);
            
            logger.debug("状态 [{}] 成功处理事件 [{}]", stateName, event);
            return result;
            
        } catch (Exception e) {
            logger.error("状态 [{}] 处理事件 [{}] 时发生异常", stateName, event, e);
            // 异常处理
            return handleException(context, event, e);
        }
    }

    /**
     * 具体的事件处理逻辑
     * <p>
     * 子类必须实现此方法，提供状态特定的处理逻辑。
     * 该方法在模板方法 {@link #handle(IStateContext, Object)} 中被调用。
     * </p>
     *
     * @param context 状态上下文
     * @param event 触发的事件
     * @return 处理结果
     * @throws Exception 当处理过程中发生异常时抛出
     */
    protected abstract R doHandle(C context, E event) throws Exception;

    /**
     * 前置处理方法
     * <p>
     * 在执行具体的事件处理逻辑之前调用。子类可以重写此方法来实现
     * 状态特定的前置处理逻辑，如参数验证、权限检查等。
     * </p>
     *
     * @param context 状态上下文
     * @param event 触发的事件
     * @throws Exception 当前置处理过程中发生异常时抛出
     */
    protected void beforeHandle(C context, E event) throws Exception {
        // 默认实现为空，子类可以根据需要重写
    }

    /**
     * 后置处理方法
     * <p>
     * 在执行具体的事件处理逻辑之后调用。子类可以重写此方法来实现
     * 状态特定的后置处理逻辑，如结果验证、通知发送等。
     * </p>
     *
     * @param context 状态上下文
     * @param event 触发的事件
     * @param result 处理结果
     * @throws Exception 当后置处理过程中发生异常时抛出
     */
    protected void afterHandle(C context, E event, R result) throws Exception {
        // 默认实现为空，子类可以根据需要重写
    }

    /**
     * 处理不支持的事件
     * <p>
     * 当当前状态无法处理指定事件时调用此方法。
     * 子类可以重写此方法来提供自定义的错误处理逻辑。
     * </p>
     *
     * @param context 状态上下文
     * @param event 不支持的事件
     * @return 错误处理结果
     * @throws Exception 当错误处理过程中发生异常时抛出
     */
    protected R handleUnsupportedEvent(C context, E event) throws Exception {
        throw new IllegalStateException(
            String.format("状态 [%s] 不支持处理事件 [%s]", stateName, event)
        );
    }

    /**
     * 异常处理方法
     * <p>
     * 当事件处理过程中发生异常时调用此方法。
     * 子类可以重写此方法来提供自定义的异常处理逻辑。
     * </p>
     *
     * @param context 状态上下文
     * @param event 触发的事件
     * @param exception 发生的异常
     * @return 异常处理结果
     * @throws Exception 当异常处理过程中发生异常时抛出
     */
    protected R handleException(C context, E event, Exception exception) throws Exception {
        // 重新抛出异常，让上层处理
        throw exception;
    }

    /**
     * 进入状态时的回调方法
     * <p>
     * 提供了默认的进入状态处理逻辑，包括日志记录。
     * 子类可以重写此方法来添加额外的初始化逻辑。
     * </p>
     *
     * @param context 状态上下文
     * @throws Exception 当初始化过程中发生异常时抛出
     */
    @Override
    public void onEnter(C context) throws Exception {
        logger.info("进入状态: [{}], 状态机ID: [{}]", stateName, context.getStateMachineId());
        doOnEnter(context);
    }

    /**
     * 退出状态时的回调方法
     * <p>
     * 提供了默认的退出状态处理逻辑，包括日志记录。
     * 子类可以重写此方法来添加额外的清理逻辑。
     * </p>
     *
     * @param context 状态上下文
     * @throws Exception 当清理过程中发生异常时抛出
     */
    @Override
    public void onExit(C context) throws Exception {
        logger.info("退出状态: [{}], 状态机ID: [{}]", stateName, context.getStateMachineId());
        doOnExit(context);
    }

    /**
     * 具体的进入状态处理逻辑
     * <p>
     * 子类可以重写此方法来实现状态特定的初始化逻辑。
     * </p>
     *
     * @param context 状态上下文
     * @throws Exception 当初始化过程中发生异常时抛出
     */
    protected void doOnEnter(C context) throws Exception {
        // 默认实现为空，子类可以根据需要重写
    }

    /**
     * 具体的退出状态处理逻辑
     * <p>
     * 子类可以重写此方法来实现状态特定的清理逻辑。
     * </p>
     *
     * @param context 状态上下文
     * @throws Exception 当清理过程中发生异常时抛出
     */
    protected void doOnExit(C context) throws Exception {
        // 默认实现为空，子类可以根据需要重写
    }

    /**
     * 获取状态名称
     *
     * @return 状态名称
     */
    @Override
    public final String getStateName() {
        return stateName;
    }

    /**
     * 检查是否可以处理指定事件
     * <p>
     * 默认实现返回true，表示可以处理所有事件。
     * 子类应该重写此方法来实现具体的事件过滤逻辑。
     * </p>
     *
     * @param event 要检查的事件
     * @return 如果当前状态可以处理该事件返回true，否则返回false
     */
    @Override
    public boolean canHandle(E event) {
        return true; // 默认实现，子类应该重写
    }

    /**
     * 重写toString方法，提供状态的字符串表示
     *
     * @return 状态的字符串表示
     */
    @Override
    public String toString() {
        return String.format("%s[name=%s]", this.getClass().getSimpleName(), stateName);
    }

    /**
     * 重写equals方法，基于状态名称进行比较
     *
     * @param obj 要比较的对象
     * @return 如果状态名称相同返回true，否则返回false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AbstractState<?, ?, ?> that = (AbstractState<?, ?, ?>) obj;
        return stateName.equals(that.stateName);
    }

    /**
     * 重写hashCode方法，基于状态名称计算哈希值
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return stateName.hashCode();
    }
}