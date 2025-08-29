package com.jasonlat;

import com.jasonlat.design.framework.link.singleton.ILogicLink;
import com.jasonlat.singleton.factory.Rule01TradeRuleFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * 应用测试类
 * <p>
 * 该测试类用于测试整个应用的功能，包括：
 * <ul>
 *   <li>单例模式责任链测试：验证Rule01TradeRuleFactory的功能</li>
 *   <li>业务逻辑链测试：测试RuleLogic101和RuleLogic102的执行流程</li>
 *   <li>Spring集成测试：验证依赖注入和组件装配</li>
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
public class SingletonAppTest {

    @Resource
    private Rule01TradeRuleFactory rule01TradeRuleFactory;

    /**
     * 测试单例模式责任链的基本功能
     * <p>
     * 验证Rule01TradeRuleFactory能够正确创建和执行责任链
     * </p>
     */
    @Test
    public void testSingletonChainBasicExecution() {
        log.info("\n=== 测试单例模式责任链基本执行 ===");
        
        try {
            // 获取逻辑链路
            ILogicLink<String, Rule01TradeRuleFactory.DynamicContext, String> logicLink = 
                rule01TradeRuleFactory.openLogicLink();
            
            assertNotNull("逻辑链路不应该为null", logicLink);
            log.info("✓ 逻辑链路创建成功");
            
            // 创建测试上下文
            Rule01TradeRuleFactory.DynamicContext context = 
                Rule01TradeRuleFactory.DynamicContext.builder()
                    .age("25")
                    .build();
            
            assertNotNull("动态上下文不应该为null", context);
            assertEquals("年龄应该正确设置", "25", context.getAge());
            log.info("✓ 动态上下文创建成功，年龄: {}", context.getAge());
            
            // 执行责任链
            String requestParameter = "测试交易请求";
            log.info("开始执行责任链，请求参数: {}", requestParameter);
            
            String result = logicLink.apply(requestParameter, context);
            
            // 验证结果
            assertNotNull("执行结果不应该为null", result);
            assertEquals("执行结果应该正确", "link model01 单实例链", result);
            log.info("✓ 责任链执行成功，结果: {}", result);
            
            log.info("✓ 单例模式责任链基本执行测试通过");
            
        } catch (Exception e) {
            log.error("✗ 单例模式责任链基本执行测试失败", e);
            fail("单例模式责任链基本执行测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试责任链的多次执行
     * <p>
     * 验证单例模式下责任链的重复使用能力
     * </p>
     */
    @Test
    public void testSingletonChainMultipleExecution() {
        log.info("\n=== 测试单例模式责任链多次执行 ===");
        
        try {
            // 获取逻辑链路
            ILogicLink<String, Rule01TradeRuleFactory.DynamicContext, String> logicLink = 
                rule01TradeRuleFactory.openLogicLink();
            
            // 执行多次测试
            for (int i = 1; i <= 3; i++) {
                log.info("--- 第{}次执行 ---", i);
                
                // 创建不同的上下文
                Rule01TradeRuleFactory.DynamicContext context = 
                    Rule01TradeRuleFactory.DynamicContext.builder()
                        .age(String.valueOf(20 + i))
                        .build();
                
                String requestParameter = "测试请求_" + i;
                log.info("执行参数: {}, 年龄: {}", requestParameter, context.getAge());
                
                String result = logicLink.apply(requestParameter, context);
                
                assertNotNull("第" + i + "次执行结果不应该为null", result);
                assertEquals("第" + i + "次执行结果应该正确", "link model01 单实例链", result);
                log.info("✓ 第{}次执行成功，结果: {}", i, result);
            }
            
            log.info("✓ 单例模式责任链多次执行测试通过");
            
        } catch (Exception e) {
            log.error("✗ 单例模式责任链多次执行测试失败", e);
            fail("单例模式责任链多次执行测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试不同年龄参数的处理
     * <p>
     * 验证责任链对不同业务参数的处理能力
     * </p>
     */
    @Test
    public void testSingletonChainWithDifferentAges() {
        log.info("\n=== 测试不同年龄参数的处理 ===");
        
        try {
            ILogicLink<String, Rule01TradeRuleFactory.DynamicContext, String> logicLink = 
                rule01TradeRuleFactory.openLogicLink();
            
            // 测试不同年龄段
            String[] ages = {"18", "25", "35", "45", "65"};
            
            for (String age : ages) {
                log.info("--- 测试年龄: {} ---", age);
                
                Rule01TradeRuleFactory.DynamicContext context = 
                    Rule01TradeRuleFactory.DynamicContext.builder()
                        .age(age)
                        .build();
                
                String requestParameter = "年龄测试_" + age;
                log.info("执行参数: {}", requestParameter);
                
                String result = logicLink.apply(requestParameter, context);
                
                assertNotNull("年龄" + age + "的执行结果不应该为null", result);
                assertEquals("年龄" + age + "的执行结果应该正确", "link model01 单实例链", result);
                log.info("✓ 年龄{}测试成功，结果: {}", age, result);
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
    public void testSingletonChainEdgeCases() {
        log.info("\n=== 测试空值和边界情况 ===");
        
        try {
            ILogicLink<String, Rule01TradeRuleFactory.DynamicContext, String> logicLink = 
                rule01TradeRuleFactory.openLogicLink();
            
            // 测试空年龄
            log.info("--- 测试空年龄 ---");
            Rule01TradeRuleFactory.DynamicContext nullAgeContext = 
                Rule01TradeRuleFactory.DynamicContext.builder()
                    .age(null)
                    .build();
            
            String result1 = logicLink.apply("空年龄测试", nullAgeContext);
            assertNotNull("空年龄执行结果不应该为null", result1);
            log.info("✓ 空年龄测试成功，结果: {}", result1);
            
            // 测试空字符串年龄
            log.info("--- 测试空字符串年龄 ---");
            Rule01TradeRuleFactory.DynamicContext emptyAgeContext = 
                Rule01TradeRuleFactory.DynamicContext.builder()
                    .age("")
                    .build();
            
            String result2 = logicLink.apply("空字符串年龄测试", emptyAgeContext);
            assertNotNull("空字符串年龄执行结果不应该为null", result2);
            log.info("✓ 空字符串年龄测试成功，结果: {}", result2);
            
            // 测试空请求参数
            log.info("--- 测试空请求参数 ---");
            Rule01TradeRuleFactory.DynamicContext normalContext = 
                Rule01TradeRuleFactory.DynamicContext.builder()
                    .age("30")
                    .build();
            
            String result3 = logicLink.apply(null, normalContext);
            assertNotNull("空请求参数执行结果不应该为null", result3);
            log.info("✓ 空请求参数测试成功，结果: {}", result3);
            
            // 测试空上下文（这个可能会抛出异常，需要特殊处理）
            log.info("--- 测试空上下文 ---");
            try {
                String result4 = logicLink.apply("空上下文测试", null);
                log.info("✓ 空上下文测试成功，结果: {}", result4);
            } catch (Exception e) {
                log.info("✓ 空上下文测试按预期抛出异常: {}", e.getMessage());
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
     * 验证单例模式责任链在大量调用下的性能表现
     * </p>
     */
    @Test
    public void testSingletonChainPerformance() {
        log.info("\n=== 测试责任链性能 ===");
        
        try {
            ILogicLink<String, Rule01TradeRuleFactory.DynamicContext, String> logicLink = 
                rule01TradeRuleFactory.openLogicLink();
            
            int executionCount = 1000;
            log.info("开始性能测试，执行次数: {}", executionCount);
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < executionCount; i++) {
                Rule01TradeRuleFactory.DynamicContext context = 
                    Rule01TradeRuleFactory.DynamicContext.builder()
                        .age(String.valueOf(20 + (i % 50)))
                        .build();
                
                String result = logicLink.apply("性能测试_" + i, context);
                assertNotNull("第" + i + "次执行结果不应该为null", result);
                
                // 每100次输出一次进度
                if ((i + 1) % 100 == 0) {
                    log.info("已完成 {}/{} 次执行", i + 1, executionCount);
                }
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            double avgTime = (double) totalTime / executionCount;
            
            log.info("性能测试完成:");
            log.info("  总执行次数: {}", executionCount);
            log.info("  总耗时: {} ms", totalTime);
            log.info("  平均耗时: {:.2f} ms/次", avgTime);
            log.info("  吞吐量: {:.2f} 次/秒", 1000.0 / avgTime);
            
            // 性能断言（平均每次执行不应超过10ms）
            assertTrue("平均执行时间应该小于10ms，实际: " + avgTime + "ms", avgTime < 10.0);
            
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
            assertNotNull("Rule01TradeRuleFactory应该被正确注入", rule01TradeRuleFactory);
            log.info("✓ Rule01TradeRuleFactory注入成功: {}", rule01TradeRuleFactory.getClass().getSimpleName());
            
            // 验证工厂类能够创建逻辑链路
            ILogicLink<String, Rule01TradeRuleFactory.DynamicContext, String> logicLink = 
                rule01TradeRuleFactory.openLogicLink();
            
            assertNotNull("工厂类应该能够创建逻辑链路", logicLink);
            log.info("✓ 逻辑链路创建成功: {}", logicLink.getClass().getSimpleName());
            
            // 验证逻辑链路的基本功能
            Rule01TradeRuleFactory.DynamicContext context = 
                Rule01TradeRuleFactory.DynamicContext.builder()
                    .age("28")
                    .build();
            
            String result = logicLink.apply("依赖注入测试", context);
            assertNotNull("逻辑链路应该能够正常执行", result);
            log.info("✓ 逻辑链路执行成功，结果: {}", result);
            
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
            ILogicLink<String, Rule01TradeRuleFactory.DynamicContext, String> logicLink = 
                rule01TradeRuleFactory.openLogicLink();
            
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
                
                Rule01TradeRuleFactory.DynamicContext context = 
                    Rule01TradeRuleFactory.DynamicContext.builder()
                        .age(age)
                        .build();
                
                String requestParameter = String.format("%s|%s", scenarioName, tradeType);
                
                long startTime = System.currentTimeMillis();
                String result = logicLink.apply(requestParameter, context);
                long endTime = System.currentTimeMillis();
                
                assertNotNull(scenarioName + "执行结果不应该为null", result);
                assertEquals(scenarioName + "执行结果应该正确", "link model01 单实例链", result);
                
                log.info("✓ {}处理成功，耗时: {}ms，结果: {}", scenarioName, (endTime - startTime), result);
            }
            
            log.info("✓ 真实业务场景测试通过");
            
        } catch (Exception e) {
            log.error("✗ 真实业务场景测试失败", e);
            fail("真实业务场景测试失败: " + e.getMessage());
        }
    }
}
