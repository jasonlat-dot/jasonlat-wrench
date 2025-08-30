package com.jasonlat.design.framework.state.machine;

import com.jasonlat.design.framework.state.AbstractStateContext;
import com.jasonlat.design.framework.state.IState;
import com.jasonlat.design.framework.state.IStateContext;
import com.jasonlat.design.framework.state.transition.StateTransition;
import com.jasonlat.design.framework.state.factory.StateFactory;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 状态机构建器类
 * <p>
 * 该类提供了一个流畅的API来构建和配置状态机，简化了状态机的创建过程。
 * 支持状态注册、转换规则定义、初始状态设置、监听器配置等功能。
 * </p>
 * 
 * @param <C> 状态上下文类型
 * @param <E> 事件类型
 * @param <R> 返回结果类型
 * 
 * @author Jasonlat
 * @version 1.0
 * @since 2025-01-20
 * @see IStateContext 状态上下文接口
 * @see IState 状态接口
 * @see StateFactory 状态工厂
 * @see StateTransition 状态转换器
 */
public class StateMachineBuilder<C extends IStateContext<C, E, R>, E, R> {
    
    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(StateMachineBuilder.class);
    
    /**
     * 状态机标识符
     * -- GETTER --
     *  获取状态机标识符
     *
     * @return 状态机标识符

     */
    @Getter
    private final String stateMachineId;
    
    /**
     * 状态工厂
     * -- GETTER --
     *  获取状态工厂
     */
    @Getter
    private final StateFactory<C, E, R> stateFactory;
    
    /**
     * 状态转换规则列表
     */
    private final List<StateTransition<C, E, R>> transitions;
    
    /**
     * 初始状态名称
     */
    private String initialStateName;
    
    /**
     * 状态转换监听器
     */
    private StateMachine.StateTransitionListener transitionListener;
    
    /**
     * 是否启用转换日志
     */
    private boolean transitionLoggingEnabled = false;

    /**
     * 是否启用生命周期回调
     */
    private boolean lifecycleCallbacksEnabled = true;

    /**
     * 状态机配置
     */
    private StateMachineConfig config;
    
    /**
     * 私有构造函数
     * 
     * @param stateMachineId 状态机标识符
     */
    private StateMachineBuilder(String stateMachineId) {
        this.stateMachineId = Objects.requireNonNull(stateMachineId, "状态机ID不能为空");
        this.stateFactory = new StateFactory<>();
        this.transitions = new ArrayList<>();
        this.config = StateMachineConfig.defaultConfig();
    }
    
    /**
     * 创建新的状态机构建器
     * 
     * @param stateMachineId 状态机标识符
     * @param <C> 状态上下文类型
     * @param <E> 事件类型
     * @param <R> 返回结果类型
     * @return 状态机构建器实例
     */
    public static <C extends IStateContext<C, E, R>, E, R> StateMachineBuilder<C, E, R> newBuilder(String stateMachineId) {
        return new StateMachineBuilder<>(stateMachineId);
    }


    /**
     * 设置状态转换监听器
     *
     * @param listener 状态转换监听器
     * @return 构建器实例
     */
    public StateMachineBuilder<C, E, R> onTransition(StateMachine.StateTransitionListener listener) {
        this.transitionListener = listener;
        logger.debug("[{}] 设置状态转换监听器", stateMachineId);
        return this;
    }


    /**
     * 添加状态（单例模式）
     *
     * @param stateName 状态名称
     * @param state     状态实例
     */
    public void addState(String stateName, IState<C, E, R> state) {
        stateFactory.registerState(stateName, state);
        logger.debug("[{}] 注册状态: {}", stateMachineId, stateName);
    }
    
    /**
     * 添加状态（原型模式 - 使用供应商）
     * 
     * @param stateName 状态名称
     * @param stateSupplier 状态供应商
     * @return 构建器实例
     */
    public StateMachineBuilder<C, E, R> addState(String stateName, Supplier<IState<C, E, R>> stateSupplier) {
        stateFactory.registerStateSupplier(stateName, stateSupplier);
        logger.debug("[{}] 注册状态供应商: {}", stateMachineId, stateName);
        return this;
    }
    
    /**
     * 添加状态（原型模式 - 使用类）
     * 
     * @param stateName 状态名称
     * @param stateClass 状态类
     * @return 构建器实例
     */
    public StateMachineBuilder<C, E, R> addState(String stateName, Class<? extends IState<C, E, R>> stateClass) {
        stateFactory.registerStateClass(stateName, stateClass);
        logger.debug("[{}] 注册状态类: {} -> {}", stateMachineId, stateName, stateClass.getSimpleName());
        return this;
    }
    
