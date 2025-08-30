package com.jasonlat.design.framework.state;

import com.jasonlat.design.framework.state.factory.StateFactory;
import com.jasonlat.design.framework.state.machine.StateMachineBuilder;
import com.jasonlat.design.framework.state.transition.StateTransition;
import lombok.Getter;

import java.util.*;

/**
 * Mermaid图表生成器
 * <p>
 * 该类用于生成状态机的Mermaid图表代码，支持状态图和流程图两种格式。
 * 生成的图表可以在支持Mermaid的平台上渲染，如GitHub、GitLab、Notion等。
 * </p>
 * 
 * @param <C> 上下文类型
 * @param <E> 事件类型
 * @param <R> 结果类型
 * 
 * @author jasonlat
 * @version 1.0
 * @since 2025-08-30
 */
public class MermaidDiagramGenerator<C extends IStateContext<C, E, R>, E, R> {
    
    /**
     * 图表类型枚举
     */
    public enum DiagramType {
        /**
         * 状态图
         */
        STATE_DIAGRAM,
        
        /**
         * 流程图
         */
        FLOWCHART
    }
    
    /**
     * 图表方向枚举
     */
    @Getter
    public enum Direction {
        /**
         * 从上到下
         */
        TOP_DOWN("TD"),
        
        /**
         * 从左到右
         */
        LEFT_RIGHT("LR"),
        
        /**
         * 从下到上
         */
        BOTTOM_UP("BT"),
        
        /**
         * 从右到左
         */
        RIGHT_LEFT("RL");
        
        private final String code;
        
        Direction(String code) {
            this.code = code;
        }

    }
    
    /**
     * 状态工厂
     */
    private final StateFactory<C, E, R> stateFactory;
    
    /**
     * 状态转换列表
     */
    private final List<StateTransition<C, E, R>> transitions;
    
    /**
     * 初始状态名称
     */
    private String initialStateName;
    
    /**
     * 终止状态名称集合
     */
    private final Set<String> finalStateNames;
    
    /**
     * 图表标题
     */
    private String title;
    
    /**
     * 图表方向
     */
    private Direction direction = Direction.TOP_DOWN;
    
    /**
     * 是否显示事件标签
     */
    private boolean showEventLabels = true;
    
    /**
     * 是否显示条件标签
     */
    private boolean showConditionLabels = true;
    
    /**
     * 是否显示优先级
     */
    private boolean showPriority = false;
    
    /**
     * 构造函数
     * 
     * @param stateFactory 状态工厂
     * @param transitions 状态转换列表
     */
    public MermaidDiagramGenerator(StateFactory<C, E, R> stateFactory, 
                                  List<StateTransition<C, E, R>> transitions) {
        this.stateFactory = Objects.requireNonNull(stateFactory, "状态工厂不能为空");
        this.transitions = new ArrayList<>(Objects.requireNonNull(transitions, "状态转换列表不能为空"));
        this.finalStateNames = new HashSet<>();
    }
    
    /**
     * 设置初始状态
     * 
     * @param initialStateName 初始状态名称
     * @return 生成器实例
     */
    public MermaidDiagramGenerator<C, E, R> setInitialState(String initialStateName) {
        this.initialStateName = initialStateName;
        return this;
    }
    
    /**
     * 添加终止状态
     * 
     * @param finalStateName 终止状态名称
     * @return 生成器实例
     */
    public MermaidDiagramGenerator<C, E, R> addFinalState(String finalStateName) {
        this.finalStateNames.add(finalStateName);
        return this;
    }
    
    /**
     * 设置图表标题
     * 
     * @param title 图表标题
     * @return 生成器实例
     */
    public MermaidDiagramGenerator<C, E, R> setTitle(String title) {
        this.title = title;
        return this;
    }
    
