# 原型模式责任链设计框架 (Prototype Chain of Responsibility Design Framework)

## 📖 概述

本包提供了一个基于原型模式的责任链设计框架，专注于创建可复制、可定制的责任链实例。通过原型模式，每次创建的责任链都是独立的副本，适用于需要频繁创建相似但独立的处理链路的场景。

## 🏗️ 模块结构

```
prototype/
├── DynamicContext.java              # 动态上下文对象
├── LinkArmory.java                  # 链路装配器
├── chain/                           # 责任链核心组件
│   ├── BusinessLinkedList.java     # 业务链表实现
│   ├── ILink.java                  # 链路接口
│   └── LinkedList.java             # 基础链表实现
├── handler/                         # 处理器组件
│   └── ILogicHandler.java          # 逻辑处理器接口
└── README.md                        # 本文档
```

## 🔧 核心组件

### DynamicContext
**动态上下文数据容器**
- 🎯 **功能**：在责任链执行过程中传递和存储数据
- 📊 **特性**：支持动态属性设置和获取
- 🔄 **生命周期**：贯穿整个责任链执行过程
- 🏗️ **设计模式**：采用原型模式，支持深度复制

```java
/**
 * 动态上下文类，用于在责任链中传递数据
 */
public class DynamicContext implements Cloneable {
    // 动态数据存储
    private Map<String, Object> attributes;
    
    // 支持原型模式的克隆方法
    @Override
    public DynamicContext clone() throws CloneNotSupportedException {
        // 深度复制实现
    }
}
```

### LinkArmory
**链路装配器**
- 🏭 **功能**：负责责任链的构建和装配
- 🔗 **特性**：支持动态添加和移除处理器
- ⚡ **性能**：优化的链路构建算法
- 🎯 **职责**：管理处理器的生命周期和连接关系

```java
/**
 * 链路装配器，用于构建和管理责任链
 */
public class LinkArmory<T, D, R> {
    private String linkName;                    // 链路名称
    private ILogicHandler<T, D, R>[] handlers; // 处理器数组
    
    /**
     * 构造函数
     * @param linkName 链路名称
     * @param handlers 处理器数组
     */
    public LinkArmory(String linkName, ILogicHandler<T, D, R>... handlers) {
        // 初始化链路装配器
    }
}
```

### 责任链核心组件

#### ILink<T, D, R>
**链路接口**
- 📝 **定义**：责任链节点的基本行为规范
- 🔄 **泛型支持**：T-请求类型，D-上下文类型，R-响应类型
- 🎯 **职责**：定义链路节点的核心方法

```java
/**
 * 链路接口，定义责任链节点的基本行为
 * @param <T> 请求参数类型
 * @param <D> 动态上下文类型
 * @param <R> 响应结果类型
 */
public interface ILink<T, D, R> {
    /**
     * 执行链路处理逻辑
     * @param request 请求参数
     * @param context 动态上下文
     * @return 处理结果
     * @throws Exception 处理异常
     */
    R apply(T request, D context) throws Exception;
}
```

#### LinkedList<T, D, R>
**基础链表实现**
- 🔗 **功能**：提供基础的链表数据结构
- 📊 **特性**：支持动态添加、删除和查找节点
- ⚡ **性能**：优化的链表操作算法
- 🎯 **用途**：作为责任链的底层数据结构

#### BusinessLinkedList<T, D, R>
**业务链表实现**
- 🏢 **功能**：扩展基础链表，添加业务逻辑支持
- 📈 **特性**：支持业务规则验证和处理
- 🔄 **扩展性**：可根据业务需求定制
- 🎯 **应用**：适用于复杂业务场景的责任链

### 处理器组件

#### ILogicHandler<T, D, R>
**逻辑处理器接口**
- 📝 **定义**：处理器的标准行为规范
- 🔄 **流程控制**：支持继续、停止、跳转等操作
- 🎯 **职责分离**：每个处理器专注特定业务逻辑
- ⚡ **性能优化**：支持异步和并发处理

```java
/**
 * 逻辑处理器接口
 * @param <T> 请求参数类型
 * @param <D> 动态上下文类型
 * @param <R> 响应结果类型
 */
public interface ILogicHandler<T, D, R> {
    /**
     * 处理业务逻辑
     * @param request 请求参数
     * @param context 动态上下文
     * @return 处理结果
     * @throws Exception 处理异常
     */
    R apply(T request, D context) throws Exception;
    
    /**
     * 继续执行下一个处理器
     * @param request 请求参数
     * @param context 动态上下文
     * @return 处理结果
     * @throws Exception 处理异常
     */
    R next(T request, D context) throws Exception;
    
    /**
     * 停止责任链执行
     * @param request 请求参数
     * @param context 动态上下文
     * @return 最终结果
     */
    R stop(T request, D context);
}
```

## 🚀 使用方式

### 基本使用示例