    /**
     * 添加状态转换
     * 
     * @param fromState 源状态名称
     * @param toState 目标状态名称
     * @param triggerEvent 触发事件
     * @return 构建器实例
     */
    public StateMachineBuilder<C, E, R> addTransition(String fromState, String toState, E triggerEvent) {
        StateTransition<C, E, R> transition = new StateTransition<>(fromState, toState, triggerEvent);
        transitions.add(transition);
        logger.debug("[{}] 添加状态转换: {} --[{}]--> {}", stateMachineId, fromState, triggerEvent, toState);
        return this;
    }
    
    /**
     * 添加带条件的状态转换
     * 
     * @param fromState 源状态名称
     * @param toState 目标状态名称
     * @param triggerEvent 触发事件
     * @param condition 转换条件
     * @return 构建器实例
     */
    public StateMachineBuilder<C, E, R> addTransition(String fromState, String toState, E triggerEvent, 
                                                      Predicate<IStateContext<C, E, R>> condition) {
        StateTransition<C, E, R> transition = new StateTransition<>(fromState, toState, triggerEvent, condition);
        transitions.add(transition);
        logger.debug("[{}] 添加条件状态转换: {} --[{}]--> {} (带条件)", stateMachineId, fromState, triggerEvent, toState);
        return this;
    }
    
    /**
     * 添加完整的状态转换
     * 
     * @param transition 状态转换实例
     * @return 构建器实例
     */
    public StateMachineBuilder<C, E, R> addTransition(StateTransition<C, E, R> transition) {
        transitions.add(Objects.requireNonNull(transition, "状态转换不能为空"));
        logger.debug("[{}] 添加状态转换: {}", stateMachineId, transition);
        return this;
    }
    
    /**
     * 设置初始状态
     *
     * @param initialStateName 初始状态名称
     */
    public void initialState(String initialStateName) {
        this.initialStateName = Objects.requireNonNull(initialStateName, "初始状态名称不能为空");
        logger.debug("[{}] 设置初始状态: {}", stateMachineId, initialStateName);
    }

    /**
     * 启用或禁用转换日志
     *
     * @param enabled 是否启用
     */
    public void enableTransitionLogging(boolean enabled) {
        this.transitionLoggingEnabled = enabled;
        logger.debug("[{}] 转换日志: {}", stateMachineId, enabled ? "启用" : "禁用");
    }
    
    /**
     * 启用或禁用生命周期回调
     * 
     * @param enabled 是否启用
     * @return 构建器实例
     */
    public StateMachineBuilder<C, E, R> enableLifecycleCallbacks(boolean enabled) {
        this.lifecycleCallbacksEnabled = enabled;
        logger.debug("[{}] 生命周期回调: {}", stateMachineId, enabled ? "启用" : "禁用");
        return this;
    }
    
    /**
     * 设置状态机配置
     * 
     * @param config 状态机配置
     * @return 构建器实例
     */
    public StateMachineBuilder<C, E, R> config(StateMachineConfig config) {
        this.config = Objects.requireNonNull(config, "状态机配置不能为空");
        logger.debug("[{}] 设置状态机配置", stateMachineId);
        return this;
    }
    
