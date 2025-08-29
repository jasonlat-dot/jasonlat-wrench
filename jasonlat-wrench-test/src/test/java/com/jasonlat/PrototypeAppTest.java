package com.jasonlat;

import com.jasonlat.design.framework.link.prototype.chain.BusinessLinkedList;
import com.jasonlat.prototype.factory.Rule02TradeRuleFactory;
import com.jasonlat.prototype.logic.XxxResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * 原型模式应用测试类
 * <p>
 * 该测试类用于测试原型模式责任链的功能，包括：
 * <ul>
 *   <li>原型模式责任链测试：验证Rule02TradeRuleFactory的功能</li>
 *   <li>业务逻辑链测试：测试RuleLogic201和RuleLogic202的执行流程</li>
 *   <li>Spring集成测试：验证依赖注入和组件装配</li>
 *   <li>多链路测试：验证demo01和demo02两个不同的责任链配置</li>
 * </ul>
 * </p>
 * 
 * @author Jasonlat
 * @version 1.0
 * @since 2025-08-29
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class PrototypeAppTest {

    @Resource(name = "demo01")
    private BusinessLinkedList<String, Rule02TradeRuleFactory.DynamicContext, XxxResponse> demo01;

    @Resource(name = "demo02")
    private BusinessLinkedList<String, Rule02TradeRuleFactory.DynamicContext, XxxResponse> demo02;

    @Resource
    private Rule02TradeRuleFactory rule02TradeRuleFactory;

    /**
     * 测试demo01责任链的基本功能
     * <p>
     * 验证demo01责任链（包含RuleLogic201和RuleLogic202）能够正确执行
     * </p>
     */
    @Test
    public void testDemo01ChainBasicExecution() {
        log.info("\n=== 测试demo01责任链基本执行 ===");
        
        try {
            assertNotNull("demo01责任链不应该为null", demo01);
            log.info("✓ demo01责任链注入成功");
            
            // 创建测试上下文
            Rule02TradeRuleFactory.DynamicContext context = 
                Rule02TradeRuleFactory.DynamicContext.builder()
                    .age("25")
                    .build();
            
            assertNotNull("动态上下文不应该为null", context);
            assertEquals("年龄应该正确设置", "25", context.getAge());
            log.info("✓ 动态上下文创建成功，年龄: {}", context.getAge());
            
            // 执行责任链
            String requestParameter = "测试原型模式请求";
            log.info("开始执行demo01责任链，请求参数: {}", requestParameter);
            
            XxxResponse result = demo01.apply(requestParameter, context);
            
            // 验证结果
            assertNotNull("执行结果不应该为null", result);
            assertNotNull("响应内容不应该为null", result.getAge());
            assertEquals("执行结果应该正确", "this is stop", result.getAge());
            log.info("✓ demo01责任链执行成功，结果: {}", result.getAge());
            
            log.info("✓ demo01责任链基本执行测试通过");
            
        } catch (Exception e) {
            log.error("✗ demo01责任链基本执行测试失败", e);
            fail("demo01责任链基本执行测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试demo02责任链的基本功能
     * <p>
     * 验证demo02责任链（只包含RuleLogic202）能够正确执行
     * </p>
     */
    @Test
    public void testDemo02ChainBasicExecution() {
        log.info("\n=== 测试demo02责任链基本执行 ===");
        
        try {
            assertNotNull("demo02责任链不应该为null", demo02);
            log.info("✓ demo02责任链注入成功");
            
            // 创建测试上下文
            Rule02TradeRuleFactory.DynamicContext context = 
                Rule02TradeRuleFactory.DynamicContext.builder()
                    .age("30")
                    .build();
            
            assertNotNull("动态上下文不应该为null", context);
            assertEquals("年龄应该正确设置", "30", context.getAge());
            log.info("✓ 动态上下文创建成功，年龄: {}", context.getAge());
            
            // 执行责任链
            String requestParameter = "测试demo02请求";
            log.info("开始执行demo02责任链，请求参数: {}", requestParameter);
            
            XxxResponse result = demo02.apply(requestParameter, context);
            
            // 验证结果
            assertNotNull("执行结果不应该为null", result);
            assertNotNull("响应内容不应该为null", result.getAge());
            assertEquals("执行结果应该正确", "this is stop", result.getAge());
            log.info("✓ demo02责任链执行成功，结果: {}", result.getAge());
            
            log.info("✓ demo02责任链基本执行测试通过");
            
        } catch (Exception e) {
            log.error("✗ demo02责任链基本执行测试失败", e);
            fail("demo02责任链基本执行测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试两个责任链的多次执行
     * <p>
     * 验证原型模式下责任链的重复使用能力
     * </p>
     */
    @Test
    public void testPrototypeChainMultipleExecution() {
        log.info("\n=== 测试原型模式责任链多次执行 ===");
        
        try {
            // 执行多次测试
            for (int i = 1; i <= 3; i++) {
                log.info("--- 第{}次执行 ---", i);
                
                // 创建不同的上下文
                Rule02TradeRuleFactory.DynamicContext context = 
                    Rule02TradeRuleFactory.DynamicContext.builder()
                        .age(String.valueOf(20 + i))
                        .build();
                
                String requestParameter = "测试请求_" + i;
                log.info("执行参数: {}, 年龄: {}", requestParameter, context.getAge());
                
                // 测试demo01
                XxxResponse result1 = demo01.apply(requestParameter, context);
                assertNotNull("demo01第" + i + "次执行结果不应该为null", result1);
                assertEquals("demo01第" + i + "次执行结果应该正确", "this is stop", result1.getAge());
                log.info("✓ demo01第{}次执行成功，结果: {}", i, result1.getAge());
                
                // 测试demo02
                XxxResponse result2 = demo02.apply(requestParameter, context);
                assertNotNull("demo02第" + i + "次执行结果不应该为null", result2);
                assertEquals("demo02第" + i + "次执行结果应该正确", "this is stop", result2.getAge());
                log.info("✓ demo02第{}次执行成功，结果: {}", i, result2.getAge());
            }
            
            log.info("✓ 原型模式责任链多次执行测试通过");
            
        } catch (Exception e) {
            log.error("✗ 原型模式责任链多次执行测试失败", e);
            fail("原型模式责任链多次执行测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试不同年龄参数的处理
     * <p>
     * 验证责任链对不同业务参数的处理能力
     * </p>
     */
    @Test
    public void testPrototypeChainWithDifferentAges() {
        log.info("\n=== 测试不同年龄参数的处理 ===");
        
        try {
            // 测试不同年龄段
            String[] ages = {"18", "25", "35", "45", "65"};
            
            for (String age : ages) {
                log.info("--- 测试年龄: {} ---", age);
                
                Rule02TradeRuleFactory.DynamicContext context = 
                    Rule02TradeRuleFactory.DynamicContext.builder()
                        .age(age)
                        .build();
                
                String requestParameter = "年龄测试_" + age;
                log.info("执行参数: {}", requestParameter);
                
                // 测试demo01
                XxxResponse result1 = demo01.apply(requestParameter, context);
                assertNotNull("demo01年龄" + age + "的执行结果不应该为null", result1);
                assertEquals("demo01年龄" + age + "的执行结果应该正确", "this is stop", result1.getAge());
                log.info("✓ demo01年龄{}测试成功，结果: {}", age, result1.getAge());
                
                // 测试demo02
                XxxResponse result2 = demo02.apply(requestParameter, context);
                assertNotNull("demo02年龄" + age + "的执行结果不应该为null", result2);
                assertEquals("demo02年龄" + age + "的执行结果应该正确", "this is stop", result2.getAge());
                log.info("✓ demo02年龄{}测试成功，结果: {}", age, result2.getAge());
            }
            
            log.info("✓ 不同年龄参数处理测试通过");
            
        } catch (Exception e) {
            log.error("✗ 不同年龄参数处理测试失败", e);
            fail("不同年龄参数处理测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试空值和边界情况
     * <p>
     * 验证责任链对异常输入的处理能力
     * </p>
     */
    @Test
    public void testPrototypeChainEdgeCases() {
        log.info("\n=== 测试空值和边界情况 ===");
        
        try {
            // 测试空年龄
            log.info("--- 测试空年龄 ---");
            Rule02TradeRuleFactory.DynamicContext nullAgeContext = 
                Rule02TradeRuleFactory.DynamicContext.builder()
                    .age(null)
                    .build();
            
            XxxResponse result1 = demo01.apply("空年龄测试", nullAgeContext);
            assertNotNull("demo01空年龄执行结果不应该为null", result1);
            log.info("✓ demo01空年龄测试成功，结果: {}", result1.getAge());
            
            XxxResponse result2 = demo02.apply("空年龄测试", nullAgeContext);
            assertNotNull("demo02空年龄执行结果不应该为null", result2);
            log.info("✓ demo02空年龄测试成功，结果: {}", result2.getAge());
            
            // 测试空字符串年龄
            log.info("--- 测试空字符串年龄 ---");
            Rule02TradeRuleFactory.DynamicContext emptyAgeContext = 
                Rule02TradeRuleFactory.DynamicContext.builder()
                    .age("")
                    .build();
            
            XxxResponse result3 = demo01.apply("空字符串年龄测试", emptyAgeContext);
            assertNotNull("demo01空字符串年龄执行结果不应该为null", result3);
            log.info("✓ demo01空字符串年龄测试成功，结果: {}", result3.getAge());
            
            XxxResponse result4 = demo02.apply("空字符串年龄测试", emptyAgeContext);
            assertNotNull("demo02空字符串年龄执行结果不应该为null", result4);
            log.info("✓ demo02空字符串年龄测试成功，结果: {}", result4.getAge());
            
            // 测试空请求参数
            log.info("--- 测试空请求参数 ---");
            Rule02TradeRuleFactory.DynamicContext normalContext = 
                Rule02TradeRuleFactory.DynamicContext.builder()
                    .age("30")
                    .build();
            
            XxxResponse result5 = demo01.apply(null, normalContext);
            assertNotNull("demo01空请求参数执行结果不应该为null", result5);
            log.info("✓ demo01空请求参数测试成功，结果: {}", result5.getAge());
            
            XxxResponse result6 = demo02.apply(null, normalContext);
            assertNotNull("demo02空请求参数执行结果不应该为null", result6);
            log.info("✓ demo02空请求参数测试成功，结果: {}", result6.getAge());
            
            // 测试空上下文（这个可能会抛出异常，需要特殊处理）
            log.info("--- 测试空上下文 ---");
            try {
                XxxResponse result7 = demo01.apply("空上下文测试", null);
                log.info("✓ demo01空上下文测试成功，结果: {}", result7.getAge());
            } catch (Exception e) {
                log.info("✓ demo01空上下文测试按预期抛出异常: {}", e.getMessage());
                // 这是预期的行为，不算测试失败
            }
            
            try {
                XxxResponse result8 = demo02.apply("空上下文测试", null);
                log.info("✓ demo02空上下文测试成功，结果: {}", result8.getAge());
            } catch (Exception e) {
                log.info("✓ demo02空上下文测试按预期抛出异常: {}", e.getMessage());
                // 这是预期的行为，不算测试失败
            }
            
            log.info("✓ 空值和边界情况测试通过");
            
        } catch (Exception e) {
            log.error("✗ 空值和边界情况测试失败", e);
            fail("空值和边界情况测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试责任链的性能
     * <p>
     * 验证原型模式责任链在大量调用下的性能表现
     * </p>
     */
    @Test
    public void testPrototypeChainPerformance() {
        log.info("\n=== 测试责任链性能 ===");
        
        try {
            int executionCount = 1000;
            log.info("开始性能测试，执行次数: {}", executionCount);
            
            // 测试demo01性能
            log.info("--- 测试demo01性能 ---");
            long startTime1 = System.currentTimeMillis();
            
            for (int i = 0; i < executionCount; i++) {
                Rule02TradeRuleFactory.DynamicContext context = 
                    Rule02TradeRuleFactory.DynamicContext.builder()
                        .age(String.valueOf(20 + (i % 50)))
                        .build();
                
                XxxResponse result = demo01.apply("性能测试_" + i, context);
                assertNotNull("demo01第" + i + "次执行结果不应该为null", result);
                
                // 每200次输出一次进度
                if ((i + 1) % 200 == 0) {
                    log.info("demo01已完成 {}/{} 次执行", i + 1, executionCount);
                }
            }
            
            long endTime1 = System.currentTimeMillis();
            long totalTime1 = endTime1 - startTime1;
            double avgTime1 = (double) totalTime1 / executionCount;
            
            log.info("demo01性能测试完成:");
            log.info("  总执行次数: {}", executionCount);
            log.info("  总耗时: {} ms", totalTime1);
            log.info("  平均耗时: {:.2f} ms/次", avgTime1);
            log.info("  吞吐量: {:.2f} 次/秒", 1000.0 / avgTime1);
            
            // 测试demo02性能
            log.info("--- 测试demo02性能 ---");
            long startTime2 = System.currentTimeMillis();
            
            for (int i = 0; i < executionCount; i++) {
                Rule02TradeRuleFactory.DynamicContext context = 
                    Rule02TradeRuleFactory.DynamicContext.builder()
                        .age(String.valueOf(20 + (i % 50)))
                        .build();
                
                XxxResponse result = demo02.apply("性能测试_" + i, context);
                assertNotNull("demo02第" + i + "次执行结果不应该为null", result);
                
                // 每200次输出一次进度
                if ((i + 1) % 200 == 0) {
                    log.info("demo02已完成 {}/{} 次执行", i + 1, executionCount);
                }
            }
            
            long endTime2 = System.currentTimeMillis();
            long totalTime2 = endTime2 - startTime2;
            double avgTime2 = (double) totalTime2 / executionCount;
            
            log.info("demo02性能测试完成:");
            log.info("  总执行次数: {}", executionCount);
            log.info("  总耗时: {} ms", totalTime2);
            log.info("  平均耗时: {:.2f} ms/次", avgTime2);
            log.info("  吞吐量: {:.2f} 次/秒", 1000.0 / avgTime2);
            
            // 性能断言（平均每次执行不应超过10ms）
            assertTrue("demo01平均执行时间应该小于10ms，实际: " + avgTime1 + "ms", avgTime1 < 10.0);
            assertTrue("demo02平均执行时间应该小于10ms，实际: " + avgTime2 + "ms", avgTime2 < 10.0);
            
            log.info("✓ 责任链性能测试通过");
            
        } catch (Exception e) {
            log.error("✗ 责任链性能测试失败", e);
            fail("责任链性能测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试Spring依赖注入
     * <p>
     * 验证Spring容器正确注入了所需的依赖
     * </p>
     */
    @Test
    public void testSpringDependencyInjection() {
        log.info("\n=== 测试Spring依赖注入 ===");
        
        try {
            // 验证工厂类注入
            assertNotNull("Rule02TradeRuleFactory应该被正确注入", rule02TradeRuleFactory);
            log.info("✓ Rule02TradeRuleFactory注入成功: {}", rule02TradeRuleFactory.getClass().getSimpleName());
            
            // 验证demo01注入
            assertNotNull("demo01应该被正确注入", demo01);
            log.info("✓ demo01注入成功: {}", demo01.getClass().getSimpleName());
            
            // 验证demo02注入
            assertNotNull("demo02应该被正确注入", demo02);
            log.info("✓ demo02注入成功: {}", demo02.getClass().getSimpleName());
            
            // 验证责任链的基本功能
            Rule02TradeRuleFactory.DynamicContext context = 
                Rule02TradeRuleFactory.DynamicContext.builder()
                    .age("28")
                    .build();
            
            XxxResponse result1 = demo01.apply("依赖注入测试", context);
            assertNotNull("demo01应该能够正常执行", result1);
            log.info("✓ demo01执行成功，结果: {}", result1.getAge());
            
            XxxResponse result2 = demo02.apply("依赖注入测试", context);
            assertNotNull("demo02应该能够正常执行", result2);
            log.info("✓ demo02执行成功，结果: {}", result2.getAge());
            
            log.info("✓ Spring依赖注入测试通过");
            
        } catch (Exception e) {
            log.error("✗ Spring依赖注入测试失败", e);
            fail("Spring依赖注入测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试真实业务场景
     * <p>
     * 模拟真实的交易规则处理场景
     * </p>
     */
    @Test
    public void testRealWorldTradeScenario() {
        log.info("\n=== 测试真实业务场景 ===");
        
        try {
            // 模拟不同的交易场景
            String[][] scenarios = {
                {"年轻用户交易", "22", "小额转账"},
                {"中年用户交易", "35", "投资理财"},
                {"老年用户交易", "65", "养老金管理"},
                {"企业用户交易", "0", "批量转账"},
                {"VIP用户交易", "40", "大额投资"}
            };
            
            for (String[] scenario : scenarios) {
                String scenarioName = scenario[0];
                String age = scenario[1];
                String tradeType = scenario[2];
                
                log.info("--- {} ---", scenarioName);
                log.info("用户年龄: {}, 交易类型: {}", age, tradeType);
                
                Rule02TradeRuleFactory.DynamicContext context = 
                    Rule02TradeRuleFactory.DynamicContext.builder()
                        .age(age)
                        .build();
                
                String requestParameter = String.format("%s|%s", scenarioName, tradeType);
                
                // 测试demo01
                long startTime1 = System.currentTimeMillis();
                XxxResponse result1 = demo01.apply(requestParameter, context);
                long endTime1 = System.currentTimeMillis();
                
                assertNotNull("demo01" + scenarioName + "执行结果不应该为null", result1);
                assertEquals("demo01" + scenarioName + "执行结果应该正确", "this is stop", result1.getAge());
                log.info("✓ demo01{}处理成功，耗时: {}ms，结果: {}", scenarioName, (endTime1 - startTime1), result1.getAge());
                
                // 测试demo02
                long startTime2 = System.currentTimeMillis();
                XxxResponse result2 = demo02.apply(requestParameter, context);
                long endTime2 = System.currentTimeMillis();
                
                assertNotNull("demo02" + scenarioName + "执行结果不应该为null", result2);
                assertEquals("demo02" + scenarioName + "执行结果应该正确", "this is stop", result2.getAge());
                log.info("✓ demo02{}处理成功，耗时: {}ms，结果: {}", scenarioName, (endTime2 - startTime2), result2.getAge());
            }
            
            log.info("✓ 真实业务场景测试通过");
            
        } catch (Exception e) {
            log.error("✗ 真实业务场景测试失败", e);
            fail("真实业务场景测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试责任链的差异性
     * <p>
     * 验证demo01和demo02两个责任链的不同行为
     * </p>
     */
    @Test
    public void testChainDifferences() {
        log.info("\n=== 测试责任链差异性 ===");
        
        try {
            Rule02TradeRuleFactory.DynamicContext context = 
                Rule02TradeRuleFactory.DynamicContext.builder()
                    .age("30")
                    .build();
            
            String requestParameter = "差异性测试";
            
            // 执行demo01（包含RuleLogic201和RuleLogic202）
            log.info("--- 执行demo01（完整链路） ---");
            XxxResponse result1 = demo01.apply(requestParameter, context);
            assertNotNull("demo01执行结果不应该为null", result1);
            log.info("✓ demo01执行结果: {}", result1.getAge());
            
            // 执行demo02（只包含RuleLogic202）
            log.info("--- 执行demo02（简化链路） ---");
            XxxResponse result2 = demo02.apply(requestParameter, context);
            assertNotNull("demo02执行结果不应该为null", result2);
            log.info("✓ demo02执行结果: {}", result2.getAge());
            
            // 验证两个链路的结果应该相同（因为最终都是RuleLogic202返回结果）
            assertEquals("两个链路的最终结果应该相同", result1.getAge(), result2.getAge());
            log.info("✓ 两个链路的最终结果一致: {}", result1.getAge());
            
            log.info("✓ 责任链差异性测试通过");
            
        } catch (Exception e) {
            log.error("✗ 责任链差异性测试失败", e);
            fail("责任链差异性测试失败: " + e.getMessage());
        }
    }
}