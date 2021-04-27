/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;


/**
 * A list of {@link ChannelHandler}s which handles or intercepts inbound events and outbound operations of a
 * {@link Channel}.  {@link ChannelPipeline} implements an advanced form of the
 * <a href="https://www.oracle.com/technetwork/java/interceptingfilter-142169.html">Intercepting Filter</a> pattern
 * to give a user full control over how an event is handled and how the {@link ChannelHandler}s in a pipeline
 * interact with each other.
 * {@link Channel}里的{@link ChannelHandler}列表用于处理和拦截入站和出站事件。{@link ChannelPipeline}实现了
 * 拦截器模式高级形式。使得用户可以完全控制事件的处理方式以及管道中的{@link ChannelHandler}s如何交互。
 *
 * <h3>Creation of a pipeline</h3>
 * <h3>创建管道</h3>
 *
 * Each channel has its own pipeline and it is created automatically when a new channel is created.
 * 每个通道都有他自己的管道，当通道被创建的时候管道会被自动创建。
 *
 * <h3>How an event flows in a pipeline</h3>
 * <h3>一个事件如何在管道里流动</h3>
 *
 * The following diagram describes how I/O events are processed by {@link ChannelHandler}s in a {@link ChannelPipeline}
 * typically. An I/O event is handled by either a {@link ChannelInboundHandler} or a {@link ChannelOutboundHandler}
 * and be forwarded to its closest handler by calling the event propagation methods defined in
 * {@link ChannelHandlerContext}, such as {@link ChannelHandlerContext#fireChannelRead(Object)} and
 * {@link ChannelHandlerContext#write(Object)}.
 *
 * 下图描述了{@link ChannelPipeline}里的{@link ChannelHandler}s如何处理I/O事件。I/O事件由{@link ChannelInboundHandler}
 * 或{@link ChannelOutboundHandler}处理并通过调用{@link ChannelHandlerContext}定义的事件传播方法转发到最近
 * 最近的处理程序，比如{@link ChannelHandlerContext#fireChannelRead(Object)}和{@link ChannelHandlerContext#write(Object)}
 *
 *
 * <pre>
 *                                                 I/O Request
 *                                            via {@link Channel} or
 *                                        {@link ChannelHandlerContext}
 *                                                      |
 *  +---------------------------------------------------+---------------+
 *  |                           ChannelPipeline         |               |
 *  |                                                  \|/              |
 *  |    +---------------------+            +-----------+----------+    |
 *  |    | Inbound Handler  N  |            | Outbound Handler  1  |    |
 *  |    +----------+----------+            +-----------+----------+    |
 *  |              /|\                                  |               |
 *  |               |                                  \|/              |
 *  |    +----------+----------+            +-----------+----------+    |
 *  |    | Inbound Handler N-1 |            | Outbound Handler  2  |    |
 *  |    +----------+----------+            +-----------+----------+    |
 *  |              /|\                                  .               |
 *  |               .                                   .               |
 *  | ChannelHandlerContext.fireIN_EVT() ChannelHandlerContext.OUT_EVT()|
 *  |        [ method call]                       [method call]         |
 *  |               .                                   .               |
 *  |               .                                  \|/              |
 *  |    +----------+----------+            +-----------+----------+    |
 *  |    | Inbound Handler  2  |            | Outbound Handler M-1 |    |
 *  |    +----------+----------+            +-----------+----------+    |
 *  |              /|\                                  |               |
 *  |               |                                  \|/              |
 *  |    +----------+----------+            +-----------+----------+    |
 *  |    | Inbound Handler  1  |            | Outbound Handler  M  |    |
 *  |    +----------+----------+            +-----------+----------+    |
 *  |              /|\                                  |               |
 *  +---------------+-----------------------------------+---------------+
 *                  |                                  \|/
 *  +---------------+-----------------------------------+---------------+
 *  |               |                                   |               |
 *  |       [ Socket.read() ]                    [ Socket.write() ]     |
 *  |                                                                   |
 *  |  Netty Internal I/O Threads (Transport Implementation)            |
 *  +-------------------------------------------------------------------+
 * </pre>
 * An inbound event is handled by the inbound handlers in the bottom-up direction as shown on the left side of the
 * diagram.  An inbound handler usually handles the inbound data generated by the I/O thread on the bottom of the
 * diagram.  The inbound data is often read from a remote peer via the actual input operation such as
 * {@link SocketChannel#read(ByteBuffer)}.  If an inbound event goes beyond the top inbound handler, it is discarded
 * silently, or logged if it needs your attention.
 *
 * 一个入站事件由入站程序自下而上的方向进行处理，如图中左侧所示。入站处理程序通常处理图中底部的I/O线程生成的入站数据。
 * 通常通过实际的输入操作（例如{@link SocketChannel#read(ByteBuffer)}）从远程对等方读取入站数据。
 * 如果入站事件超出了顶部入站处理程序的范围，则将其静默丢弃，或者在需要引起注意时进行记录。
 * 备注：处理程序的范围指消息的数据类型
 * <p>
 * An outbound event is handled by the outbound handler in the top-down direction as shown on the right side of the
 * diagram.  An outbound handler usually generates or transforms the outbound traffic such as write requests.
 * If an outbound event goes beyond the bottom outbound handler, it is handled by an I/O thread associated with the
 * {@link Channel}. The I/O thread often performs the actual output operation such as
 * {@link SocketChannel#write(ByteBuffer)}.
 * 一个出站事件由出站程序自上而下的方向进行处理。如图中右侧所示。出站处理程序通常会生成或转换出站流量，例如写请求。
 * 如果一个出站事件超出底部处理程序的处理范围，则由与{@link Channel}关联的I/O线程处理。
 * I/O线程通常执行实际的出站操作，例如{@link SocketChannel#write(ByteBuffer)}
 *
 * <p>
 * For example, let us assume that we created the following pipeline:
 * 举个例子，让我们假设创建了以下管道。
 * <pre>
 * {@link ChannelPipeline} p = ...;
 * p.addLast("1", new InboundHandlerA());
 * p.addLast("2", new InboundHandlerB());
 * p.addLast("3", new OutboundHandlerA());
 * p.addLast("4", new OutboundHandlerB());
 * p.addLast("5", new InboundOutboundHandlerX());
 * </pre>
 *
 * 备注：拦截器责任链顺序为5-4-3-2-1，addLast方法
 *
 * In the example above, the class whose name starts with {@code Inbound} means it is an inbound handler.
 * The class whose name starts with {@code Outbound} means it is a outbound handler.
 * 在以上例子中，名称以{@code Inbound}开头的类表示它是一个入站处理程序，名称以{@code Outbound}
 * 开头的类表示它是一个出站处理程序
 *
 * <p>
 * In the given example configuration, the handler evaluation order is 1, 2, 3, 4, 5 when an event goes inbound.
 * When an event goes outbound, the order is 5, 4, 3, 2, 1.  On top of this principle, {@link ChannelPipeline} skips
 * the evaluation of certain handlers to shorten the stack depth:
 * 备注：读取事件是入站事件，自下而上，与拦截器责任链顺序相反
 *      写事件是出站事件，自上而上，与拦截器责任链顺序一致
 * 在给定的示例配置中，当一个事件入站时，处理程序的计算顺序为1，2，3，4，5。当一个事件入站时，顺序为5，4，3，2，1。
 * 在此原则之上，{@link ChannelPipeline}跳过某些处理程序的计算评估，以缩短堆栈深度
 *
 *
* <ul>
 * <li>3 and 4 don't implement {@link ChannelInboundHandler}, and therefore the actual evaluation order of an inbound
 *     event will be: 1, 2, and 5.</li>
 * <li>3和4没有实现{@link ChannelInboundHandler}，因此入站事件实际顺序为1，2，5</li>
 * <li>1 and 2 don't implement {@link ChannelOutboundHandler}, and therefore the actual evaluation order of a
 *     outbound event will be: 5, 4, and 3.</li>
 * <li>1和2没有实现{@link ChannelOutboundHandler}，因此出站实际顺序为5，4，3</li>
 * <li>If 5 implements both {@link ChannelInboundHandler} and {@link ChannelOutboundHandler}, the evaluation order of
 *     an inbound and a outbound event could be 125 and 543 respectively.</li>
 * <li>因为5同时实现了{@link ChannelInboundHandler}和{@link ChannelOutboundHandler}，
 * 入站和出站事件的评估顺序分别为1-2-5和5-4-3。
 * </li>
 * </ul>
 *
 * <h3>Forwarding an event to the next handler</h3>
 * <h3>转发事件到下一个处理程序</h3>
 *
 * As you might noticed in the diagram shows, a handler has to invoke the event propagation methods in
 * {@link ChannelHandlerContext} to forward an event to its next handler.  Those methods include:
 * 你可能会在图表中看到，一个处理程序通过调用{@link ChannelHandlerContext}的事件传播方法将事件转发到下一个处理
 * 程序，这些方法包括
 * <ul>
 * <li>Inbound event propagation methods:
 *     入站事件传播方法
 *     <ul>
 *     <li>{@link ChannelHandlerContext#fireChannelRegistered()}</li>
 *     <li>{@link ChannelHandlerContext#fireChannelActive()}</li>
 *     <li>{@link ChannelHandlerContext#fireChannelRead(Object)}</li>
 *     <li>{@link ChannelHandlerContext#fireChannelReadComplete()}</li>
 *     <li>{@link ChannelHandlerContext#fireExceptionCaught(Throwable)}</li>
 *     <li>{@link ChannelHandlerContext#fireUserEventTriggered(Object)}</li>
 *     <li>{@link ChannelHandlerContext#fireChannelWritabilityChanged()}</li>
 *     <li>{@link ChannelHandlerContext#fireChannelInactive()}</li>
 *     <li>{@link ChannelHandlerContext#fireChannelUnregistered()}</li>
 *     </ul>
 * </li>
 * <li>Outbound event propagation methods:
 *     出站事件传播方法
 *     <ul>
 *     <li>{@link ChannelHandlerContext#bind(SocketAddress, ChannelPromise)}</li>
 *     <li>{@link ChannelHandlerContext#connect(SocketAddress, SocketAddress, ChannelPromise)}</li>
 *     <li>{@link ChannelHandlerContext#write(Object, ChannelPromise)}</li>
 *     <li>{@link ChannelHandlerContext#flush()}</li>
 *     <li>{@link ChannelHandlerContext#read()}</li>
 *     <li>{@link ChannelHandlerContext#disconnect(ChannelPromise)}</li>
 *     <li>{@link ChannelHandlerContext#close(ChannelPromise)}</li>
 *     <li>{@link ChannelHandlerContext#deregister(ChannelPromise)}</li>
 *     </ul>
 * </li>
 * </ul>
 *
 * and the following example shows how the event propagation is usually done:
 * 以下示例展示了事件传播通常是如何实现
 *
 * <pre>
 * public class MyInboundHandler extends {@link ChannelInboundHandlerAdapter} {
 *     {@code @Override}
 *     public void channelActive({@link ChannelHandlerContext} ctx) {
 *         System.out.println("Connected!");
 *         ctx.fireChannelActive();
 *     }
 * }
 *
 * public class MyOutboundHandler extends {@link ChannelOutboundHandlerAdapter} {
 *     {@code @Override}
 *     public void close({@link ChannelHandlerContext} ctx, {@link ChannelPromise} promise) {
 *         System.out.println("Closing ..");
 *         ctx.close(promise);
 *     }
 * }
 * </pre>
 *
 * <h3>Building a pipeline</h3>
 * <h3>构建一个管道</h3>
 * <p>
 * A user is supposed to have one or more {@link ChannelHandler}s in a pipeline to receive I/O events (e.g. read) and
 * to request I/O operations (e.g. write and close).  For example, a typical server will have the following handlers
 * in each channel's pipeline, but your mileage may vary depending on the complexity and characteristics of the
 * protocol and business logic:
 *
 * 假定用户在管道中具有一个或多个{@link ChannelHandler}，以接收IO事件（例如，读取）并请求IO操作（例如，写入和关闭）。
 * 例如，典型的服务器在每个通道的管道中将具有以下处理程序，但是您的里程可能会有所不同，具体取决于协议和业务逻辑的复杂性和特征。
 *
 *
 * <ol>
 * <li>Protocol Decoder - translates binary data (e.g. {@link ByteBuf}) into a Java object.</li>
 * <li>协议解码程序-转换二进制数据（比如{@link ByteBuf}）成java对象</li>
 * <li>Protocol Encoder - translates a Java object into binary data.</li>
 * <li>协议编码程序-转换java对象成二进制数据</li>
 * <li>Business Logic Handler - performs the actual business logic (e.g. database access).</li>
 * <li>业务逻辑处理程序-执行实际的业务逻辑（比如数据库访问）</li>
 * </ol>
 *
 * and it could be represented as shown in the following example:
 * 他可以表示为以下示例所示：
 *
 * <pre>
 * static final {@link EventExecutorGroup} group = new {@link DefaultEventExecutorGroup}(16);
 * ...
 *
 * {@link ChannelPipeline} pipeline = ch.pipeline();
 *
 * pipeline.addLast("decoder", new MyProtocolDecoder());
 * pipeline.addLast("encoder", new MyProtocolEncoder());
 *
 * // Tell the pipeline to run MyBusinessLogicHandler's event handler methods
 * // in a different thread than an I/O thread so that the I/O thread is not blocked by
 * // a time-consuming task.
 * // 告诉管道在与IO线程不同的线程中运行MyBusinessLogicHandler的事件处理程序方法，以使I/O线程不会被耗时的任务阻塞
 * // If your business logic is fully asynchronous or finished very quickly, you don't
 * // need to specify a group.
 * // 如果业务逻辑完全异步，或者很快被完成，你不需要指定group
 *
 * pipeline.addLast(group, "handler", new MyBusinessLogicHandler());
 * </pre>
 *
 * Be aware that while using {@link DefaultEventLoopGroup} will offload the operation from the {@link EventLoop} it will
 * still process tasks in a serial fashion per {@link ChannelHandlerContext} and so guarantee ordering. Due the ordering
 * it may still become a bottle-neck. If ordering is not a requirement for your use-case you may want to consider using
 * {@link UnorderedThreadPoolEventExecutor} to maximize the parallelism of the task execution.
 *
 * <h3>Thread safety</h3>
 * <h3>线程安全</h3>
 * <p>
 * A {@link ChannelHandler} can be added or removed at any time because a {@link ChannelPipeline} is thread safe.
 * For example, you can insert an encryption handler when sensitive information is about to be exchanged, and remove it
 * after the exchange.
 * {@link ChannelHandler}可以在任何时间被删除或添加，因为{@link ChannelPipeline}是线程安全。
 * 例如：你可以在敏感信息即将交换的时候添加一个加密拦截程序，并在交换后删除。
 */