    /**
     * 构建状态机上下文
     * 
     * @param contextData 上下文数据
     * @return 状态机上下文实例
     * @throws IllegalStateException 如果构建过程中发生错误
     */
    @SuppressWarnings("unchecked")
    public C buildContext(Object contextData) {
        validateConfiguration();
        
        try {
            // 创建默认状态上下文实现
            DefaultStateContext context = new DefaultStateContext(stateMachineId, contextData);
            
            // 设置初始状态
            if (initialStateName != null) {
                IState<C, E, R> initialState = stateFactory.getState(initialStateName);
                context.setState((IState<DefaultStateContext, E, R>) initialState);
            }
            
            // 注册到状态机管理器
            StateMachine stateMachine = StateMachine.getInstance();
            stateMachine.register(context);
            
            // 添加转换监听器
            if (transitionListener != null) {
                stateMachine.addStateTransitionListener(transitionListener);
            }
            
            logger.info("[{}] 状态机构建完成，初始状态: {}", stateMachineId, initialStateName);
            return (C) context;
            
        } catch (Exception e) {
            logger.error("[{}] 状态机构建失败", stateMachineId, e);
            throw new IllegalStateException("状态机构建失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证配置
     * 
     * @throws IllegalStateException 如果配置无效
     */
    private void validateConfiguration() {
        if (stateFactory.isEmpty()) {
            throw new IllegalStateException("至少需要注册一个状态");
        }
        
        if (initialStateName != null && stateFactory.containsState(initialStateName)) {
            throw new IllegalStateException("初始状态不存在: " + initialStateName);
        }
        
        // 验证转换规则中的状态是否都已注册
        for (StateTransition<C, E, R> transition : transitions) {
            if (stateFactory.containsState(transition.getFromState())) {
                throw new IllegalStateException("转换规则中的源状态不存在: " + transition.getFromState());
            }
            if (stateFactory.containsState(transition.getToState())) {
                throw new IllegalStateException("转换规则中的目标状态不存在: " + transition.getToState());
            }
        }
        
        logger.debug("[{}] 配置验证通过", stateMachineId);
    }

    /**
     * 获取状态转换规则列表
     * 
     * @return 状态转换规则列表
     */
    public List<StateTransition<C, E, R>> getTransitions() {
        return new ArrayList<>(transitions);
    }

    /**
     * 默认状态上下文实现
     * <p>
     * 这是一个内部实现类，提供了状态机上下文的基本功能。
     * 它继承自AbstractStateContext，并添加了状态转换逻辑。
     * </p>
     */
    @SuppressWarnings("unchecked")
    private class DefaultStateContext extends AbstractStateContext<DefaultStateContext, E, R> {
        
        /**
         * 上下文数据
         */
        private final Object contextData;
        
        /**
         * 构造函数
         * 
         * @param stateMachineId 状态机标识符
         * @param contextData 上下文数据
         */
        public DefaultStateContext(String stateMachineId, Object contextData) {
            super(stateMachineId);
            this.contextData = contextData;
        }
        
        /**
         * 处理请求事件
         * 
         * @param event 事件
         * @return 处理结果
         * @throws Exception 处理异常
         */
        @Override
        @SuppressWarnings("unchecked")
        public R request(E event) throws Exception {
            if (getCurrentState() == null) {
                throw new IllegalStateException("当前状态为空，无法处理事件");
            }
            
            if (transitionLoggingEnabled) {
                logger.info("[{}] 处理事件: {} (当前状态: {})", 
                    getStateMachineId(), event, getCurrentState().getStateName());
            }
            
            // 记录处理前的状态
            IState<DefaultStateContext, E, R> beforeState = getCurrentState();
            
            try {
                // 委托给当前状态处理
                R result = beforeState.handle(this, event);
                
                // 检查是否需要状态转换
                checkAndPerformTransition(event);
                
                // 记录处理后的状态
                IState<DefaultStateContext, E, R> afterState = getCurrentState();
                
                // 如果状态发生了变化，通知监听器
                if (beforeState != afterState && transitionListener != null) {
                    transitionListener.onStateTransition(getStateMachineId(), 
                        beforeState.getStateName(), afterState.getStateName(), event);
                }
                
                return result;
                
            } catch (Exception e) {
                logger.error("[{}] 事件处理失败: {} (状态: {})", 
                    getStateMachineId(), event, getCurrentState().getStateName(), e);
                throw e;
            }
        }
        
        /**
         * 检查并执行状态转换
         * 
         * @param event 触发事件
         * @throws Exception 转换异常
         */
        @SuppressWarnings("unchecked")
        private void checkAndPerformTransition(E event) throws Exception {
            String currentStateName = getCurrentState().getStateName();
            
            // 查找匹配的转换规则
            for (StateTransition<C, E, R> transition : transitions) {
                if (transition.canTransition((IStateContext<C, E, R>) this, event)) {
                    // 执行状态转换
                    IState<DefaultStateContext, E, R> newState = (IState<DefaultStateContext, E, R>) stateFactory.getState(transition.getToState());
                    setState(newState);
                    
                    if (transitionLoggingEnabled) {
                        logger.info("[{}] 状态转换: {} --[{}]--> {}", 
                            getStateMachineId(), currentStateName, event, transition.getToState());
                    }
                    
                    break; // 只执行第一个匹配的转换
                }
            }
        }
        
        /**
         * 获取上下文数据
         * 
         * @return 上下文数据
         */
        @SuppressWarnings("unchecked")
        public <T> T getContextData() {
            return (T) contextData;
        }
        
        /**
         * 创建初始状态（模板方法）
         * 
         * @return 初始状态实例
         */
        @Override
        protected IState<DefaultStateContext, E, R> createInitialState() {
            // 默认返回null，由外部设置初始状态
            return null;
        }
    }
}