package com.jasonlat.design.framework.state.factory;

import com.jasonlat.design.framework.state.IState;
import com.jasonlat.design.framework.state.IStateContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 状态工厂类
 * <p>
 * 该类负责管理和创建状态实例，支持单例模式和原型模式两种创建方式。
 * 提供状态注册、获取、清理等功能，确保状态实例的正确管理和生命周期控制。
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
@Service
public class StateFactory<C extends IStateContext<C, E, R>, E, R> {
    
    /**
     * 状态实例缓存（单例模式）
     */
    private final Map<String, IState<C, E, R>> singletonStates = new ConcurrentHashMap<>();
    
    /**
     * 状态供应商缓存（原型模式）
     */
    private final Map<String, Supplier<IState<C, E, R>>> stateSuppliers = new ConcurrentHashMap<>();
    
    /**
     * 状态创建模式枚举
     */
    public enum CreationMode {
        /**
         * 单例模式：每个状态名称对应一个实例
         */
        SINGLETON,
        
        /**
         * 原型模式：每次获取都创建新实例
         */
        PROTOTYPE
    }
    
    /**
     * 状态创建模式映射
     */
    private final Map<String, CreationMode> creationModes = new ConcurrentHashMap<>();
    
    /**
     * 默认构造函数
     */
    public StateFactory() {
    }
    
    /**
     * 注册状态（单例模式）
     * 
     * @param stateName 状态名称
     * @param state 状态实例
     * @throws IllegalArgumentException 如果状态名称或状态实例为空
     * @throws IllegalStateException 如果状态名称已存在
     */
    public void registerState(String stateName, IState<C, E, R> state) {
        Objects.requireNonNull(stateName, "状态名称不能为空");
        Objects.requireNonNull(state, "状态实例不能为空");
        
        if (singletonStates.containsKey(stateName) || stateSuppliers.containsKey(stateName)) {
            throw new IllegalStateException("状态名称已存在: " + stateName);
        }
        
        singletonStates.put(stateName, state);
        creationModes.put(stateName, CreationMode.SINGLETON);
    }
    
    /**
     * 注册状态供应商（原型模式）
     * 
     * @param stateName 状态名称
     * @param stateSupplier 状态供应商
     * @throws IllegalArgumentException 如果状态名称或状态供应商为空
     * @throws IllegalStateException 如果状态名称已存在
     */
    public void registerStateSupplier(String stateName, Supplier<IState<C, E, R>> stateSupplier) {
        Objects.requireNonNull(stateName, "状态名称不能为空");
        Objects.requireNonNull(stateSupplier, "状态供应商不能为空");
        
        if (singletonStates.containsKey(stateName) || stateSuppliers.containsKey(stateName)) {
            throw new IllegalStateException("状态名称已存在: " + stateName);
        }
        
        stateSuppliers.put(stateName, stateSupplier);
        creationModes.put(stateName, CreationMode.PROTOTYPE);
    }
    
    /**
     * 注册状态类（原型模式）
     * 
     * @param stateName 状态名称
     * @param stateClass 状态类
     * @throws IllegalArgumentException 如果状态名称或状态类为空
     * @throws IllegalStateException 如果状态名称已存在或状态类无法实例化
     */
    public void registerStateClass(String stateName, Class<? extends IState<C, E, R>> stateClass) {
        Objects.requireNonNull(stateName, "状态名称不能为空");
        Objects.requireNonNull(stateClass, "状态类不能为空");
        
        registerStateSupplier(stateName, () -> {
            try {
                return stateClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("无法创建状态实例: " + stateClass.getName(), e);
            }
        });
    }
    
    /**
     * 获取状态实例
     * 
     * @param stateName 状态名称
     * @return 状态实例
     * @throws IllegalArgumentException 如果状态名称为空或不存在
     */
    public IState<C, E, R> getState(String stateName) {
        Objects.requireNonNull(stateName, "状态名称不能为空");
        
        // 优先从单例缓存获取
        IState<C, E, R> singletonState = singletonStates.get(stateName);
        if (singletonState != null) {
            return singletonState;
        }
        
        // 从供应商获取（原型模式）
        Supplier<IState<C, E, R>> supplier = stateSuppliers.get(stateName);
        if (supplier != null) {
            try {
                return supplier.get();
            } catch (Exception e) {
                throw new RuntimeException("创建状态实例失败: " + stateName, e);
            }
        }
        
        throw new IllegalArgumentException("状态不存在: " + stateName);
    }
    
    /**
     * 检查状态是否存在
     * 
     * @param stateName 状态名称
     * @return 如果状态存在返回true，否则返回false
     */
    public boolean containsState(String stateName) {
        if (stateName == null) {
            return true;
        }
        return !singletonStates.containsKey(stateName) && !stateSuppliers.containsKey(stateName);
    }
    
