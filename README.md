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
│   │           └── tree/                        # 策略模式规则树实现
│   │               ├── AbstractStrategyRouter.java
│   │               ├── AbstractMultiThreadStrategyRouter.java
│   │               ├── StrategyHandler.java
│   │               └── StrategyMapper.java
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
    <groupId>com.jasonlat</groupId>
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

# 运行特定测试方法
mvn test -Dtest=AppTest#testCompleteChainExecution
mvn test -Dtest=PrototypeAppTest#testPrototypeChainBasicExecution
mvn test -Dtest=TreeAppTest#testTreeStrategyExecution
```

### 测试覆盖率

项目包含完整的测试用例，覆盖以下场景：
- **单元测试** - 测试单个责任链节点和策略节点的功能
- **集成测试** - 测试完整责任链和规则树的执行流程
- **异常处理测试** - 验证异常情况下的处理机制
- **性能测试** - 测试责任链和多线程异步处理的执行性能
- **边界条件测试** - 验证各种边界情况
- **真实业务场景测试** - 模拟实际业务流程
- **设计模式测试** - 分别测试单例模式、原型模式责任链和策略模式规则树实现
- **多线程测试** - 验证异步数据加载和并发处理能力
- **Spring集成测试** - 验证与Spring框架的集成功能

### 测试示例

项目提供了完整的测试用例，包括：
- **单例模式责任链测试** - 验证节点复用和无状态处理
- **原型模式责任链测试** - 验证动态创建和状态隔离
- **策略模式规则树测试** - 验证多线程异步数据加载和决策路由

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

## 🔄 版本历史

### v1.0
- 初始版本发布
- 实现责任链模式框架
- 提供完整的测试用例
- 添加详细的文档说明

### v1.1
- 新增原型模式责任链支持
- 优化异常处理机制
- 完善文档和示例

### v1.2
- 增强Spring框架集成
- 添加性能优化
- 扩展测试覆盖率

### v1.3
- 新增策略模式规则树框架
- 实现多线程异步数据加载支持
- 添加动态上下文和策略路由功能
- 完善规则决策树测试用例

---

**⭐ 如果这个项目对你有帮助，请给我们一个Star！**