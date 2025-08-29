package com.jasonlat.design.framework.link.prototype.chain;

/**
 * 链表操作接口
 * 定义了链表的基本操作方法，包括添加、删除、获取元素等功能
 * 
 * @param <E> 链表中元素的类型
 * @author Jasonlat
 * @description 链接口
 * @create 2025-08-29
 */
public interface ILink<E> {

    /**
     * 向链表中添加元素（默认添加到末尾）
     * 
     * @param e 要添加的元素
     * @return 添加成功返回true，失败返回false
     */
    boolean add(E e);

    /**
     * 向链表头部添加元素
     * 
     * @param e 要添加的元素
     * @return 添加成功返回true，失败返回false
     */
    boolean addFirst(E e);

    /**
     * 向链表尾部添加元素
     * 
     * @param e 要添加的元素
     * @return 添加成功返回true，失败返回false
     */
    boolean addLast(E e);

    /**
     * 从链表中移除指定元素
     * 
     * @param o 要移除的元素
     * @return 移除成功返回true，如果元素不存在返回false
     */
    boolean remove(Object o);

    /**
     * 获取指定索引位置的元素
     * 
     * @param index 元素索引，从0开始
     * @return 指定位置的元素
     * @throws IndexOutOfBoundsException 如果索引超出范围
     */
    E get(int index);

    /**
     * 打印链表的所有元素
     * 用于调试和查看链表当前状态
     */
    void printLinkList();

}
