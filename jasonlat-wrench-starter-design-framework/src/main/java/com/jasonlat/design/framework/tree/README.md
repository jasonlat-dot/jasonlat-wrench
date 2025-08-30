# 策略模式设计框架 (Tree Strategy Pattern Framework)

## 📖 概述

本模块实现了基于策略模式的规则决策树框架，提供了灵活的策略路由和处理能力。通过树形结构组织业务逻辑，支持多线程异步数据加载，实现了高效的规则引擎功能。

## 🏗️ 框架结构

```
tree/
├── AbstractMultiThreadStrategyRouter.java    # 多线程策略路由抽象类
├── AbstractStrategyRouter.java               # 策略路由抽象类
├── StrategyHandler.java                      # 策略处理器接口
├── StrategyMapper.java                       # 策略映射器接口
└── README.md                                 # 本文档
```

## 🔧 核心组件

### 1. StrategyMapper<T, D, R>
**策略映射器接口**
- 🎯 **功能**：根据请求参数和上下文选择合适的策略处理器
- 🔄 **动态选择**：支持基于业务规则的智能策略映射
- 🎨 **泛型设计**：支持多种数据类型的灵活配置

```java
public interface StrategyMapper<T, D, R> {
    StrategyHandler<T, D, R> get(T requestParameter, D dynamicContext) throws Exception;
}
```

### 2. StrategyHandler<T, D, R>
**策略处理器接口**
- ⚡ **处理逻辑**：定义具体的业务处理方法
- 🔧 **函数式接口**：支持Lambda表达式和方法引用
- 🛡️ **异常处理**：内置异常处理机制

```java
public interface StrategyHandler<T, D, R> {
    StrategyHandler DEFAULT = (T, D) -> null;
    R apply(T requestParameter, D dynamicContext) throws Exception;
}
```

### 3. AbstractStrategyRouter<T, D, R>
**策略路由抽象类**
- 🚀 **基础路由**：实现基本的策略路由功能
- 🔗 **接口整合**：同时实现StrategyMapper和StrategyHandler
- 🎯 **模板方法**：提供可扩展的路由模板

### 4. AbstractMultiThreadStrategyRouter<T, D, R>
**多线程策略路由抽象类**
- 🔄 **异步处理**：支持多线程异步数据加载
- ⚡ **性能优化**：提高大数据量场景下的处理效率
- 🛠️ **可选实现**：multiThread方法可根据需要选择性重写

## 🌳 规则决策树节点图

### 测试用例中的规则树结构

```
                    ┌─────────────┐
                    │  RootNode   │
                    │   (根节点)   │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ SwitchRoot  │
                    │  (开关节点)  │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ AccountNode │ ◄─── 🔥 多线程异步加载节点
                    │  (账户节点)  │
                    └──────┬──────┘
                           │
                    ┌──────┴──────┐
                    │             │
                    ▼             ▼
            ┌─────────────┐ ┌─────────────┐
            │accountType01│ │accountType02│
            │ (账户标签)   │ │ (授信数据)   │
            │异步查询线程1 │ │异步查询线程2 │
            └─────────────┘ └─────────────┘
                    │             │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │  决策逻辑    │
                    │ (路由选择)   │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │账户冻结      │ │    拦截      │ │  level=1   │
    │             │ │             │ │            │
    └──────┬──────┘ └──────┬──────┘ └──────┬─────┘
           │               │               │
           └───────────────┼───────────────┘
                           │
                           ▼
                ┌─────────────────┐
                │ MemberLevel1Node│
                │   (级别节点-1)   │
                └─────────────────┘
                           │
                           ▼
                ┌─────────────────┐
                │返回: level1 +   │
                │    context      │
                └─────────────────┘

                                    其他情况
                                        │
                                        ▼
                                ┌─────────────────┐
                                │ MemberLevel2Node│
                                │   (级别节点-2)   │
                                └─────────────────┘
                                        │
                                        ▼
                                ┌─────────────────┐
                                │返回: level2 +   │
                                │    context      │
                                └─────────────────┘
```

### 节点执行流程

