package com.jasonlat.design.framework.state.machine;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * 状态机配置类
 * <p>
 * 该类提供状态机框架的全局配置选项，包括日志级别、性能监控、
 * 超时设置、线程池配置等。支持从配置文件加载和程序化配置两种方式。
 * </p>
 * 
 * @author jasonlat
 * @version 1.0
 * @since 2024
 */
@Setter
@Getter
public class StateMachineConfig {
    
    /**
     * 默认状态转换超时时间（毫秒）
     */
    public static final long DEFAULT_TRANSITION_TIMEOUT_MS = 5000L;
    
    /**
     * 默认状态机清理间隔（毫秒）
     */
    public static final long DEFAULT_CLEANUP_INTERVAL_MS = 60000L;
    
    /**
     * 默认最大状态机数量
     */
    public static final int DEFAULT_MAX_STATE_MACHINES = 1000;
    
    /**
     * 默认线程池核心线程数
     */
    public static final int DEFAULT_CORE_POOL_SIZE = 2;
    
    /**
     * 默认线程池最大线程数
     */
    public static final int DEFAULT_MAX_POOL_SIZE = 10;
    
    /**
     * 默认线程池空闲时间（秒）
     */
    public static final long DEFAULT_KEEP_ALIVE_TIME_SECONDS = 60L;
    
