package com.jasonlat;

import com.jasonlat.design.framework.state.MermaidDiagramGenerator;
import com.jasonlat.design.framework.state.factory.StateFactory;
import com.jasonlat.design.framework.state.transition.StateTransition;
import com.jasonlat.state.context.OrderContext;
import com.jasonlat.state.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Mermaid图表生成器使用示例
 * <p>
 * 演示如何使用MermaidDiagramGenerator为订单状态机生成可视化图表，
 * 包括状态图和流程图两种格式，可以在GitHub、GitLab、Notion等
 * 支持Mermaid的平台上渲染显示。
 * </p>
 * 
 * @author jasonlat
 * @since 1.0.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MermaidDiagramGeneratorExample {
    
    /**
     * 基础状态图生成示例
     * <p>
     * 演示如何生成订单状态机的基础状态图
     * </p>
     */
    @Test
    public void testBasicStateDiagram() {
        log.info("\n=== 基础状态图生成示例 ===");
        
        // 1. 创建状态工厂
        StateFactory<OrderContext, OrderEvent, String> stateFactory = new StateFactory<>();
        
        // 2. 定义状态转换规则
        List<StateTransition<OrderContext, OrderEvent, String>> transitions = createOrderTransitions();
        
        // 3. 创建Mermaid图表生成器
        MermaidDiagramGenerator<OrderContext, OrderEvent, String> generator = 
            MermaidDiagramGenerator.create(stateFactory, transitions)
                .setTitle("订单状态机")
                .setInitialState("PendingPaymentState")
                .addFinalState("RefundedState")
                .showEventLabels(true)
                .showConditionLabels(false);
        
        // 4. 生成状态图
        String stateDiagram = generator.generateStateDiagram();
        
        log.info("\n生成的状态图代码：\n{}", stateDiagram);
        
        // 5. 保存到文件（可选）
        saveToFile("order-state-diagram" + System.currentTimeMillis() + ".md", generator.generateMarkdown());
    }
    
    /**
     * 流程图生成示例
     * <p>
     * 演示如何生成订单状态机的流程图
     * </p>
     */
    @Test
    public void testFlowchartDiagram() {
        log.info("\n=== 流程图生成示例 ===");
        
        // 创建状态工厂和转换规则
        StateFactory<OrderContext, OrderEvent, String> stateFactory = new StateFactory<>();
        List<StateTransition<OrderContext, OrderEvent, String>> transitions = createOrderTransitions();
        
        // 创建流程图生成器
        MermaidDiagramGenerator<OrderContext, OrderEvent, String> generator = 
            MermaidDiagramGenerator.create(stateFactory, transitions)
                .setTitle("订单处理流程")
                .setDirection(MermaidDiagramGenerator.Direction.LEFT_RIGHT)
                .setInitialState("PendingPaymentState")
                .addFinalState("RefundedState")
                .showEventLabels(true)
                .showConditionLabels(true)
                .showPriority(false);
        
        // 生成流程图
        String flowchart = generator.generateFlowchart();
        
        log.info("\n生成的流程图代码：\n{}", flowchart);
        
        // 保存到文件
        saveToFile("order-flowchart" + System.currentTimeMillis() + ".md", generator.generateMarkdown(MermaidDiagramGenerator.DiagramType.FLOWCHART));
    }
    
    /**
     * 完整功能演示
     * <p>
     * 演示MermaidDiagramGenerator的所有功能特性
     * </p>
     */
    @Test
    public void testCompleteFeatures() {
        log.info("\n=== 完整功能演示 ===");
        
        StateFactory<OrderContext, OrderEvent, String> stateFactory = new StateFactory<>();
        List<StateTransition<OrderContext, OrderEvent, String>> transitions = createDetailedTransitions();
        
        // 创建功能完整的生成器
        MermaidDiagramGenerator<OrderContext, OrderEvent, String> generator = 
            MermaidDiagramGenerator.create(stateFactory, transitions)
                .setTitle("订单状态机完整流程图")
                .setDirection(MermaidDiagramGenerator.Direction.TOP_DOWN)
                .setInitialState("PendingPaymentState")
                .addFinalState("RefundedState")
                .showEventLabels(true)
                .showConditionLabels(true)
                .showPriority(true);
        
        // 生成不同类型的图表
        log.info("\n=== 状态图 ===");
        String stateDiagram = generator.generate(MermaidDiagramGenerator.DiagramType.STATE_DIAGRAM);
        log.info(stateDiagram);
        
        log.info("\n=== 流程图 ===");
        String flowchart = generator.generate(MermaidDiagramGenerator.DiagramType.FLOWCHART);
        log.info(flowchart);
        
        // 生成Markdown文档
        log.info("\n=== Markdown文档 ===");
        String markdown = generator.generateMarkdown();
        log.info(markdown);
        
        // 保存所有格式
        saveToFile("complete-state-diagram" + System.currentTimeMillis() + ".md", generator.generateMarkdown(MermaidDiagramGenerator.DiagramType.STATE_DIAGRAM));
        saveToFile("complete-flowchart" + System.currentTimeMillis() + ".md", generator.generateMarkdown(MermaidDiagramGenerator.DiagramType.FLOWCHART));
    }
    
    /**
     * 自定义样式示例
     * <p>
     * 演示如何自定义图表的显示样式
     * </p>
     */
    @Test
    public void testCustomStyles() {
        log.info("\n=== 自定义样式示例 ===");
        
        StateFactory<OrderContext, OrderEvent, String> stateFactory = new StateFactory<>();
        List<StateTransition<OrderContext, OrderEvent, String>> transitions = createOrderTransitions();
        
        // 简洁样式 - 只显示状态转换
        MermaidDiagramGenerator<OrderContext, OrderEvent, String> simpleGenerator = 
            MermaidDiagramGenerator.create(stateFactory, transitions)
                .setTitle("简洁订单状态图")
                .setDirection(MermaidDiagramGenerator.Direction.LEFT_RIGHT)
                .showEventLabels(false)
                .showConditionLabels(false)
                .showPriority(false);
        
        log.info("\n简洁样式状态图：\n{}", simpleGenerator.generateStateDiagram());
        
        // 详细样式 - 显示所有信息
        MermaidDiagramGenerator<OrderContext, OrderEvent, String> detailedGenerator = 
            MermaidDiagramGenerator.create(stateFactory, transitions)
                .setTitle("详细订单状态图")
                .setDirection(MermaidDiagramGenerator.Direction.TOP_DOWN)
                .showEventLabels(true)
                .showConditionLabels(true)
                .showPriority(true);
        
        log.info("\n详细样式状态图：\n{}", detailedGenerator.generateStateDiagram());
        
        // 保存不同样式
        saveToFile("simple-style" + System.currentTimeMillis() + ".md", simpleGenerator.generateMarkdown());
        saveToFile("detailed-style" + System.currentTimeMillis() + ".md", detailedGenerator.generateMarkdown());
    }
    
    /**
     * 创建订单状态转换规则
     * 
     * @return 状态转换列表
     */
    private List<StateTransition<OrderContext, OrderEvent, String>> createOrderTransitions() {
        List<StateTransition<OrderContext, OrderEvent, String>> transitions = new ArrayList<>();
        
        // 待支付 -> 已支付
        transitions.add(StateTransition.<OrderContext, OrderEvent, String>builder()
            .from("PendingPaymentState")
            .to("PaidState")
            .on(OrderEvent.PAY)
            .description("支付成功")
            .priority(1)
            .build());
        
        // 待支付 -> 止付
        transitions.add(StateTransition.<OrderContext, OrderEvent, String>builder()
            .from("PendingPaymentState")
            .to("StoppedPaymentState")
            .on(OrderEvent.STOP_PAYMENT)
            .description("风控止付")
            .priority(2)
            .build());
        
        // 已支付 -> 已退款
        transitions.add(StateTransition.<OrderContext, OrderEvent, String>builder()
            .from("PaidState")
            .to("RefundedState")
            .on(OrderEvent.REFUND)
            .description("申请退款")
            .priority(1)
            .build());
        
        // 止付 -> 待支付
        transitions.add(StateTransition.<OrderContext, OrderEvent, String>builder()
            .from("StoppedPaymentState")
            .to("PendingPaymentState")
            .on(OrderEvent.RESUME_PAYMENT)
            .description("恢复支付")
            .priority(1)
            .build());
        
        // 止付 -> 已支付
        transitions.add(StateTransition.<OrderContext, OrderEvent, String>builder()
            .from("StoppedPaymentState")
            .to("PaidState")
            .on(OrderEvent.PAY)
            .description("直接支付")
            .priority(2)
            .build());
        
        return transitions;
    }
    
    /**
     * 创建详细的状态转换规则
     * 
     * @return 详细状态转换列表
     */
    private List<StateTransition<OrderContext, OrderEvent, String>> createDetailedTransitions() {
        List<StateTransition<OrderContext, OrderEvent, String>> transitions = createOrderTransitions();
        
        // 添加更多转换规则
        transitions.add(StateTransition.<OrderContext, OrderEvent, String>builder()
            .from("PendingPaymentState")
            .to("PendingPaymentState")
            .on(OrderEvent.QUERY)
            .description("查询订单")
            .priority(3)
            .build());
        
        transitions.add(StateTransition.<OrderContext, OrderEvent, String>builder()
            .from("RefundedState")
            .to("RefundedState")
            .on(OrderEvent.QUERY)
            .description("查询退款")
            .priority(1)
            .build());
        
        return transitions;
    }
    
    /**
     * 保存内容到文件
     * <p>
     * 将生成的图表内容保存到指定的文件中，便于后续查看和使用。
     * 文件将保存到 target/generated-docs/ 目录下。
     * </p>
     * 
     * @param filename 文件名
     * @param content 文件内容
     */
    private void saveToFile(String filename, String content) {
        try {
            // 创建输出目录
            String outputDir = "./mermaid/generated-docs/";
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(outputDir));
            
            // 构建完整文件路径
            String filePath = outputDir + filename;
            
            // 写入文件内容
            java.nio.file.Files.write(java.nio.file.Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8));
            
            // 记录保存成功信息
            log.info("文件已保存: {} (大小: {} 字节)", filePath, content.getBytes(StandardCharsets.UTF_8).length);
            
            // 显示文件内容预览
            int previewLength = Math.min(200, content.length());
            String preview = content.substring(0, previewLength);
            if (content.length() > previewLength) {
                preview += "...";
            }
            log.info("文件内容预览：\n{}", preview);
            
        } catch (java.io.IOException e) {
            log.error("保存文件失败: {} - {}", filename, e.getMessage(), e);
        } catch (Exception e) {
            log.error("保存文件时发生未知错误: {} - {}", filename, e.getMessage(), e);
        }
    }
    

}