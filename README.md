# JasonLat Wrench

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-8+-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.12-green.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)](https://maven.apache.org/)

## 📖 项目简介

**JasonLat Wrench** 是一个企业级Java工具库，专注于提供高质量、可复用的设计模式实现和开发工具。项目采用模块化设计，旨在帮助开发者快速构建稳定、可维护的企业级应用。

### 🎯 核心特性

- **🔗 责任链模式框架** - 提供完整的责任链模式实现，支持灵活的业务流程编排
- **🌳 策略模式规则树** - 基于策略模式的规则决策树框架，支持多线程异步数据加载
- **🔄 状态机模式框架** - 提供企业级状态机实现，支持状态转换、生命周期管理和监控统计
- **🏗️ 企业级架构** - 基于Spring Boot生态，遵循企业级开发最佳实践
- **📦 模块化设计** - 采用Maven多模块架构，支持按需引入
- **🧪 完整测试覆盖** - 提供全面的单元测试和集成测试
- **📚 详细文档** - 包含完整的API文档和使用示例

## 🏗️ 项目架构

```
jasonlat-wrench/
├── jasonlat-wrench-starter-design-framework/    # 设计模式框架模块
│   ├── src/main/java/
│   │   └── com/jasonlat/design/
│   │       └── framework/
│   │           ├── link/                        # 责任链模式实现
│   │           │   ├── singleton/              # 单例模式责任链
│   │           │   └── prototype/              # 原型模式责任链
│   │           ├── tree/                        # 策略模式规则树实现
│   │           │   ├── AbstractStrategyRouter.java
│   │           │   ├── AbstractMultiThreadStrategyRouter.java
│   │           │   ├── StrategyHandler.java
│   │           │   └── StrategyMapper.java
│   │           └── state/                       # 状态机模式实现
│   │               ├── IState.java             # 状态接口
│   │               ├── IStateContext.java      # 状态上下文接口
│   │               ├── AbstractState.java      # 抽象状态实现
│   │               ├── AbstractStateContext.java # 抽象状态上下文
│   │               └── machine/                # 状态机管理组件
│   │                   ├── StateMachine.java   # 状态机管理器
│   │                   ├── StateMachineBuilder.java # 状态机构建器
│   │                   └── StateMachineConfig.java  # 状态机配置
│   └── src/test/java/                          # 测试代码
├── jasonlat-wrench-test/                       # 测试模块
│   └── src/test/java/com/jasonlat/tree/        # 规则树测试用例
├── pom.xml                                     # 父级POM配置
├── LICENSE                                     # MIT许可证
└── README.md                                   # 项目文档
```

## 🚀 快速开始

### 环境要求

- **Java**: 8 或更高版本
- **Maven**: 3.6 或更高版本
- **Spring Boot**: 2.7.12

### 安装依赖

在你的项目中添加以下Maven依赖：

```xml
<dependency>
    <groupId>com.jasonlat.dot</groupId>
    <artifactId>jasonlat-wrench-starter-design-framework</artifactId>
    <version>1.0</version>
</dependency>
```

### 基本使用

#### 单例模式责任链示例

```java
import com.jasonlat.design.framework.link.singleton.*;

// 1. 创建责任链节点
public class ValidationLink extends AbstractLogicLink<String, Map<String, Object>, Boolean> {
    @Override
    public Boolean apply(String request, Map<String, Object> context) throws Exception {
        // 验证逻辑
        if (request == null || request.isEmpty()) {
            return false;
        }
        return next(request, context);
    }
}

// 2. 构建责任链
ValidationLink validator = new ValidationLink();
ProcessLink processor = new ProcessLink();
validator.appendNext(processor);

// 3. 执行责任链
Map<String, Object> context = new HashMap<>();
Boolean result = validator.apply("test request", context);
```

#### 原型模式责任链示例

```java
import com.jasonlat.design.framework.link.prototype.*;

// 1. 创建业务逻辑处理器
public class RuleLogic201 implements ILogicHandler<String, DynamicContext> {
    @Override
    public String apply(String request, DynamicContext context) throws Exception {
        // 业务逻辑处理
        context.setData("processed", true);
        return "处理完成";
    }
}

// 2. 使用工厂创建责任链
Rule02TradeRuleFactory factory = new Rule02TradeRuleFactory();
ILink<String, DynamicContext> chain = factory.openLogicChain();

// 3. 执行责任链
DynamicContext context = new DynamicContext();
String result = chain.apply("trade_request", context);
```

#### 状态机模式示例

```java
import com.jasonlat.design.framework.state.*;
import com.jasonlat.design.framework.state.machine.*;

// 1. 定义订单状态枚举
public enum OrderEvent {
    CREATE, PAY, SHIP, DELIVER, CANCEL
}

// 2. 创建具体状态实现
public class IdleState extends AbstractState<OrderContext, OrderEvent, OrderResult> {
    
    public IdleState() {
        super("IDLE");
    }
    
    @Override
    protected OrderResult doHandle(OrderContext context, OrderEvent event) throws Exception {
        switch (event) {
            case CREATE:
                context.setState(new CreatedState());
                return OrderResult.success("订单已创建");
            default:
                return OrderResult.error("无效的事件: " + event);
        }
    }
}

public class CreatedState extends AbstractState<OrderContext, OrderEvent, OrderResult> {
    
    public CreatedState() {
        super("CREATED");
    }
    
    @Override
    protected OrderResult doHandle(OrderContext context, OrderEvent event) throws Exception {
        switch (event) {
            case PAY:
                context.setState(new PaidState());
                return OrderResult.success("订单已支付");
            case CANCEL:
                context.setState(new CancelledState());
                return OrderResult.success("订单已取消");
            default:
                return OrderResult.error("无效的事件: " + event);
        }
    }
}

// 3. 创建状态上下文
public class OrderContext extends AbstractStateContext<OrderContext, OrderEvent, OrderResult> {
    
    public OrderContext(String orderId) {
        super(orderId);
    }
    
    @Override
    protected IState<OrderContext, OrderEvent, OrderResult> createInitialState() {
        return new IdleState();
    }
}

// 4. 使用状态机
OrderContext orderContext = new OrderContext("ORDER_001");

// 创建订单
OrderResult result1 = orderContext.request(OrderEvent.CREATE);
System.out.println(result1.getMessage()); // 输出: 订单已创建

// 支付订单
OrderResult result2 = orderContext.request(OrderEvent.PAY);
System.out.println(result2.getMessage()); // 输出: 订单已支付

// 获取当前状态
String currentState = orderContext.getCurrentState().getStateName();
System.out.println("当前状态: " + currentState); // 输出: 当前状态: PAID
```

#### 策略模式规则树示例

```java
import com.jasonlat.design.framework.tree.*;

// 1. 创建策略节点
@Component
public class AccountNode extends AbstractMultiThreadStrategyRouter<String, DynamicContext, String> {
    
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    
    @Autowired
    private MemberLevel1Node memberLevel1Node;
    
    @Autowired
    private MemberLevel2Node memberLevel2Node;
    
    // 多线程异步数据加载
    @Override
    protected void multiThread(String requestParameter, DynamicContext dynamicContext) 
            throws ExecutionException, InterruptedException, TimeoutException {
        
        // 异步查询账户标签
        CompletableFuture<String> accountType01 = CompletableFuture.supplyAsync(() -> {
            // 模拟查询账户状态
            return new Random().nextBoolean() ? "账户冻结" : "账户可用";
        }, threadPoolExecutor);
        
        // 异步查询授信数据
        CompletableFuture<String> accountType02 = CompletableFuture.supplyAsync(() -> {
            // 模拟查询授信状态
            return new Random().nextBoolean() ? "拦截" : "已授信";
        }, threadPoolExecutor);
        
        // 等待所有异步任务完成
        CompletableFuture.allOf(accountType01, accountType02)
                .thenRun(() -> {
                    dynamicContext.setValue("accountType01", accountType01.join());
                    dynamicContext.setValue("accountType02", accountType02.join());
                }).join();
    }
    
    // 策略路由选择
    @Override
    public StrategyHandler<String, DynamicContext, String> get(
            String requestParameter, DynamicContext dynamicContext) throws Exception {
        
        String accountType01 = dynamicContext.getValue("accountType01");
        String accountType02 = dynamicContext.getValue("accountType02");
        
        // 根据异步数据进行路由决策
        if ("账户冻结".equals(accountType01) || "拦截".equals(accountType02)) {
            return memberLevel1Node;
        }
        return memberLevel2Node;
    }
}

// 2. 使用策略工厂
@Service
public class StrategyFactory {
    
    @Autowired
    private RootNode rootNode;
    
    public String processRequest(String requestParameter) throws Exception {
        DynamicContext context = new DynamicContext();
        return rootNode.apply(requestParameter, context);
    }
}
```

## 📦 模块说明

### jasonlat-wrench-starter-design-framework

设计模式框架模块，提供常用设计模式的企业级实现。

#### 责任链模式 (Chain of Responsibility)

**单例模式责任链**:
- **核心接口**: `ILogicLink<T, D, R>` - 责任链节点接口
- **抽象实现**: `AbstractLogicLink<T, D, R>` - 抽象责任链节点
- **特性**: 节点复用、内存高效、适合无状态处理

**原型模式责任链**:
- **核心接口**: `ILink<T, D>` - 链路接口，`ILogicHandler<T, D>` - 逻辑处理器接口
- **工厂实现**: `LinkArmory` - 链路装配工厂
- **特性**: 动态创建、状态隔离、支持并发处理

**共同特性**:
- 泛型支持，类型安全
- 灵活的节点连接方式
- 完整的异常处理机制
- Spring框架集成支持

#### 策略模式规则树 (Tree Strategy Pattern)

**核心组件**:
- **StrategyMapper<T, D, R>** - 策略映射器接口，根据条件选择处理策略
- **StrategyHandler<T, D, R>** - 策略处理器接口，定义具体业务处理逻辑
- **AbstractStrategyRouter<T, D, R>** - 策略路由抽象类，提供基础路由功能
- **AbstractMultiThreadStrategyRouter<T, D, R>** - 多线程策略路由，支持异步数据加载

**规则决策树特性**:
- **树形结构** - 层次化组织业务逻辑，路径清晰可视化
- **动态路由** - 运行时根据业务条件智能选择处理节点
- **多线程异步** - 支持并行数据加载，显著提升处理性能
- **策略模式** - 易于扩展新节点，无需修改现有代码
- **上下文传递** - 动态上下文支持节点间数据共享和传递

**应用场景**:
- 复杂业务规则引擎
- 多条件决策流程
- 需要异步数据加载的业务场景
- 风控规则处理
- 用户权限和等级判断

#### 状态机模式 (State Machine Pattern)

**核心组件**:
- **IState<C, E, R>** - 状态接口，定义状态的基本行为和事件处理逻辑
- **IStateContext<C, E, R>** - 状态上下文接口，管理状态机的运行时状态和数据
- **AbstractState<C, E, R>** - 抽象状态实现，提供状态的通用功能和生命周期管理
- **AbstractStateContext<C, E, R>** - 抽象状态上下文实现，封装状态转换和数据管理
- **StateMachine** - 状态机管理器，提供状态机的注册、监控和统计功能
- **StateMachineBuilder<C, E, R>** - 状态机构建器，提供流畅的API进行状态机配置
- **StateMachineConfig** - 状态机配置类，管理状态机的运行参数和环境配置

**状态机框架特性**:
- **泛型支持** - 支持自定义上下文、事件和结果类型，提供类型安全保障
- **线程安全** - 内置并发控制机制，支持多线程环境下的状态转换
- **生命周期管理** - 完整的状态进入/退出回调，支持状态转换的前置和后置处理
- **数据上下文** - 基于ConcurrentHashMap的线程安全数据存储，支持状态间数据共享
- **状态转换监控** - 内置状态转换监听器，支持状态变更的监控和统计
- **异常处理** - 完善的异常处理机制，确保状态机的稳定性和可靠性
- **Spring集成** - 无缝集成Spring框架，支持依赖注入和Bean管理
- **可视化支持** - 支持生成Mermaid状态图，便于状态机的可视化展示

**状态机设计模式优势**:
- **封装状态逻辑** - 将状态相关的行为封装在状态类内部，提高代码的内聚性
- **消除条件分支** - 避免大量的if-else或switch-case语句，提高代码可读性
- **易于扩展** - 新增状态只需添加新的状态类，符合开闭原则
- **状态转换清晰** - 状态转换规则明确，便于理解和维护
- **支持复杂业务流程** - 适合处理具有明确状态转换规则的复杂业务场景

**应用场景**:
- 订单状态管理（创建、支付、发货、完成、取消等状态转换）
- 工作流引擎（审批流程、任务状态管理）
- 游戏状态管理（角色状态、游戏关卡状态）
- 设备状态控制（设备启动、运行、停止、故障等状态）
- 用户会话管理（登录、活跃、超时、注销等状态）
- 文档生命周期管理（草稿、审核、发布、归档等状态）

## 🧪 测试

### 运行所有测试

```bash
mvn clean test
```

### 运行特定模块测试

```bash
cd jasonlat-wrench-starter-design-framework
mvn test
```

### 运行特定测试类

```bash
# 运行单例模式责任链测试
mvn test -Dtest=AppTest

# 运行原型模式责任链测试
mvn test -Dtest=PrototypeAppTest

# 运行策略模式规则树测试
mvn test -Dtest=TreeAppTest

# 运行状态机模式测试
mvn test -Dtest=StateAppTest

# 运行特定测试方法
mvn test -Dtest=AppTest#testCompleteChainExecution
mvn test -Dtest=PrototypeAppTest#testPrototypeChainBasicExecution
mvn test -Dtest=TreeAppTest#testTreeStrategyExecution
mvn test -Dtest=StateAppTest#testStateMachineExecution
```

### 测试覆盖率

项目包含完整的测试用例，覆盖以下场景：
- **单元测试** - 测试单个责任链节点和策略节点的功能
- **集成测试** - 测试完整责任链和规则树的执行流程
- **异常处理测试** - 验证异常情况下的处理机制
- **性能测试** - 测试责任链和多线程异步处理的执行性能
- **边界条件测试** - 验证各种边界情况
- **真实业务场景测试** - 模拟实际业务流程
- **设计模式测试** - 分别测试单例模式、原型模式责任链、策略模式规则树和状态机模式实现
- **多线程测试** - 验证异步数据加载、并发处理能力和状态机的线程安全性
- **Spring集成测试** - 验证与Spring框架的集成功能
- **状态机测试** - 验证状态转换逻辑、数据上下文管理和异常处理机制

### 测试示例

项目提供了完整的测试用例，包括：
- **单例模式责任链测试** - 验证节点复用和无状态处理
- **原型模式责任链测试** - 验证动态创建和状态隔离
- **策略模式规则树测试** - 验证多线程异步数据加载和决策路由
- **状态机模式测试** - 验证状态转换、生命周期管理和并发安全性

详细的测试示例和执行日志请参考各模块的README文档。

## 📚 文档

### 责任链模式文档
- [单例模式责任链使用指南](./jasonlat-wrench-starter-design-framework/src/main/java/com/jasonlat/design/framework/link/singleton/README.md)
- [单例模式测试指南](./jasonlat-wrench-test/src/test/java/com/jasonlat/singleton/README.md)
- [原型模式责任链使用指南](./jasonlat-wrench-starter-design-framework/src/main/java/com/jasonlat/design/framework/link/prototype/README.md)
- [原型模式测试指南](./jasonlat-wrench-test/src/test/java/com/jasonlat/prototype/README.md)

### 策略模式规则树文档
- [策略模式规则树使用指南](./jasonlat-wrench-starter-design-framework/src/main/java/com/jasonlat/design/framework/tree/README.md)
- [策略模式规则树测试指南](./jasonlat-wrench-test/src/test/java/com/jasonlat/tree/README.md)

### 状态机模式文档
- [状态机模式使用指南](./jasonlat-wrench-starter-design-framework/src/main/java/com/jasonlat/design/framework/state/README.md)
- [状态机模式测试指南](./jasonlat-wrench-test/src/test/java/com/jasonlat/state/README.md)

## 🤝 贡献指南

我们欢迎所有形式的贡献！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 开发规范

- 遵循Java编码规范
- 添加完整的单元测试
- 更新相关文档
- 确保所有测试通过

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。

## 👥 作者

- **Jasonlat** - *项目创建者* - [GitHub](https://github.com/jasonlat-dot)

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！

## 📞 联系我们

- 项目主页: [https://github.com/jasonlat-dot/jasonlat-wrench](https://github.com/jasonlat-dot/jasonlat-wrench)
- 问题反馈: [Issues](https://github.com/jasonlat-dot/jasonlat-wrench/issues)

## 🔄 版本发布

### 2025-08-27
- 初始版本发布
- 实现责任链模式框架
- 提供完整的测试用例
- 添加详细的文档说明

### 2025-08-28
- 新增原型模式责任链支持
- 优化异常处理机制
- 完善文档和示例

### 2025-08-29
- 增强Spring框架集成
- 添加性能优化
- 扩展测试覆盖率

### 2025-08-30
- 新增策略模式规则树框架
- 实现多线程异步数据加载支持
- 添加动态上下文和策略路由功能
- 完善规则决策树测试用例

### 2025-08-31
- 新增状态机模式框架
- 实现线程安全的状态转换和生命周期管理
- 添加状态机管理器和构建器支持
- 提供完整的状态转换监控和统计功能
- 支持Spring框架集成和依赖注入
- 完善状态机模式测试用例和文档

---

**⭐ 如果这个项目对你有帮助，请给我们一个Star！**