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
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.AttributeMap;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


/**
 * A nexus to a network socket or a component which is capable of I/O
 * operations such as read, write, connect, and bind.
 * 与网络套接字或能够进行I/O操作(例如读取，写入，连接和绑定)的组件的纽带
 * <p>
 * A channel provides a user:
 * 为用户提供了一个频道
 * <ul>
 * <li>the current state of the channel (e.g. is it open? is it connected?),</li>
 * <li>通道当前的状态比如(是否打开,是否连接上)</li>
 * <li>the {@linkplain ChannelConfig configuration parameters} of the channel (e.g. receive buffer size),</li>
 * <li>通道的{@linkplain ChannelConfig 配置参数}，比如接受缓冲区大小</li>
 * <li>the I/O operations that the channel supports (e.g. read, write, connect, and bind), and</li>
 * <li>通道支持的I/O操作(比如读,写,连接和绑定)</li>
 * <li>the {@link ChannelPipeline} which handles all I/O events and requests
 *     associated with the channel.</li>
 * </ul>
 *
 * <h3>All I/O operations are asynchronous.</h3>\
 * <h3>所有的I/O操作都是异步的</h3>
 * <p>
 * All I/O operations in Netty are asynchronous.  It means any I/O calls will
 * return immediately with no guarantee that the requested I/O operation has
 * been completed at the end of the call.  Instead, you will be returned with
 * a {@link ChannelFuture} instance which will notify you when the requested I/O
 * operation has succeeded, failed, or canceled.
 * Netty中所有的I/O操作都是异步的.这意味着任何I/O调用都会返回,而不能保证所请求的I/O操作在调用
 * 结束时已经完成.相反,你将获得一个{@link ChannelFuture 通道回调}实例,该实例在请求的I/O操作
 * 已经成功,失败或者取消时通知你.
 *
 * <h3>Channels are hierarchical</h3>
 * <h3>通道是分等级的</h3>
 * <p>
 * A {@link Channel} can have a {@linkplain #parent() parent} depending on
 * how it was created.  For instance, a {@link SocketChannel}, that was accepted
 * by {@link ServerSocketChannel}, will return the {@link ServerSocketChannel}
 * as its parent on {@link #parent()}.
 * <p>
 * <p>
 * 一个{@link Channel 通道}可以有一个 {@linkplain #parent() 父通道}取决于他是如何被创建的.
 * 例如,一个{@link SocketChannel 套接字通道}被{@link ServerSocketChannel 服务端套接字通道}接受，
 * {@link SocketChannel}调用{@link #parent()}后将会返回{@link ServerSocketChannel}
 * </p>
 *
 * The semantics of the hierarchical structure depends on the transport
 * implementation where the {@link Channel} belongs to.  For example, you could
 * write a new {@link Channel} implementation that creates the sub-channels that
 * share one socket connection, as <a href="http://beepcore.org/">BEEP</a> and
 * <a href="https://en.wikipedia.org/wiki/Secure_Shell">SSH</a> do.
 *
 * 层次结构的语义取决于{@link Channel}所属的transport实现.举个例子,你可以写一个新的{@link Channel}实现,
 * 以创建一个共享套接字连接的子通道，比如BEEP,SSH这些
 *
 * <h3>Downcast to access transport-specific operations</h3>
 * <h3>向下访问以访问特定于传输的操作</h3>
 * <p>
 * Some transports exposes additional operations that is specific to the
 * transport.  Down-cast the {@link Channel} to sub-type to invoke such
 * operations.  For example, with the old I/O datagram transport, multicast
 * join / leave operations are provided by {@link DatagramChannel}.
 *
 * 一些传输会暴露特定于传输的其他操作,将{@link Channel}向下转换为子类型以调用此类操作.
 * 比如,使用旧的I/O数据报传输，{@link DatagramChannel}提供了组播的加入/离开操作.
 *
 * <h3>Release resources</h3>
 * <h3>释放资源</h3>
 * <p>
 * It is important to call {@link #close()} or {@link #close(ChannelPromise)} to release all
 * resources once you are done with the {@link Channel}. This ensures all resources are
 * released in a proper way, i.e. filehandles.
 *
 * 在完成{@link Channel}后，请调用{@link #close()}或{@link #close(ChannelPromise)}以释放所有资源，这一点很重要.
 * 这样可以确保以适当的方式释放所有资源(即文件句柄)。
 *
 *
 *
 */
