/*
 * Copyright 2016 The Netty Project
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

public interface ChannelInboundInvoker {

    /**
     * A {@link Channel} was registered to its {@link EventLoop}.
     * A {@link Channel}已经注册到他的{@link EventLoop}.
     *
     * This will result in having the  {@link ChannelInboundHandler#channelRegistered(ChannelHandlerContext)} method
     * called of the next  {@link ChannelInboundHandler} contained in the  {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelInboundHandler}的
     * {@link ChannelInboundHandler#channelRegistered(ChannelHandlerContext)}方法
     */
    ChannelInboundInvoker fireChannelRegistered();

    /**
     * A {@link Channel} was unregistered from its {@link EventLoop}.
     * {@link Channel}已从其{@link EventLoop}中注销
     *
     * This will result in having the  {@link ChannelInboundHandler#channelUnregistered(ChannelHandlerContext)} method
     * called of the next  {@link ChannelInboundHandler} contained in the  {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelInboundHandler}的
     * {@link ChannelInboundHandler#channelUnregistered(ChannelHandlerContext)}方法
     */
    ChannelInboundInvoker fireChannelUnregistered();

    /**
     * A {@link Channel} is active now, which means it is connected.
     * {@link Channel}现在处于活动状态,这意味着它已连接。
     *
     * This will result in having the  {@link ChannelInboundHandler#channelActive(ChannelHandlerContext)} method
     * called of the next  {@link ChannelInboundHandler} contained in the  {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelInboundHandler}的
     * @link ChannelInboundHandler#channelActive(ChannelHandlerContext)}方法
     */
    ChannelInboundInvoker fireChannelActive();

    /**
     * A {@link Channel} is inactive now, which means it is closed.
     * {@link Channel}现在处于非活动状态，这意味着它已关闭。
     *
     * This will result in having the  {@link ChannelInboundHandler#channelInactive(ChannelHandlerContext)} method
     * called of the next  {@link ChannelInboundHandler} contained in the  {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}的{@link ChannelPipeline}包含的下一个{@link ChannelInboundHandler}的
     * {@link ChannelInboundHandler#channelInactive(ChannelHandlerContext)}方法
     */
    ChannelInboundInvoker fireChannelInactive();

    /**
     * A {@link Channel} received an {@link Throwable} in one of its inbound operations.
     * {@link Channel}在其入站操作之一中收到了{@link Throwable}.
     *
     * This will result in having the  {@link ChannelInboundHandler#exceptionCaught(ChannelHandlerContext, Throwable)}
     * method  called of the next  {@link ChannelInboundHandler} contained in the  {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelInboundHandler}的
     * {@link ChannelInboundHandler#exceptionCaught(ChannelHandlerContext, Throwable)}方法
     */
    ChannelInboundInvoker fireExceptionCaught(Throwable cause);

    /**
     * A {@link Channel} received an user defined event.
     * {@link Channel}收到了用户定义的事件.
     *
     * This will result in having the  {@link ChannelInboundHandler#userEventTriggered(ChannelHandlerContext, Object)}
     * method  called of the next  {@link ChannelInboundHandler} contained in the  {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelInboundHandler}的
     * {@link ChannelInboundHandler#userEventTriggered(ChannelHandlerContext, Object)}方法
     */
    ChannelInboundInvoker fireUserEventTriggered(Object event);

    /**
     * A {@link Channel} received a message.
     * {@link Channel} 收到一条消息
     *
     * This will result in having the {@link ChannelInboundHandler#channelRead(ChannelHandlerContext, Object)}
     * method  called of the next {@link ChannelInboundHandler} contained in the  {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelInboundHandler}的
     * {@link ChannelInboundHandler#channelRead(ChannelHandlerContext, Object)}方法
     */
    ChannelInboundInvoker fireChannelRead(Object msg);

    /**
     * {@link Channel}读取数据完毕
     *
     * Triggers an {@link ChannelInboundHandler#channelReadComplete(ChannelHandlerContext)}
     * event to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * 触发{@link ChannelPipeline}的下一个{@link ChannelInboundHandler}的
     * {@link ChannelInboundHandler#channelReadComplete(ChannelHandlerContext)}事件
     */
    ChannelInboundInvoker fireChannelReadComplete();

    /**
     * {@link Channel}可写状态变更
     *
     * Triggers an {@link ChannelInboundHandler#channelWritabilityChanged(ChannelHandlerContext)}
     * event to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * 触发{@link ChannelPipeline}里的下一个{@link ChannelInboundHandler}的
     * {@link ChannelInboundHandler#channelWritabilityChanged(ChannelHandlerContext)}事件
     */
    ChannelInboundInvoker fireChannelWritabilityChanged();
}
