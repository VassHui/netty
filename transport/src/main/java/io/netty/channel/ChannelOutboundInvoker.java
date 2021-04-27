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

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.FutureListener;

import java.net.ConnectException;
import java.net.SocketAddress;

/**
 * 通道出站调用器
 */
public interface ChannelOutboundInvoker {

    /**
     * Request to bind to the given {@link SocketAddress} and notify the {@link ChannelFuture} once the operation
     * completes, either because the operation was successful or because of an error.
     * 请求绑定到给定的{@link SocketAddress}地址,一旦操作完成通知{@link ChannelFuture},无论操作成功还是失败
     *
     * <p>
     * This will result in having the
     * {@link ChannelOutboundHandler#bind(ChannelHandlerContext, SocketAddress, ChannelPromise)} method
     * called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelOutboundHandler}的
     * {@link ChannelOutboundHandler#bind(ChannelHandlerContext, SocketAddress, ChannelPromise)}方法
     *
     */
    ChannelFuture bind(SocketAddress localAddress);

    /**
     * Request to connect to the given {@link SocketAddress} and notify the {@link ChannelFuture} once the operation
     * completes, either because the operation was successful or because of an error.
     * 请求连接到给定的{@link SocketAddress}一旦操作完成通知{@link ChannelFuture},无论操作成功还是失败.
     * <p>
     * If the connection fails because of a connection timeout, the {@link ChannelFuture} will get failed with
     * a {@link ConnectTimeoutException}. If it fails because of connection refused a {@link ConnectException}
     * will be used.
     * 如果由于连接超时而导致连接失败，则{@link ChannelFuture}将失败，并带有{@link ConnectTimeoutException}.
     * 如果由于连接拒绝而失败，会返回{@link ConnectException}
     * <p>
     * This will result in having the
     * {@link ChannelOutboundHandler#connect(ChannelHandlerContext, SocketAddress, SocketAddress, ChannelPromise)}
     * method called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelOutboundHandler}的
     * {@link ChannelOutboundHandler#connect(ChannelHandlerContext, SocketAddress, SocketAddress, ChannelPromise)}方法
     *
     */
    ChannelFuture connect(SocketAddress remoteAddress);