public interface Channel extends AttributeMap, ChannelOutboundInvoker, Comparable<Channel> {

    /**
     * Returns the globally unique identifier of this {@link Channel}.
     * 返回此{@link Channel}的全局唯一标识符
     */
    ChannelId id();

    /**
     * Return the {@link EventLoop} this {@link Channel} was registered to.
     * 返回此{@link Channel}注册到的{@link EventLoop}.
     */
    EventLoop eventLoop();

    /**
     * Returns the parent of this channel.
     * 返回此通道的父级。
     * @return the parent channel.
     *         父级通道
     *         {@code null} if this channel does not have a parent channel.
     *         如果这个通道没有父级通道就返回{@code null}
     *
     */
    Channel parent();

    /**
     * Returns the configuration of this channel.
     * 返回此通道的配置
     */
    ChannelConfig config();

    /**
     * Returns {@code true} if the {@link Channel} is open and may get active later
     * 如果{@link Channel}已打开并且之后可能会激活，则返回{@code true}
     */
    boolean isOpen();

    /**
     * Returns {@code true} if the {@link Channel} is registered with an {@link EventLoop}.
     * 如果{@link Channel}已向{@link EventLoop}注册 Returns {@code true}
     */
    boolean isRegistered();

    /**
     * Return {@code true} if the {@link Channel} is active and so connected.
     * 如果{@link Channel}已经激活并且已经连接成功 Return {@code true}
     */
    boolean isActive();

    /**
     * Return the {@link ChannelMetadata} of the {@link Channel} which describe the nature of the {@link Channel}.
     * 返回{@link Channel}的{@link ChannelMetadata}，它描述了{@link Channel}的性质。
     *
     */
    ChannelMetadata metadata();

    /**
     * Returns the local address where this channel is bound to.  The returned
     * {@link SocketAddress} is supposed to be down-cast into more concrete
     * type such as {@link InetSocketAddress} to retrieve the detailed
     * information.
     * 返回此通道绑定到的本地地址.返回的{@link SocketAddress}应该向下转换为更具体的类型，
     * 例如{@link InetSocketAddress}以检索详细的信息。
     *
     * @return the local address of this channel.
     *         {@code null} if this channel is not bound.
     *         该通道的本地地址。 {@code null}如果此频道未绑定
     */
    SocketAddress localAddress();

    /**
     * Returns the remote address where this channel is connected to.  The
     * returned {@link SocketAddress} is supposed to be down-cast into more
     * concrete type such as {@link InetSocketAddress} to retrieve the detailed
     * information.
     * 返回此通道连接到的远程地址.返回的{@link SocketAddress}应该向下转换为更具体的类型,
     * 例如{@link InetSocketAddress}以检索详细的信息.
     *
     * @return the remote address of this channel.
     *         这个通道的远程地址.
     *         {@code null} if this channel is not connected.
     *         如果这个通道没有连接,return {@code null}
     *         If this channel is not connected but it can receive messages
     *         from arbitrary remote addresses (e.g. {@link DatagramChannel},
     *         use {@link DatagramPacket#recipient()} to determine
     *         the origination of the received message as this method will
     *         return {@code null}.
     *         如果未连接此通道，但可以从任意远程地址接收消息(例如:{@link DatagramChannel}
     *         使用{@link DatagramPacket#recipient()})来确定,此方法将接受到的消息的起源
     *         返回{@code null}.
     *
     *
     */
    SocketAddress remoteAddress();

    /**
     * Returns the {@link ChannelFuture} which will be notified when this
     * channel is closed.  This method always returns the same future instance.
     * 当这个通道已经关闭,将会返回{@link ChannelFuture}将会通知该事件.
     * 此方法始终返回相同的future实例
     */
    ChannelFuture closeFuture();

