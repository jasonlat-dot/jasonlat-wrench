# JasonLat Wrench

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-8+-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.12-green.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)](https://maven.apache.org/)

## 📖 项目简介

**JasonLat Wrench** 是一个企业级Java工具库，专注于提供高质量、可复用的设计模式实现和开发工具。项目采用模块化设计，旨在帮助开发者快速构建稳定、可维护的企业级应用。

### 🎯 核心特性

- **🔗 责任链模式框架** - 提供完整的责任链模式实现，支持灵活的业务流程编排
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
│   │           └── link/                        # 责任链模式实现
│   │               ├── singleton/              # 单例模式责任链
│   │               └── prototype/              # 原型模式责任链
│   └── src/test/java/                          # 测试代码
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

#### 责任链模式示例

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
        // 传递给下一个节点
        return next(request, context);
    }
}

public class ProcessLink extends AbstractLogicLink<String, Map<String, Object>, Boolean> {
    @Override
    public Boolean apply(String request, Map<String, Object> context) throws Exception {
        // 处理逻辑
        context.put("processed", true);
        // 传递给下一个节点
        return next(request, context);
    }
}

// 2. 构建责任链
ValidationLink validator = new ValidationLink();
ProcessLink processor = new ProcessLink();

validator.appendNext(processor)

// 3. 执行责任链
Map<String, Object> context = new HashMap<>();
Boolean result = validator.apply("test request", context);
```

## 📦 模块说明

### jasonlat-wrench-starter-design-framework

设计模式框架模块，提供常用设计模式的企业级实现。

#### 责任链模式 (Chain of Responsibility)

- **核心接口**:
  - `ILogicLink<T, D, R>` - 责任链节点接口
  - `ILogicChainArmory<T, D, R>` - 责任链装配接口

- **抽象实现**:
  - `AbstractLogicLink<T, D, R>` - 抽象责任链节点

- **特性**:
  - 泛型支持，类型安全
  - 灵活的节点连接方式
  - 完整的异常处理机制
  - 支持单例和原型两种模式

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
# 运行责任链框架测试
mvn test -Dtest=AppTest

# 运行特定测试方法
mvn test -Dtest=AppTest#testCompleteChainExecution
```

### 测试覆盖率

项目包含完整的测试用例，覆盖以下场景：
- **单元测试** - 测试单个责任链节点的功能
- **集成测试** - 测试完整责任链的执行流程
- **异常处理测试** - 验证异常情况下的处理机制
- **性能测试** - 测试责任链的执行性能
- **边界条件测试** - 验证各种边界情况
- **真实业务场景测试** - 模拟实际业务流程

### 测试示例

#### 责任链完整执行测试

```java
@Test
public void testCompleteChainExecution() {
    // 构建责任链
    ValidationLink validator = new ValidationLink();
    ProcessLink processor = new ProcessLink();
    AuditLink auditor = new AuditLink();
    
    validator.appendNext(processor).appendNext(auditor);
    ILogicLink<TestRequest, TestContext, TestResult> chain = validator;
    
    // 执行测试
    TestRequest request = new TestRequest("test001", "chainData", true);
    TestContext context = new TestContext();
    
    TestResult result = chain.apply(request, context);
    
    // 验证结果
    assertTrue("责任链应该执行成功", result.isSuccess());
    assertEquals("应该设置验证标志", true, context.getAttribute("validated"));
    assertEquals("应该设置处理数据", "processed_chainData", context.getAttribute("processedData"));
}
```

#### 异常处理测试

```java
@Test
public void testChainException() {
    // 构建包含异常节点的责任链
    ValidationLink validator = new ValidationLink();
    ExceptionLink exceptionNode = new ExceptionLink();
    AuditLink auditor = new AuditLink();
    
    validator.appendNext(exceptionNode).appendNext(auditor);
    
    TestRequest request = new TestRequest("test004", "exceptionData", true);
    TestContext context = new TestContext();
    
    try {
        chain.apply(request, context);
        fail("应该抛出异常");
    } catch (RuntimeException e) {
        assertEquals("模拟异常", e.getMessage());
        assertTrue("应该记录错误", context.getErrors().size() > 0);
    }
}
```

#### 性能测试

```java
@Test
public void testChainPerformance() {
    // 构建长责任链（12个节点）
    ILogicLink<TestRequest, TestContext, TestResult> chain = buildPerformanceChain();
    
    TestRequest request = new TestRequest("perf001", "performanceData", true);
    
    long startTime = System.currentTimeMillis();
    
    // 执行100次测试
    for (int i = 0; i < 100; i++) {
        TestContext context = new TestContext();
        TestResult result = chain.apply(request, context);
        assertTrue("性能测试中每次执行都应该成功", result.isSuccess());
    }
    
    long duration = System.currentTimeMillis() - startTime;
    
    // 验证性能（100次执行应该在5秒内完成）
    assertTrue("100次责任链执行应该在5秒内完成", duration < 5000);
}
```

### 测试输出示例

运行测试时，你将看到详细的执行日志：

```
=== 开始测试完整责任链执行 ===
责任链构建完成，链头节点: ValidationLink

开始执行责任链...
请求信息: ID=test001, Data=chainData, Valid=true

--- ValidationLink 执行 ---
验证请求: test001
验证通过，设置验证标志
传递给下一个节点: ProcessLink

--- ProcessLink 执行 ---
检查验证状态: true
处理数据: chainData -> processed_chainData
传递给下一个节点: AuditLink

--- AuditLink 执行 ---
开始审计记录: test001
设置审计时间和用户
审计记录完成

✓ 完整责任链执行测试通过
```

## 📚 文档

- [责任链模式使用指南](./jasonlat-wrench-starter-design-framework/src/main/java/com/jasonlat/design/framework/link/singleton/README.md)
- [API文档](./docs/api/)
- [最佳实践](./docs/best-practices.md)

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

---

**⭐ 如果这个项目对你有帮助，请给我们一个Star！**