    /**
     * 设置图表方向
     * 
     * @param direction 图表方向
     * @return 生成器实例
     */
    public MermaidDiagramGenerator<C, E, R> setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }
    
    /**
     * 设置是否显示事件标签
     * 
     * @param show 是否显示
     * @return 生成器实例
     */
    public MermaidDiagramGenerator<C, E, R> showEventLabels(boolean show) {
        this.showEventLabels = show;
        return this;
    }
    
    /**
     * 设置是否显示条件标签
     * 
     * @param show 是否显示
     * @return 生成器实例
     */
    public MermaidDiagramGenerator<C, E, R> showConditionLabels(boolean show) {
        this.showConditionLabels = show;
        return this;
    }
    
    /**
     * 设置是否显示优先级
     * 
     * @param show 是否显示
     * @return 生成器实例
     */
    public MermaidDiagramGenerator<C, E, R> showPriority(boolean show) {
        this.showPriority = show;
        return this;
    }
    
    /**
     * 生成状态图
     * 
     * @return Mermaid状态图代码
     */
    public String generateStateDiagram() {
        StringBuilder sb = new StringBuilder();
        
        // 图表头部
        sb.append("stateDiagram-v2\n");
        
        // 图表方向
        if (direction != Direction.TOP_DOWN) {
            sb.append("    direction ").append(direction.getCode()).append("\n");
        }
        
        // 图表标题
        if (title != null && !title.trim().isEmpty()) {
            sb.append("    title: ").append(title).append("\n");
        }
        
        sb.append("\n");
        
        // 获取所有状态
        Set<String> allStates = getAllStates();
        
        // 定义状态
        for (String stateName : allStates) {
            String displayName = formatStateName(stateName);
            if (!stateName.equals(displayName)) {
                sb.append("    ").append(stateName).append(" : ").append(displayName).append("\n");
            }
        }
        
        sb.append("\n");
        
        // 初始状态
        if (initialStateName != null) {
            sb.append("    [*] --> ").append(initialStateName).append("\n");
        }
        
        // 状态转换
        for (StateTransition<C, E, R> transition : transitions) {
            sb.append("    ").append(transition.getFromState())
              .append(" --> ").append(transition.getToState());
            
            // 转换标签
            String label = buildTransitionLabel(transition);
            if (!label.isEmpty()) {
                sb.append(" : ").append(label);
            }
            
            sb.append("\n");
        }
        
        // 终止状态
        for (String finalState : finalStateNames) {
            sb.append("    ").append(finalState).append(" --> [*]\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 生成流程图
     * 
     * @return Mermaid流程图代码
     */
    public String generateFlowchart() {
        StringBuilder sb = new StringBuilder();
        
        // 图表头部
        sb.append("flowchart ").append(direction.getCode()).append("\n");
        
        // 图表标题
        if (title != null && !title.trim().isEmpty()) {
            sb.append("    title[\"").append(title).append("\"]\n");
        }
        
        sb.append("\n");
        
        // 获取所有状态
        Set<String> allStates = getAllStates();
        
        // 定义状态节点
        for (String stateName : allStates) {
            String nodeShape = getNodeShape(stateName);
            String displayName = formatStateName(stateName);
            sb.append("    ").append(stateName).append(nodeShape.replace("{text}", displayName)).append("\n");
        }
        
        sb.append("\n");
        
        // 初始状态
        if (initialStateName != null) {
            sb.append("    Start([开始]) --> ").append(initialStateName).append("\n");
        }
        
        // 状态转换
        for (StateTransition<C, E, R> transition : transitions) {
            sb.append("    ").append(transition.getFromState());
            
            // 转换标签
            String label = buildTransitionLabel(transition);
            if (!label.isEmpty()) {
                sb.append(" -->|").append(label).append("| ").append(transition.getToState());
            } else {
                sb.append(" --> ").append(transition.getToState());
            }
            
            sb.append("\n");
        }
        
        // 终止状态
        for (String finalState : finalStateNames) {
            sb.append("    ").append(finalState).append(" --> End([结束])\n");
        }
        
        // 样式定义
        sb.append("\n");
        sb.append("    classDef startEnd fill:#e1f5fe,stroke:#01579b,stroke-width:2px\n");
        sb.append("    classDef state fill:#f3e5f5,stroke:#4a148c,stroke-width:2px\n");
        sb.append("    classDef finalState fill:#e8f5e8,stroke:#1b5e20,stroke-width:2px\n");
        
        // 应用样式
        if (initialStateName != null) {
            sb.append("    class Start startEnd\n");
        }
        for (String finalState : finalStateNames) {
            sb.append("    class ").append(finalState).append(" finalState\n");
        }
        sb.append("    class End startEnd\n");
        
        return sb.toString();
    }
    
    /**
     * 生成图表（默认状态图）
     * 
     * @param type 图表类型
     * @return Mermaid图表代码
     */
    public String generate(DiagramType type) {
        switch (type) {
            case STATE_DIAGRAM:
                return generateStateDiagram();
            case FLOWCHART:
                return generateFlowchart();
            default:
                return generateStateDiagram();
        }
    }
    
    /**
     * 生成图表（默认状态图）
     * 
     * @return Mermaid状态图代码
     */
    public String generate() {
        return generateStateDiagram();
    }
    
    /**
     * 获取所有状态名称
     * 
     * @return 状态名称集合
     */
    private Set<String> getAllStates() {
        Set<String> states = new HashSet<>();
        
        // 从状态工厂获取
        states.addAll(stateFactory.getRegisteredStateNames());
        
        // 从转换规则获取
        for (StateTransition<C, E, R> transition : transitions) {
            states.add(transition.getFromState());
            states.add(transition.getToState());
        }
        
        return states;
    }
    
    /**
     * 状态名称中文映射表
     */
    private static final Map<String, String> STATE_NAME_MAPPING = new HashMap<String, String>() {{
        put("PENDING_PAYMENT", "待支付");
        put("PAID", "已支付");
        put("STOPPED_PAYMENT", "已止付");
        put("CANCELLED", "已取消");
        put("REFUNDED", "已退款");
        put("SHIPPED", "已发货");
        put("DELIVERED", "已送达");
        put("COMPLETED", "已完成");
        put("FAILED", "失败");
        put("PROCESSING", "处理中");
        put("CONFIRMED", "已确认");
        put("REJECTED", "已拒绝");
    }};
    
    /**
     * 格式化状态名称
     * 
     * @param stateName 原始状态名称
     * @return 格式化后的状态名称
     */
    private String formatStateName(String stateName) {
        if (stateName == null) {
            return "Unknown";
        }
        
        // 如果有中文映射，返回中文描述加英文原名
        String chineseName = STATE_NAME_MAPPING.get(stateName);
        if (chineseName != null) {
            return chineseName + "（" + stateName + "）";
        }
        
        // 将驼峰命名转换为可读格式
        return stateName.replaceAll("([a-z])([A-Z])", "$1 $2")
                       .replaceAll("_", " ")
                       .trim();
    }
    
    /**
     * 构建转换标签
     * 
     * @param transition 状态转换
     * @return 转换标签
     */
    private String buildTransitionLabel(StateTransition<C, E, R> transition) {
        List<String> labelParts = new ArrayList<>();
        
        // 事件标签
        if (showEventLabels && transition.getTriggerEvent() != null) {
            labelParts.add(transition.getTriggerEvent().toString());
        }
        
        // 条件标签
        if (showConditionLabels && transition.hasCondition()) {
            labelParts.add("[条件]");
        }
        
        // 优先级
        if (showPriority && transition.getPriority() != 0) {
            labelParts.add("P" + transition.getPriority());
        }
        
        // 描述
        if (transition.getDescription() != null && !transition.getDescription().trim().isEmpty()) {
            labelParts.add(transition.getDescription());
        }
        
        return String.join(" | ", labelParts);
    }
    
    /**
     * 获取节点形状
     * 
     * @param stateName 状态名称
     * @return 节点形状代码
     */
    private String getNodeShape(String stateName) {
        if (finalStateNames.contains(stateName)) {
            return "(({text}))";
        } else if (stateName.equals(initialStateName)) {
            return "[{text}]";
        } else {
            return "[{text}]";
        }
    }
    
    /**
     * 生成完整的Markdown文档
     * 
     * @param type 图表类型
     * @return 包含Mermaid图表的Markdown文档
     */
    public String generateMarkdown(DiagramType type) {
        StringBuilder sb = new StringBuilder();
        
        if (title != null && !title.trim().isEmpty()) {
            sb.append("# ").append(title).append("\n\n");
        }
        
        sb.append("```mermaid\n");
        sb.append(generate(type));
        sb.append("```\n");
        
        return sb.toString();
    }
    
    /**
     * 生成完整的Markdown文档（默认状态图）
     * 
     * @return 包含Mermaid状态图的Markdown文档
     */
    public String generateMarkdown() {
        return generateMarkdown(DiagramType.STATE_DIAGRAM);
    }
    
    /**
     * 创建Mermaid图表生成器
     * 
     * @param stateFactory 状态工厂
     * @param transitions 状态转换列表
     * @param <C> 上下文类型
     * @param <E> 事件类型
     * @param <R> 结果类型
     * @return Mermaid图表生成器实例
     */
    public static <C extends IStateContext<C, E, R>, E, R> MermaidDiagramGenerator<C, E, R> create(
            StateFactory<C, E, R> stateFactory, 
            List<StateTransition<C, E, R>> transitions) {
        return new MermaidDiagramGenerator<>(stateFactory, transitions);
    }
    
    /**
     * 从状态机构建器创建Mermaid图表生成器
     * 
     * @param builder 状态机构建器
     * @param <C> 上下文类型
     * @param <E> 事件类型
     * @param <R> 结果类型
     * @return Mermaid图表生成器实例
     */
    public static <C extends IStateContext<C, E, R>, E, R> MermaidDiagramGenerator<C, E, R> fromBuilder(
            StateMachineBuilder<C, E, R> builder) {
        // 注意：这里需要访问builder的私有字段，实际使用时可能需要添加getter方法
        // 或者在StateMachineBuilder中添加createDiagramGenerator方法
        throw new UnsupportedOperationException("需要在StateMachineBuilder中添加支持方法");
    }
}