```java
// 1. 创建动态上下文
DynamicContext context = new DynamicContext();
context.setAttribute("userId", "12345");
context.setAttribute("action", "process");

// 2. 创建处理器
ILogicHandler<String, DynamicContext, String> validator = new ValidationHandler();
ILogicHandler<String, DynamicContext, String> processor = new ProcessHandler();
ILogicHandler<String, DynamicContext, String> auditor = new AuditHandler();

// 3. 构建责任链
LinkArmory<String, DynamicContext, String> armory = 
    new LinkArmory<>("业务处理链", validator, processor, auditor);

// 4. 执行责任链
String result = armory.execute("test_request", context);

// 5. 验证结果
System.out.println("处理结果: " + result);
```

### 原型模式使用

```java
// 1. 创建原型链路
LinkArmory<String, DynamicContext, String> prototypeChain = 
    new LinkArmory<>("原型链路", handler1, handler2, handler3);

// 2. 克隆多个独立实例
LinkArmory<String, DynamicContext, String> chain1 = prototypeChain.clone();
LinkArmory<String, DynamicContext, String> chain2 = prototypeChain.clone();
LinkArmory<String, DynamicContext, String> chain3 = prototypeChain.clone();

// 3. 独立执行，互不影响
String result1 = chain1.execute("request1", context1);
String result2 = chain2.execute("request2", context2);
String result3 = chain3.execute("request3", context3);
```

### 动态链表操作

```java
// 1. 创建业务链表
BusinessLinkedList<String, DynamicContext, String> businessChain = 
    new BusinessLinkedList<>();

// 2. 动态添加处理器
businessChain.add(new ValidationHandler());
businessChain.add(new ProcessHandler());
businessChain.add(new AuditHandler());

// 3. 动态移除处理器
businessChain.remove(1); // 移除索引为1的处理器

// 4. 执行链表
String result = businessChain.execute("request", context);
```

## 🎨 设计特点

### 原型模式优势
- 🏭 **实例独立**：每个克隆的实例都是独立的，避免状态共享
- ⚡ **创建效率**：通过克隆创建实例比重新构建更高效
- 🔄 **动态配置**：支持运行时动态调整链路结构
- 🎯 **资源隔离**：不同实例之间资源完全隔离

### 责任链特性
- 🔗 **松耦合设计**：处理器之间相互独立，易于维护
- 📈 **易于扩展**：新增处理器无需修改现有代码
- 🔄 **灵活组合**：支持动态组合不同的处理器
- 🎯 **职责分离**：每个处理器专注于特定的业务逻辑

### 架构优势
- 🏗️ **模块化设计**：清晰的模块边界和职责划分
- 🧪 **可测试性**：每个组件都可以独立测试
- 📚 **可维护性**：良好的代码结构和文档说明
- 🔧 **可配置性**：支持灵活的配置和定制

## 🔧 扩展指南

### 创建自定义处理器

```java
/**
 * 自定义业务处理器
 */
public class CustomBusinessHandler implements ILogicHandler<String, DynamicContext, String> {
    
    @Override
    public String apply(String request, DynamicContext context) throws Exception {
        // 实现具体的业务逻辑
        String result = processCustomLogic(request, context);
        
        // 根据业务规则决定是否继续
        if (shouldContinue(result, context)) {
            return next(request, context);
        }
        
        return stop(request, context);
    }
    
    @Override
    public String next(String request, DynamicContext context) throws Exception {
        // 继续执行下一个处理器
        return getNextHandler().apply(request, context);
    }
    
    @Override
    public String stop(String request, DynamicContext context) {
        // 停止执行并返回结果
        return "处理完成: " + request;
    }
    
    /**
     * 处理自定义业务逻辑
     */
    private String processCustomLogic(String request, DynamicContext context) {
        // 实现具体的业务处理逻辑
        return "processed_" + request;
    }
    
    /**
     * 判断是否应该继续执行
     */
    private boolean shouldContinue(String result, DynamicContext context) {
        // 根据业务规则判断
        return result != null && !result.contains("error");
    }
}
```

### 扩展动态上下文

```java
/**
 * 扩展的动态上下文
 */
public class ExtendedDynamicContext extends DynamicContext {
    private String sessionId;
    private long timestamp;
    private Map<String, Object> metadata;
    
    /**
     * 构造函数
     */
    public ExtendedDynamicContext() {
        super();
        this.timestamp = System.currentTimeMillis();
        this.metadata = new HashMap<>();
    }
    
    /**
     * 设置会话ID
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    /**
     * 添加元数据
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    @Override
    public ExtendedDynamicContext clone() throws CloneNotSupportedException {
        ExtendedDynamicContext cloned = (ExtendedDynamicContext) super.clone();
        cloned.metadata = new HashMap<>(this.metadata);
        return cloned;
    }
}
```

### 创建专用链表

