package com.jasonlat;

import com.jasonlat.design.framework.link.singleton.AbstractLogicLink;
import com.jasonlat.design.framework.link.singleton.ILogicChainArmory;
import com.jasonlat.design.framework.link.singleton.ILogicLink;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 责任链设计框架测试类
 * <p>
 * 该测试类提供了对责任链框架的全面测试，包括：
 * <ul>
 *   <li>基础功能测试：节点连接、链式调用等</li>
 *   <li>业务逻辑测试：模拟真实业务场景的处理流程</li>
 *   <li>异常处理测试：验证异常情况下的行为</li>
 *   <li>性能测试：验证框架在高并发场景下的表现</li>
 * </ul>
 * </p>
 * 
 * @author Jasonlat
 * @version 1.0
 * @since 2025-01-18
 */
public class SingletonChainTest extends TestCase {

    /**
     * 创建测试用例
     *
     * @param testName 测试用例名称
     */
    public SingletonChainTest(String testName) {
        super(testName);
    }

    /**
     * 获取测试套件
     *
     * @return 测试套件
     */
    public static Test suite() {
        return new TestSuite(SingletonChainTest.class);
    }

    // ==================== 测试用的模拟类 ====================

    /**
     * 测试请求类
     */
    public static class TestRequest {
        private String id;
        private String data;
        private boolean valid;

        public TestRequest(String id, String data, boolean valid) {
            this.id = id;
            this.data = data;
            this.valid = valid;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
    }

    /**
     * 测试上下文类
     */
    public static class TestContext {
        private final Map<String, Object> attributes = new HashMap<>();
        private final List<String> logs = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();

        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }

        public Object getAttribute(String key) {
            return attributes.get(key);
        }

        public void addLog(String log) {
            logs.add(log);
        }

        public void addError(String error) {
            errors.add(error);
        }

        public List<String> getLogs() { return logs; }
        public List<String> getErrors() { return errors; }
        public Map<String, Object> getAttributes() { return attributes; }
    }

    /**
     * 测试结果类
     */
    public static class TestResult {
        private boolean success;
        private String message;
        private Object data;

        public TestResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public TestResult(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }

    // ==================== 测试用的责任链节点实现 ====================

    /**
     * 验证链节点
     * <p>
     * 负责验证请求的有效性
     * </p>
     */
    public static class ValidationLink extends AbstractLogicLink<TestRequest, TestContext, TestResult> {

        @Override
        public TestResult apply(TestRequest request, TestContext context) throws Exception {
            context.addLog("开始验证请求: " + request.getId());
            
            if (!request.isValid()) {
                context.addError("请求验证失败: " + request.getId());
                context.addLog("验证失败，终止处理: " + request.getId());
                return new TestResult(false, "验证失败");
            }
            
            context.addLog("请求验证通过: " + request.getId());
            context.setAttribute("validated", true);
            
            // 传递给下一个节点
            if (next() != null) {
                context.addLog("验证完成，传递给下一个节点: " + next().getClass().getSimpleName());
                return next(request, context);
            }
            
            context.addLog("验证完成，无下一个节点");
            return new TestResult(true, "验证完成");
        }
    }

    /**
     * 处理链节点
     * <p>
     * 负责处理业务逻辑
     * </p>
     */
    public static class ProcessLink extends AbstractLogicLink<TestRequest, TestContext, TestResult> {

        @Override
        public TestResult apply(TestRequest request, TestContext context) throws Exception {
            context.addLog("开始处理请求: " + request.getId());
            
            // 检查是否已验证
            Boolean validated = (Boolean) context.getAttribute("validated");
            context.addLog("检查验证状态: " + validated);
            if (validated == null || !validated) {
                context.addError("请求未通过验证，无法处理");
                context.addLog("处理失败，终止处理: 未验证");
                return new TestResult(false, "处理失败：未验证");
            }
            
            // 模拟处理逻辑
            String processedData = "processed_" + request.getData();
            context.setAttribute("processedData", processedData);
            context.addLog("设置处理数据: " + processedData);
            context.addLog("请求处理完成: " + request.getId());
            
            // 传递给下一个节点
            if (next() != null) {
                context.addLog("处理完成，传递给下一个节点: " + next().getClass().getSimpleName());
                return next(request, context);
            }
            
            context.addLog("处理完成，无下一个节点");
            return new TestResult(true, "处理完成", processedData);
        }
    }

