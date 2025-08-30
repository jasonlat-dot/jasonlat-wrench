# 订单状态机测试代码文档

## 概述

本目录包含了订单状态机的完整测试实现，演示了基于状态模式的订单生命周期管理。测试代码展示了订单从创建到完成（或取消）的各种状态转换场景。

## 目录结构

```
state/
├── PaidState.java              # 已支付状态实现
├── PendingPaymentState.java     # 待支付状态实现
├── RefundedState.java           # 已退款状态实现
├── StoppedPaymentState.java     # 止付状态实现
├── context/
│   └── OrderContext.java        # 订单上下文类
├── event/
│   └── OrderEvent.java          # 订单事件枚举
└── README.md                    # 本文档
```

## 核心组件说明

### 1. 订单事件枚举 (OrderEvent)

定义了订单状态转换的触发事件：

- **PAY**: 支付事件，用户完成支付操作
- **STOP_PAYMENT**: 止付事件，停止支付（风控等原因）
- **CANCEL**: 取消事件，取消订单
- **REFUND**: 退款事件，申请退款操作
- **RESUME_PAYMENT**: 恢复支付事件，恢复被止付的订单
- **QUERY**: 查询事件，查询订单状态和详情

### 2. 订单上下文 (OrderContext)

继承自 `AbstractStateContext`，管理订单的所有数据和状态：

**核心属性：**
- `orderId`: 订单ID
- `status`: 订单状态
- `amount`: 订单金额
- `userId`: 用户ID
- `productInfo`: 商品信息
- `createTime`: 创建时间
- `paidTime`: 支付时间
- `stoppedTime`: 止付时间
- `logs`: 操作日志列表
- `attributes`: 扩展属性映射

**核心方法：**
- `addLog(String message)`: 添加操作日志
- `setAttribute(String key, Object value)`: 设置扩展属性
- `getAttribute(String key)`: 获取扩展属性

### 3. 状态实现类

#### PendingPaymentState (待支付状态)
- **支持事件**: PAY, STOP_PAYMENT, CANCEL, QUERY
- **状态转换**:
  - PAY → PaidState (已支付)
  - STOP_PAYMENT → StoppedPaymentState (止付)
  - CANCEL → 订单取消

#### PaidState (已支付状态)
- **支持事件**: REFUND, QUERY
- **状态转换**:
  - REFUND → RefundedState (已退款)

#### StoppedPaymentState (止付状态)
- **支持事件**: RESUME_PAYMENT, PAY, CANCEL, QUERY
- **状态转换**:
  - RESUME_PAYMENT → PendingPaymentState (待支付)
  - PAY → PaidState (已支付)
  - CANCEL → 订单取消

#### RefundedState (已退款状态)
- **支持事件**: QUERY
- **状态转换**: 无（终态）

## 测试执行流程

### 整体执行流程架构

```
InitNode → StateBuilderNode → TransitionNode → ExecutionNode/ValidationNode
    ↓             ↓               ↓                    ↓
  日志记录      状态注册        规则配置           返回测试结果
              + 构建器创建     + 事件处理         + 断言验证
              + 初始状态设置   + 状态转换         + 日志输出
```

**流程说明：**
- **InitNode**: 初始化测试环境，创建订单上下文，记录初始化日志
- **StateBuilderNode**: 创建状态机构建器，注册所有状态，设置初始状态
- **TransitionNode**: 配置状态转换规则，处理事件触发，执行状态转换逻辑
- **ExecutionNode/ValidationNode**: 执行具体测试场景，验证结果，输出测试报告

### 测试类：OrderStateMachineTest

测试类使用Spring Boot测试框架，按照上述节点流程执行以下测试场景：

#### 1. 初始化流程 (`initializeStateMachine()`)

```
1. 创建状态机构建器 (StateMachineBuilder)
2. 注册所有订单状态
   - PENDING_PAYMENT → PendingPaymentState
   - PAID → PaidState
   - STOPPED_PAYMENT → StoppedPaymentState
   - REFUNDED → RefundedState
3. 配置状态转换规则
4. 设置初始状态为 PENDING_PAYMENT
5. 启用状态转换日志记录
```

#### 2. 正常支付流程测试 (`testNormalPaymentFlow()`)

```
执行流程：
1. 初始化状态机
2. 创建订单上下文 (订单ID: ORDER_001)
3. 验证初始状态为 PENDING_PAYMENT
4. 触发 PAY 事件
5. 验证状态转换为 PAID
6. 验证支付时间已设置
7. 打印订单日志

预期结果：
- 状态成功从 PENDING_PAYMENT 转换为 PAID
- 支付时间被正确记录
- 操作日志包含完整的状态转换记录
```

#### 3. 止付流程测试 (`testStopPaymentFlow()`)

```
执行流程：
1. 初始化状态机
2. 创建订单上下文 (订单ID: ORDER_002)
3. 触发 STOP_PAYMENT 事件 (PENDING_PAYMENT → STOPPED_PAYMENT)
4. 验证状态转换为 STOPPED_PAYMENT
5. 触发 RESUME_PAYMENT 事件 (STOPPED_PAYMENT → PENDING_PAYMENT)
6. 验证状态恢复为 PENDING_PAYMENT
7. 触发 PAY 事件 (PENDING_PAYMENT → PAID)
8. 验证最终状态为 PAID
9. 打印订单日志

预期结果：
- 完整的止付-恢复-支付流程执行成功
- 每个状态转换都被正确记录
- 止付时间和支付时间都被正确设置
```