    /**
     * 获取状态创建模式
     * 
     * @param stateName 状态名称
     * @return 状态创建模式
     * @throws IllegalArgumentException 如果状态名称为空或不存在
     */
    public CreationMode getCreationMode(String stateName) {
        Objects.requireNonNull(stateName, "状态名称不能为空");
        
        CreationMode mode = creationModes.get(stateName);
        if (mode == null) {
            throw new IllegalArgumentException("状态不存在: " + stateName);
        }
        
        return mode;
    }
    
    /**
     * 注销状态
     * 
     * @param stateName 状态名称
     * @return 如果状态存在并被成功注销返回true，否则返回false
     */
    public boolean unregisterState(String stateName) {
        if (stateName == null) {
            return false;
        }
        
        boolean removed = false;
        
        if (singletonStates.remove(stateName) != null) {
            removed = true;
        }
        
        if (stateSuppliers.remove(stateName) != null) {
            removed = true;
        }
        
        if (creationModes.remove(stateName) != null) {
            removed = true;
        }
        
        return removed;
    }
    
    /**
     * 获取所有已注册的状态名称
     * 
     * @return 状态名称集合
     */
    public java.util.Set<String> getRegisteredStateNames() {
        java.util.Set<String> allNames = new java.util.HashSet<>();
        allNames.addAll(singletonStates.keySet());
        allNames.addAll(stateSuppliers.keySet());
        return allNames;
    }
    
    /**
     * 获取单例状态数量
     * 
     * @return 单例状态数量
     */
    public int getSingletonStateCount() {
        return singletonStates.size();
    }
    
    /**
     * 获取原型状态数量
     * 
     * @return 原型状态数量
     */
    public int getPrototypeStateCount() {
        return stateSuppliers.size();
    }
    
    /**
     * 获取总状态数量
     * 
     * @return 总状态数量
     */
    public int getTotalStateCount() {
        return getSingletonStateCount() + getPrototypeStateCount();
    }
    
    /**
     * 清理所有状态
     */
    public void clear() {
        singletonStates.clear();
        stateSuppliers.clear();
        creationModes.clear();
    }
    
    /**
     * 检查工厂是否为空
     * 
     * @return 如果没有注册任何状态返回true，否则返回false
     */
    public boolean isEmpty() {
        return singletonStates.isEmpty() && stateSuppliers.isEmpty();
    }
    
    /**
     * 获取工厂状态信息
     * 
     * @return 工厂状态信息字符串
     */
    public String getFactoryInfo() {
        return "StateFactory{" +
                "singletonStates=" + getSingletonStateCount() +
                ", prototypeStates=" + getPrototypeStateCount() +
                ", totalStates=" + getTotalStateCount() +
                ", registeredNames=" + getRegisteredStateNames() +
                "}";
    }
    
    @Override
    public String toString() {
        return getFactoryInfo();
    }
    
    /**
     * 创建状态工厂构建器
     * 
     * @param <C> 上下文类型
     * @param <E> 事件类型
     * @param <R> 结果类型
     * @return 状态工厂构建器
     */
    public static <C extends IStateContext<C, E, R>, E, R> Builder<C, E, R> builder() {
        return new Builder<>();
    }
    
    /**
     * 状态工厂构建器
     * 
     * @param <C> 上下文类型
     * @param <E> 事件类型
     * @param <R> 结果类型
     */
    public static class Builder<C extends IStateContext<C, E, R>, E, R> {
        private final StateFactory<C, E, R> factory = new StateFactory<>();
        
        /**
         * 添加单例状态
         * 
         * @param stateName 状态名称
         * @param state 状态实例
         * @return 构建器实例
         */
        public Builder<C, E, R> addSingletonState(String stateName, IState<C, E, R> state) {
            factory.registerState(stateName, state);
            return this;
        }
        
        /**
         * 添加原型状态
         * 
         * @param stateName 状态名称
         * @param stateSupplier 状态供应商
         * @return 构建器实例
         */
        public Builder<C, E, R> addPrototypeState(String stateName, Supplier<IState<C, E, R>> stateSupplier) {
            factory.registerStateSupplier(stateName, stateSupplier);
            return this;
        }
        
        /**
         * 添加原型状态类
         * 
         * @param stateName 状态名称
         * @param stateClass 状态类
         * @return 构建器实例
         */
        public Builder<C, E, R> addPrototypeStateClass(String stateName, Class<? extends IState<C, E, R>> stateClass) {
            factory.registerStateClass(stateName, stateClass);
            return this;
        }
        
        /**
         * 构建状态工厂实例
         * 
         * @return 状态工厂实例
         */
        public StateFactory<C, E, R> build() {
            return factory;
        }
    }
}