```
1. 请求进入 → RootNode (根节点)
   ↓
2. 路由到 → SwitchRoot (开关节点)
   ↓
3. 路由到 → AccountNode (账户节点)
   ↓
4. 异步加载数据:
   - accountType01: 账户标签 (开户|冻结|止付|可用)
   - accountType02: 授信数据 (拦截|已授信|已降档)
   ↓
5. 决策路由:
   - 如果 accountType01 = "账户冻结" → MemberLevel1Node
   - 如果 accountType02 = "拦截" → MemberLevel1Node  
   - 如果 level = 1 → MemberLevel1Node
   - 其他情况 → MemberLevel2Node
   ↓
6. 返回处理结果
```

## ⚙️ 多线程配置

### ThreadExecutorPool Bean 配置

在使用多线程异步加载功能时，需要配置线程池Bean：

```java
@Configuration
public class ThreadPoolConfig {
    
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(
            5,                          // 核心线程数
            10,                         // 最大线程数
            60L,                        // 空闲线程存活时间
            TimeUnit.SECONDS,           // 时间单位
            new LinkedBlockingQueue<>(100),  // 工作队列
            new ThreadFactoryBuilder()
                .setNameFormat("strategy-pool-%d")
                .build(),               // 线程工厂
            new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略
        );
    }
}
```

### 🔥 多线程使用场景

**AccountNode 中的多线程异步加载：**

```java
@Override
protected void multiThread(String requestParameter, DefaultStrategyFactory.DynamicContext dynamicContext) 
        throws ExecutionException, InterruptedException, TimeoutException {
    
    // 🔥 异步查询账户标签 - 线程1
    CompletableFuture<String> accountType01 = CompletableFuture.supplyAsync(() -> {
        log.info("异步查询账户标签，账户标签；开户|冻结|止付|可用");
        return new Random().nextBoolean() ? "账户冻结" : "账户可用";
    }, threadPoolExecutor);  // ← 需要注入 ThreadPoolExecutor

    // 🔥 异步查询授信数据 - 线程2
    CompletableFuture<String> accountType02 = CompletableFuture.supplyAsync(() -> {
        log.info("异步查询授信数据，拦截|已授信|已降档");
        return new Random().nextBoolean() ? "拦截" : "已授信";
    }, threadPoolExecutor);  // ← 需要注入 ThreadPoolExecutor

    // 等待所有异步任务完成
    CompletableFuture.allOf(accountType01, accountType02)
            .thenRun(() -> {
                dynamicContext.setValue("accountType01", accountType01.join());
                dynamicContext.setValue("accountType02", accountType02.join());
            }).join();
}
```

**⚠️ 重要提示：**
- 只有 `AccountNode` 需要使用多线程异步加载
- 其他节点（RootNode、SwitchRoot、MemberLevel1Node、MemberLevel2Node）不需要重写 `multiThread` 方法
- 必须在Spring配置中提供 `ThreadPoolExecutor` Bean

## 🚀 使用方式

### 1. 基本策略节点实现

```java
@Component
public class CustomNode extends AbstractMultiThreadStrategyRouter<String, DynamicContext, String> {
    
    @Override
    protected void multiThread(String requestParameter, DynamicContext dynamicContext) 
            throws ExecutionException, InterruptedException, TimeoutException {
        // 异步数据加载（可选）
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            // 异步查询逻辑
            return "异步数据";
        });
        
        dynamicContext.setValue("asyncData", future.get());
    }
    
    @Override
    protected String doApply(String requestParameter, DynamicContext dynamicContext) throws Exception {
        // 业务处理逻辑
        return router(requestParameter, dynamicContext);
    }
    
    @Override
    public StrategyHandler<String, DynamicContext, String> get(String requestParameter, DynamicContext dynamicContext) {
        // 策略选择逻辑
        if (someCondition) {
            return nextNodeA;
        }
        return nextNodeB;
    }
}
```

### 2. 策略工厂配置

```java
@Service
public class StrategyFactory {
    
    @Autowired
    private RootNode rootNode;
    
    public StrategyHandler<String, DynamicContext, String> getStrategyHandler() {
        return rootNode;
    }
}
```

