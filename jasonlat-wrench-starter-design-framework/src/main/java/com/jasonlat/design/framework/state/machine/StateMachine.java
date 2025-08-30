package com.jasonlat.design.framework.state.machine;

import com.jasonlat.design.framework.state.IState;
import com.jasonlat.design.framework.state.IStateContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 状态机管理器
 * <p>
 * 该类是状态机框架的核心管理组件，提供了状态机的创建、管理、监控和生命周期控制功能。
 * 支持多个状态机实例的并发管理，并提供了丰富的监控和统计功能。
 * </p>
 * 
 * <p>
 * 核心功能：
 * <ul>
 *   <li>状态机实例的注册和管理</li>
 *   <li>状态转换的监控和统计</li>
 *   <li>状态机生命周期的控制</li>
 *   <li>异常处理和错误恢复</li>
 * </ul>
 * </p>
 * @author Jasonlat
 * @version 1.0
 * @since 2025-08-30
 * @see IStateContext 状态上下文接口
 * @see IState 状态接口
 */
@Builder
@AllArgsConstructor
@Data
public class StateMachine {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(StateMachine.class);

    /**
     * 单例实例
     */
    private static volatile StateMachine instance;

    /**
     * 状态机注册表
     */
    private final Map<String, IStateContext<?, ?, ?>> stateMachineRegistry;

    /**
     * 状态转换监听器列表
     */
    private final List<StateTransitionListener> listeners;

    /**
     * 统计信息
     */
    private final Statistics statistics;

    /**
     * 私有构造函数
     */
    private StateMachine() {
        this.stateMachineRegistry = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        this.statistics = new Statistics();
        
        logger.info("状态机管理器初始化完成");
    }

    /**
     * 获取状态机管理器单例实例
     *
     * @return 状态机管理器实例
     */
    public static StateMachine getInstance() {
        if (instance == null) {
            synchronized (StateMachine.class) {
                if (instance == null) {
                    instance = new StateMachine();
                }
            }
        }
        return instance;
    }

    /**
     * 注册状态机
     * <p>
     * 将状态机实例注册到管理器中，以便进行统一管理和监控。
     * 如果已存在相同ID的状态机，则会抛出异常。
     * </p>
     *
     * @param context 状态机上下文
     * @param <C> 状态上下文类型
     * @param <E> 事件类型
     * @param <R> 返回结果类型
     * @throws IllegalArgumentException 当状态机ID已存在时抛出
     */
    public <C extends IStateContext<C, E, R>, E, R> void register(C context) {
        if (context == null) {
            throw new IllegalArgumentException("状态机上下文不能为null");
        }
        
        String stateMachineId = context.getStateMachineId();
        if (stateMachineRegistry.containsKey(stateMachineId)) {
            throw new IllegalArgumentException("状态机ID已存在: " + stateMachineId);
        }
        
        stateMachineRegistry.put(stateMachineId, context);
        statistics.incrementRegistered();
        
        logger.info("注册状态机: [{}], 当前总数: [{}]", stateMachineId, stateMachineRegistry.size());
        
        // 通知监听器
        notifyListeners(listener -> listener.onStateMachineRegistered(stateMachineId, context));
    }

    /**
     * 注销状态机
     * <p>
     * 从管理器中移除指定的状态机实例。如果状态机不存在，则返回false。
     * </p>
     *
     * @param stateMachineId 状态机ID
     * @return 如果成功注销返回true，否则返回false
     */
    public boolean unregister(String stateMachineId) {
        if (stateMachineId == null) {
            return false;
        }
        
        IStateContext<?, ?, ?> context = stateMachineRegistry.remove(stateMachineId);
        if (context != null) {
            statistics.incrementUnregistered();
            
            logger.info("注销状态机: [{}], 当前总数: [{}]", stateMachineId, stateMachineRegistry.size());
            
            // 通知监听器
            notifyListeners(listener -> listener.onStateMachineUnregistered(stateMachineId, context));
            
            return true;
        }
        
        return false;
    }

    /**
     * 获取状态机实例
     *
     * @param stateMachineId 状态机ID
     * @param <C> 状态上下文类型
     * @param <E> 事件类型
     * @param <R> 返回结果类型
     * @return 状态机实例，如果不存在则返回null
     */
    @SuppressWarnings("unchecked")
    public <C extends IStateContext<C, E, R>, E, R> C getStateMachine(String stateMachineId) {
        if (stateMachineId == null) {
            return null;
        }
        return (C) stateMachineRegistry.get(stateMachineId);
    }

