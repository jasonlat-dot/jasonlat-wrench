package com.jasonlat.design.framework.state;

import com.jasonlat.design.framework.state.machine.StateMachine;
import com.jasonlat.design.framework.state.machine.StateMachineBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;
import java.util.UUID;

/**
 * 抽象状态上下文实现类
 * <p>
 * 该抽象类提供了状态上下文的基础实现，封装了状态管理、数据存储和生命周期控制的通用逻辑。
 * 具体的状态上下文实现类只需要继承此类并提供初始状态即可。
 * </p>
 * 
 * <p>
 * 核心特性：
 * <ul>
 *   <li>线程安全的状态管理</li>
 *   <li>基于ConcurrentHashMap的数据存储</li>
 *   <li>完整的状态转换生命周期管理</li>
 *   <li>统一的日志记录和异常处理</li>
 * </ul>
 * </p>
 * 
 * <p>
 * 设计模式应用：
 * <ul>
 *   <li>模板方法模式：定义了状态上下文的基本框架</li>
 *   <li>状态模式：管理状态的转换和维护</li>
 *   <li>观察者模式：支持状态变更通知（可扩展）</li>
 * </ul>
 * </p>
 *
 * @param <C> 状态上下文类型，通常是实现类自身的类型
 * @param <E> 事件类型，表示触发状态转换的事件
 * @param <R> 返回结果类型，表示状态处理后的返回值
 * 
 * @author Jasonlat
 * @version 1.0
 * @since 2025-08-30
 * @see IStateContext 状态上下文接口
 * @see IState 状态接口
 */
public abstract class AbstractStateContext<C extends IStateContext<C, E, R>, E, R> implements IStateContext<C, E, R> {

    /**
     * 日志记录器
     */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 状态机标识符
     */
    protected final String stateMachineId;

    /**
     * 当前状态
     */
    private volatile IState<C, E, R> currentState;

    /**
     * 上下文数据存储
     */
    private final Map<String, Object> dataMap;

    /**
     * 终止状态标识
     */
    private final AtomicBoolean terminated;

    /**
     * 状态转换锁对象
     */
    private final Object stateLock = new Object();

    /**
     * 构造函数
     *
     * @param stateMachineId 状态机标识符，如果为null则自动生成UUID
     */
    protected AbstractStateContext(String stateMachineId) {
        this.stateMachineId = StringUtils.hasLength(stateMachineId) ? stateMachineId : UUID.randomUUID().toString();
        this.dataMap = new ConcurrentHashMap<>();
        this.terminated = new AtomicBoolean(false);
        logger.debug("创建状态上下文，状态机ID: [{}]", this.stateMachineId);
    }

    /**
     * 设置初始状态
     * <p>
     * 该方法用于设置状态机的初始状态，只能在状态机创建时调用一次。
     * 如果当前状态不为null，则会抛出异常。
     * </p>
     *
     * @param initialState 初始状态
     * @throws Exception 当设置初始状态失败时抛出
     */
    protected void setInitialState(IState<C, E, R> initialState) throws Exception {
        if (currentState != null) {
            throw new IllegalStateException("状态机已经初始化，不能重复设置初始状态");
        }
        
        synchronized (stateLock) {
            if (currentState != null) {
                throw new IllegalStateException("状态机已经初始化，不能重复设置初始状态");
            }
            
            this.currentState = initialState;
            logger.info("设置初始状态: [{}], 状态机ID: [{}]", initialState.getStateName(), stateMachineId);
            
            // 调用初始状态的进入方法
            initialState.onEnter(self());
        }
    }