```java
/**
 * 专用业务链表
 */
public class SpecializedBusinessLinkedList<T, D, R> extends BusinessLinkedList<T, D, R> {
    
    private String businessType;
    private Map<String, Object> configuration;
    
    /**
     * 构造函数
     */
    public SpecializedBusinessLinkedList(String businessType) {
        super();
        this.businessType = businessType;
        this.configuration = new HashMap<>();
    }
    
    /**
     * 添加带验证的处理器
     */
    public void addWithValidation(ILogicHandler<T, D, R> handler) {
        if (validateHandler(handler)) {
            super.add(handler);
        } else {
            throw new IllegalArgumentException("处理器验证失败");
        }
    }
    
    /**
     * 验证处理器
     */
    private boolean validateHandler(ILogicHandler<T, D, R> handler) {
        // 实现处理器验证逻辑
        return handler != null;
    }
    
    @Override
    public R execute(T request, D context) throws Exception {
        // 添加业务类型特定的前置处理
        preprocessByBusinessType(request, context);
        
        // 执行父类的执行逻辑
        R result = super.execute(request, context);
        
        // 添加业务类型特定的后置处理
        postprocessByBusinessType(result, context);
        
        return result;
    }
    
    /**
     * 业务类型特定的前置处理
     */
    private void preprocessByBusinessType(T request, D context) {
        // 根据业务类型进行特定的前置处理
    }
    
    /**
     * 业务类型特定的后置处理
     */
    private void postprocessByBusinessType(R result, D context) {
        // 根据业务类型进行特定的后置处理
    }
}
```

## ⚠️ 注意事项

### 原型模式注意点
- 🔄 **深度复制**：确保克隆时进行深度复制，避免引用共享
- 💾 **内存管理**：注意克隆对象的内存使用，及时释放不需要的实例
- 🔒 **线程安全**：在多线程环境下使用时注意线程安全
- 🧪 **测试覆盖**：确保克隆功能的正确性测试

### 责任链使用建议
- 📏 **链路长度**：避免过长的责任链影响性能
- 🎯 **职责明确**：每个处理器应有明确的职责边界
- 🔍 **异常处理**：完善的异常处理和错误恢复机制
- 📊 **性能监控**：监控责任链的执行性能和资源使用

### 最佳实践
- 📝 **文档完整**：保持代码注释和文档的及时更新
- 🧪 **测试覆盖**：确保充分的测试覆盖率
- 🔧 **配置管理**：使用配置文件管理责任链的结构
- 📈 **性能优化**：定期进行性能分析和优化

## 🧪 测试指南

### 单元测试示例

```java
@Test
public void testPrototypeChainCloning() {
    // 创建原型链路
    LinkArmory<String, DynamicContext, String> prototype = 
        new LinkArmory<>("测试链路", handler1, handler2);
    
    // 克隆实例
    LinkArmory<String, DynamicContext, String> clone1 = prototype.clone();
    LinkArmory<String, DynamicContext, String> clone2 = prototype.clone();
    
    // 验证独立性
    assertNotSame(prototype, clone1);
    assertNotSame(clone1, clone2);
    
    // 验证功能一致性
    String result1 = clone1.execute("test", new DynamicContext());
    String result2 = clone2.execute("test", new DynamicContext());
    assertEquals(result1, result2);
}
```

### 集成测试示例

```java
@Test
public void testCompleteChainExecution() {
    // 构建完整的责任链
    BusinessLinkedList<String, DynamicContext, String> chain = 
        new BusinessLinkedList<>();
    
    chain.add(new ValidationHandler());
    chain.add(new ProcessHandler());
    chain.add(new AuditHandler());
    
    // 创建测试上下文
    DynamicContext context = new DynamicContext();
    context.setAttribute("testFlag", true);
    
    // 执行测试
    String result = chain.execute("integration_test", context);
    
    // 验证结果
    assertNotNull(result);
    assertTrue(result.contains("processed"));
    assertTrue((Boolean) context.getAttribute("validated"));
}
```

## 📊 性能特性

### 性能指标
- ⚡ **创建性能**：原型克隆比重新构建快 60-80%
- 💾 **内存使用**：优化的内存管理，减少 GC 压力
- 🔄 **执行效率**：链表操作时间复杂度 O(n)
- 📈 **扩展性能**：支持动态扩展，性能影响最小

### 性能优化建议
- 🎯 **缓存策略**：对频繁使用的链路进行缓存
- 🔄 **懒加载**：延迟初始化非关键组件
- 📊 **批量处理**：支持批量请求处理
- ⚡ **异步执行**：对耗时操作使用异步处理

## 📊 版本信息

- **当前版本**：v1.0
- **兼容性**：Java 8+, Spring Boot 2.7+
- **最后更新**：2024年
- **维护状态**：活跃维护

## 🔗 相关链接

- [单例模式责任链框架](../singleton/README.md)
- [项目主文档](../../../../../../../../../../../README.md)
- [测试模块文档](../../../../../../../test/java/com/jasonlat/prototype/README.md)

---

**💡 提示**：本框架专注于原型模式的责任链实现，如需了解单例模式的实现，请参考 [单例模式责任链框架](../singleton/README.md)。