    /**
     * Request to connect to the given {@link SocketAddress} while bind to the localAddress and notify the
     * {@link ChannelFuture} once the operation completes, either because the operation was successful or because of
     * an error.
     * 请求连接到给定的远程地址并绑定本地地址，一旦操作完成通知{@link ChannelFuture},无论操作成功还是失败.
     * <p>
     * This will result in having the
     * {@link ChannelOutboundHandler#connect(ChannelHandlerContext, SocketAddress, SocketAddress, ChannelPromise)}
     * method called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个的{@link ChannelOutboundHandler}的
     * {@link ChannelOutboundHandler#connect(ChannelHandlerContext, SocketAddress, SocketAddress, ChannelPromise)}方法
     */
    ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress);

    /**
     * Request to disconnect from the remote peer and notify the {@link ChannelFuture} once the operation completes,
     * either because the operation was successful or because of an error.
     * 请求与远程对等方断开连接,一旦操作完成通知{@link ChannelFuture},无论操作成功还是失败.
     * <p>
     * This will result in having the
     * {@link ChannelOutboundHandler#disconnect(ChannelHandlerContext, ChannelPromise)}
     * method called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用的{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelOutboundHandler}的
     * {@link ChannelOutboundHandler#disconnect(ChannelHandlerContext, ChannelPromise)}方法
     */
    ChannelFuture disconnect();

    /**
     * Request to close the {@link Channel} and notify the {@link ChannelFuture} once the operation completes,
     * either because the operation was successful or because of
     * an error.
     * 请求关闭{@link Channel},一旦操作完成通知{@link ChannelFuture},无论操作成功还是失败.
     *
     * After it is closed it is not possible to reuse it again.
     * 关闭后,无法再次使用.
     * <p>
     * This will result in having the
     * {@link ChannelOutboundHandler#close(ChannelHandlerContext, ChannelPromise)}
     * method called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelOutboundHandler}的
     * {@link ChannelOutboundHandler#close(ChannelHandlerContext, ChannelPromise)}方法
     */
    ChannelFuture close();

    /**
     * Request to deregister from the previous assigned {@link EventExecutor} and notify the
     * {@link ChannelFuture} once the operation completes, either because the operation was successful or because of
     * an error.
     * 请求从先前分配的{@link EventExecutor}注销,一旦操作完成通知{@link ChannelFuture},无论操作成功还是失败
     *
     * <p>
     * This will result in having the
     * {@link ChannelOutboundHandler#deregister(ChannelHandlerContext, ChannelPromise)}
     * method called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelOutboundHandler}的
     * {@link ChannelOutboundHandler#deregister(ChannelHandlerContext, ChannelPromise)}方法
     *
     */
    ChannelFuture deregister();

    /**
     * Request to bind to the given {@link SocketAddress localAddress} and notify the {@link ChannelFuture} once the operation
     * completes, either because the operation was successful or because of an error.
     * 请求绑定到给定的本地{@link SocketAddress localAddress},一旦操作完成通知{@link ChannelFuture},
     * 无论操作成功还是失败.
     * The given {@link ChannelPromise} will be notified.
     * 给定的{@link ChannelPromise}也将收到通知
     *
     * 备注:跟{@link #bind(SocketAddress)}的区别是多了一个{@link ChannelPromise}入参，高层代码会直接return promise
     *
     * <p>
     * This will result in having the
     * {@link ChannelOutboundHandler#bind(ChannelHandlerContext, SocketAddress, ChannelPromise)} method
     * called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelOutboundHandler}的
     * {@link ChannelOutboundHandler#bind(ChannelHandlerContext, SocketAddress, ChannelPromise)}方法
     */
    ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise);

    /**
     * Request to connect to the given {@link SocketAddress} and notify the {@link ChannelFuture} once the operation
     * completes, either because the operation was successful or because of an error.
     * 请求连接到给定的{@link SocketAddress},一旦操作完成通知{@link ChannelFuture},无论操作成功还是失败.
     *
     * The given {@link ChannelFuture} will be notified.
     * 给定的{@link ChannelFuture}也会被通知到
     * <p>
     * If the connection fails because of a connection timeout, the {@link ChannelFuture} will get failed with
     * a {@link ConnectTimeoutException}. If it fails because of connection refused a {@link ConnectException}
     * will be used.
     * 如果因为连接超时,{@link ChannelFuture}将会失败并带一个{@link ConnectTimeoutException}异常.
     * 如果因为连接拒绝,将会附带一个{@link ConnectException}
     * <p>
     * This will result in having the
     * {@link ChannelOutboundHandler#connect(ChannelHandlerContext, SocketAddress, SocketAddress, ChannelPromise)}
     * method called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelOutboundHandler}的
     * {@link ChannelOutboundHandler#connect(ChannelHandlerContext, SocketAddress, SocketAddress, ChannelPromise)}方法
     */
    ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise);

    /**
     * Request to connect to the given {@link SocketAddress} while bind to the localAddress and notify the
     * {@link ChannelFuture} once the operation completes, either because the operation was successful or because of
     * an error.
     * 请求连接给给定的远程地址并绑定本地地址，一旦操作完成通知{@link ChannelFuture},无论成功还是失败.
     *
     * The given {@link ChannelPromise} will be notified and also returned.
     * 给定的{@link ChannelPromise}得到通知并返回
     * <p>
     * This will result in having the
     * {@link ChannelOutboundHandler#connect(ChannelHandlerContext, SocketAddress, SocketAddress, ChannelPromise)}
     * method called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的一个{@link ChannelOutboundHandler}的
     * {@link ChannelOutboundHandler#connect(ChannelHandlerContext, SocketAddress, SocketAddress, ChannelPromise)}方法
     */
    ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise);

    /**
     * Request to disconnect from the remote peer and notify the {@link ChannelFuture} once the operation completes,
     * either because the operation was successful or because of an error.
     * 请求从远程对等放断开连接，一旦操作完成通知{@link ChannelFuture},无论操作成功还是失败.
     * The given {@link ChannelPromise} will be notified.
     * 给定的{@link ChannelPromise}会被通知并被返回
     * <p>
     * This will result in having the
     * {@link ChannelOutboundHandler#disconnect(ChannelHandlerContext, ChannelPromise)}
     * method called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelOutboundHandler}的
     * {@link ChannelOutboundHandler#disconnect(ChannelHandlerContext, ChannelPromise)}方法
     */
    ChannelFuture disconnect(ChannelPromise promise);

    /**
     * Request to close the {@link Channel} and notify the {@link ChannelFuture} once the operation completes,
     * either because the operation was successful or because of
     * an error.
     * 请求关闭{@link Channel},一旦操作完成通知{@link ChannelFuture},无论操作成功还是失败.
     *
     * After it is closed it is not possible to reuse it again.
     * 关闭之后将无法再使用他
     * The given {@link ChannelPromise} will be notified.
     * 给定的{@link ChannelPromise}也将会收到通知.
     *
     * <p>
     * This will result in having the
     * {@link ChannelOutboundHandler#close(ChannelHandlerContext, ChannelPromise)}
     * method called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个 {@link ChannelOutboundHandler}的
     * {@link ChannelOutboundHandler#close(ChannelHandlerContext, ChannelPromise)}方法
     */
    ChannelFuture close(ChannelPromise promise);

    /**
     * Request to deregister from the previous assigned {@link EventExecutor} and notify the
     * {@link ChannelFuture} once the operation completes, either because the operation was successful or because of
     * an error.
     * 请求从之前分配的{@link EventExecutor}的注销,一旦操作完成通知{@link ChannelFuture},无论操作成功还是失败.
     *
     * The given {@link ChannelPromise} will be notified.
     * 给定的{@link ChannelPromise}也会收到通知
     * <p>
     * This will result in having the
     * {@link ChannelOutboundHandler#deregister(ChannelHandlerContext, ChannelPromise)}
     * method called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline} of the
     * {@link Channel}.
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelOutboundHandler}的
     * {@link ChannelOutboundHandler#deregister(ChannelHandlerContext, ChannelPromise)}
     */
    ChannelFuture deregister(ChannelPromise promise);

    /**
     * Request to Read data from the {@link Channel} into the first inbound buffer, triggers an
     * {@link ChannelInboundHandler#channelRead(ChannelHandlerContext, Object)} event if data was
     * read, and triggers a
     * {@link ChannelInboundHandler#channelReadComplete(ChannelHandlerContext) channelReadComplete} event so the
     * handler can decide to continue reading.  If there's a pending read operation already, this method does nothing.
     * 请求从{@link Channel}读取数据到第一个入站缓冲区，如果数据已经读取触发一个
     * {@link ChannelInboundHandler#channelRead(ChannelHandlerContext, Object)}事件,并会触发
     * {@link ChannelInboundHandler#channelReadComplete(ChannelHandlerContext) channelReadComplete}
     * 事件,处理程序可以决定继续阅读,如果已经有一个待处理的读取操作，则此方法不执行任何操作.
     * <p>
     * This will result in having the
     * {@link ChannelOutboundHandler#read(ChannelHandlerContext)}
     * method called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline} of the
     * {@link Channel}
     * 这将会调用{@link Channel}里的{@link ChannelPipeline}包含的下一个{@link ChannelOutboundHandler}的
     * {@link ChannelOutboundHandler#read(ChannelHandlerContext)}方法
     */
    ChannelOutboundInvoker read();

    /**
     * Request to write a message via this {@link ChannelHandlerContext} through the {@link ChannelPipeline}.
     * 请求通过{@link ChannelPipeline}的{@link ChannelHandlerContext}写一个消息
     * This method will not request to actual flush, so be sure to call {@link #flush()}
     * once you want to request to flush all pending data to the actual transport.
     * 此方法将不要求实际冲刷数据,因此，一旦要请求将所有未决数据刷新到实际传输中,请确保调用{@link #flush()}
     */
    ChannelFuture write(Object msg);

    /**
     * Request to write a message via this {@link ChannelHandlerContext} through the {@link ChannelPipeline}.
     * 请求通过{@link ChannelPipeline}的{@link ChannelHandlerContext}写一个消息
     * This method will not request to actual flush, so be sure to call {@link #flush()}
     * once you want to request to flush all pending data to the actual transport.
     * 此方法将不要求实际冲刷数据,因此，一旦要请求将所有未决数据刷新到实际传输中,请确保调用{@link #flush()}
     */
    ChannelFuture write(Object msg, ChannelPromise promise);

    /**
     * Request to flush all pending messages via this ChannelOutboundInvoker.
     * 通过此ChannelOutboundInvoker请求刷新所有未决消息。
     */
    ChannelOutboundInvoker flush();

    /**
     * Shortcut for call {@link #write(Object, ChannelPromise)} and {@link #flush()}.
     * 调用{@link #write(Object，ChannelPromise)}和{@link #flush()}的快捷方式
     */
    ChannelFuture writeAndFlush(Object msg, ChannelPromise promise);

    /**
     * Shortcut for call {@link #write(Object)} and {@link #flush()}.
     * 调用{@link #write(Object，ChannelPromise)}和{@link #flush()}的快捷方式
     */
    ChannelFuture writeAndFlush(Object msg);

    /**
     * Return a new {@link ChannelPromise}.
     * 返回一个新的{@link ChannelPromise}.
     */
    ChannelPromise newPromise();

    /**
     * Return an new {@link ChannelProgressivePromise}
     * 返回一个新的{@link ChannelProgressivePromise}
     */
    ChannelProgressivePromise newProgressivePromise();

    /**
     * Create a new {@link ChannelFuture} which is marked as succeeded already. So {@link ChannelFuture#isSuccess()}
     * will return {@code true}. All {@link FutureListener} added to it will be notified directly. Also
     * every call of blocking methods will just return without blocking.
     * 创建一个新的{@link ChannelFuture}，将其标记为已成功.因此,{@link ChannelFuture#isSuccess()}将会返回{@code true}.
     * 所有添加到其中的{@link FutureListener}将直接收到通知.
     * 另外每次对阻塞方法的调用都将返回而不阻塞.
     */
    ChannelFuture newSucceededFuture();

    /**
     * Create a new {@link ChannelFuture} which is marked as failed already. So {@link ChannelFuture#isSuccess()}
     * will return {@code false}. All {@link FutureListener} added to it will be notified directly. Also
     * every call of blocking methods will just return without blocking.
     * 创建一个新的{@link ChannelFuture}，将其标记为已失败.因此,{@link ChannelFuture#isSuccess()}将会返回false,
     * 所有添加到其中的{@link FutureListener}将直接收到通知.另外每次对阻塞方法的调用都将返回而不阻塞.
     */
    ChannelFuture newFailedFuture(Throwable cause);

    /**
     * Return a special ChannelPromise which can be reused for different operations.
     * 返回一个特殊的ChannelPromise，可以将其重复用于不同的操作
     * <p>
     * It's only supported to use
     * it for {@link ChannelOutboundInvoker#write(Object, ChannelPromise)}.
     * 仅支持将它用于{@link ChannelOutboundInvoker#write(Object，ChannelPromise)}
     * </p>
     * <p>
     * Be aware that the returned {@link ChannelPromise} will not support most operations and should only be used
     * if you want to save an object allocation for every write operation. You will not be able to detect if the
     * operation  was complete, only if it failed as the implementation will call
     * {@link ChannelPipeline#fireExceptionCaught(Throwable)} in this case.
     * 请注意，返回的{@link ChannelPromise}不支持大部分操作，仅能用于保存每个写操作的对象分配.
     * 您将无法检测操作是否完成，仅当操作失败时,因为在这种情况下，实现将调用{@link ChannelPipeline＃fireExceptionCaught(Throwable)}.
     *
     * </p>
     * <strong>Be aware this is an expert feature and should be used with care!</strong>
     * 请注意，这是一项专家功能，应谨慎使用！
     */
    ChannelPromise voidPromise();
}