#### 4. 退款流程测试 (`testRefundFlow()`)

```
执行流程：
1. 初始化状态机
2. 创建订单上下文 (订单ID: ORDER_003)
3. 触发 PAY 事件 (PENDING_PAYMENT → PAID)
4. 验证支付成功
5. 触发 REFUND 事件 (PAID → REFUNDED)
6. 验证状态转换为 REFUNDED
7. 验证退款时间已设置
8. 打印订单日志

预期结果：
- 支付后退款流程执行成功
- 退款时间通过扩展属性正确存储
- 状态转换日志完整记录
```

#### 5. 止付状态直接支付测试 (`testDirectPaymentInStoppedState()`)

```
执行流程：
1. 初始化状态机
2. 创建订单上下文 (订单ID: ORDER_004)
3. 触发 STOP_PAYMENT 事件 (PENDING_PAYMENT → STOPPED_PAYMENT)
4. 直接触发 PAY 事件 (STOPPED_PAYMENT → PAID)
5. 验证可以从止付状态直接支付
6. 打印订单日志

预期结果：
- 从止付状态可以直接转换到已支付状态
- 跳过恢复支付步骤的快捷流程正常工作
```

#### 6. 无效事件处理测试 (`testInvalidEventHandling()`)

```
执行流程：
1. 初始化状态机
2. 创建订单上下文 (订单ID: ORDER_005)
3. 在 PENDING_PAYMENT 状态触发 REFUND 事件（无效操作）
4. 验证返回错误信息
5. 验证状态未发生变化
6. 打印订单日志

预期结果：
- 无效事件被正确识别和处理
- 状态机保持稳定，不会因无效事件崩溃
- 错误信息清晰明确
```

#### 7. 状态机统计信息测试 (`testStateMachineStatistics()`)

```
执行流程：
1. 初始化状态机
2. 获取状态机统计信息
3. 验证注册状态数量
4. 验证活跃状态机数量
5. 验证事件处理数量
6. 验证状态转换数量
7. 打印统计信息

预期结果：
- 统计信息准确反映状态机的运行状态
- 各项指标数值合理
```

#### 8. 状态描述测试 (`testStateDescriptions()`)

```
执行流程：
1. 初始化状态机
2. 创建订单上下文
3. 遍历所有状态
4. 验证每个状态的名称和描述
5. 打印状态信息

预期结果：
- 所有状态都有清晰的名称和描述
- 状态信息有助于理解业务逻辑
```

## 日志输出说明

测试执行过程中会输出详细的日志信息，包括：

### 1. 状态机初始化日志
```
=== 开始初始化订单状态机 ===
=== 订单状态机初始化完成 ===
```

### 2. 状态转换日志
```
订单[ORDER_001]进入待支付状态
订单[ORDER_001]收到支付事件，开始处理支付
订单[ORDER_001]支付成功，金额：100.00
订单[ORDER_001]进入已支付状态
```

### 3. 订单详细信息日志
```
=== 订单状态机日志 ===
订单ID: ORDER_001
当前状态: 已支付
订单状态: PAID
订单金额: 100.00
用户ID: USER_001
产品信息: 测试商品
创建时间: 2025-01-20 10:30:00
支付时间: 2025-01-20 10:31:00
```

### 4. 操作日志
```
=== 操作日志 ===
[2025-01-20 10:30:00] 订单创建 - 用户:USER_001, 金额:100.00, 商品:测试商品
[2025-01-20 10:30:01] 订单进入待支付状态
[2025-01-20 10:31:00] 收到支付事件，开始处理
[2025-01-20 10:31:00] 支付成功，订单状态更新为已支付
[2025-01-20 10:31:00] 订单进入已支付状态
```

## 运行测试

### 前置条件
1. Java 8+
2. Maven 3.6+
3. Spring Boot 2.x

### 执行命令
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=OrderStateMachineTest

# 运行特定测试方法
mvn test -Dtest=OrderStateMachineTest#testNormalPaymentFlow
```

## 扩展说明

### 1. 添加新状态
1. 创建新的状态类，继承 `AbstractState`
2. 实现 `doHandle` 方法处理业务逻辑
3. 实现 `canHandle` 方法定义支持的事件
4. 在测试类中注册新状态

### 2. 添加新事件
1. 在 `OrderEvent` 枚举中添加新事件
2. 在相关状态类中添加事件处理逻辑
3. 更新状态转换规则
4. 编写对应的测试用例

### 3. 自定义扩展属性
使用 `OrderContext` 的 `setAttribute` 和 `getAttribute` 方法可以存储任意业务数据，如：
- 退款原因
- 风控信息
- 用户备注
- 第三方交易号

## 最佳实践

1. **状态设计**: 每个状态应该职责单一，只处理特定的业务逻辑
2. **事件定义**: 事件应该语义明确，避免歧义
3. **日志记录**: 关键状态转换和业务操作都应该记录日志
4. **异常处理**: 妥善处理无效事件和异常情况
5. **测试覆盖**: 确保所有状态转换路径都有对应的测试用例

## 总结

本测试代码完整演示了状态机模式在订单管理中的应用，通过清晰的状态定义、事件驱动的转换机制和完善的测试覆盖，展示了如何构建一个健壮、可扩展的订单状态管理系统。