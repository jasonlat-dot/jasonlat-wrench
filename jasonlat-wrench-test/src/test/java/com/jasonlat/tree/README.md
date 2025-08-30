# 策略模式规则树实现文档

## 📋 概述

本文档介绍了基于策略模式的规则决策树框架实现，该框架支持多线程异步数据加载、动态上下文传递和灵活的策略路由机制。

## 🏗️ 架构设计

### 核心组件

```
com.jasonlat.tree/
├── AbstractXxxSupport.java           # 抽象策略支持基类
├── factory/
│   └── DefaultStrategyFactory.java   # 默认策略工厂
└── node/
    ├── RootNode.java                 # 根节点
    ├── SwitchRoot.java               # 开关根节点
    ├── AccountNode.java              # 账户节点（支持多线程异步）
    ├── MemberLevel1Node.java         # 会员一级节点
    └── MemberLevel2Node.java         # 会员二级节点
```

### 执行流程

```
RootNode → SwitchRoot → AccountNode → MemberLevel1Node/MemberLevel2Node
    ↓           ↓           ↓                    ↓
  日志记录    日志记录   异步数据加载        返回最终结果
                         + 用户级别判断
                         + 策略路由选择
```

## 🔧 核心实现

### 1. 抽象基类 - AbstractXxxSupport

**功能特性**:
- 继承自 `AbstractMultiThreadStrategyRouter`
- 提供多线程异步数据加载的基础架构
- 为所有节点提供统一的日志记录和路由机制

**关键方法**:
```java
/**
 * 多线程异步数据加载方法
 * 默认实现为空，子类可根据需要重写
 */
protected void multiThread(String requestParameter, 
                          DefaultStrategyFactory.DynamicContext dynamicContext)
```

### 2. 策略工厂 - DefaultStrategyFactory

**核心功能**:
- 管理策略处理器的创建和获取
- 提供动态上下文数据结构
- 作为整个策略树的入口点

**动态上下文结构**:
```java
public static class DynamicContext {
    private int level;                           // 用户级别
    private final Map<String, Object> dataObjects; // 动态数据存储
    
    public <T> void setValue(String key, T value);  // 设置值
    public <T> T getValue(String key);               // 获取值
}
```

### 3. 节点实现详解

#### RootNode - 根节点
**职责**: 作为策略树的入口，负责初始化和路由到开关节点
```java
@Override
protected String doApply(String requestParameter, DynamicContext dynamicContext) {
    log.info("【开关节点】规则决策树 userId:{}", requestParameter);
    return router(requestParameter, dynamicContext);
}
```

#### SwitchRoot - 开关根节点
**职责**: 中间路由节点，直接路由到账户节点
```java
@Override
public StrategyHandler<String, DynamicContext, String> get(String requestParameter, 
                                                          DynamicContext dynamicContext) {
    return accountNode;
}
```

#### AccountNode - 账户节点（核心节点）
**职责**: 
- 执行多线程异步数据加载
- 模拟查询用户级别
- 根据账户状态和用户级别进行策略路由

**异步数据加载实现**:
```java
@Override
protected void multiThread(String requestParameter, DynamicContext dynamicContext) {
    // 异步查询账户标签
    CompletableFuture<String> accountType01 = CompletableFuture.supplyAsync(() -> {
        log.info("异步查询账户标签，账户标签；开户|冻结|止付|可用");
        return new Random().nextBoolean() ? "账户冻结" : "账户可用";
    }, threadPoolExecutor);
    
    // 异步查询授信数据
    CompletableFuture<String> accountType02 = CompletableFuture.supplyAsync(() -> {
        log.info("异步查询授信数据，拦截|已授信|已降档");
        return new Random().nextBoolean() ? "拦截" : "已授信";
    }, threadPoolExecutor);
    
    // 等待所有异步任务完成并存储结果
    CompletableFuture.allOf(accountType01, accountType02)
        .thenRun(() -> {
            dynamicContext.setValue("accountType01", accountType01.join());
            dynamicContext.setValue("accountType02", accountType02.join());
        }).join();
}
```

**策略路由逻辑**:
```java
@Override
public StrategyHandler<String, DynamicContext, String> get(String requestParameter, 
                                                          DynamicContext dynamicContext) {
    String accountType01 = dynamicContext.getValue("accountType01");
    String accountType02 = dynamicContext.getValue("accountType02");
    int level = dynamicContext.getLevel();
    
    // 路由规则：账户冻结 OR 拦截 OR 用户级别为1 → Level1Node
    if ("账户冻结".equals(accountType01) || "拦截".equals(accountType02) || level == 1) {
        return memberLevel1Node;
    }
    
    return memberLevel2Node;
}
```

#### MemberLevel1Node & MemberLevel2Node - 终端节点
**职责**: 
- 处理最终的业务逻辑
- 返回包含动态上下文的结果字符串
- 作为策略树的叶子节点

```java
@Override
protected String doApply(String requestParameter, DynamicContext dynamicContext) {
    log.info("【级别节点-1/2】规则决策树 userId:{}", requestParameter);
    return "level1/level2" + JSON.toJSONString(dynamicContext);
}
```

## 🚀 使用示例

### 基本使用
```java
@Autowired
private DefaultStrategyFactory strategyFactory;

// 获取策略处理器
StrategyHandler<String, DynamicContext, String> handler = strategyFactory.strategyHandler();

// 创建动态上下文
DynamicContext context = new DynamicContext();

// 执行策略
String result = handler.apply("user_001", context);
```

### 执行流程示例
```
输入: userId = "user_001"

1. RootNode: 记录日志，路由到 SwitchRoot
2. SwitchRoot: 记录日志，路由到 AccountNode
3. AccountNode: 
   - 执行异步数据加载
     * accountType01 = "账户可用" (随机)
     * accountType02 = "已授信" (随机)
   - 模拟查询用户级别: level = 0 (随机)
   - 根据路由规则选择 MemberLevel2Node
4. MemberLevel2Node: 返回 "level2{\"level\":0,\"dataObjects\":{\"accountType01\":\"账户可用\",\"accountType02\":\"已授信\"}}"
```

## 🔍 技术特点

### 1. 多线程异步支持
- 使用 `CompletableFuture` 实现异步数据加载
- 支持并行查询多个数据源
- 通过 `ThreadPoolExecutor` 管理线程池

### 2. 动态上下文传递
- 支持节点间数据共享
- 类型安全的数据存取
- 灵活的数据结构扩展

### 3. 灵活的策略路由
- 基于业务规则的动态路由
- 支持复杂的条件判断
- 易于扩展新的路由规则

### 4. 统一的日志记录
- 每个节点都有详细的执行日志
- 便于问题排查和性能监控
- 支持分布式链路追踪

## 📈 扩展指南

### 添加新节点
1. 继承 `AbstractXxxSupport` 类
2. 实现 `doApply` 和 `get` 方法
3. 根据需要重写 `multiThread` 方法
4. 使用 `@Component` 注解注册为Spring Bean

### 扩展动态上下文
```java
// 在 DynamicContext 中添加新字段
private String customField;

// 或使用 dataObjects 存储复杂对象
dynamicContext.setValue("customData", new CustomObject());
```

### 自定义路由规则
```java
@Override
public StrategyHandler<String, DynamicContext, String> get(String requestParameter, 
                                                          DynamicContext dynamicContext) {
    // 实现自定义路由逻辑
    if (customCondition(requestParameter, dynamicContext)) {
        return customNode;
    }
    return defaultNode;
}
```

---

**注意**: 本文档基于实际代码实现编写，展示了策略模式规则树框架的核心架构和实现细节。