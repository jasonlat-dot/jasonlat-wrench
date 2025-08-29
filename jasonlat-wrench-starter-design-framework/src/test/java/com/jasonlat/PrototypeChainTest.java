package com.jasonlat;

import com.jasonlat.design.framework.link.prototype.*;
import com.jasonlat.design.framework.link.prototype.chain.*;
import com.jasonlat.design.framework.link.prototype.handler.ILogicHandler;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;

/**
 * 原型模式责任链设计框架测试类
 * <p>
 * 该测试类提供了对原型模式责任链框架的全面测试，包括：
 * <ul>
 *   <li>DynamicContext动态上下文测试：键值对操作、状态控制等</li>
 *   <li>LinkedList链表测试：增删查改、打印等基础操作</li>
 *   <li>BusinessLinkedList业务链表测试：执行流程、中断机制等</li>
 *   <li>LinkArmory链路军械库测试：处理器管理、链路构建等</li>
 *   <li>ILogicHandler逻辑处理器测试：next、stop方法等</li>
 *   <li>ILink链路接口测试：接口兼容性验证</li>
 * </ul>
 * </p>
 * 
 * @author Jasonlat
 * @version 1.0
 * @since 2025-08-29
 */
public class PrototypeChainTest extends TestCase {

    /**
     * 创建测试用例
     *
     * @param testName 测试用例名称
     */
    public PrototypeChainTest(String testName) {
        super(testName);
    }

    /**
     * 获取测试套件
     *
     * @return 测试套件
     */
    public static Test suite() {
        return new TestSuite(PrototypeChainTest.class);
    }

    // ==================== 测试用的辅助类 ====================

    /**
     * 测试用的简单处理器
     */
    public static class TestHandler implements ILogicHandler<String, DynamicContext, String> {
        private final String name;
        private final boolean shouldStop;
        private final String stopResult;

        public TestHandler(String name) {
            this(name, false, null);
        }

        public TestHandler(String name, boolean shouldStop, String stopResult) {
            this.name = name;
            this.shouldStop = shouldStop;
            this.stopResult = stopResult;
        }

        @Override
        public String apply(String requestParameter, DynamicContext dynamicContext) {
            // 记录处理过程
            dynamicContext.setValue(name + "_processed", requestParameter + "_" + name);
            dynamicContext.setValue("last_handler", name);
            
            if (shouldStop) {
                return stop(requestParameter, dynamicContext, stopResult);
            }
            
            return next(requestParameter, dynamicContext);
        }
    }

    /**
     * 抛出异常的处理器
     */
    public static class ExceptionHandler implements ILogicHandler<String, DynamicContext, String> {
        private final String name;

        public ExceptionHandler(String name) {
            this.name = name;
        }

        @Override
        public String apply(String requestParameter, DynamicContext dynamicContext) throws Exception {
            dynamicContext.setValue("exception_handler", name);
            throw new RuntimeException("测试异常: " + name);
        }
    }

    /**
     * 条件处理器
     */
    public static class ConditionalHandler implements ILogicHandler<String, DynamicContext, String> {
        private final String name;
        private final String conditionKey;

        public ConditionalHandler(String name, String conditionKey) {
            this.name = name;
            this.conditionKey = conditionKey;
        }

        @Override
        public String apply(String requestParameter, DynamicContext dynamicContext) {
            dynamicContext.setValue(name + "_executed", true);
            
            Boolean condition = dynamicContext.getValue(conditionKey);
            if (condition != null && condition) {
                return stop(requestParameter, dynamicContext, "条件停止: " + name);
            }
            
            return next(requestParameter, dynamicContext);
        }
    }

    /**
     * 测试对象类
     */
    public static class TestObject {
        private final String name;
        private final int value;

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestObject that = (TestObject) obj;
            return value == that.value && 
                   (name != null ? name.equals(that.name) : that.name == null);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + value;
            return result;
        }

        @Override
        public String toString() {
            return "TestObject{name='" + name + "', value=" + value + "}";
        }