public interface ChannelPipeline
        extends ChannelInboundInvoker, ChannelOutboundInvoker, Iterable<Entry<String, ChannelHandler>> {

    /**
     * Inserts a {@link ChannelHandler} at the first position of this pipeline.
     * 在这个管道的第一个位置插入{@link ChannelHandler}
     *
     * @param name     the name of the handler to insert first
     *                 插入的第一位处理程序的名称
     * @param handler  the handler to insert first
     *                 插入的第一位处理程序
     *
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     *         如果管道中已经有了一个同名的处理程序
     * @throws NullPointerException
     *         if the specified handler is {@code null}
     *         如果指定的处理程序为{@code null}
     */
    ChannelPipeline addFirst(String name, ChannelHandler handler);

    /**
     * Inserts a {@link ChannelHandler} at the first position of this pipeline.
     * 在这个管道的第一个位置插入{@link ChannelHandler}
     *
     * @param group    the {@link EventExecutorGroup} which will be used to execute the {@link ChannelHandler}
     *                 methods
     *                 {@link EventExecutorGroup}用于执行{@link ChannelHandler}方法
     * @param name     the name of the handler to insert first
     *                 插入的第一位的程序名称
     * @param handler  the handler to insert first
     *                 插入到第一位的处理程序
     *
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     *         如果管道中已经有同名的条目
     * @throws NullPointerException
     *         if the specified handler is {@code null}
     *         如果指定的处理程序为空
     */
    ChannelPipeline addFirst(EventExecutorGroup group, String name, ChannelHandler handler);

    /**
     * Appends a {@link ChannelHandler} at the last position of this pipeline.
     * 在管道的最后一个位置添加{@link ChannelHandler}
     *
     * @param name     the name of the handler to append
     *                 要附加的处理程序名称
     * @param handler  the handler to append
     *                 附加的程序
     *
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     *         如果管道中已经存在一个同名的条目
     * @throws NullPointerException
     *         if the specified handler is {@code null}
     *         如果指定的处理程序为空
     */
    ChannelPipeline addLast(String name, ChannelHandler handler);

    /**
     * Appends a {@link ChannelHandler} at the last position of this pipeline.
     * 在管道的最后一个附加{@link ChannelHandler}
     *
     * @param group    the {@link EventExecutorGroup} which will be used to execute the {@link ChannelHandler}
     *                 methods
     *                 {@link EventExecutorGroup} 将会用于执行{@link ChannelHandler}方法
     * @param name     the name of the handler to append
     *                 附加的处理程序名称
     * @param handler  the handler to append
     *                 附加的处理程序
     *
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     *         如果管道中已经有同名的条目
     * @throws NullPointerException
     *         if the specified handler is {@code null}
     *         如果指定的处理程序为空
     */
    ChannelPipeline addLast(EventExecutorGroup group, String name, ChannelHandler handler);

    /**
     * Inserts a {@link ChannelHandler} before an existing handler of this
     * pipeline.
     * 在管道中已经存在的处理程序前插入{@link ChannelHandler}
     *
     * @param baseName  the name of the existing handler
     *                  已经存在的处理程序名称
     * @param name      the name of the handler to insert before
     *                  在baseName前面插入的处理程序名称
     * @param handler   the handler to insert before
     *                  在baseName前面插入的处理程序
     *
     * @throws NoSuchElementException
     *         if there's no such entry with the specified {@code baseName}
     *         如果不存在指定{@code baseName}的条目
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     *         如果管道中已经存在同名的处理程序
     * @throws NullPointerException
     *         if the specified baseName or handler is {@code null}
     *         如果指定的程序程序或插入的处理程序为{@code null}
     */
    ChannelPipeline addBefore(String baseName, String name, ChannelHandler handler);

    /**
     * Inserts a {@link ChannelHandler} before an existing handler of this
     * pipeline.
     * 在管道中已经存在的处理程序前插入{@link ChannelHandler}
     *
     * @param group     the {@link EventExecutorGroup} which will be used to execute the {@link ChannelHandler}
     *                  methods
     *                  {@link EventExecutorGroup}用于执行{@link ChannelHandler}方法
     * @param baseName  the name of the existing handler
     *                  已经存在的处理程序名称
     * @param name      the name of the handler to insert before
     *                  在baseName前插入的处理程序名称
     * @param handler   the handler to insert before
     *                  在baseName前插入的处理程序
     *
     * @throws NoSuchElementException
     *         if there's no such entry with the specified {@code baseName}
     *         如果管道中不存在指定{@code baseName}的条目
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     *         如果管道中已经存在同名的条目
     * @throws NullPointerException
     *         if the specified baseName or handler is {@code null}
     *         如果指定的程序名称或插入处理程序为{@code null}
     *
     */
    ChannelPipeline addBefore(EventExecutorGroup group, String baseName, String name, ChannelHandler handler);

    /**
     * Inserts a {@link ChannelHandler} after an existing handler of this
     * pipeline.
     * 在管道中已经存在的处理程序后插入{@link ChannelHandler}
     *
     * @param baseName  the name of the existing handler
     *                  已经存在处理程序名称
     * @param name      the name of the handler to insert after
     *                  在baseName之后插入的处理程序名称
     * @param handler   the handler to insert after
     *                  在baseName之后插入的处理程序
     *
     * @throws NoSuchElementException
     *         if there's no such entry with the specified {@code baseName}
     *         如果指定{@code baseName}的处理程序在管道中不存在
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     *         如果管道中已经存在同名的条目
     * @throws NullPointerException
     *         if the specified baseName or handler is {@code null}
     *         如果指定的程序名称或处理程序为{@code null}
     */
    ChannelPipeline addAfter(String baseName, String name, ChannelHandler handler);

    /**
     * Inserts a {@link ChannelHandler} after an existing handler of this
     * pipeline.
     * 在管道中已经存在的处理程序后插入{@link ChannelHandler}
     *
     * @param group     the {@link EventExecutorGroup} which will be used to execute the {@link ChannelHandler}
     *                  methods
     *                  {@link EventExecutorGroup}将被用于执行{@link ChannelHandler}方法
     * @param baseName  the name of the existing handler
     *                  已经存在处理程序名称
     * @param name      the name of the handler to insert after
     *                  在baseName后插入的处理程序名称
     * @param handler   the handler to insert after
     *                  在baseName后插入的处理程序
     *
     * @throws NoSuchElementException
     *         if there's no such entry with the specified {@code baseName}
     *         如果管道中不存在指定名称的条目
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     *         如果管道中已经存在同名的条目
     * @throws NullPointerException
     *         if the specified baseName or handler is {@code null}
     *         如果指定的已存在程序名称或插入程序为{@code null}
     */
    ChannelPipeline addAfter(EventExecutorGroup group, String baseName, String name, ChannelHandler handler);

    /**
     * Inserts {@link ChannelHandler}s at the first position of this pipeline.
     * 在管道的第一个位置批量插入{@link ChannelHandler}s
     *
     * @param handlers  the handlers to insert first
     *                  在第一个位置插入的批量处理程序
     *
     */
    ChannelPipeline addFirst(ChannelHandler... handlers);

    /**
     * Inserts {@link ChannelHandler}s at the first position of this pipeline.
     * 在管道的第一个位置批量插入处理程序
     *
     * @param group     the {@link EventExecutorGroup} which will be used to execute the {@link ChannelHandler}s
     *                  methods.
     *                  {@link EventExecutorGroup}将被用于执行{@link ChannelHandler}s的方法
     *
     * @param handlers  the handlers to insert first
     *                  在第一个位置批量插入的处理程序
     *
     */
    ChannelPipeline addFirst(EventExecutorGroup group, ChannelHandler... handlers);

    /**
     * Inserts {@link ChannelHandler}s at the last position of this pipeline.
     * 在管道中的最后一个位置批量插入处理程序
     *
     * @param handlers  the handlers to insert last
     *                  在最后一个位置批量插入的处理程序
     *
     */
    ChannelPipeline addLast(ChannelHandler... handlers);

    /**
     * Inserts {@link ChannelHandler}s at the last position of this pipeline.
     * 在管道的最后一个位置批量插入处理程序
     *
     * @param group     the {@link EventExecutorGroup} which will be used to execute the {@link ChannelHandler}s
     *                  methods.
     *                  {@link EventExecutorGroup}将被用于执行{@link ChannelHandler}s方法
     * @param handlers  the handlers to insert last
     *                  在最后一个位置批量插入的处理程序
     *
     */
    ChannelPipeline addLast(EventExecutorGroup group, ChannelHandler... handlers);

    /**
     * Removes the specified {@link ChannelHandler} from this pipeline.
     * 从管道中删除指定的{@link ChannelHandler}
     *
     * @param  handler          the {@link ChannelHandler} to remove
     *                          需要删除的{@link ChannelHandler}
     * @throws NoSuchElementException
     *         if there's no such handler in this pipeline
     *         如果管道中没有指定的处理程序
     * @throws NullPointerException
     *         if the specified handler is {@code null}
     *         如果指定的处理程序为空
     */
    ChannelPipeline remove(ChannelHandler handler);

    /**
     * Removes the {@link ChannelHandler} with the specified name from this pipeline.
     * 在管道中删除指定名称的{@link ChannelHandler}
     *
     * @param  name             the name under which the {@link ChannelHandler} was stored.
     *                          已经被存储的{@link ChannelHandler}名称
     *
     * @return the removed handler
     *         被删除的处理程序
     *
     * @throws NoSuchElementException
     *         if there's no such handler with the specified name in this pipeline
     *         如果管道里没有指定名称的处理程序
     * @throws NullPointerException
     *         if the specified name is {@code null}
     *         如果指定的名称为{@code null}
     */
    ChannelHandler remove(String name);

    /**
     * Removes the {@link ChannelHandler} of the specified type from this pipeline.
     * 在这个管道里删除指定类型的{@link ChannelHandler}
     *
     * @param <T>           the type of the handler
     *                      处理程序的类型
     * @param handlerType   the type of the handler
     *                      处理程序的类型
     *
     * @return the removed handler  被删除处理程序
     *
     * @throws NoSuchElementException
     *         if there's no such handler of the specified type in this pipeline
     *         如果管道里没有指定类型的处理程序
     * @throws NullPointerException
     *         if the specified handler type is {@code null}
     *         如果指定的处理类型为{@code null}
     */
    <T extends ChannelHandler> T remove(Class<T> handlerType);

    /**
     * Removes the first {@link ChannelHandler} in this pipeline.
     * 移除管道里的第一个{@link ChannelHandler}
     *
     * @return the removed handler
     *         被删除的处理程序
     *
     * @throws NoSuchElementException
     *         if this pipeline is empty
     *         如果管道为空
     */
    ChannelHandler removeFirst();

    /**
     * Removes the last {@link ChannelHandler} in this pipeline.
     * 删除管道里的最后一个位置的处理程序
     *
     * @return the removed handler
     *         被删除的处理程序
     *
     * @throws NoSuchElementException
     *         if this pipeline is empty
     *         如果管道为空
     */
    ChannelHandler removeLast();

    /**
     * Replaces the specified {@link ChannelHandler} with a new handler in this pipeline.
     * 在管道中用一个新的处理程序替换指定的{@link ChannelHandler}
     *
     * @param  oldHandler    the {@link ChannelHandler} to be replaced
     *                       将被替换的{@link ChannelHandler}
     * @param  newName       the name under which the replacement should be added
     *                       添加的替代处理程序的名称
     * @param  newHandler    the {@link ChannelHandler} which is used as replacement
     *                       添加的替代处理程序
     *
     * @return itself
     *         本身

     * @throws NoSuchElementException
     *         if the specified old handler does not exist in this pipeline
     *         如果管道中不存在指定的被替换的旧处理程序
     * @throws IllegalArgumentException
     *         if a handler with the specified new name already exists in this
     *         pipeline, except for the handler to be replaced
     *         如果在该管道中已经存在具有指定新名称的处理程序，但要替换的处理程序除外
     * @throws NullPointerException
     *         if the specified old handler or new handler is
     *         {@code null}
     *         如果指定的旧处理程序或新处理程序为{@code null}
     */
    ChannelPipeline replace(ChannelHandler oldHandler, String newName, ChannelHandler newHandler);

    /**
     * Replaces the {@link ChannelHandler} of the specified name with a new handler in this pipeline.
     * 使用此管道中的新处理程序替换指定名称的{@link ChannelHandler}
     *
     * @param  oldName       the name of the {@link ChannelHandler} to be replaced
     *                       被替换的{@link ChannelHandler}的名称
     * @param  newName       the name under which the replacement should be added
     *                       将要添加的替代应用程序名称
     * @param  newHandler    the {@link ChannelHandler} which is used as replacement
     *                       将要添加的替代应用程序
     *
     * @return the removed handler
     *         被移除的处理程序
     *
     * @throws NoSuchElementException
     *         if the handler with the specified old name does not exist in this pipeline
     *         如果管道不存在指定名称的旧处理程序
     * @throws IllegalArgumentException
     *         if a handler with the specified new name already exists in this
     *         pipeline, except for the handler to be replaced
     *         如果在该管道中已经存在具有指定新名称的处理程序，但要替换的处理程序除外
     * @throws NullPointerException
     *         if the specified old handler or new handler is
     *         {@code null}
     *         如果指定的新处理程序或旧处理程序为{@code null}
     */
    ChannelHandler replace(String oldName, String newName, ChannelHandler newHandler);

    /**
     * Replaces the {@link ChannelHandler} of the specified type with a new handler in this pipeline.
     * 在此管道中，将指定类型的{@link ChannelHandler}替换为新的处理程序。
     *
     * @param  oldHandlerType   the type of the handler to be removed
     *                          需要移除的应用程序类型
     * @param  newName          the name under which the replacement should be added
     *                          替代处理程序的名称
     * @param  newHandler       the {@link ChannelHandler} which is used as replacement
     *                          替代处理程序
     *
     * @return the removed handler
     *         移除的处理程序
     *
     * @throws NoSuchElementException
     *         if the handler of the specified old handler type does not exist
     *         in this pipeline
     *         在管道里不存在指定类型的应用程序
     * @throws IllegalArgumentException
     *         if a handler with the specified new name already exists in this
     *         pipeline, except for the handler to be replaced
     *         如果在该管道中已经存在具有指定新名称的处理程序，但要替换的处理程序除外
     * @throws NullPointerException
     *         if the specified old handler or new handler is
     *         {@code null}
     *         如果指定的旧处理程序或新处理程序为{@code null}
     */
    <T extends ChannelHandler> T replace(Class<T> oldHandlerType, String newName,
                                         ChannelHandler newHandler);

    /**
     * Returns the first {@link ChannelHandler} in this pipeline.
     * 返回管道里的第一个处理程序
     *
     * @return the first handler.  {@code null} if this pipeline is empty.
     *         第一个处理程序，如果这个管道为空返回{@code null}
     */
    ChannelHandler first();

    /**
     * Returns the context of the first {@link ChannelHandler} in this pipeline.
     * 返回此管道中第一个{@link ChannelHandler}的上下文。
     *
     * @return the context of the first handler.  {@code null} if this pipeline is empty.
     *         第一个处理程序的上下问，如果管道为空，返回{@code null}
     */
    ChannelHandlerContext firstContext();

    /**
     * Returns the last {@link ChannelHandler} in this pipeline.
     * 返回管道的最后一个{@link ChannelHandler}
     *
     * @return the last handler.  {@code null} if this pipeline is empty.
     *         最后位置的处理程序，如果管道为空返回{@code null}
     */
    ChannelHandler last();

    /**
     * Returns the context of the last {@link ChannelHandler} in this pipeline.
     * 返回此管道中最后一个{@link ChannelHandler}的上下文
     *
     * @return the context of the last handler.  {@code null} if this pipeline is empty.
     *         返回最后一个处理程序的上下文，如果管道为空返回{@code null}
     */
    ChannelHandlerContext lastContext();

    /**
     * Returns the {@link ChannelHandler} with the specified name in this
     * pipeline.
     * 返回管道里指定名称的{@link ChannelHandler}
     *
     * @return the handler with the specified name.
     *         指定名称的处理程序
     *         {@code null} if there's no such handler in this pipeline.
     *         如果这个管道里没有指定的处理程序就返回{@code null}
     */
    ChannelHandler get(String name);

    /**
     * Returns the {@link ChannelHandler} of the specified type in this
     * pipeline.
     * 返回管道里指定类型的{@link ChannelHandler}
     *
     * @return the handler of the specified handler type.
     *         指定类型的处理程序
     *         {@code null} if there's no such handler in this pipeline.
     *         如果管道里指定的应用程序为空返回{@code null}
     */
    <T extends ChannelHandler> T get(Class<T> handlerType);

    /**
     * Returns the context object of the specified {@link ChannelHandler} in
     * this pipeline.
     * 返回管道里指定{@link ChannelHandler}的上下文对象
     *
     * @return the context object of the specified handler.
     *         指定处理程序的上下文对象
     *         {@code null} if there's no such handler in this pipeline.
     *         如果管道里没有这个处理程序返回{@code null}
     */
    ChannelHandlerContext context(ChannelHandler handler);

    /**
     * Returns the context object of the {@link ChannelHandler} with the
     * specified name in this pipeline.
     * 如果管道里指定名称的{@link ChannelHandler}的上下文对象
     *
     * @return the context object of the handler with the specified name.
     *         指定名称的处理程序的上下文对象
     *         {@code null} if there's no such handler in this pipeline.
     *         如果管道里没有这个处理程序返回{@code null}
     */
    ChannelHandlerContext context(String name);

    /**
     * Returns the context object of the {@link ChannelHandler} of the
     * specified type in this pipeline.
     * 返回管道里指定类型的{@link ChannelHandler}的上下文对象
     *
     * @return the context object of the handler of the specified type.
     *         指定类型的处理程序的上下文对象
     *         {@code null} if there's no such handler in this pipeline.
     *         如果管道里没有这个处理程序返回{@code null}
     */
    ChannelHandlerContext context(Class<? extends ChannelHandler> handlerType);

    /**
     * Returns the {@link Channel} that this pipeline is attached to.
     * 返回此管道附加到的{@link Channel}。
     *
     * @return the channel. {@code null} if this pipeline is not attached yet.
     * 返回频道。 {@code null}（如果尚未附加到此管道）
     */
    Channel channel();

    /**
     * Returns the {@link List} of the handler names.
     * 返回处理程序名称的{@link List}。
     */
    List<String> names();

    /**
     * Converts this pipeline into an ordered {@link Map} whose keys are
     * handler names and whose values are handlers.
     * 将此管道转换为有序的{@link Map}，其键是处理程序名称，其值是处理程序。
     *
     */
    Map<String, ChannelHandler> toMap();

    @Override
    ChannelPipeline fireChannelRegistered();

    @Override
    ChannelPipeline fireChannelUnregistered();

    @Override
    ChannelPipeline fireChannelActive();

    @Override
    ChannelPipeline fireChannelInactive();

    @Override
    ChannelPipeline fireExceptionCaught(Throwable cause);

    @Override
    ChannelPipeline fireUserEventTriggered(Object event);

    @Override
    ChannelPipeline fireChannelRead(Object msg);

    @Override
    ChannelPipeline fireChannelReadComplete();

    @Override
    ChannelPipeline fireChannelWritabilityChanged();

    @Override
    ChannelPipeline flush();
}