    /**
     * 设置当前状态
     * <p>
     * 该方法实现了完整的状态转换流程，包括旧状态的退出和新状态的进入。
     * 使用同步锁确保状态转换的原子性和线程安全性。
     * </p>
     *
     * @param newState 新的状态实例
     * @throws Exception 当状态转换过程中发生异常时抛出
     */
    @Override
    public void setState(IState<C, E, R> newState) throws Exception {
        if (newState == null) {
            throw new IllegalArgumentException("新状态不能为null");
        }
        
        if (terminated.get()) {
            throw new IllegalStateException("状态机已终止，不能进行状态转换");
        }
        
        synchronized (stateLock) {
            IState<C, E, R> oldState = this.currentState;
            
            if (oldState == newState) {
                logger.debug("状态未发生变化: [{}]", newState.getStateName());
                return;
            }
            
            logger.info("状态转换: [{}] -> [{}], 状态机ID: [{}]", 
                oldState != null ? oldState.getStateName() : "null", 
                newState.getStateName(), 
                stateMachineId);
            
            try {
                // 执行状态转换前的处理
                beforeStateTransition(oldState, newState);
                
                // 退出旧状态
                if (oldState != null) {
                    oldState.onExit(self());
                }
                
                // 更新当前状态
                this.currentState = newState;
                
                // 进入新状态
                newState.onEnter(self());
                
                // 执行状态转换后的处理
                afterStateTransition(oldState, newState);
                
                logger.debug("状态转换完成: [{}] -> [{}]", 
                    oldState != null ? oldState.getStateName() : "null", 
                    newState.getStateName());
                    
            } catch (Exception e) {
                logger.error("状态转换失败: [{}] -> [{}], 状态机ID: [{}]", 
                    oldState != null ? oldState.getStateName() : "null", 
                    newState.getStateName(), 
                    stateMachineId, e);
                
                // 状态转换失败时的处理
                handleStateTransitionError(oldState, newState, e);
                throw e;
            }
        }
    }

    /**
     * 获取当前状态
     *
     * @return 当前状态实例
     */
    @Override
    public IState<C, E, R> getCurrentState() {
        return currentState;
    }

    /**
     * 处理请求事件
     * <p>
     * 该方法是状态机的主要入口点，将事件委托给当前状态进行处理。
     * 包含了完整的异常处理和日志记录。
     * </p>
     *
     * @param event 要处理的事件
     * @return 处理结果
     * @throws Exception 当处理过程中发生异常时抛出
     */
    @Override
    public R request(E event) throws Exception {
        if (event == null) {
            throw new IllegalArgumentException("事件不能为null");
        }
        
        if (terminated.get()) {
            throw new IllegalStateException("状态机已终止，不能处理事件");
        }
        IState<C, E, R> state = currentState;
        if (state == null) {
            throw new IllegalStateException("状态机未初始化，当前状态为null");
        }

        logger.debug("处理事件: [{}], 当前状态: [{}], 状态机ID: [{}]", event, state.getStateName(), stateMachineId);

        try {
            R result = state.handle(self(), event);
            logger.debug("事件处理完成: [{}], 结果: [{}]", event, result);

            return result;
        } catch (Exception e) {
            logger.error("事件处理失败: [{}], 当前状态: [{}], 状态机ID: [{}]", event, state.getStateName(), stateMachineId, e);
            throw e;
        }
    }


    /**
     * 获取上下文数据
     *
     * @param key 数据键
     * @param <T> 数据类型
     * @return 对应的数据值，如果不存在则返回null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        if (key == null) {
            return null;
        }
        return (T) dataMap.get(key);
    }

    /**
     * 设置上下文数据
     *
     * @param key 数据键
     * @param value 数据值
     * @param <T> 数据类型
     */
    @Override
    public <T> void setData(String key, T value) {
        if (key != null) {
            if (value != null) {
                dataMap.put(key, value);
            } else {
                dataMap.remove(key);
            }
        }
    }

    /**
     * 移除上下文数据
     *
     * @param key 要移除的数据键
     * @return 被移除的数据值，如果不存在则返回null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T removeData(String key) {
        if (key == null) {
            return null;
        }
        return (T) dataMap.remove(key);
    }

    /**
     * 清空所有上下文数据
     */
    @Override
    public void clearData() {
        dataMap.clear();
        logger.debug("清空上下文数据，状态机ID: [{}]", stateMachineId);
    }

    /**
     * 获取状态机标识
     *
     * @return 状态机标识符
     */
    @Override
    public String getStateMachineId() {
        return stateMachineId;
    }

