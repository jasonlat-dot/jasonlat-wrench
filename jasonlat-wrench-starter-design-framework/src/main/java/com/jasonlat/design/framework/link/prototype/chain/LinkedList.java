package com.jasonlat.design.framework.link.prototype.chain;

/**
 * 双向链表实现类
 * 实现了ILink接口，提供了完整的双向链表功能
 * 支持在头部、尾部添加元素，以及删除、查找等操作
 * 
 * @param <E> 链表中元素的类型
 * @author Jasonlat
 * @description 功能链路
 * @create 2025-08-29
 */
public class LinkedList<E> implements ILink<E> {

    /**
     * 责任链名称，用于标识和调试
     */
    private final String name;

    /**
     * 链表中元素的数量
     */
    transient int size = 0;

    /**
     * 链表的第一个节点
     */
    transient Node<E> first;

    /**
     * 链表的最后一个节点
     */
    transient Node<E> last;

    /**
     * 构造函数，创建具有指定名称的链表
     * 
     * @param name 链表名称
     */
    public LinkedList(String name) {
        this.name = name;
    }

    /**
     * 在链表头部插入元素
     * 
     * @param e 要插入的元素
     */
    void linkFirst(E e) {
        final Node<E> f = first;
        final Node<E> newNode = new Node<>(null, e, f);
        first = newNode;
        if (f == null)
            last = newNode;
        else
            f.prev = newNode;
        size++;
    }

    /**
     * 在链表尾部插入元素
     * 
     * @param e 要插入的元素
     */
    void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }
        size++;
    }

    @Override
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

    @Override
    public boolean addFirst(E e) {
        linkFirst(e);
        return true;
    }

    @Override
    public boolean addLast(E e) {
        linkLast(e);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 从链表中移除指定节点
     * 
     * @param x 要移除的节点
     * @return 被移除节点的元素值
     */
    E unlink(Node<E> x) {
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.item = null;
        size--;
        return element;
    }

    @Override
    public E get(int index) {
        return node(index).item;
    }

    /**
     * 根据索引获取对应的节点
     * 使用二分查找优化，从距离目标索引更近的一端开始遍历
     * 
     * @param index 节点索引
     * @return 对应索引的节点
     */
    Node<E> node(int index) {
        if (index < (size >> 1)) {
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }

    public void printLinkList() {
        if (this.size == 0) {
            System.out.println("链表为空");
        } else {
            Node<E> temp = first;
            System.out.print("目前的列表，头节点：" + first.item + " 尾节点：" + last.item + " 整体：");
            while (temp != null) {
                System.out.print(temp.item + "，");
                temp = temp.next;
            }
            System.out.println();
        }
    }

    /**
     * 双向链表节点类
     * 包含元素值以及指向前一个和后一个节点的引用
     * 
     * @param <E> 节点中存储的元素类型
     */
    protected static class Node<E> {

        /**
         * 节点存储的元素
         */
        E item;
        
        /**
         * 指向下一个节点的引用
         */
        Node<E> next;
        
        /**
         * 指向前一个节点的引用
         */
        Node<E> prev;

        /**
         * 构造函数，创建一个新的节点
         * 
         * @param prev 前一个节点
         * @param element 节点存储的元素
         * @param next 下一个节点
         */
        public Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }

    }

    /**
     * 获取链表名称
     * 
     * @return 链表名称
     */
    @SuppressWarnings("unchecked")
    public String getName() {
        return name;
    }

}