    /**
     * 审计链节点
     * <p>
     * 负责记录审计日志
     * </p>
     */
    public static class AuditLink extends AbstractLogicLink<TestRequest, TestContext, TestResult> {

        @Override
        public TestResult apply(TestRequest request, TestContext context) throws Exception {
            context.addLog("开始审计记录: " + request.getId());
            
            // 检查前置条件
            Boolean validated = (Boolean) context.getAttribute("validated");
            String processedData = (String) context.getAttribute("processedData");
            context.addLog("审计检查 - 验证状态: " + validated + ", 处理数据: " + processedData);
            
            // 记录审计信息
            long auditTime = System.currentTimeMillis();
            context.setAttribute("auditTime", auditTime);
            context.setAttribute("auditUser", "system");
            context.addLog("设置审计时间: " + auditTime);
            context.addLog("设置审计用户: system");
            context.addLog("审计记录完成: " + request.getId());
            
            // 传递给下一个节点
            if (next() != null) {
                context.addLog("审计完成，传递给下一个节点: " + next().getClass().getSimpleName());
                return next(request, context);
            }
            
            context.addLog("审计完成，无下一个节点");
            return new TestResult(true, "审计完成");
        }
    }

    /**
     * 异常链节点
     * <p>
     * 用于测试异常处理
     * </p>
     */
    public static class ExceptionLink extends AbstractLogicLink<TestRequest, TestContext, TestResult> {

        @Override
        public TestResult apply(TestRequest request, TestContext context) throws Exception {
            context.addLog("异常节点开始处理: " + request.getId());
            throw new RuntimeException("模拟异常");
        }
    }

    // ==================== 基础功能测试 ====================

    /**
     * 测试单个节点的基本功能
     */
    public void testSingleNode() {
        System.out.println("\n=== 开始测试单个节点功能 ===");
        ValidationLink validator = new ValidationLink();
        TestRequest request = new TestRequest("test001", "testData", true);
        TestContext context = new TestContext();
        
        try {
            System.out.println("执行单节点验证...");
            TestResult result = validator.apply(request, context);
            
            System.out.println("测试结果: " + (result.isSuccess() ? "成功" : "失败"));
            System.out.println("返回消息: " + result.getMessage());
            System.out.println("日志数量: " + context.getLogs().size());
            System.out.println("验证标志: " + context.getAttribute("validated"));
            
            // 打印所有日志
            System.out.println("执行日志:");
            for (String log : context.getLogs()) {
                System.out.println("  - " + log);
            }
            
            assertTrue("单节点处理应该成功", result.isSuccess());
            assertEquals("验证完成", result.getMessage());
            assertFalse("应该记录处理日志", context.getLogs().isEmpty());
            assertEquals("应该设置验证标志", true, context.getAttribute("validated"));
            
            System.out.println("✓ 单节点测试通过");
        } catch (Exception e) {
            System.err.println("✗ 单节点测试失败: " + e.getMessage());
            fail("单节点处理不应该抛出异常: " + e.getMessage());
        }
    }

    /**
     * 测试责任链的构建和连接
     */
    public void testChainBuilding() {
        System.out.println("\n=== 开始测试责任链构建 ===");
        ValidationLink validator = new ValidationLink();
        ProcessLink processor = new ProcessLink();
        AuditLink auditor = new AuditLink();
        
        System.out.println("创建节点: ValidationLink -> ProcessLink -> AuditLink");
        
        // 构建责任链
        validator.appendNext(processor).appendNext(auditor);
        System.out.println("责任链构建完成");
        
        // 验证链结构
        System.out.println("验证链结构:");
        System.out.println("  validator.next() = " + (validator.next() != null ? validator.next().getClass().getSimpleName() : "null"));
        System.out.println("  processor.next() = " + (processor.next() != null ? processor.next().getClass().getSimpleName() : "null"));
        System.out.println("  auditor.next() = " + (auditor.next() != null ? auditor.next().getClass().getSimpleName() : "null"));
        
        assertEquals("第一个节点的下一个应该是处理节点", processor, validator.next());
        assertEquals("第二个节点的下一个应该是审计节点", auditor, processor.next());
        assertNull("最后一个节点的下一个应该为null", auditor.next());
        
        System.out.println("✓ 责任链构建测试通过");
    }

