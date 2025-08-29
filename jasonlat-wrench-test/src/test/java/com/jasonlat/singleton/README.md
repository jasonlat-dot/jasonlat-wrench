# 单例模式责任链测试模块

## 📖 概述

本模块提供了基于单例模式的责任链设计模式测试实现，专注于测试责任链在单例模式下的行为特性和性能表现。通过完整的测试用例验证责任链的正确性、稳定性和可靠性。

## 🏗️ 模块结构

```
singleton/
├── factory/
│   └── Rule01TradeRuleFactory.java     # 单例模式交易规则工厂
├── logic/
│   ├── RuleLogic101.java               # 第一个逻辑处理器
│   └── RuleLogic102.java               # 第二个逻辑处理器
└── README.md                           # 本文档
```

## 🔧 核心组件

### Rule01TradeRuleFactory
**单例模式交易规则工厂类**
- 🎯 **功能**：提供交易规则责任链的构建和管理
- 🔗 **责任链构建**：通过 `openLogicLink()` 方法创建完整的处理链路
- 📊 **内部上下文**：包含 `DynamicContext` 内部类用于数据传递
- 🏭 **工厂模式**：采用工厂方法模式创建责任链实例

### 逻辑处理器

#### RuleLogic101
- 📝 **类型**：继承自 `AbstractLogicLink`
- 🔄 **行为**：处理请求后继续传递给下一个节点
- 🎯 **职责**：执行第一阶段的业务逻辑处理
- ⚡ **特性**：支持链式调用，保证处理流程的连续性

#### RuleLogic102
- 📝 **类型**：继承自 `AbstractLogicLink`
- 🏁 **行为**：作为链路的终结节点，返回最终处理结果
- 🎯 **职责**：执行最终的业务逻辑处理和结果封装
- ✅ **特性**：提供链路执行的最终输出

### 数据对象

#### DynamicContext
- 📦 **用途**：责任链执行过程中的上下文数据容器
- 🔄 **生命周期**：贯穿整个责任链执行过程
- 📊 **数据管理**：支持动态数据的存储和传递
- 🔒 **线程安全**：在单例模式下保证数据的一致性

## 🚀 使用方式

### 基本使用示例

```java
// 1. 获取工厂实例
Rule01TradeRuleFactory factory = new Rule01TradeRuleFactory();

// 2. 构建责任链
ILogicLink<String, DynamicContext, String> chain = factory.openLogicLink();

// 3. 创建上下文
DynamicContext context = new DynamicContext();
context.setAge("25");

// 4. 执行责任链
String result = chain.apply("test_request", context);

// 5. 验证结果
System.out.println("处理结果: " + result);
```

### Spring 集成使用

```java
@SpringBootTest
public class SingletonChainTest {
    
    @Autowired
    private Rule01TradeRuleFactory factory;
    
    @Test
    public void testSpringIntegration() {
        // Spring 自动注入的工厂实例
        ILogicLink<String, DynamicContext, String> chain = factory.openLogicLink();
        
        DynamicContext context = new DynamicContext();
        context.setAge("30");
        
        String result = chain.apply("spring_test", context);
        assertNotNull(result);
    }
}
```

## 🧪 测试说明

### 测试类位置
- **主测试类**：`AppTest.java`（项目根测试目录）
- **专用测试类**：`SingletonChainTest.java`（框架模块测试目录）

### 测试覆盖范围

#### 功能测试
- ✅ **基本执行测试**：验证责任链的正常执行流程
- ✅ **重复使用测试**：验证单例模式下的重复调用能力
- ✅ **参数变化测试**：测试不同参数下的处理结果
- ✅ **边界条件测试**：验证异常情况和边界值处理

#### 性能测试
- ⚡ **执行效率测试**：测量责任链的执行时间
- 🔄 **并发安全测试**：验证单例模式下的线程安全性
- 📊 **内存使用测试**：监控内存占用和垃圾回收

#### 集成测试
- 🌱 **Spring 依赖注入测试**：验证与 Spring 框架的集成
- 🔗 **完整流程测试**：模拟真实业务场景的端到端测试

### 运行测试

```bash
# 运行单例模式测试
mvn test -Dtest=AppTest

# 运行特定测试方法
mvn test -Dtest=AppTest#testSingletonChainBasicExecution

# 运行性能测试
mvn test -Dtest=AppTest#testSingletonChainPerformance
```

## 🎨 设计特点

### 单例模式优势
- 🏭 **资源节约**：全局唯一实例，减少内存占用
- ⚡ **访问效率**：避免重复创建，提高访问速度
- 🔒 **状态一致**：保证全局状态的一致性
- 🎯 **控制实例化**：严格控制类的实例化过程

### 责任链特性
- 🔗 **松耦合设计**：处理器之间相互独立
- 🔄 **动态组合**：支持运行时动态调整链路结构
- 📈 **易于扩展**：新增处理器无需修改现有代码
- 🎯 **职责分离**：每个处理器专注于特定的业务逻辑

### 架构优势
- 🏗️ **模块化设计**：清晰的模块边界和职责划分
- 🧪 **可测试性**：完整的测试覆盖和验证机制
- 📚 **可维护性**：良好的代码结构和文档说明
- 🔧 **可配置性**：支持灵活的配置和定制

## 🔧 扩展指南

### 添加新的逻辑处理器

1. **创建处理器类**
```java
public class RuleLogic103 extends AbstractLogicLink<String, DynamicContext, String> {
    @Override
    public String apply(String request, DynamicContext context) throws Exception {
        // 实现具体的业务逻辑
        String result = processBusinessLogic(request, context);
        
        // 决定是否继续传递
        if (shouldContinue(result)) {
            return next(request, context);
        }
        return result;
    }
}
```

2. **更新工厂类**
```java
public ILogicLink<String, DynamicContext, String> openLogicLink() {
    RuleLogic101 logic101 = new RuleLogic101();
    RuleLogic102 logic102 = new RuleLogic102();
    RuleLogic103 logic103 = new RuleLogic103(); // 新增
    
    logic101.appendNext(logic103).appendNext(logic102); // 调整链路
    return logic101;
}
```

3. **添加测试用例**
```java
@Test
public void testNewLogicProcessor() {
    // 测试新增的处理器功能
}
```

### 自定义上下文对象

```java
public class CustomContext extends DynamicContext {
    private String customField;
    private Map<String, Object> additionalData;
    
    // 添加自定义字段和方法
}
```

## ⚠️ 注意事项

### 单例模式注意点
- 🔒 **线程安全**：确保在多线程环境下的安全性
- 💾 **内存泄漏**：注意长期持有的对象引用
- 🔄 **状态管理**：避免在单例中维护可变状态
- 🧪 **测试困难**：单例可能影响单元测试的独立性

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

## 📊 版本信息

- **当前版本**：v1.0
- **兼容性**：Java 8+, Spring Boot 2.7+
- **最后更新**：2024年
- **维护状态**：活跃维护

## 🔗 相关链接

- [原型模式责任链测试模块](../prototype/README.md)
- [项目主文档](../../../../../../../../../README.md)
- [设计模式框架文档](../../../../../../../main/java/com/jasonlat/design/framework/link/singleton/README.md)

---

**💡 提示**：本模块专注于单例模式下的责任链测试，如需了解原型模式的实现，请参考 [原型模式测试模块](../prototype/README.md)。