    /**
     * 处理事件
     * <p>
     * 向指定的状态机发送事件进行处理。如果状态机不存在或处理失败，会记录相应的日志。
     * </p>
     *
     * @param stateMachineId 状态机ID
     * @param event 要处理的事件
     * @param <E> 事件类型
     * @param <R> 返回结果类型
     * @return 处理结果，如果状态机不存在则返回null
     * @throws Exception 当事件处理过程中发生异常时抛出
     */
    @SuppressWarnings("unchecked")
    public <E, R> R processEvent(String stateMachineId, E event) throws Exception {
        if (stateMachineId == null || event == null) {
            throw new IllegalArgumentException("状态机ID和事件不能为null");
        }
        
        IStateContext<?, E, R> context = (IStateContext<?, E, R>) stateMachineRegistry.get(stateMachineId);
        if (context == null) {
            logger.warn("状态机不存在: [{}]", stateMachineId);
            return null;
        }
        
        try {
            statistics.incrementEventProcessed();
            
            // 记录状态转换前的状态
            IState<?, E, R> beforeState = context.getCurrentState();
            
            // 处理事件
            R result = context.request(event);
            
            // 记录状态转换后的状态
            IState<?, E, R> afterState = context.getCurrentState();
            
            // 如果状态发生了变化，通知监听器
            if (beforeState != afterState) {
                statistics.incrementStateTransition();
                notifyListeners(listener -> listener.onStateTransition(
                    stateMachineId, 
                    beforeState != null ? beforeState.getStateName() : null,
                    afterState != null ? afterState.getStateName() : null,
                    event
                ));
            }
            
            return result;
            
        } catch (Exception e) {
            statistics.incrementEventFailed();
            logger.error("处理事件失败: 状态机ID=[{}], 事件=[{}]", stateMachineId, event, e);
            
            // 通知监听器
            notifyListeners(listener -> listener.onEventProcessingError(stateMachineId, event, e));
            
            throw e;
        }
    }

    /**
     * 获取所有状态机ID
     *
     * @return 状态机ID集合
     */
    public Set<String> getAllStateMachineIds() {
        return new HashSet<>(stateMachineRegistry.keySet());
    }

    /**
     * 获取活跃状态机数量
     *
     * @return 活跃状态机数量
     */
    public int getActiveStateMachineCount() {
        return stateMachineRegistry.size();
    }

    /**
     * 检查状态机是否存在
     *
     * @param stateMachineId 状态机ID
     * @return 如果存在返回true，否则返回false
     */
    public boolean exists(String stateMachineId) {
        return stateMachineId != null && stateMachineRegistry.containsKey(stateMachineId);
    }

    /**
     * 清空所有状态机
     * <p>
     * 移除所有注册的状态机实例。通常在系统关闭或重置时调用。
     * </p>
     */
    public void clear() {
        int count = stateMachineRegistry.size();
        stateMachineRegistry.clear();
        
        logger.info("清空所有状态机，共移除 [{}] 个实例", count);
        
        // 通知监听器
        notifyListeners(listener -> listener.onAllStateMachinesCleared(count));
    }

    /**
     * 添加状态转换监听器
     *
     * @param listener 监听器实例
     */
    public void addStateTransitionListener(StateTransitionListener listener) {
        if (listener != null) {
            synchronized (listeners) {
                listeners.add(listener);
            }
            logger.debug("添加状态转换监听器: [{}]", listener.getClass().getSimpleName());
        }
    }

    /**
     * 移除状态转换监听器
     *
     * @param listener 监听器实例
     * @return 如果成功移除返回true，否则返回false
     */
    public boolean removeStateTransitionListener(StateTransitionListener listener) {
        if (listener != null) {
            synchronized (listeners) {
                boolean removed = listeners.remove(listener);
                if (removed) {
                    logger.debug("移除状态转换监听器: [{}]", listener.getClass().getSimpleName());
                }
                return removed;
            }
        }
        return false;
    }


    /**
     * 通知所有监听器
     *
     * @param action 要执行的动作
     */
    private void notifyListeners(Consumer<StateTransitionListener> action) {
        synchronized (listeners) {
            for (StateTransitionListener listener : listeners) {
                try {
                    action.accept(listener);
                } catch (Exception e) {
                    logger.error("通知监听器时发生异常: [{}]", listener.getClass().getSimpleName(), e);
                }
            }
        }
    }

    /**
     * 状态转换监听器接口
     */
    public interface StateTransitionListener {
        