    /**
     * 测试完整的责任链执行
     */
    public void testCompleteChainExecution() {
        System.out.println("\n=== 开始测试完整责任链执行 ===");
        
        // 构建责任链 - 正确的方式
        ILogicLink<TestRequest, TestContext, TestResult> chain = new ValidationLink(); // 构建第一个节点
        ProcessLink processor = new ProcessLink();
        AuditLink auditor = new AuditLink();
        
        // 构建链式结构
        chain.appendNext(processor).appendNext(auditor);

        System.out.println("责任链构建完成: ValidationLink -> ProcessLink -> AuditLink");

        ILogicLink<TestRequest, TestContext, TestResult> temp = chain;
        System.out.print("链头节点: " + temp.getClass().getSimpleName());
        while (temp != null) {
            System.out.print("  -> " + temp.getClass().getSimpleName());
            temp = temp.next();
        }
        System.out.println();

        TestRequest request = new TestRequest("test002", "chainData", true);
        TestContext context = new TestContext();
        
        System.out.println("请求信息: ID=" + request.getId() + ", Data=" + request.getData() + ", Valid=" + request.isValid());
        
        try {
            System.out.println("开始执行责任链...");
            TestResult result = chain.apply(request, context);
            
            System.out.println("\n执行结果:");
            System.out.println("  成功状态: " + result.isSuccess());
            System.out.println("  返回消息: " + result.getMessage());
            
            System.out.println("\n上下文状态:");
            System.out.println("  验证标志: " + context.getAttribute("validated"));
            System.out.println("  处理数据: " + context.getAttribute("processedData"));
            System.out.println("  审计时间: " + context.getAttribute("auditTime"));
            System.out.println("  审计用户: " + context.getAttribute("auditUser"));
            
            System.out.println("\n执行日志 (" + context.getLogs().size() + " 条):");
            for (int i = 0; i < context.getLogs().size(); i++) {
                System.out.println("  " + (i + 1) + ". " + context.getLogs().get(i));
            }
            
            if (!context.getErrors().isEmpty()) {
                System.out.println("\n错误信息 (" + context.getErrors().size() + " 条):");
                for (int i = 0; i < context.getErrors().size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + context.getErrors().get(i));
                }
            }
            
            // 验证结果
            assertTrue("责任链执行应该成功", result.isSuccess());
            assertEquals("最终消息应该是审计完成", "审计完成", result.getMessage());
            
            // 验证上下文状态
            assertEquals("应该设置验证标志", true, context.getAttribute("validated"));
            assertEquals("应该设置处理数据", "processed_chainData", context.getAttribute("processedData"));
            assertNotNull("应该设置审计时间", context.getAttribute("auditTime"));
            assertEquals("应该设置审计用户", "system", context.getAttribute("auditUser"));
            
            // 验证日志记录
            List<String> logs = context.getLogs();
            assertTrue("应该有验证日志", logs.stream().anyMatch(log -> log.contains("验证")));
            assertTrue("应该有处理日志", logs.stream().anyMatch(log -> log.contains("处理")));
            assertTrue("应该有审计日志", logs.stream().anyMatch(log -> log.contains("审计")));
            
            System.out.println("✓ 完整责任链执行测试通过");
            
        } catch (Exception e) {
            System.err.println("✗ 责任链执行失败: " + e.getMessage());
            e.printStackTrace();
            fail("责任链执行不应该抛出异常: " + e.getMessage());
        }
    }

    // ==================== 异常处理测试 ====================