### 3. 动态上下文使用

```java
@Data
public class DynamicContext {
    private int level;
    private Map<String, Object> dataObjects = new HashMap<>();
    
    public <T> void setValue(String key, T value) {
        dataObjects.put(key, value);
    }
    
    public <T> T getValue(String key) {
        return (T) dataObjects.get(key);
    }
}
```

## 🎯 设计特点

### 1. 策略模式优势
- **动态选择**：运行时根据条件选择不同的处理策略
- **易于扩展**：新增策略节点无需修改现有代码
- **职责分离**：每个节点专注于特定的业务逻辑

### 2. 树形结构优势
- **层次清晰**：业务逻辑按层次组织，便于理解和维护
- **路径灵活**：支持多种决策路径和条件分支
- **可视化强**：树形结构便于业务流程的可视化展示

### 3. 多线程支持
- **异步加载**：支持并行数据获取，提高处理效率
- **性能优化**：在数据密集型场景下显著提升性能
- **可选实现**：根据节点需要选择性启用多线程功能

### 4. 泛型设计
- **类型安全**：编译时类型检查，避免运行时错误
- **灵活配置**：支持不同类型的请求参数、上下文和返回值
- **代码复用**：同一套框架支持多种业务场景

## 📊 性能特性

### 1. 多线程异步处理
- **并发数据加载**：支持多个数据源的并行查询
- **线程池管理**：合理利用系统资源，避免线程创建开销
- **超时控制**：防止长时间等待影响系统响应

### 2. 内存优化
- **按需加载**：只在需要时才执行多线程数据加载
- **上下文复用**：动态上下文支持数据的高效传递和复用
- **垃圾回收友好**：合理的对象生命周期管理

## 🧪 测试指南

### 测试用例位置
- **测试目录**：`jasonlat-wrench-test/src/test/java/com/jasonlat/tree/`
- **节点实现**：`node/` 目录下的各种节点实现
- **工厂类**：`factory/DefaultStrategyFactory.java`

### 运行测试

```bash
# 运行所有策略模式测试
mvn test -Dtest=*Tree*

# 运行特定节点测试
mvn test -Dtest=TreeAppTest
```

### 测试覆盖场景
- ✅ 基本策略路由功能
- ✅ 多线程异步数据加载
- ✅ 决策树路径选择
- ✅ 异常处理机制
- ✅ Spring集成测试
- ✅ 性能压力测试

## 🔧 扩展指南

### 添加新的策略节点

1. **创建节点类**
```java
@Component
public class NewStrategyNode extends AbstractMultiThreadStrategyRouter<String, DynamicContext, String> {
    // 实现必要的方法
}
```

2. **配置依赖注入**
```java
@Autowired
private NewStrategyNode newStrategyNode;
```

3. **更新路由逻辑**
```java
public StrategyHandler<String, DynamicContext, String> get(String requestParameter, DynamicContext dynamicContext) {
    if (newCondition) {
        return newStrategyNode;
    }
    return existingNode;
}
```

### 自定义上下文类型

```java
public class CustomContext extends DynamicContext {
    private String customField;
    // 添加业务特定的字段和方法
}
```

## ⚠️ 注意事项

1. **线程安全**：确保节点实现的线程安全性
2. **异常处理**：合理处理业务异常，避免影响整个决策流程
3. **性能监控**：关注多线程场景下的性能表现
4. **内存管理**：避免在上下文中存储大量数据导致内存泄漏
5. **循环依赖**：避免节点间的循环引用

## 📚 相关文档

- [单例模式责任链使用指南](../link/singleton/README.md)
- [原型模式责任链使用指南](../link/prototype/README.md)
- [项目整体架构说明](../../../../../../README.md)

## 📝 版本信息

- **当前版本**：1.0.0
- **最后更新**：2025-01-20
- **维护状态**：活跃开发中

---

> 💡 **提示**：本框架采用策略模式和树形结构的结合，为复杂业务规则的实现提供了清晰、可扩展的解决方案。通过合理的节点设计和路由配置，可以构建出高效、易维护的规则引擎系统。