    /**
     * 检查状态机是否处于终止状态
     *
     * @return 如果处于终止状态返回true，否则返回false
     */
    @Override
    public boolean isTerminated() {
        return terminated.get();
    }

    /**
     * 终止状态机
     * <p>
     * 将状态机设置为终止状态，不再接受新的事件处理。
     * 该操作是不可逆的。
     * </p>
     */
    public void terminate() {
        if (terminated.compareAndSet(false, true)) {
            logger.info("状态机已终止，状态机ID: [{}]", stateMachineId);
            
            synchronized (stateLock) {
                try {
                    // 退出当前状态
                    if (currentState != null) {
                        currentState.onExit(self());
                    }
                } catch (Exception e) {
                    logger.error("终止状态机时退出当前状态失败", e);
                }
            }
        }
    }

    /**
     * 重置状态机
     * <p>
     * 将状态机重置到初始状态，清除所有上下文数据。
     * 只有在状态机未终止的情况下才能重置。
     * </p>
     *
     * @throws Exception 当重置过程中发生异常时抛出
     */
    @Override
    public void reset() throws Exception {
        if (terminated.get()) {
            throw new IllegalStateException("已终止的状态机不能重置");
        }
        
        synchronized (stateLock) {
            logger.info("重置状态机，状态机ID: [{}]", stateMachineId);
            
            // 退出当前状态
            if (currentState != null) {
                currentState.onExit(self());
                currentState = null;
            }
            
            // 清空数据
            clearData();
            
            // 重新设置初始状态
            IState<C, E, R> initialState = createInitialState();
            if (initialState != null) {
                setInitialState(initialState);
            }
        }
    }

    /**
     * 创建初始状态
     * <p>
     * 子类可以重写此方法来提供初始状态的创建逻辑。
     * 在状态机重置时会调用此方法。
     * </p>
     *
     * @return 初始状态实例，如果返回null则不设置初始状态
     */
    protected IState<C, E, R> createInitialState() {
        return null; // 默认实现返回null，子类可以重写
    }

    /**
     * 状态转换前的处理
     * <p>
     * 在状态转换之前调用，子类可以重写此方法来实现自定义的前置处理逻辑。
     * </p>
     *
     * @param oldState 旧状态
     * @param newState 新状态
     * @throws Exception 当前置处理过程中发生异常时抛出
     */
    protected void beforeStateTransition(IState<C, E, R> oldState, IState<C, E, R> newState) throws Exception {
        // 默认实现为空，子类可以根据需要重写
    }

    /**
     * 状态转换后的处理
     * <p>
     * 在状态转换之后调用，子类可以重写此方法来实现自定义的后置处理逻辑。
     * </p>
     *
     * @param oldState 旧状态
     * @param newState 新状态
     * @throws Exception 当后置处理过程中发生异常时抛出
     */
    protected void afterStateTransition(IState<C, E, R> oldState, IState<C, E, R> newState) throws Exception {
        // 默认实现为空，子类可以根据需要重写
    }

    /**
     * 处理状态转换错误
     * <p>
     * 当状态转换过程中发生异常时调用，子类可以重写此方法来实现自定义的错误处理逻辑。
     * </p>
     *
     * @param oldState 旧状态
     * @param newState 新状态
     * @param exception 发生的异常
     * @throws Exception 当错误处理过程中发生异常时抛出
     */
    protected void handleStateTransitionError(IState<C, E, R> oldState, IState<C, E, R> newState, Exception exception) throws Exception {
        // 默认实现为空，子类可以根据需要重写
    }

    /**
     * 获取自身引用
     * <p>
     * 返回当前实例的强类型引用，用于传递给状态方法。
     * </p>
     *
     * @return 自身引用
     */
    @SuppressWarnings("unchecked")
    protected C self() {
        return (C) this;
    }

    /**
     * 重写toString方法，提供状态上下文的字符串表示
     *
     * @return 状态上下文的字符串表示
     */
    @Override
    public String toString() {
        return String.format("%s[id=%s, state=%s, terminated=%s]", 
            this.getClass().getSimpleName(), 
            stateMachineId, 
            currentState != null ? currentState.getStateName() : "null", 
            terminated.get());
    }
}