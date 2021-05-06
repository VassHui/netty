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
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;
import io.netty.util.concurrent.EventExecutor;

import java.nio.channels.Channels;

/**
 * Enables a {@link ChannelHandler} to interact with its {@link ChannelPipeline}
 * and other handlers. Among other things a handler can notify the next {@link ChannelHandler} in the
 * {@link ChannelPipeline} as well as modify the {@link ChannelPipeline} it belongs to dynamically.
 *
 * 使{@link ChannelHandler}和他所属的{@link ChannelPipeline}能够和其他处理程序交互。
 *
 *
 * <h3>Notify</h3>
 * <h3>通知</h3>
 *
 * You can notify the closest handler in the same {@link ChannelPipeline} by calling one of the various methods
 * provided here.
 * 你可以通过调用这里提供的各种方法来通知同个{@link ChannelPipeline}下的最近的处理程序。
 *
 * Please refer to {@link ChannelPipeline} to understand how an event flows.
 * 请参阅{@link ChannelPipeline}来理解一个事件如何流动
 *
 *
 * <h3>Modifying a pipeline</h3>
 * <h3>修改管道</h3>
 *
 * You can get the {@link ChannelPipeline} your handler belongs to by calling
 * {@link #pipeline()}.  A non-trivial application could insert, remove, or
 * replace handlers in the pipeline dynamically at runtime.
 *
 * 你可以通过调用{@link #pipeline()}方法获取你的处理程序归属的{@link ChannelPipeline}。
 * 一个不平凡的应用可以在管道中任何时间动态的插入，移除或者替换处理程序。
 *
 *
 * <h3>Retrieving for later use</h3>
 * <h3>检索以备后用</h3>
 *
 *
 * You can keep the {@link ChannelHandlerContext} for later use, such as
 * triggering an event outside the handler methods, even from a different thread.
 * 你可以保留{@link ChannelHandlerContext}供以后使用。例如在处理程序外部触发事件。
 * 即使来自其他线程。
 *
 * <pre>
 * public class MyHandler extends {@link ChannelDuplexHandler} {
 *
 *     <b>private {@link ChannelHandlerContext} ctx;</b>
 *
 *     public void beforeAdd({@link ChannelHandlerContext} ctx) {
 *         <b>this.ctx = ctx;</b>
 *     }
 *
 *     public void login(String username, password) {
 *         ctx.write(new LoginMessage(username, password));
 *     }
 *     ...
 * }
 * </pre>
 *
 * <h3>Storing stateful information</h3>
 * <h3>存储状态信息</h3>
 *
 * {@link #attr(AttributeKey)} allow you to
 * store and access stateful information that is related with a {@link ChannelHandler} / {@link Channel} and its
 * context. Please refer to {@link ChannelHandler} to learn various recommended
 * ways to manage stateful information.
 * {@link #attr（AttributeKey）}允许您存储和访问与{@link ChannelHandler} {@link Channel}及其上下文相关的有状态信息。
 * 请参阅{@link ChannelHandler}，以了解有关管理状态信息的各种推荐方法。
 *
 * <h3>A handler can have more than one {@link ChannelHandlerContext}</h3>
 * <h3>一个处理程序可以有多个{@link ChannelHandlerContext}</h3>
 *
 * Please note that a {@link ChannelHandler} instance can be added to more than
 * one {@link ChannelPipeline}.  It means a single {@link ChannelHandler}
 * instance can have more than one {@link ChannelHandlerContext} and therefore
 * the single instance can be invoked with different
 * {@link ChannelHandlerContext}s if it is added to one or more {@link ChannelPipeline}s more than once.
 * Also note that a {@link ChannelHandler} that is supposed to be added to multiple {@link ChannelPipeline}s should
 * be marked as {@link io.netty.channel.ChannelHandler.Sharable}.
 * 请注意一个{@link ChannelHandler}实例可以被添加到多个{@link ChannelPipeline}。
 * 这意味着单个{@link ChannelHandler}实例可以具有多个{@link ChannelHandlerContext}，
 * 因此，如果将单个实例添加到一个或多个{@link ChannelPipeline}中，
 * 则可以使用不同的{@link ChannelHandlerContext}调用该实例。不止一次。
 * 还要注意，应该添加到多个{@link ChannelPipeline}中的{@link ChannelHandler}
 * 应该标记为{@link io.netty.channel.ChannelHandler.Sharable}。
 *
 * <h3>Additional resources worth reading</h3>
 * <h3>值得阅读的其他资源</h3>
 * <p>
 * Please refer to the {@link ChannelHandler}, and
 * {@link ChannelPipeline} to find out more about inbound and outbound operations,
 * what fundamental differences they have, how they flow in a  pipeline,  and how to handle
 * the operation in your application.
 * 请参阅{@link ChannelHandler}和{@link ChannelPipeline}，以了解有关入站和出站操作，
 * 它们之间有哪些根本区别，它们如何在管道中流动以及如何在应用程序中处理该操作的更多信息。
 */