        public String getName() { return name; }
        public int getValue() { return value; }
    }

    // ==================== DynamicContext 测试 ====================

    /**
     * 测试DynamicContext的基本功能
     */
    public void testDynamicContextBasicOperations() {
        System.out.println("\n=== 测试DynamicContext基本操作 ===");
        
        try {
            DynamicContext context = new DynamicContext();
            
            // 测试初始状态
            assertTrue("初始状态应该为proceed", context.isProceed());
            
            // 测试基本的键值对操作
            context.setValue("key1", "value1");
            context.setValue("key2", 123);
            context.setValue("key3", true);
            
            assertEquals("字符串值应该正确", "value1", context.getValue("key1"));
            assertEquals("整数值应该正确", Integer.valueOf(123), context.getValue("key2"));
            assertEquals("布尔值应该正确", Boolean.TRUE, context.getValue("key3"));
            
            // 测试null值处理
            context.setValue("nullKey", null);
            assertNull("null值应该被正确存储", context.getValue("nullKey"));
            
            // 测试不存在的键
            assertNull("不存在的键应该返回null", context.getValue("nonExistentKey"));
            
            // 测试状态控制
            context.setProceed(false);
            assertFalse("状态应该被正确设置为false", context.isProceed());
            
            context.setProceed(true);
            assertTrue("状态应该被正确设置为true", context.isProceed());
            
            System.out.println("DynamicContext基本操作测试通过");
            
        } catch (Exception e) {
            fail("DynamicContext基本操作测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试DynamicContext的复杂对象存储
     */
    public void testDynamicContextComplexObjects() {
        System.out.println("\n=== 测试DynamicContext复杂对象存储 ===");
        
        try {
            DynamicContext context = new DynamicContext();
            
            // 测试复杂对象存储
            TestObject obj = new TestObject("测试对象", 100);
            List<String> list = new ArrayList<>();
            list.add("item1");
            list.add("item2");
            
            context.setValue("testObject", obj);
            context.setValue("testList", list);
            
            TestObject retrievedObj = context.getValue("testObject");
            List<String> retrievedList = context.getValue("testList");
            
            assertEquals("对象应该被正确存储和检索", obj, retrievedObj);
            assertEquals("列表应该被正确存储和检索", list, retrievedList);
            assertEquals("列表大小应该正确", 2, retrievedList.size());
            
            System.out.println("DynamicContext复杂对象存储测试通过");
            
        } catch (Exception e) {
            fail("DynamicContext复杂对象存储测试失败: " + e.getMessage());
        }
    }

    // ==================== LinkedList 测试 ====================

    /**
     * 测试LinkedList的基本操作
     */
    public void testLinkedListBasicOperations() {
        System.out.println("\n=== 测试LinkedList基本操作 ===");
        
        try {
            LinkedList<String> list = new LinkedList<>("测试链表");
            
            // 测试空链表
            try {
                list.get(0);
                fail("空链表get操作应该抛出异常");
                System.out.println("空链表get操作测试通过");
            } catch (Exception e) {
                // 预期的异常
            }
            
            // 测试添加操作
            list.add("元素1");
            list.add("元素2");
            list.add("元素3");
            
            assertEquals("第一个元素应该正确", "元素1", list.get(0));
            assertEquals("第二个元素应该正确", "元素2", list.get(1));
            assertEquals("第三个元素应该正确", "元素3", list.get(2));
            
            // 测试addFirst
            list.addFirst("开头元素");
            assertEquals("开头元素应该在第一位", "开头元素", list.get(0));
            assertEquals("原第一个元素应该在第二位", "元素1", list.get(1));
            
            // 测试addLast
            list.addLast("结尾元素");
            assertEquals("结尾元素应该在最后", "结尾元素", list.get(4));
            
            // 测试remove
            boolean removed = list.remove("元素2");
            assertTrue("移除存在的元素应该返回true", removed);
            
            // 测试移除不存在的元素
            boolean notFound = list.remove("不存在的元素");
            assertFalse("移除不存在的元素应该返回false", notFound);
            
            System.out.println("LinkedList基本操作测试通过");
            
        } catch (Exception e) {
            fail("LinkedList基本操作测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试LinkedList的null值处理
     */
    public void testLinkedListNullHandling() {
        System.out.println("\n=== 测试LinkedList的null值处理 ===");
        
        try {
            LinkedList<String> list = new LinkedList<>("null测试链表");
            
            // 添加null值
            list.add(null);
            list.add("正常元素");
            list.add(null);
            
            assertNull("第一个null应该被正确存储", list.get(0));
            assertEquals("正常元素应该被正确存储", "正常元素", list.get(1));
            assertNull("第二个null应该被正确存储", list.get(2));
            
            // 移除null值
            boolean removed = list.remove(null);
            assertTrue("移除存在的null值应该返回true", removed);
            
            // 验证第一个null被移除，第二个null还在
            assertEquals("正常元素应该移到第一位", "正常元素", list.get(0));
            assertNull("第二个null应该还在", list.get(1));
            
            System.out.println("LinkedList的null值处理测试通过");
            
        } catch (Exception e) {
            fail("LinkedList的null值处理测试失败: " + e.getMessage());
        }
    }

    // ==================== BusinessLinkedList 测试 ====================

    /**
     * 测试BusinessLinkedList的基本执行
     */
    public void testBusinessLinkedListBasicExecution() {
        System.out.println("\n=== 测试BusinessLinkedList基本执行 ===");
        
        try {
            BusinessLinkedList<String, DynamicContext, String> businessList = 
                new BusinessLinkedList<>("测试业务链表");
            DynamicContext context = new DynamicContext();

            // 测试空链表执行
            String result = businessList.apply("测试输入", context);
            assertNull("空链表应该返回null", result);
            assertTrue("空链表执行后状态应该为true", context.isProceed());
            
            // 添加处理器
            TestHandler handler1 = new TestHandler("Handler1");
            TestHandler handler2 = new TestHandler("Handler2");
            
            businessList.add(handler1);
            businessList.add(handler2);
            
            // 执行链表
            context = new DynamicContext(); // 重置上下文
            result = businessList.apply("输入数据", context);
            
            assertNull("正常执行应该返回null", result);
            assertTrue("正常执行后状态应该为proceed", context.isProceed());
            assertEquals("第一个处理器应该被执行", "输入数据_Handler1", context.getValue("Handler1_processed"));
            assertEquals("第二个处理器应该被执行", "输入数据_Handler2", context.getValue("Handler2_processed"));
            assertEquals("最后执行的处理器应该是Handler2", "Handler2", context.getValue("last_handler"));
            
            System.out.println("BusinessLinkedList基本执行测试通过");
            
        } catch (Exception e) {
            fail("BusinessLinkedList基本执行测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试BusinessLinkedList的中断机制
     */
    public void testBusinessLinkedListInterruption() {
        System.out.println("\n=== 测试BusinessLinkedList中断机制 ===");
        
        try {
            BusinessLinkedList<String, DynamicContext, String> businessList = 
                new BusinessLinkedList<>("中断测试链表");
            DynamicContext context = new DynamicContext();
            
            TestHandler handler1 = new TestHandler("Handler1");
            TestHandler stopHandler = new TestHandler("StopHandler", true, "中断结果");
            TestHandler handler3 = new TestHandler("Handler3"); // 不应该被执行
            
            businessList.add(handler1);
            businessList.add(stopHandler);
            businessList.add(handler3);
            
            String result = businessList.apply("输入数据", context);
            
            assertEquals("应该返回中断结果", "中断结果", result);
            assertFalse("中断后状态应该为false", context.isProceed());
            assertEquals("第一个处理器应该被执行", "输入数据_Handler1", context.getValue("Handler1_processed"));
            assertEquals("停止处理器应该被执行", "输入数据_StopHandler", context.getValue("StopHandler_processed"));
            assertNull("第三个处理器不应该被执行", context.getValue("Handler3_processed"));
            
            System.out.println("BusinessLinkedList中断机制测试通过");
            
        } catch (Exception e) {
            fail("BusinessLinkedList中断机制测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试BusinessLinkedList的异常处理
     */
    public void testBusinessLinkedListExceptionHandling() {
        System.out.println("\n=== 测试BusinessLinkedList异常处理 ===");
        
        try {
            BusinessLinkedList<String, DynamicContext, String> businessList = 
                new BusinessLinkedList<>("异常测试链表");
            DynamicContext context = new DynamicContext();
            
            TestHandler normalHandler = new TestHandler("NormalHandler");
            ExceptionHandler exceptionHandler = new ExceptionHandler("ExceptionHandler");
            
            businessList.add(normalHandler);
            businessList.add(exceptionHandler);
            
            try {
                businessList.apply("输入数据", context);
                fail("应该抛出异常");
            } catch (RuntimeException e) {
                assertTrue("异常消息应该包含测试异常", e.getMessage().contains("测试异常"));
                assertEquals("正常处理器应该被执行", "输入数据_NormalHandler", context.getValue("NormalHandler_processed"));
                assertEquals("异常处理器应该被标记", "ExceptionHandler", context.getValue("exception_handler"));
            }
            
            System.out.println("BusinessLinkedList异常处理测试通过");
            
        } catch (Exception e) {
            fail("BusinessLinkedList异常处理测试失败: " + e.getMessage());
        }
    }

    // ==================== LinkArmory 测试 ====================

    /**
     * 测试LinkArmory的基本功能
     */
    public void testLinkArmoryBasicFunctionality() {
        System.out.println("\n=== 测试LinkArmory基本功能 ===");
        
        try {
            TestHandler handler1 = new TestHandler("ArmoryHandler1");
            TestHandler handler2 = new TestHandler("ArmoryHandler2");
            TestHandler handler3 = new TestHandler("ArmoryHandler3");
            
            @SuppressWarnings("unchecked")
            ILogicHandler<String, DynamicContext, String>[] handlers = new ILogicHandler[] {
                handler1, handler2, handler3
            };
            
            LinkArmory<String, DynamicContext, String> armory = new LinkArmory<>("测试军械库", handlers);
            
            BusinessLinkedList<String, DynamicContext, String> logicLink = armory.getLogicLink();
            assertNotNull("逻辑链路不应该为null", logicLink);
            
            DynamicContext context = new DynamicContext();
            String result = logicLink.apply("军械库测试", context);
            
            assertNull("正常执行应该返回null", result);
            assertTrue("执行后状态应该为proceed", context.isProceed());
            assertEquals("第一个处理器应该被执行", "军械库测试_ArmoryHandler1", context.getValue("ArmoryHandler1_processed"));
            assertEquals("第二个处理器应该被执行", "军械库测试_ArmoryHandler2", context.getValue("ArmoryHandler2_processed"));
            assertEquals("第三个处理器应该被执行", "军械库测试_ArmoryHandler3", context.getValue("ArmoryHandler3_processed"));
            
            System.out.println("LinkArmory基本功能测试通过");
            
        } catch (Exception e) {
            fail("LinkArmory基本功能测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试LinkArmory的空处理器数组
     */
    public void testLinkArmoryEmptyHandlers() {
        System.out.println("\n=== 测试LinkArmory空处理器数组 ===");
        
        try {
            @SuppressWarnings("unchecked")
            ILogicHandler<String, DynamicContext, String>[] emptyHandlers = new ILogicHandler[0];
            
            LinkArmory<String, DynamicContext, String> armory = new LinkArmory<>("空军械库", emptyHandlers);
            BusinessLinkedList<String, DynamicContext, String> logicLink = armory.getLogicLink();
            
            assertNotNull("逻辑链路不应该为null", logicLink);
            
            DynamicContext context = new DynamicContext();
            String result = logicLink.apply("空军械库测试", context);
            
            assertNull("空链路应该返回null", result);
            assertTrue("空链路执行后状态应该为proceed", context.isProceed());
            
            System.out.println("LinkArmory空处理器数组测试通过");
            
        } catch (Exception e) {
            fail("LinkArmory空处理器数组测试失败: " + e.getMessage());
        }
    }

    // ==================== ILogicHandler 测试 ====================

    /**
     * 测试ILogicHandler的next和stop方法
     */
    public void testILogicHandlerNextAndStop() {
        System.out.println("\n=== 测试ILogicHandler的next和stop方法 ===");
        
        try {
            DynamicContext context = new DynamicContext();
            
            // 测试next方法
            TestHandler nextHandler = new TestHandler("NextHandler");
            String nextResult = nextHandler.apply("next测试", context);
            
            assertNull("next方法应该返回null", nextResult);
            assertTrue("next方法执行后状态应该为proceed", context.isProceed());
            assertEquals("处理器应该被执行", "next测试_NextHandler", context.getValue("NextHandler_processed"));
            
            // 重置上下文
            context = new DynamicContext();
            
            // 测试stop方法
            TestHandler stopHandler = new TestHandler("StopHandler", true, "停止结果");
            String stopResult = stopHandler.apply("stop测试", context);
            
            assertEquals("stop方法应该返回指定结果", "停止结果", stopResult);
            assertFalse("stop方法执行后状态应该为false", context.isProceed());
            assertEquals("处理器应该被执行", "stop测试_StopHandler", context.getValue("StopHandler_processed"));
            
            System.out.println("ILogicHandler的next和stop方法测试通过");
            
        } catch (Exception e) {
            fail("ILogicHandler的next和stop方法测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试ILogicHandler的条件处理
     */
    public void testILogicHandlerConditionalProcessing() {
        System.out.println("\n=== 测试ILogicHandler条件处理 ===");
        
        try {
            ConditionalHandler conditionalHandler = new ConditionalHandler("ConditionalHandler", "shouldStop");
            
            // 测试条件为false时继续执行
            DynamicContext context1 = new DynamicContext(false);

            String result1 = conditionalHandler.apply("条件测试1", context1);
            assertNull("条件为false时应该返回null", result1);
            assertTrue("条件为false时状态应该为proceed", context1.isProceed());
            assertEquals("处理器应该被执行", Boolean.TRUE, context1.getValue("ConditionalHandler_executed"));
            
            // 测试条件为true时停止执行
            DynamicContext context2 = new DynamicContext();
            context2.setValue("shouldStop", true);

            String result2 = conditionalHandler.apply("条件测试2", context2);
            assertEquals("条件为true时应该返回停止结果", "条件停止: ConditionalHandler", result2);
            assertFalse("条件为true时状态应该为false", context2.isProceed());
            assertEquals("处理器应该被执行", Boolean.TRUE, context2.getValue("ConditionalHandler_executed"));
            
            System.out.println("ILogicHandler条件处理测试通过");
            
        } catch (Exception e) {
            fail("ILogicHandler条件处理测试失败: " + e.getMessage());
        }
    }

    // ==================== 综合测试 ====================

    /**
     * 测试完整的责任链流程
     */
    public void testCompleteChainFlow() {
        System.out.println("\n=== 测试完整的责任链流程 ===");
        
        try {
            // 创建复杂的处理器链
            TestHandler validationHandler = new TestHandler("ValidationHandler");
            ConditionalHandler conditionalHandler = new ConditionalHandler("ConditionalHandler", "skipProcessing");
            TestHandler processingHandler = new TestHandler("ProcessingHandler");
            TestHandler auditHandler = new TestHandler("AuditHandler");
            
            @SuppressWarnings("unchecked")
            ILogicHandler<String, DynamicContext, String>[] handlers = new ILogicHandler[] {
                validationHandler, conditionalHandler, processingHandler, auditHandler
            };
            
            LinkArmory<String, DynamicContext, String> armory = new LinkArmory<>("完整流程军械库", handlers);
            BusinessLinkedList<String, DynamicContext, String> chain = armory.getLogicLink();
            
            // 测试正常流程
            DynamicContext normalContext = new DynamicContext();
            normalContext.setValue("skipProcessing", false);
            
            String normalResult = chain.apply("正常业务数据", normalContext);
            
            assertNull("正常流程应该返回null", normalResult);
            assertTrue("正常流程执行后状态应该为proceed", normalContext.isProceed());
            assertEquals("验证处理器应该被执行", "正常业务数据_ValidationHandler", normalContext.getValue("ValidationHandler_processed"));
            assertEquals("条件处理器应该被执行", Boolean.TRUE, normalContext.getValue("ConditionalHandler_executed"));
            assertEquals("处理处理器应该被执行", "正常业务数据_ProcessingHandler", normalContext.getValue("ProcessingHandler_processed"));
            assertEquals("审计处理器应该被执行", "正常业务数据_AuditHandler", normalContext.getValue("AuditHandler_processed"));
            
            // 测试条件中断流程
            DynamicContext skipContext = new DynamicContext();
            skipContext.setValue("skipProcessing", true);
            
            String skipResult = chain.apply("跳过处理数据", skipContext);
            
            assertEquals("条件中断应该返回指定结果", "条件停止: ConditionalHandler", skipResult);
            assertFalse("条件中断后状态应该为false", skipContext.isProceed());
            assertEquals("验证处理器应该被执行", "跳过处理数据_ValidationHandler", skipContext.getValue("ValidationHandler_processed"));
            assertEquals("条件处理器应该被执行", Boolean.TRUE, skipContext.getValue("ConditionalHandler_executed"));
            assertNull("处理处理器不应该被执行", skipContext.getValue("ProcessingHandler_processed"));
            assertNull("审计处理器不应该被执行", skipContext.getValue("AuditHandler_processed"));
            
            System.out.println("完整的责任链流程测试通过");
            
        } catch (Exception e) {
            fail("完整的责任链流程测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试接口兼容性
     */
    public void testInterfaceCompatibility() {
        System.out.println("\n=== 测试接口兼容性 ===");
        
        try {
            // 测试ILink接口兼容性
            ILink<String> stringLink = new LinkedList<>("接口测试链表");
            
            stringLink.add("元素1");
            stringLink.addFirst("开头元素");
            stringLink.addLast("结尾元素");
            
            assertEquals("ILink接口应该正常工作", "开头元素", stringLink.get(0));
            assertEquals("ILink接口应该正常工作", "元素1", stringLink.get(1));
            assertEquals("ILink接口应该正常工作", "结尾元素", stringLink.get(2));
            
            boolean removed = stringLink.remove("元素1");
            assertTrue("ILink接口移除应该正常工作", removed);
            
            // 测试不同类型的链表
            ILink<Integer> intLink = new LinkedList<>("整数链表");
            intLink.add(100);
            intLink.add(200);
            
            assertEquals("整数链表应该正常工作", Integer.valueOf(100), intLink.get(0));
            assertEquals("整数链表应该正常工作", Integer.valueOf(200), intLink.get(1));
            
            // 测试自定义对象链表
            ILink<TestObject> objLink = new LinkedList<>("对象链表");
            TestObject obj1 = new TestObject("对象1", 1);
            TestObject obj2 = new TestObject("对象2", 2);
            
            objLink.add(obj1);
            objLink.add(obj2);
            
            assertEquals("对象链表应该正常工作", obj1, objLink.get(0));
            assertEquals("对象链表应该正常工作", obj2, objLink.get(1));
            
            System.out.println("接口兼容性测试通过");
            
        } catch (Exception e) {
            fail("接口兼容性测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试性能和大量数据处理
     */
    public void testPerformanceWithLargeData() {
        System.out.println("\n=== 测试性能和大量数据处理 ===");
        
        try {
            LinkedList<String> largeList = new LinkedList<>("大数据链表");
            
            // 添加大量数据
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 1000; i++) {
                largeList.add("元素" + i);
            }
            long addTime = System.currentTimeMillis() - startTime;
            
            // 验证数据正确性
            assertEquals("第一个元素应该正确", "元素0", largeList.get(0));
            assertEquals("中间元素应该正确", "元素500", largeList.get(500));
            assertEquals("最后元素应该正确", "元素999", largeList.get(999));
            
            // 测试查找性能
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                largeList.get(i * 10);
            }
            long getTime = System.currentTimeMillis() - startTime;
            
            // 测试移除性能
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                boolean removeResult = largeList.remove("元素" + (i * 100));
                assertTrue("移除操作应该成功", removeResult);
            }
            long removeTime = System.currentTimeMillis() - startTime;
            
            System.out.println("添加1000个元素耗时: " + addTime + "ms");
            System.out.println("查找100次耗时: " + getTime + "ms");
            System.out.println("移除10个元素耗时: " + removeTime + "ms");
            
            // 性能应该在合理范围内（这里只是简单验证不会超时）
            assertTrue("添加操作性能应该合理", addTime < 1000);
            assertTrue("查找操作性能应该合理", getTime < 1000);
            assertTrue("移除操作性能应该合理", removeTime < 1000);
            
            System.out.println("性能和大量数据处理测试通过");
            
        } catch (Exception e) {
            fail("性能和大量数据处理测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试真实业务场景
     */
    public void testRealWorldScenario() {
        System.out.println("\n=== 测试真实业务场景 ===");
        
        try {
            // 模拟一个订单处理的责任链
            TestHandler authHandler = new TestHandler("AuthenticationHandler");
            TestHandler validationHandler = new TestHandler("OrderValidationHandler");
            ConditionalHandler inventoryHandler = new ConditionalHandler("InventoryCheckHandler", "outOfStock");
            TestHandler paymentHandler = new TestHandler("PaymentHandler");
            TestHandler fulfillmentHandler = new TestHandler("FulfillmentHandler");
            
            @SuppressWarnings("unchecked")
            ILogicHandler<String, DynamicContext, String>[] orderHandlers = new ILogicHandler[] {
                authHandler, validationHandler, inventoryHandler, paymentHandler, fulfillmentHandler
            };
            
            LinkArmory<String, DynamicContext, String> orderArmory = new LinkArmory<>("订单处理军械库", orderHandlers);
            BusinessLinkedList<String, DynamicContext, String> orderChain = orderArmory.getLogicLink();
            
            // 测试成功的订单处理
            DynamicContext successContext = new DynamicContext();
            // 用户 ID
            successContext.setValue("userId", "user123");
            // 订单 ID
            successContext.setValue("orderId", "order456");
            // 商品库存是否超额
            successContext.setValue("outOfStock", false);
            
            String successResult = orderChain.apply("订单数据", successContext);
            
            assertNull("成功订单应该返回null", successResult);
            assertTrue("成功订单处理后状态应该为proceed", successContext.isProceed());
            assertEquals("认证处理器应该被执行", "订单数据_AuthenticationHandler", successContext.getValue("AuthenticationHandler_processed"));
            assertEquals("验证处理器应该被执行", "订单数据_OrderValidationHandler", successContext.getValue("OrderValidationHandler_processed"));
            assertEquals("库存检查处理器应该被执行", Boolean.TRUE, successContext.getValue("InventoryCheckHandler_executed"));
            assertEquals("支付处理器应该被执行", "订单数据_PaymentHandler", successContext.getValue("PaymentHandler_processed"));
            assertEquals("履约处理器应该被执行", "订单数据_FulfillmentHandler", successContext.getValue("FulfillmentHandler_processed"));
            
            // 测试库存不足的订单处理
            DynamicContext outOfStockContext = new DynamicContext();
            outOfStockContext.setValue("userId", "user789");
            outOfStockContext.setValue("orderId", "order101");
            outOfStockContext.setValue("outOfStock", true);
            
            String outOfStockResult = orderChain.apply("缺货订单数据", outOfStockContext);
            
            assertEquals("缺货订单应该返回停止结果", "条件停止: InventoryCheckHandler", outOfStockResult);
            assertFalse("缺货订单处理后状态应该为false", outOfStockContext.isProceed());
            assertEquals("认证处理器应该被执行", "缺货订单数据_AuthenticationHandler", outOfStockContext.getValue("AuthenticationHandler_processed"));
            assertEquals("验证处理器应该被执行", "缺货订单数据_OrderValidationHandler", outOfStockContext.getValue("OrderValidationHandler_processed"));
            assertEquals("库存检查处理器应该被执行", Boolean.TRUE, outOfStockContext.getValue("InventoryCheckHandler_executed"));
            assertNull("支付处理器不应该被执行", outOfStockContext.getValue("PaymentHandler_processed"));
            assertNull("履约处理器不应该被执行", outOfStockContext.getValue("FulfillmentHandler_processed"));
            
            System.out.println("真实业务场景测试通过");
            
        } catch (Exception e) {
            fail("真实业务场景测试失败: " + e.getMessage());
        }
    }
}