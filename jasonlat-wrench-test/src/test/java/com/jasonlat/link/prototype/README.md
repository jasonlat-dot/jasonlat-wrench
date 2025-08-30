# 原型模式责任链测试模块

## 概述

本模块实现了基于原型模式的责任链设计模式，提供了灵活的业务逻辑处理链路配置和执行能力。通过Spring框架的依赖注入，可以轻松构建和管理不同的责任链配置。

## 模块结构

```
prototype/
├── factory/
│   └── Rule02TradeRuleFactory.java    # 交易规则工厂类
├── logic/
│   ├── RuleLogic201.java              # 第一个逻辑处理器
│   ├── RuleLogic202.java              # 第二个逻辑处理器
│   └── XxxResponse.java               # 响应对象
└── README.md                          # 本文档
```

## 核心组件

### 1. Rule02TradeRuleFactory

交易规则工厂类，负责创建和配置不同的责任链实例：

- **demo01**: 完整的责任链，包含 `RuleLogic201` 和 `RuleLogic202`
- **demo02**: 简化的责任链，只包含 `RuleLogic202`

```java
@Bean("demo01")
public BusinessLinkedList<String, DynamicContext, XxxResponse> demo01(
    RuleLogic201 ruleLogic201, RuleLogic202 ruleLogic202) {
    LinkArmory<String, DynamicContext, XxxResponse> linkArmory = 
        new LinkArmory<>("demo01", ruleLogic201, ruleLogic202);
    return linkArmory.getLogicLink();
}
```

### 2. 逻辑处理器

#### RuleLogic201
- 实现 `ILogicHandler` 接口
- 执行业务逻辑后调用 `next()` 方法继续链路执行
- 记录执行日志："link model02 RuleLogic201"

#### RuleLogic202
- 实现 `ILogicHandler` 接口
- 执行业务逻辑后调用 `stop()` 方法终止链路执行
- 返回固定响应："hi 小傅哥！"
- 记录执行日志："link model02 RuleLogic202"

### 3. 数据对象

#### DynamicContext
- 继承自框架的 `DynamicContext` 基类
- 包含 `age` 字段用于业务参数传递
- 使用 Lombok 注解简化代码

#### XxxResponse
- 简单的响应对象
- 包含 `age` 字段用于返回结果
- 提供构造函数和 getter 方法

## 使用方式

### 1. Spring 依赖注入

```java
@Resource(name = "demo01")
private BusinessLinkedList<String, Rule02TradeRuleFactory.DynamicContext, XxxResponse> demo01;

@Resource(name = "demo02")
private BusinessLinkedList<String, Rule02TradeRuleFactory.DynamicContext, XxxResponse> demo02;
```

### 2. 创建上下文

```java
Rule02TradeRuleFactory.DynamicContext context = 
    Rule02TradeRuleFactory.DynamicContext.builder()
        .age("25")
        .build();
```

### 3. 执行责任链

```java
// 执行完整链路（demo01）
XxxResponse result1 = demo01.apply("请求参数", context);

// 执行简化链路（demo02）
XxxResponse result2 = demo02.apply("请求参数", context);
```

## 测试说明

本模块的测试由 `PrototypeAppTest` 类提供，包含以下测试场景：

### 基本功能测试
- `testDemo01ChainBasicExecution()`: 测试demo01责任链基本功能
- `testDemo02ChainBasicExecution()`: 测试demo02责任链基本功能

### 高级功能测试
- `testPrototypeChainMultipleExecution()`: 多次执行测试
- `testPrototypeChainWithDifferentAges()`: 不同参数测试
- `testPrototypeChainEdgeCases()`: 边界情况测试
- `testPrototypeChainPerformance()`: 性能测试
- `testSpringDependencyInjection()`: 依赖注入测试
- `testRealWorldTradeScenario()`: 真实业务场景测试
- `testChainDifferences()`: 责任链差异性测试

### 运行测试

```bash
# 运行所有原型模式测试
mvn test -Dtest=PrototypeAppTest

# 运行特定测试方法
mvn test -Dtest=PrototypeAppTest#testDemo01ChainBasicExecution
```

## 设计特点

### 1. 原型模式优势
- **灵活配置**: 可以通过不同的Bean配置创建多种责任链组合
- **动态扩展**: 易于添加新的逻辑处理器和链路配置
- **Spring集成**: 充分利用Spring的依赖注入和生命周期管理

### 2. 责任链模式优势
- **解耦合**: 请求发送者和接收者解耦
- **动态组合**: 可以动态改变链内的成员或调整顺序
- **职责分离**: 每个处理器只关注自己的业务逻辑

### 3. 框架集成
- 基于 `jasonlat-wrench-starter-design-framework` 框架
- 使用 `LinkArmory` 进行链路装配
- 支持 `BusinessLinkedList` 链式执行
- 集成 `ILogicHandler` 处理器接口

## 扩展指南

### 添加新的逻辑处理器

1. 创建新的处理器类实现 `ILogicHandler` 接口
2. 在 `Rule02TradeRuleFactory` 中添加新的Bean配置
3. 在测试类中添加相应的测试用例

### 添加新的响应类型

1. 创建新的响应对象类
2. 更新泛型参数配置
3. 调整相关的处理器和测试代码

### 添加新的上下文字段

1. 在 `DynamicContext` 中添加新字段
2. 更新相关的Builder方法
3. 在测试中验证新字段的功能

## 注意事项

1. **异常处理**: 所有处理器都需要正确处理异常情况
2. **性能考虑**: 长链路可能影响性能，建议进行性能测试
3. **线程安全**: 处理器应该是无状态的，确保线程安全
4. **日志记录**: 每个处理器都应该记录适当的执行日志
5. **测试覆盖**: 新增功能必须有相应的测试用例

## 版本信息

- **版本**: 1.0
- **作者**: Jasonlat
- **创建日期**: 2025-08-29
- **框架版本**: jasonlat-wrench-starter-design-framework
- **Spring Boot版本**: 兼容Spring Boot 2.x+