public interface ChannelHandlerContext extends AttributeMap, ChannelInboundInvoker, ChannelOutboundInvoker {

    /**
     * Return the {@link Channel} which is bound to the {@link ChannelHandlerContext}.
     * 返回绑定到{@link ChannelHandlerContext}的{@link Channel}
     */
    Channel channel();

    /**
     * Returns the {@link EventExecutor} which is used to execute an arbitrary task.
     * 返回用于执行任意任务的{@link EventExecutor}。
     */
    EventExecutor executor();

    /**
     * The unique name of the {@link ChannelHandlerContext}.The name was used when then {@link ChannelHandler}
     * was added to the {@link ChannelPipeline}. This name can also be used to access the registered
     * {@link ChannelHandler} from the {@link ChannelPipeline}.
     * {@link ChannelHandlerContext}的唯一名称。此名称是将{@link ChannelHandler}添加到{@link ChannelPipeline}
     * 时使用。此名称还可用于从{@link ChannelPipeline}访问已注册的{@link ChannelHandler}。
     *
     */
    String name();

    /**
     * The {@link ChannelHandler} that is bound this {@link ChannelHandlerContext}.
     * 这个{@link ChannelHandlerContext}已经绑定的{@link ChannelHandler}。
     */
    ChannelHandler handler();

    /**
     * Return {@code true} if the {@link ChannelHandler} which belongs to this context was removed
     * from the {@link ChannelPipeline}. Note that this method is only meant to be called from with in the
     * {@link EventLoop}.
     * 如果属于该上下文的{@link ChannelHandler}已从{@link ChannelPipeline}中删除，则返回{@code true}。
     * 请注意，此方法仅应在{@link EventLoop}中使用进行调用。
     *
     */
    boolean isRemoved();

    @Override
    ChannelHandlerContext fireChannelRegistered();

    @Override
    ChannelHandlerContext fireChannelUnregistered();

    @Override
    ChannelHandlerContext fireChannelActive();

    @Override
    ChannelHandlerContext fireChannelInactive();

    @Override
    ChannelHandlerContext fireExceptionCaught(Throwable cause);

    @Override
    ChannelHandlerContext fireUserEventTriggered(Object evt);

    @Override
    ChannelHandlerContext fireChannelRead(Object msg);

    @Override
    ChannelHandlerContext fireChannelReadComplete();

    @Override
    ChannelHandlerContext fireChannelWritabilityChanged();

    @Override
    ChannelHandlerContext read();

    @Override
    ChannelHandlerContext flush();

    /**
     * Return the assigned {@link ChannelPipeline}
     * 返回分配的{@link ChannelPipeline}
     */
    ChannelPipeline pipeline();

    /**
     * Return the assigned {@link ByteBufAllocator} which will be used to allocate {@link ByteBuf}s.
     * 返回分配的{@link ByteBufAllocator}，它将用于分配{@link ByteBuf}。
     */
    ByteBufAllocator alloc();

    /**
     * @deprecated Use {@link Channel#attr(AttributeKey)}
     */
    @Deprecated
    @Override
    <T> Attribute<T> attr(AttributeKey<T> key);

    /**
     * @deprecated Use {@link Channel#hasAttr(AttributeKey)}
     */
    @Deprecated
    @Override
    <T> boolean hasAttr(AttributeKey<T> key);
}
