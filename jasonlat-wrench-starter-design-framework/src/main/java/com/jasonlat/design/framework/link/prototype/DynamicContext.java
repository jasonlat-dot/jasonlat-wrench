package com.jasonlat.design.framework.link.prototype;

import java.util.HashMap;
import java.util.Map;

/**
 * 链路动态上下文
 *
 * @author Jasonlat
 * 2025-08-29
 */
public class DynamicContext {

    /**
     * 是否继续执行链路的标识
     * 当此标识为false时，链路执行将会中断
     */
    private boolean proceed;

    /**
     * 构造函数，默认设置为继续执行
     * 初始化时proceed设置为true，表示链路默认会继续执行
     */
    public DynamicContext() {
        this.proceed = true;
    }

    public DynamicContext(boolean proceed) {
        this.proceed = proceed;
    }

    /**
     * 存储动态数据的Map容器
     * 用于在链路执行过程中传递和共享数据
     */
    private final Map<String, Object> dynamicContextData = new HashMap<>();

    /**
     * 设置键值对数据到上下文中
     * 将指定的键值对存储到动态上下文中，供链路中的各个节点使用
     * 
     * @param key 数据键，不能为null
     * @param value 数据值，可以为任意类型的对象
     * @param <T> 值的类型参数
     */
    public <T> void setValue(String key, T value) {
        if (null == key) throw new NullPointerException("key cannot be null");
        dynamicContextData.put(key, value);
    }

    /**
     * 从上下文中获取指定键的值
     * 根据提供的键从动态上下文中获取对应的值
     * 
     * @param key 数据键，用于查找对应的值
     * @param <T> 返回值的类型参数
     * @return 对应键的值，如果键不存在则返回null
     * @throws ClassCastException 如果类型转换失败
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        return (T) dynamicContextData.get(key);
    }

    /**
     * 获取是否继续执行链路的状态
     * 用于判断链路是否应该继续执行下去
     * 
     * @return true表示继续执行链路，false表示停止执行链路
     */
    @SuppressWarnings("unchecked")
    public boolean isProceed() {
        return proceed;
    }

    /**
     * 设置是否继续执行链路的状态
     * 通过此方法可以控制链路的执行流程，设置为false可以中断链路执行
     * 
     * @param proceed true表示继续执行链路，false表示停止执行链路
     */
    @SuppressWarnings("unchecked")
    public void setProceed(boolean proceed) {
        this.proceed = proceed;
    }
}