        /**
         * 状态机注册事件
         *
         * @param stateMachineId 状态机ID
         * @param context 状态机上下文
         */
        default void onStateMachineRegistered(String stateMachineId, IStateContext<?, ?, ?> context) {
            // 默认实现为空
            logger.debug("状态机注册: [{}]", stateMachineId);
        }
        
        /**
         * 状态机注销事件
         *
         * @param stateMachineId 状态机ID
         * @param context 状态机上下文
         */
        default void onStateMachineUnregistered(String stateMachineId, IStateContext<?, ?, ?> context) {
            // 默认实现为空
            logger.debug("状态机注销: [{}]", stateMachineId);
        }
        
        /**
         * 状态转换事件
         *
         * @param stateMachineId 状态机ID
         * @param fromState 源状态名称
         * @param toState 目标状态名称
         * @param event 触发事件
         */
        default void onStateTransition(String stateMachineId, String fromState, String toState, Object event) {
            // 默认实现为空
            logger.debug("状态机: [{}], 状态转换: [{} -> {}]", stateMachineId, fromState, toState);
        }
        
        /**
         * 事件处理错误事件
         *
         * @param stateMachineId 状态机ID
         * @param event 触发事件
         * @param exception 异常信息
         */
        default void onEventProcessingError(String stateMachineId, Object event, Exception exception) {
            // 默认实现为空
            logger.error("事件处理错误: 状态机ID=[{}], 事件=[{}]", stateMachineId, event, exception);
        }
        
        /**
         * 所有状态机清空事件
         *
         * @param count 清空的状态机数量
         */
        default void onAllStateMachinesCleared(int count) {
            // 默认实现为空
            logger.info("所有状态机已清空，共清空 [{}] 个实例", count);
        }
    }

    /**
     * 统计信息类
     */
    public static class Statistics {
        
        private final AtomicLong registeredCount = new AtomicLong(0);
        private final AtomicLong unregisteredCount = new AtomicLong(0);
        private final AtomicLong eventProcessedCount = new AtomicLong(0);
        private final AtomicLong eventFailedCount = new AtomicLong(0);
        private final AtomicLong stateTransitionCount = new AtomicLong(0);
        private final long startTime = System.currentTimeMillis();
        
        /**
         * 增加注册计数
         */
        void incrementRegistered() {
            registeredCount.incrementAndGet();
        }
        
        /**
         * 增加注销计数
         */
        void incrementUnregistered() {
            unregisteredCount.incrementAndGet();
        }
        
        /**
         * 增加事件处理计数
         */
        void incrementEventProcessed() {
            eventProcessedCount.incrementAndGet();
        }
        
        /**
         * 增加事件失败计数
         */
        void incrementEventFailed() {
            eventFailedCount.incrementAndGet();
        }
        
        /**
         * 增加状态转换计数
         */
        void incrementStateTransition() {
            stateTransitionCount.incrementAndGet();
        }
        
        /**
         * 获取注册的状态机总数
         */
        public long getRegisteredCount() {
            return registeredCount.get();
        }
        
        /**
         * 获取注销的状态机总数
         */
        public long getUnregisteredCount() {
            return unregisteredCount.get();
        }
        
        /**
         * 获取当前活跃的状态机数量
         */
        public long getActiveStateMachines() {
            return registeredCount.get() - unregisteredCount.get();
        }
        
        /**
         * 获取处理的事件总数
         */
        public long getEventProcessedCount() {
            return eventProcessedCount.get();
        }
        
        /**
         * 获取失败的事件总数
         */
        public long getEventFailedCount() {
            return eventFailedCount.get();
        }
        
        /**
         * 获取状态转换总数
         */
        public long getStateTransitionCount() {
            return stateTransitionCount.get();
        }
        
        /**
         * 获取运行时间（毫秒）
         */
        public long getUptime() {
            return System.currentTimeMillis() - startTime;
        }
        
        /**
         * 获取事件处理成功率
         */
        public double getEventSuccessRate() {
            long total = eventProcessedCount.get();
            if (total == 0) {
                return 0.0;
            }
            return (double) (total - eventFailedCount.get()) / total;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Statistics{registered=%d, unregistered=%d, active=%d, eventProcessed=%d, eventFailed=%d, stateTransition=%d, successRate=%.2f%%, uptime=%dms}",
                registeredCount.get(),
                unregisteredCount.get(),
                getActiveStateMachines(),
                eventProcessedCount.get(),
                eventFailedCount.get(),
                stateTransitionCount.get(),
                getEventSuccessRate() * 100,
                getUptime()
            );
        }
    }
}