    /**
     * Returns {@code true} if and only if the I/O thread will perform the
     * requested write operation immediately.  Any write requests made when
     * this method returns {@code false} are queued until the I/O thread is
     * ready to process the queued write requests.
     * 当且仅当I/O线程将立即执行请求的写操作时，才返回{@code true}
     * 当此方法返回{@code false}时发出的所有写请求都将排队，直到I/O线程准备好处理排队的写请求为止.
     */
    boolean isWritable();

    /**
     * Get how many bytes can be written until {@link #isWritable()} returns {@code false}.
     * 获取可以写入多少字节，直到{@link #isWritable()}返回{@code false}
     * This quantity will always be non-negative. If {@link #isWritable()} is {@code false} then 0.
     * 这个数字始终为非负数,如果{@link #isWritable()} 为 {@code false},值为0
     */
    long bytesBeforeUnwritable();

    /**
     * Get how many bytes must be drained from underlying buffers until {@link #isWritable()} returns {@code true}.
     * This quantity will always be non-negative. If {@link #isWritable()} is {@code true} then 0.
     * 获取必须从底层缓冲区中使用多少字节，直到{@link #isWritable()}返回{@code true}
     * 这个数字始终为非负数,如果{@link #isWritable()}为{@code true},值为0
     */
    long bytesBeforeWritable();

    /**
     * Returns an <em>internal-use-only</em> object that provides unsafe operations.
     * 返回一个<em>仅供内部使用的</em>不安全操作的提供者对象
     */
    Unsafe unsafe();

    /**
     * Return the assigned {@link ChannelPipeline}.
     * 返回已分配的{@link ChannelPipeline 信道管道}
     */
    ChannelPipeline pipeline();

    /**
     * Return the assigned {@link ByteBufAllocator} which will be used to allocate {@link ByteBuf}s.
     * 返回分配的{@link ByteBufAllocator}，它将用于分配{@link ByteBuf}
     */
    ByteBufAllocator alloc();

    @Override
    Channel read();

    @Override
    Channel flush();

    /**
     * <em>Unsafe</em> operations that should <em>never</em> be called from user-code. These methods
     * are only provided to implement the actual transport, and must be invoked from an I/O thread except for the
     * following methods:
     * <em>Unsafe</em>应该永远被用户代码调用.这些方法只能用于实现实际的传输,并且必须从I/O线程调用，但是以下方法除外
     * <ul>
     *   <li>{@link #localAddress()}</li>
     *   <li>{@link #remoteAddress()}</li>
     *   <li>{@link #closeForcibly()}</li>
     *   <li>{@link #register(EventLoop, ChannelPromise)}</li>
     *   <li>{@link #deregister(ChannelPromise)}</li>
     *   <li>{@link #voidPromise()}</li>
     * </ul>
     */
    interface Unsafe {

        /**
         * Return the assigned {@link RecvByteBufAllocator.Handle} which will be used to allocate {@link ByteBuf}'s when
         * receiving data.
         * 返回已分配的{@link RecvByteBufAllocator.Handle 接受字节缓存分配器处理器},当接受到数据时用于分配{@link ByteBuf}
         *
         */
        RecvByteBufAllocator.Handle recvBufAllocHandle();

        /**
         * Return the {@link SocketAddress} to which is bound local or
         * {@code null} if none.
         * 返回绑定的本地地址@link SocketAddress},如果没有返回{@code null}
         */
        SocketAddress localAddress();

        /**
         * Return the {@link SocketAddress} to which is bound remote or
         * {@code null} if none is bound yet.
         * 返回绑定的远程地址{@link SocketAddress},如果没有返回{@code null}
         */
        SocketAddress remoteAddress();

        /**
         * Register the {@link Channel} of the {@link ChannelPromise} and notify
         * the {@link ChannelFuture} once the registration was complete.
         * 将{@link ChannelPromise}的{@link Channel}注册到{@link EventLoop},一旦操作完成通知{@link ChannelFuture}
         */
        void register(EventLoop eventLoop, ChannelPromise promise);