    /**
     * 日志级别枚举
     */
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR, OFF
    }
    
    /**
     * 性能监控级别枚举
     */
    public enum MonitoringLevel {
        /**
         * 关闭监控
         */
        OFF,
        
        /**
         * 基础监控：状态转换次数、错误次数
         */
        BASIC,
        
        /**
         * 详细监控：包含执行时间、内存使用等
         */
        DETAILED,
        
        /**
         * 完整监控：包含所有指标和性能分析
         */
        FULL
    }
    
    // 配置属性
    private LogLevel logLevel = LogLevel.INFO;
    private MonitoringLevel monitoringLevel = MonitoringLevel.BASIC;
    private boolean enableTransitionLogging = true;
    private boolean enableLifecycleCallbacks = true;
    private boolean enablePerformanceMonitoring = true;
    private boolean enableStateValidation = true;
    private boolean enableConcurrentAccess = true;
    private boolean enableAutoCleanup = true;
    
    // 超时和限制配置
    private long transitionTimeoutMs = DEFAULT_TRANSITION_TIMEOUT_MS;
    private long cleanupIntervalMs = DEFAULT_CLEANUP_INTERVAL_MS;
    private int maxStateMachines = DEFAULT_MAX_STATE_MACHINES;
    private int maxTransitionsPerSecond = 1000;
    private int maxStateDepth = 10;
    
    // 线程池配置
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;
    private long keepAliveTimeSeconds = DEFAULT_KEEP_ALIVE_TIME_SECONDS;
    private TimeUnit keepAliveTimeUnit = TimeUnit.SECONDS;
    
    // 缓存配置
    private boolean enableStateCache = true;
    private int stateCacheSize = 100;
    private long stateCacheExpirationMs = 300000L; // 5分钟
    
    // 错误处理配置
    private boolean enableErrorRecovery = true;
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 1000L;
    private boolean failFastOnError = false;
    
    /**
     * 默认构造函数
     */
    public StateMachineConfig() {
    }

    /**
     * 从Properties创建配置
     * 
     * @param properties 配置属性
     * @return 状态机配置实例
     */
    public static StateMachineConfig fromProperties(Properties properties) {
        StateMachineConfig config = new StateMachineConfig();
        
        // 日志配置
        config.setLogLevel(LogLevel.valueOf(
            properties.getProperty("statemachine.log.level", "INFO").toUpperCase()));
        config.setEnableTransitionLogging(Boolean.parseBoolean(
            properties.getProperty("statemachine.log.transitions", "true")));
        
        // 监控配置
        config.setMonitoringLevel(MonitoringLevel.valueOf(
            properties.getProperty("statemachine.monitoring.level", "BASIC").toUpperCase()));
        config.setEnablePerformanceMonitoring(Boolean.parseBoolean(
            properties.getProperty("statemachine.monitoring.performance", "true")));
        
        // 功能配置
        config.setEnableLifecycleCallbacks(Boolean.parseBoolean(
            properties.getProperty("statemachine.lifecycle.callbacks", "true")));
        config.setEnableStateValidation(Boolean.parseBoolean(
            properties.getProperty("statemachine.validation.enabled", "true")));
        config.setEnableConcurrentAccess(Boolean.parseBoolean(
            properties.getProperty("statemachine.concurrent.enabled", "true")));
        
        // 超时和限制配置
        config.setTransitionTimeoutMs(Long.parseLong(
            properties.getProperty("statemachine.timeout.transition", String.valueOf(DEFAULT_TRANSITION_TIMEOUT_MS))));
        config.setMaxStateMachines(Integer.parseInt(
            properties.getProperty("statemachine.limit.max", String.valueOf(DEFAULT_MAX_STATE_MACHINES))));
        config.setMaxTransitionsPerSecond(Integer.parseInt(
            properties.getProperty("statemachine.limit.transitions.per.second", "1000")));
        
        // 线程池配置
        config.setCorePoolSize(Integer.parseInt(
            properties.getProperty("statemachine.threadpool.core.size", String.valueOf(DEFAULT_CORE_POOL_SIZE))));
        config.setMaxPoolSize(Integer.parseInt(
            properties.getProperty("statemachine.threadpool.max.size", String.valueOf(DEFAULT_MAX_POOL_SIZE))));
        config.setKeepAliveTimeSeconds(Long.parseLong(
            properties.getProperty("statemachine.threadpool.keepalive.seconds", String.valueOf(DEFAULT_KEEP_ALIVE_TIME_SECONDS))));
        
        return config;
    }
    
    /**
     * 创建默认配置
     * 
     * @return 默认状态机配置实例
     */
    public static StateMachineConfig defaultConfig() {
        return new StateMachineConfig();
    }
    
    /**
     * 创建开发环境配置
     * 
     * @return 开发环境状态机配置实例
     */
    public static StateMachineConfig developmentConfig() {
        StateMachineConfig config = new StateMachineConfig();
        config.setLogLevel(LogLevel.DEBUG);
        config.setMonitoringLevel(MonitoringLevel.DETAILED);
        config.setEnableTransitionLogging(true);
        config.setEnablePerformanceMonitoring(true);
        config.setEnableStateValidation(true);
        return config;
    }
    
    /**
     * 创建生产环境配置
     * 
     * @return 生产环境状态机配置实例
     */
    public static StateMachineConfig productionConfig() {
        StateMachineConfig config = new StateMachineConfig();
        config.setLogLevel(LogLevel.WARN);
        config.setMonitoringLevel(MonitoringLevel.BASIC);
        config.setEnableTransitionLogging(false);
        config.setEnablePerformanceMonitoring(true);
        config.setEnableStateValidation(false);
        config.setFailFastOnError(true);
        return config;
    }
    
    // Getter和Setter方法

    /**
     * 验证配置有效性
     * 
     * @throws IllegalArgumentException 如果配置无效
     */
    public void validate() {
        if (transitionTimeoutMs <= 0) {
            throw new IllegalArgumentException("状态转换超时时间必须大于0");
        }
        if (maxStateMachines <= 0) {
            throw new IllegalArgumentException("最大状态机数量必须大于0");
        }
        if (corePoolSize <= 0) {
            throw new IllegalArgumentException("核心线程池大小必须大于0");
        }
        if (maxPoolSize < corePoolSize) {
            throw new IllegalArgumentException("最大线程池大小不能小于核心线程池大小");
        }
        if (maxRetryAttempts < 0) {
            throw new IllegalArgumentException("最大重试次数不能小于0");
        }
        if (retryDelayMs < 0) {
            throw new IllegalArgumentException("重试延迟时间不能小于0");
        }
    }
    
    @Override
    public String toString() {
        return "StateMachineConfig{" +
                "logLevel=" + logLevel +
                ", monitoringLevel=" + monitoringLevel +
                ", enableTransitionLogging=" + enableTransitionLogging +
                ", enableLifecycleCallbacks=" + enableLifecycleCallbacks +
                ", enablePerformanceMonitoring=" + enablePerformanceMonitoring +
                ", transitionTimeoutMs=" + transitionTimeoutMs +
                ", maxStateMachines=" + maxStateMachines +
                ", corePoolSize=" + corePoolSize +
                ", maxPoolSize=" + maxPoolSize +
                "}";
    }
}