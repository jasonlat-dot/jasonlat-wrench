package com.jasonlat.design.framework.link.prototype.chain;

import com.jasonlat.design.framework.link.prototype.DynamicContext;
import com.jasonlat.design.framework.link.prototype.handler.ILogicHandler;

/**
 * 业务链路实现类
 * 继承自LinkedList并实现ILogicHandler接口，用于管理和执行业务逻辑处理器链
 * 支持按顺序执行多个逻辑处理器，并根据动态上下文控制执行流程
 * 
 * @param <T> 请求参数类型
 * @param <D> 动态上下文类型，必须继承自DynamicContext
 * @param <R> 返回结果类型
 * @author Jasonlat
 * @description 业务链路
 * @create 2025-08-29
 */
public class BusinessLinkedList<T, D extends DynamicContext, R> extends LinkedList<ILogicHandler<T, D, R>> implements ILogicHandler<T, D, R>{

    /**
     * 构造函数，创建具有指定名称的业务链路
     * 
     * @param name 链路名称，用于标识和调试
     */
    public BusinessLinkedList(String name) {
        super(name);
    }

    /**
     * 执行业务链路中的所有逻辑处理器
     * 按照链路中处理器的顺序依次执行，如果某个处理器设置了停止标识，则中断执行并返回结果
     * 
     * @param requestParameter 请求参数，传递给每个逻辑处理器
     * @param dynamicContext 动态上下文，用于控制执行流程和传递数据
     * @return 处理结果，如果链路正常执行完毕返回null，如果中途停止则返回停止时的结果
     * @throws Exception 如果任何逻辑处理器执行过程中抛出异常
     */
    @Override
    public R apply(T requestParameter, D dynamicContext) throws Exception {
        Node<ILogicHandler<T, D, R>> current = this.first;
        if (null == current) return null;
        do {
            ILogicHandler<T, D, R> item = current.item;
            R apply = item.apply(requestParameter, dynamicContext);
            // 如果动态上下文设置为不继续执行，则返回当前结果
            if (!dynamicContext.isProceed()) return apply;

            current = current.next;
        } while (null != current);

        return null;
    }

}