    /**
     * 测试验证失败的情况
     */
    public void testValidationFailure() {
        System.out.println("\n=== 开始测试验证失败场景 ===");

        // 构建责任链 - 正确的方式
        ValidationLink validator = new ValidationLink();
        ProcessLink processor = new ProcessLink();
        AuditLink auditor = new AuditLink();

        validator.appendNext(processor).appendNext(auditor);

        TestRequest invalidRequest = new TestRequest("test003", "invalidData", false);
        TestContext context = new TestContext();
        
        System.out.println("无效请求信息: ID=" + invalidRequest.getId() + ", Data=" + invalidRequest.getData() + ", Valid=" + invalidRequest.isValid());
        
        try {
            System.out.println("执行责任链处理无效请求...");
            TestResult result = validator.apply(invalidRequest, context);
            
            System.out.println("\n执行结果:");
            System.out.println("  成功状态: " + result.isSuccess());
            System.out.println("  返回消息: " + result.getMessage());
            
            System.out.println("\n执行日志 (" + context.getLogs().size() + " 条):");
            for (int i = 0; i < context.getLogs().size(); i++) {
                System.out.println("  " + (i + 1) + ". " + context.getLogs().get(i));
            }
            
            System.out.println("\n错误信息 (" + context.getErrors().size() + " 条):");
            for (int i = 0; i < context.getErrors().size(); i++) {
                System.out.println("  " + (i + 1) + ". " + context.getErrors().get(i));
            }
            
            // 验证结果
            assertFalse("验证失败时应该返回失败结果", result.isSuccess());
            assertEquals("应该返回验证失败消息", "验证失败", result.getMessage());
            
            // 验证错误记录
            assertFalse("应该记录错误信息", context.getErrors().isEmpty());
            assertTrue("错误信息应该包含验证失败", 
                context.getErrors().stream().anyMatch(error -> error.contains("验证失败")));
            
            System.out.println("✓ 验证失败测试通过");
            
        } catch (Exception e) {
            System.err.println("✗ 验证失败测试异常: " + e.getMessage());
            e.printStackTrace();
            fail("验证失败不应该抛出异常: " + e.getMessage());
        }
    }

    /**
     * 测试链中节点抛出异常的情况
     */
    public void testChainException() {
        System.out.println("\n=== 开始测试异常处理场景 ===");
        
        // 构建责任链 - 正确的方式
        ValidationLink validator = new ValidationLink();
        ExceptionLink exceptionNode = new ExceptionLink();
        AuditLink auditor = new AuditLink();

        validator.appendNext(exceptionNode).appendNext(auditor);

        TestRequest request = new TestRequest("test004", "exceptionData", true);
        TestContext context = new TestContext();
        
        System.out.println("请求信息: ID=" + request.getId() + ", Data=" + request.getData() + ", Valid=" + request.isValid());
        System.out.println("责任链包含异常节点，预期会抛出异常...");
        
        try {
            validator.apply(request, context);
            System.err.println("✗ 异常测试失败: 应该抛出异常但没有抛出");
            fail("应该抛出异常");
        } catch (Exception e) {
            System.out.println("\n捕获到预期异常:");
            System.out.println("  异常类型: " + e.getClass().getSimpleName());
            System.out.println("  异常消息: " + e.getMessage());
            
            System.out.println("\n异常发生前的执行日志 (" + context.getLogs().size() + " 条):");
            for (int i = 0; i < context.getLogs().size(); i++) {
                System.out.println("  " + (i + 1) + ". " + context.getLogs().get(i));
            }
            
            assertEquals("应该抛出模拟异常", "模拟异常", e.getMessage());
            assertTrue("应该记录到异常节点的日志", 
                context.getLogs().stream().anyMatch(log -> log.contains("异常节点")));
            
            System.out.println("✓ 异常处理测试通过");
        }
    }

    // ==================== 边界条件测试 ====================

