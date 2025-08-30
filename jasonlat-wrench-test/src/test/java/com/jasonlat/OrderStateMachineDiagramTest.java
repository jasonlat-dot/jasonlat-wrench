package com.jasonlat;

import com.jasonlat.design.framework.state.MermaidDiagramGenerator;
import com.jasonlat.design.framework.state.factory.StateFactory;
import com.jasonlat.design.framework.state.transition.StateTransition;
import com.jasonlat.state.context.OrderContext;
import com.jasonlat.state.event.OrderEvent;
import com.jasonlat.state.factory.OrderStatusMermaidDiagramFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * 订单状态机图表生成测试类
 * <p>
 * 基于 OrderStatusFactory 的状态机配置，使用 MermaidDiagramGenerator 
 * 生成订单状态机的可视化图表，包括状态图和流程图两种格式。
 * 演示如何将现有的状态机配置转换为可在 GitHub、GitLab 等平台
 * 渲染的 Mermaid 图表代码。
 * </p>
 * 
 * @author jasonlat
 * @since 1.0.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class OrderStateMachineDiagramTest {
    
    @Autowired
    private OrderStatusMermaidDiagramFactory orderStatusMermaidDiagramFactory;
    
    private StateFactory<OrderContext, OrderEvent, String> stateFactory;
    private List<StateTransition<OrderContext, OrderEvent, String>> transitions;
    
    /**
     * 测试初始化
     * <p>
     * 准备状态工厂和状态转换列表，基于 OrderStatusFactory 的配置
     * 构建用于图表生成的数据结构。
     * </p>
     */
    @Before
    public void setUp() {
        log.info("\n=== 初始化订单状态机图表生成测试 ===");
        
        // 获取状态工厂
        stateFactory = orderStatusMermaidDiagramFactory.stateFactory();
        // 基于 OrderStatusFactory 的配置创建状态转换列表
        transitions = orderStatusMermaidDiagramFactory.transitions();
        
        log.info("状态工厂和转换规则初始化完成，共 {} 个状态转换", transitions.size());
    }
    
    /**
     * 测试生成基础订单状态图
     * <p>
     * 生成简洁的订单状态图，适用于文档和演示场景。
     * 显示主要的状态转换关系，隐藏复杂的条件和优先级信息。
     * </p>
     */
    @Test
    public void testGenerateBasicOrderStateDiagram() {
        log.info("\n=== 生成基础订单状态图 ===");
        
        // 创建基础状态图生成器
        MermaidDiagramGenerator<OrderContext, OrderEvent, String> generator = 
            MermaidDiagramGenerator.create(stateFactory, transitions)
                .setTitle("订单状态机 - 基础版")
                .setDirection(MermaidDiagramGenerator.Direction.LEFT_RIGHT)
                .setInitialState("PendingPaymentState")
                .addFinalState("RefundedState")
                .showEventLabels(true)
                .showConditionLabels(false)
                .showPriority(false);
        
        // 生成状态图
        String stateDiagram = generator.generateStateDiagram();
        
        log.info("\n生成的基础状态图：\n{}", stateDiagram);
        
        // 保存到文件
        saveToFile("basic-order-state-diagram-" + System.currentTimeMillis() + ".md",
                  generator.generateMarkdown(MermaidDiagramGenerator.DiagramType.STATE_DIAGRAM));
        
        log.info("基础订单状态图生成成功");
    }
    
    /**
     * 测试生成详细订单流程图
     * <p>
     * 生成包含所有状态转换细节的流程图，适用于系统设计和
     * 技术文档场景。显示事件、条件和优先级等详细信息。
     * </p>
     */
    @Test
    public void testGenerateDetailedOrderFlowchart() {
        log.info("\n=== 生成详细订单流程图 ===");
        
        // 创建详细流程图生成器
        MermaidDiagramGenerator<OrderContext, OrderEvent, String> generator = 
            MermaidDiagramGenerator.create(stateFactory, transitions)
                .setTitle("订单处理流程 - 详细版")
                .setDirection(MermaidDiagramGenerator.Direction.TOP_DOWN)
                .setInitialState("PendingPaymentState")
                .addFinalState("RefundedState")
                .showEventLabels(true)
                .showConditionLabels(true)
                .showPriority(true);
        
        // 生成流程图
        String flowchart = generator.generateFlowchart();
        
        log.info("\n生成的详细流程图：\n{}", flowchart);
        
        // 保存到文件
        saveToFile("detailed-order-flowchart" + System.currentTimeMillis() + ".md",
                  generator.generateMarkdown(MermaidDiagramGenerator.DiagramType.FLOWCHART));

        log.info("详细订单流程图生成成功");
    }
    
    /**
     * 测试生成多种格式的图表
     * <p>
     * 同时生成状态图和流程图两种格式，并创建包含两种图表的
     * 完整 Markdown 文档，适用于项目文档和知识库。
     * </p>
     */
    @Test
    public void testGenerateMultipleFormats() {
        log.info("\n=== 生成多种格式的订单状态机图表 ===");
        
        // 创建通用图表生成器
        MermaidDiagramGenerator<OrderContext, OrderEvent, String> generator = 
            MermaidDiagramGenerator.create(stateFactory, transitions)
                .setTitle("订单状态机完整文档")
                .setDirection(MermaidDiagramGenerator.Direction.LEFT_RIGHT)
                .setInitialState("PendingPaymentState")
                .addFinalState("RefundedState")
                .showEventLabels(true)
                .showConditionLabels(false)
                .showPriority(false);
        
        // 生成状态图
        String stateDiagram = generator.generate(MermaidDiagramGenerator.DiagramType.STATE_DIAGRAM);
        log.info("\n=== 状态图格式 ===");
        log.info(stateDiagram);
        
        // 生成流程图
        String flowchart = generator.generate(MermaidDiagramGenerator.DiagramType.FLOWCHART);
        log.info("\n=== 流程图格式 ===");
        log.info(flowchart);
        
        // 生成完整的 Markdown 文档
        String completeMarkdown = createCompleteDocumentation(generator);
        log.info("\n=== 完整文档 ===");
        log.info("文档长度: {} 字符", completeMarkdown.length());
        
        // 保存完整文档
        saveToFile("complete-order-state-machine-docs.md", completeMarkdown);
        
        log.info("多种格式的订单状态机图表生成成功");
    }
    
    /**
     * 测试不同样式配置
     * <p>
     * 演示如何通过不同的配置选项生成适用于不同场景的图表样式，
     * 包括简洁样式、详细样式和演示样式。
     * </p>
     */
    @Test
    public void testDifferentStyles() {
        log.info("\n=== 测试不同样式配置 ===");
        
        // 简洁样式 - 适用于概览
        log.info("\n--- 简洁样式 ---");
        String simpleStyle = MermaidDiagramGenerator.create(stateFactory, transitions)
            .setTitle("订单状态概览")
            .setDirection(MermaidDiagramGenerator.Direction.LEFT_RIGHT)
            .showEventLabels(false)
            .showConditionLabels(false)
            .showPriority(false)
            .generateStateDiagram();
        log.info(simpleStyle);

        
        // 演示样式 - 适用于培训和演示
        log.info("\n--- 演示样式 ---");
        String presentationStyle = MermaidDiagramGenerator.create(stateFactory, transitions)
            .setTitle("订单处理演示")
            .setDirection(MermaidDiagramGenerator.Direction.LEFT_RIGHT)
            .setInitialState("PendingPaymentState")
            .addFinalState("RefundedState")
            .showEventLabels(true)
            .showConditionLabels(false)
            .showPriority(false)
            .generateStateDiagram();
        log.info(presentationStyle);
        
        // 保存不同样式
        saveToFile("simple-style-" + System.currentTimeMillis() + ".md", wrapInMarkdown("简洁样式", simpleStyle));
        saveToFile("presentation-style-" + System.currentTimeMillis() + ".md", wrapInMarkdown("演示样式", presentationStyle));
        
        log.info("不同样式配置测试完成");
    }

    
    /**
     * 创建完整的文档内容
     * <p>
     * 生成包含项目介绍、状态图、流程图和使用说明的完整 Markdown 文档。
     * </p>
     * 
     * @param generator 图表生成器
     * @return 完整的 Markdown 文档内容
     */
    private String createCompleteDocumentation(MermaidDiagramGenerator<OrderContext, OrderEvent, String> generator) {
        StringBuilder doc = new StringBuilder();
        
        doc.append("# 订单状态机文档\n\n");
        doc.append("## 概述\n\n");
        doc.append("本文档描述了订单处理系统中的状态机设计，包括各种状态之间的转换关系和触发条件。\n\n");
        
        doc.append("## 状态说明\n\n");
        doc.append("- **待支付 (PendingPaymentState)**: 订单创建后的初始状态\n");
        doc.append("- **已支付 (PaidState)**: 用户完成支付后的状态\n");
        doc.append("- **止付 (StoppedPaymentState)**: 风控系统阻止支付的状态\n");
        doc.append("- **已退款 (RefundedState)**: 订单退款完成的终态\n\n");
        
        doc.append("## 状态转换图\n\n");
        doc.append("### 状态图视图\n\n");
        doc.append("```mermaid\n");
        doc.append(generator.generateStateDiagram());
        doc.append("\n```\n\n");
        
        doc.append("### 流程图视图\n\n");
        doc.append("```mermaid\n");
        doc.append(generator.generateFlowchart());
        doc.append("\n```\n\n");
        
        doc.append("## 事件说明\n\n");
        doc.append("- **PAY**: 支付事件\n");
        doc.append("- **REFUND**: 退款事件\n");
        doc.append("- **STOP_PAYMENT**: 止付事件\n");
        doc.append("- **RESUME_PAYMENT**: 恢复支付事件\n");
        doc.append("- **QUERY**: 查询事件\n\n");
        
        doc.append("## 使用说明\n\n");
        doc.append("该状态机基于 OrderStatusFactory 的配置生成，");
        doc.append("可以通过 MermaidDiagramGenerator 工具自动生成可视化图表。\n\n");
        
        doc.append("生成时间: ").append(java.time.LocalDateTime.now()).append("\n");
        
        return doc.toString();
    }
    
    /**
     * 将图表代码包装为 Markdown 格式
     * <p>
     * 为单个图表添加标题和 Mermaid 代码块格式。
     * </p>
     * 
     * @param title 图表标题
     * @param diagramCode 图表代码
     * @return Markdown 格式的内容
     */
    private String wrapInMarkdown(String title, String diagramCode) {
        return String.format("# %s\n\n```mermaid\n%s\n```\n", title, diagramCode);
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
            Files.createDirectories(Paths.get(outputDir));
            
            // 构建完整文件路径
            String filePath = outputDir + filename;
            
            // 写入文件内容
            Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8));
            
            // 记录保存成功信息
            log.info("文件已保存: {} (大小: {} 字节)", filePath, content.getBytes(StandardCharsets.UTF_8).length);
            
            // 显示文件内容预览
            int previewLength = Math.min(200, content.length());
            String preview = content.substring(0, previewLength);
            if (content.length() > previewLength) {
                preview += "...";
            }
            log.info("文件内容预览：\n{}", preview);
            
        } catch (IOException e) {
            log.error("保存文件失败: {} - {}", filename, e.getMessage(), e);
        } catch (Exception e) {
            log.error("保存文件时发生未知错误: {} - {}", filename, e.getMessage(), e);
        }
    }
    
    
}