        /**
         * Bind the {@link SocketAddress} to the {@link Channel} of the {@link ChannelPromise} and notify
         * it once its done.
         * 将{@link SocketAddress}绑定到{@link ChannelPromise}的{@link Channel}中,一旦完成通知{@link ChannelPromise}
         */
        void bind(SocketAddress localAddress, ChannelPromise promise);

        /**
         * Connect the {@link Channel} of the given {@link ChannelFuture} with the given remote {@link SocketAddress}.
         * If a specific local {@link SocketAddress} should be used it need to be given as argument. Otherwise just
         * pass {@code null} to it.
         * 将给定的{@link ChannelFuture}的{@link Channel}与给定的远程{@link SocketAddress}连接.
         * 如果应使用特定的本地{@link SocketAddress}，则需要将其作为参数.否则,只需将{@code null}传递给它.
         *
         * The {@link ChannelPromise} will get notified once the connect operation was complete.
         * 连接操作完成后，{@link ChannelPromise}将收到通知
         */
        void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise);

        /**
         * Disconnect the {@link Channel} of the {@link ChannelFuture} and notify the {@link ChannelPromise} once the
         * operation was complete.
         * {@link ChannelFuture}的{@link Channel}断开连接，一旦操作完成通知{@link ChannelPromise}
         */
        void disconnect(ChannelPromise promise);

        /**
         * Close the {@link Channel} of the {@link ChannelPromise} and notify the {@link ChannelPromise} once the
         * operation was complete.
         * 关闭{@link ChannelPromise}的{@link Channel},一旦操作完成通知{@link ChannelPromise}
         */
        void close(ChannelPromise promise);

        /**
         * Closes the {@link Channel} immediately without firing any events.  Probably only useful
         * when registration attempt failed.
         * 立即关闭{@link Channel}而不触发任何事件.可能仅在注册尝试失败时有用.
         */
        void closeForcibly();

        /**
         * Deregister the {@link Channel} of the {@link ChannelPromise} from {@link EventLoop} and notify the
         * {@link ChannelPromise} once the operation was complete.
         * @link ChannelPromise}的{@link Channel}从{@link EventLoop}中注销,一旦操作完成通知{@link ChannelPromise}
         */
        void deregister(ChannelPromise promise);

        /**
         * Schedules a read operation that fills the inbound buffer of the first {@link ChannelInboundHandler} in the
         * {@link ChannelPipeline}.  If there's already a pending read operation, this method does nothing.
         * 安排读取操作,该操作将填充{@link ChannelPipeline}中第一个{@link ChannelInboundHandler}的入站缓冲区
         * 如果已经有一个待处理的读取操作，则此方法不执行任何操作
         */
        void beginRead();

        /**
         * Schedules a write operation.
         * 安排一个写操作
         */
        void write(Object msg, ChannelPromise promise);

        /**
         * Flush out all write operations scheduled via {@link #write(Object, ChannelPromise)}.
         * 通过{@link #write(Object，ChannelPromise)}执行缓冲区中的所有写操作
         */
        void flush();

        /**
         * Return a special ChannelPromise which can be reused and passed to the operations in {@link Unsafe}.
         * It will never be notified of a success or error and so is only a placeholder for operations
         * that take a {@link ChannelPromise} as argument but for which you not want to get notified.
         * 返回一个特殊的ChannelPromise，可以重复使用并将其传递给{@link Unsafe}中的操作
         * 它永远不会收到成功或错误的通知，因此仅是一个占位符，用于以{@link ChannelPromise}作为参数但您不希望收到其通知的操作
         */
        ChannelPromise voidPromise();

        /**
         * Returns the {@link ChannelOutboundBuffer} of the {@link Channel} where the pending write requests are stored.
         * 返回{@link Channel}的{@link ChannelOutboundBuffer}，存储待处理的写请求。
         */
        ChannelOutboundBuffer outboundBuffer();
    }
}