    /**
     * 测试空链的情况
     */
    public void testEmptyChain() {
        System.out.println("\n=== 开始测试空链场景 ===");
        
        ValidationLink singleNode = new ValidationLink();
        TestRequest request = new TestRequest("test005", "emptyChainData", true);
        TestContext context = new TestContext();
        
        System.out.println("请求信息: ID=" + request.getId() + ", Data=" + request.getData() + ", Valid=" + request.isValid());
        System.out.println("测试单节点执行（无后续节点）...");
        
        try {
            TestResult result = singleNode.apply(request, context);
            
            System.out.println("\n执行结果:");
            System.out.println("  成功状态: " + result.isSuccess());
            System.out.println("  返回消息: " + result.getMessage());
            System.out.println("  下一个节点: " + (singleNode.next() == null ? "null" : singleNode.next().getClass().getSimpleName()));
            
            System.out.println("\n执行日志 (" + context.getLogs().size() + " 条):");
            for (int i = 0; i < context.getLogs().size(); i++) {
                System.out.println("  " + (i + 1) + ". " + context.getLogs().get(i));
            }
            
            assertTrue("单节点应该正常执行", result.isSuccess());
            assertNull("单节点的next应该为null", singleNode.next());
            
            System.out.println("✓ 空链测试通过");
            
        } catch (Exception e) {
            System.err.println("✗ 空链测试失败: " + e.getMessage());
            e.printStackTrace();
            fail("单节点执行不应该抛出异常: " + e.getMessage());
        }
    }

    /**
     * 测试链式调用的返回值
     */
    public void testChainReturnValue() {
        System.out.println("\n=== 开始测试链式调用返回值 ===");
        
        ValidationLink validator = new ValidationLink();
        ProcessLink processor = new ProcessLink();
        AuditLink auditor = new AuditLink();
        
        System.out.println("创建节点: ValidationLink, ProcessLink, AuditLink");
        
        // 测试appendNext的返回值
        System.out.println("执行 validator.appendNext(processor)...");
        ILogicLink<TestRequest, TestContext, TestResult> returnedProcessor = validator.appendNext(processor);
        
        System.out.println("执行 returnedProcessor.appendNext(auditor)...");
        ILogicLink<TestRequest, TestContext, TestResult> returnedAuditor = returnedProcessor.appendNext(auditor);
        
        System.out.println("\n返回值验证:");
        System.out.println("  validator.appendNext(processor) 返回: " + returnedProcessor.getClass().getSimpleName());
        System.out.println("  processor 类型: " + processor.getClass().getSimpleName());
        System.out.println("  returnedProcessor.appendNext(auditor) 返回: " + returnedAuditor.getClass().getSimpleName());
        System.out.println("  auditor 类型: " + auditor.getClass().getSimpleName());
        
        System.out.println("\n链结构验证:");
        System.out.println("  validator.next(): " + (validator.next() != null ? validator.next().getClass().getSimpleName() : "null"));
        System.out.println("  processor.next(): " + (processor.next() != null ? processor.next().getClass().getSimpleName() : "null"));
        System.out.println("  auditor.next(): " + (auditor.next() != null ? auditor.next().getClass().getSimpleName() : "null"));
        
        assertEquals("appendNext应该返回追加的节点", processor, returnedProcessor);
        assertEquals("appendNext应该返回追加的节点", auditor, returnedAuditor);
        
        System.out.println("✓ 链式调用返回值测试通过");
    }

    // ==================== 性能测试 ====================

