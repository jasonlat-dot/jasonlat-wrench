package com.jasonlat.design.framework.state.transition;

import com.jasonlat.design.framework.state.IStateContext;
import lombok.Getter;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * 状态转换器类
 * <p>
 * 该类定义了状态机中状态之间的转换规则，包括源状态、目标状态、触发事件和转换条件。
 * 支持条件转换，只有当转换条件满足时才会执行状态转换。
 * </p>
 * 
 * @param <C> 上下文类型
 * @param <E> 事件类型
 * @param <R> 结果类型
 * 
 * @author jasonlat
 * @version 1.0
 * @since 2024
 */
@Getter
public class StateTransition<C extends IStateContext<C, E, R>, E, R> {
    
    /**
     * 源状态名称
     * -- GETTER --
     *  获取源状态名称
     */
    private final String fromState;
    
    /**
     * 目标状态名称
     * -- GETTER --
     *  获取目标状态名称
     */
    private final String toState;
    
    /**
     * 触发事件
     * -- GETTER --
     *  获取触发事件
     */
    private final E triggerEvent;
    
    /**
     * 转换条件（可选）
     * -- GETTER --
     *  获取转换条件
     */
    private final Predicate<IStateContext<C, E, R>> condition;
    
    /**
     * 转换优先级（数值越小优先级越高）
     * -- GETTER --
     *  获取转换优先级
     */
    private final int priority;
    
    /**
     * 转换描述
     * -- GETTER --
     *  获取转换描述
     */
    private final String description;
    
    /**
     * 构造函数
     * 
     * @param fromState 源状态名称
     * @param toState 目标状态名称
     * @param triggerEvent 触发事件
     */
    public StateTransition(String fromState, String toState, E triggerEvent) {
        this(fromState, toState, triggerEvent, null, 0, null);
    }
    
    /**
     * 构造函数（带条件）
     * 
     * @param fromState 源状态名称
     * @param toState 目标状态名称
     * @param triggerEvent 触发事件
     * @param condition 转换条件
     */
    public StateTransition(String fromState, String toState, E triggerEvent, 
                          Predicate<IStateContext<C, E, R>> condition) {
        this(fromState, toState, triggerEvent, condition, 0, null);
    }
    
    /**
     * 完整构造函数
     * 
     * @param fromState 源状态名称
     * @param toState 目标状态名称
     * @param triggerEvent 触发事件
     * @param condition 转换条件
     * @param priority 转换优先级
     * @param description 转换描述
     */
    public StateTransition(String fromState, String toState, E triggerEvent, 
                          Predicate<IStateContext<C, E, R>> condition, 
                          int priority, String description) {
        this.fromState = Objects.requireNonNull(fromState, "源状态不能为空");
        this.toState = Objects.requireNonNull(toState, "目标状态不能为空");
        this.triggerEvent = Objects.requireNonNull(triggerEvent, "触发事件不能为空");
        this.condition = condition;
        this.priority = priority;
        this.description = description;
    }
    
    /**
     * 检查是否可以执行转换
     * 
     * @param context 状态上下文
     * @param event 当前事件
     * @return 如果可以转换返回true，否则返回false
     */
    public boolean canTransition(IStateContext<C, E, R> context, E event) {
        // 检查当前状态是否匹配
        if (!fromState.equals(context.getCurrentState().getStateName())) {
            return false;
        }
        
        // 检查事件是否匹配
        if (!Objects.equals(triggerEvent, event)) {
            return false;
        }
        
        // 检查转换条件
        if (condition != null) {
            try {
                return condition.test(context);
            } catch (Exception e) {
                // 条件检查异常时，默认不允许转换
                return false;
            }
        }
        
        return true;
    }

    /**
     * 检查是否有转换条件
     * 
     * @return 如果有条件返回true，否则返回false
     */
    public boolean hasCondition() {
        return condition != null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateTransition<?, ?, ?> that = (StateTransition<?, ?, ?>) o;
        return priority == that.priority &&
               Objects.equals(fromState, that.fromState) &&
               Objects.equals(toState, that.toState) &&
               Objects.equals(triggerEvent, that.triggerEvent);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fromState, toState, triggerEvent, priority);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("StateTransition{");
        sb.append("from='").append(fromState).append("'");
        sb.append(", to='").append(toState).append("'");
        sb.append(", event='").append(triggerEvent).append("'");
        sb.append(", priority=").append(priority);
        if (hasCondition()) {
            sb.append(", hasCondition=true");
        }
        if (description != null) {
            sb.append(", description='").append(description).append("'");
        }
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * 创建状态转换构建器
     * 
     * @param <C> 上下文类型
     * @param <E> 事件类型
     * @param <R> 结果类型
     * @return 状态转换构建器
     */
    public static <C extends IStateContext<C, E, R>, E, R> Builder<C, E, R> builder() {
        return new Builder<>();
    }
    
    /**
     * 状态转换构建器
     * 
     * @param <C> 上下文类型
     * @param <E> 事件类型
     * @param <R> 结果类型
     */
    public static class Builder<C extends IStateContext<C, E, R>, E, R> {
        private String fromState;
        private String toState;
        private E triggerEvent;
        private Predicate<IStateContext<C, E, R>> condition;
        private int priority = 0;
        private String description;
        
        /**
         * 设置源状态
         * 
         * @param fromState 源状态名称
         * @return 构建器实例
         */
        public Builder<C, E, R> from(String fromState) {
            this.fromState = fromState;
            return this;
        }
        
        /**
         * 设置目标状态
         * 
         * @param toState 目标状态名称
         * @return 构建器实例
         */
        public Builder<C, E, R> to(String toState) {
            this.toState = toState;
            return this;
        }
        
        /**
         * 设置触发事件
         * 
         * @param triggerEvent 触发事件
         * @return 构建器实例
         */
        public Builder<C, E, R> on(E triggerEvent) {
            this.triggerEvent = triggerEvent;
            return this;
        }
        
        /**
         * 设置转换条件
         * 
         * @param condition 转换条件
         * @return 构建器实例
         */
        public Builder<C, E, R> when(Predicate<IStateContext<C, E, R>> condition) {
            this.condition = condition;
            return this;
        }
        
        /**
         * 设置转换优先级
         * 
         * @param priority 转换优先级
         * @return 构建器实例
         */
        public Builder<C, E, R> priority(int priority) {
            this.priority = priority;
            return this;
        }
        
        /**
         * 设置转换描述
         * 
         * @param description 转换描述
         * @return 构建器实例
         */
        public Builder<C, E, R> description(String description) {
            this.description = description;
            return this;
        }
        
        /**
         * 构建状态转换实例
         * 
         * @return 状态转换实例
         */
        public StateTransition<C, E, R> build() {
            return new StateTransition<>(fromState, toState, triggerEvent, condition, priority, description);
        }
    }
}