    /**
     * 测试责任链的性能
     */
    public void testChainPerformance() {
        System.out.println("\n=== 开始测试责任链性能 ===");
        
        // 构建较长的责任链
        ILogicLink<TestRequest, TestContext, TestResult> chain = new ValidationLink();
        ILogicLink<TestRequest, TestContext, TestResult> current = chain;
        
        System.out.println("构建长责任链: ValidationLink + 10个ProcessLink + AuditLink");
        
        // 添加多个处理节点
        for (int i = 0; i < 10; i++) {
            current = current.appendNext(new ProcessLink());
        }
        current.appendNext(new AuditLink());
        
        System.out.println("责任链构建完成，总共12个节点");
        
        TestRequest request = new TestRequest("perf001", "performanceData", true);
        System.out.println("开始性能测试，执行100次...");
        
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        
        // 执行多次测试
        for (int i = 0; i < 100; i++) {
            TestContext context = new TestContext();
            try {
                TestResult result = chain.apply(request, context);
                if (result.isSuccess()) {
                    successCount++;
                }
                assertTrue("性能测试中每次执行都应该成功", result.isSuccess());
                
                // 每20次打印一次进度
                if ((i + 1) % 20 == 0) {
                    System.out.println("  已完成 " + (i + 1) + "/100 次执行");
                }
            } catch (Exception e) {
                System.err.println("  第 " + (i + 1) + " 次执行失败: " + e.getMessage());
                fail("性能测试中不应该抛出异常: " + e.getMessage());
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("\n性能测试结果:");
        System.out.println("  总执行次数: 100");
        System.out.println("  成功次数: " + successCount);
        System.out.println("  总耗时: " + duration + "ms");
        System.out.println("  平均耗时: " + (duration / 100.0) + "ms/次");
        System.out.println("  吞吐量: " + (100000 / duration) + "次/秒");
        
        // 验证性能（100次执行应该在合理时间内完成）
        assertTrue("100次责任链执行应该在5秒内完成", duration < 5000);
        
        System.out.println("✓ 性能测试通过 (耗时 < 5秒)");
    }

    // ==================== 集成测试 ====================

    /**
     * 综合测试：模拟真实业务场景
     */
    public void testRealWorldScenario() {
        System.out.println("\n=== 开始测试真实业务场景 ===");
        
        // 模拟订单处理场景 - 正确的构建方式
        ValidationLink validator = new ValidationLink();  // 订单验证
        ProcessLink processor = new ProcessLink();        // 订单处理
        AuditLink auditor = new AuditLink();             // 审计记录
        
        validator.appendNext(processor).appendNext(auditor);

        System.out.println("构建订单处理责任链: 验证 -> 处理 -> 审计");
        System.out.println("责任链构建完成，链头节点: " + validator.getClass().getSimpleName());
        
        // 测试成功场景
        System.out.println("\n--- 测试有效订单处理 ---");
        TestRequest validOrder = new TestRequest("ORDER001", "validOrderData", true);
        TestContext successContext = new TestContext();
        
        System.out.println("有效订单信息: ID=" + validOrder.getId() + ", Data=" + validOrder.getData() + ", Valid=" + validOrder.isValid());
        
        try {
            System.out.println("开始处理有效订单...");
            TestResult successResult = validator.apply(validOrder, successContext);
            
            System.out.println("\n有效订单处理结果:");
            System.out.println("  成功状态: " + successResult.isSuccess());
            System.out.println("  返回消息: " + successResult.getMessage());
            System.out.println("  日志数量: " + successContext.getLogs().size());
            System.out.println("  错误数量: " + successContext.getErrors().size());
            
            System.out.println("\n处理日志:");
            for (int i = 0; i < successContext.getLogs().size(); i++) {
                System.out.println("  " + (i + 1) + ". " + successContext.getLogs().get(i));
            }
            
            assertTrue("有效订单应该处理成功", successResult.isSuccess());
            assertEquals("应该完成审计", "审计完成", successResult.getMessage());
            
            // 验证完整的处理流程
            assertTrue("应该包含所有处理步骤的日志", successContext.getLogs().size() >= 6);
            assertEquals("应该没有错误", 0, successContext.getErrors().size());
            
            System.out.println("✓ 有效订单处理测试通过");
            
        } catch (Exception e) {
            System.err.println("✗ 有效订单处理失败: " + e.getMessage());
            e.printStackTrace();
            fail("有效订单处理不应该抛出异常: " + e.getMessage());
        }
        
        // 测试失败场景
        System.out.println("\n--- 测试无效订单处理 ---");
        TestRequest invalidOrder = new TestRequest("ORDER002", "invalidOrderData", false);
        TestContext failureContext = new TestContext();
        
        System.out.println("无效订单信息: ID=" + invalidOrder.getId() + ", Data=" + invalidOrder.getData() + ", Valid=" + invalidOrder.isValid());
        
        try {
            System.out.println("开始处理无效订单...");
            TestResult failureResult = validator.apply(invalidOrder, failureContext);
            
            System.out.println("\n无效订单处理结果:");
            System.out.println("  成功状态: " + failureResult.isSuccess());
            System.out.println("  返回消息: " + failureResult.getMessage());
            System.out.println("  日志数量: " + failureContext.getLogs().size());
            System.out.println("  错误数量: " + failureContext.getErrors().size());
            
            System.out.println("\n处理日志:");
            for (int i = 0; i < failureContext.getLogs().size(); i++) {
                System.out.println("  " + (i + 1) + ". " + failureContext.getLogs().get(i));
            }
            
            System.out.println("\n错误信息:");
            for (int i = 0; i < failureContext.getErrors().size(); i++) {
                System.out.println("  " + (i + 1) + ". " + failureContext.getErrors().get(i));
            }
            
            assertFalse("无效订单应该处理失败", failureResult.isSuccess());
            assertTrue("应该记录错误信息", failureContext.getErrors().size() > 0);
            
            System.out.println("✓ 无效订单处理测试通过");
            
        } catch (Exception e) {
            System.err.println("✗ 无效订单处理异常: " + e.getMessage());
            e.printStackTrace();
            fail("无效订单处理不应该抛出异常: " + e.getMessage());
        }
        
        System.out.println("✓ 真实业务场景测试完成");
    }

    /**
     * 测试接口兼容性
     */
    public void testInterfaceCompatibility() {
        System.out.println("\n=== 开始测试接口兼容性 ===");
        
        // 测试ILogicChainArmory接口
        System.out.println("测试 ILogicChainArmory 接口...");
        ILogicChainArmory<TestRequest, TestContext, TestResult> armory = new ValidationLink();
        ProcessLink processor = new ProcessLink();
        
        System.out.println("执行 armory.appendNext(processor)...");
        ILogicLink<TestRequest, TestContext, TestResult> returned = armory.appendNext(processor);
        
        System.out.println("验证接口方法返回值:");
        System.out.println("  返回的节点类型: " + returned.getClass().getSimpleName());
        System.out.println("  期望的节点类型: " + processor.getClass().getSimpleName());
        System.out.println("  armory.next() 类型: " + (armory.next() != null ? armory.next().getClass().getSimpleName() : "null"));
        
        assertEquals("接口方法应该正常工作", processor, returned);
        assertEquals("接口方法应该正常工作", processor, armory.next());
        
        System.out.println("✓ ILogicChainArmory 接口测试通过");
        
        // 测试ILogicLink接口
        System.out.println("\n测试 ILogicLink 接口...");
        ILogicLink<TestRequest, TestContext, TestResult> link = new ValidationLink();
        TestRequest request = new TestRequest("interface001", "interfaceData", true);
        TestContext context = new TestContext();
        
        System.out.println("请求信息: ID=" + request.getId() + ", Data=" + request.getData() + ", Valid=" + request.isValid());
        
        try {
            System.out.println("执行 link.apply(request, context)...");
            TestResult result = link.apply(request, context);
            
            System.out.println("\n接口执行结果:");
            System.out.println("  结果对象: " + (result != null ? "非null" : "null"));
            System.out.println("  成功状态: " + (result != null ? result.isSuccess() : "N/A"));
            System.out.println("  返回消息: " + (result != null ? result.getMessage() : "N/A"));
            
            System.out.println("\n执行日志 (" + context.getLogs().size() + " 条):");
            for (int i = 0; i < context.getLogs().size(); i++) {
                System.out.println("  " + (i + 1) + ". " + context.getLogs().get(i));
            }
            
            assertNotNull("接口方法应该返回结果", result);
            
            System.out.println("✓ ILogicLink 接口测试通过");
            
        } catch (Exception e) {
            System.err.println("✗ ILogicLink 接口测试失败: " + e.getMessage());
            e.printStackTrace();
            fail("接口方法不应该抛出异常: " + e.getMessage());
        }
        
        System.out.println("✓ 接口兼容性测试完成");
    }
}
