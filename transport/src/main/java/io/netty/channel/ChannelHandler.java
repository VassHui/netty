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

import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Handles an I/O event or intercepts an I/O operation, and forwards it to its next handler in
 * its {@link ChannelPipeline}.
 * 处理或者拦截I/O事件，并将I/O事件转发到{@link ChannelPipeline}的下一个处理程序。
 *
 *
 * <h3>Sub-types</h3>
 * <h3>子类型</h3>
 * <p>
 * {@link ChannelHandler} itself does not provide many methods, but you usually have to implement one of its subtypes:
 * {@link ChannelHandler}本身不提供很多方法，但是通常你必须事先他的子类型中的一个
 * <ul>
 * <li>{@link ChannelInboundHandler} to handle inbound I/O events, and</li>
 * <li>{@link ChannelInboundHandler}用于处理入站I/O事件</li>
 * <li>{@link ChannelOutboundHandler} to handle outbound I/O operations.</li>
 * <li>{@link ChannelOutboundHandler}用于处理出站I/O事件</li>
 * </ul>
 * </p>
 * <p>
 * Alternatively, the following adapter classes are provided for your convenience:
 * 或者，为方便起见，提供了以下适配器类：
 * <ul>
 * <li>{@link ChannelInboundHandlerAdapter} to handle inbound I/O events,</li>
 * <li>{@link ChannelInboundHandlerAdapter}用于处理入站I/O事件</li>
 * <li>{@link ChannelOutboundHandlerAdapter} to handle outbound I/O operations, and</li>
 * <li>{@link ChannelOutboundHandlerAdapter}用于处理出站I/O事件</li>
 * <li>{@link ChannelDuplexHandler} to handle both inbound and outbound events</li>
 * <li>{@link ChannelDuplexHandler}可以处理入站和出站事件</li>
 * </ul>
 * </p>
 * <p>
 * For more information, please refer to the documentation of each subtype.
 * 有关更多信息，请参阅每个子类型的文档。
 * </p>
 *
 * <h3>The context object</h3>
 * <h3>内容上下文对象</h3>
 * <p>
 * A {@link ChannelHandler} is provided with a {@link ChannelHandlerContext}
 * object.  A {@link ChannelHandler} is supposed to interact with the
 * {@link ChannelPipeline} it belongs to via a context object.  Using the
 * context object, the {@link ChannelHandler} can pass events upstream or
 * downstream, modify the pipeline dynamically, or store the information
 * (using {@link AttributeKey}s) which is specific to the handler.
 *
 * {@link ChannelHandlerContext}对象提供了一个{@link ChannelHandler}。
 * {@link ChannelHandler}应该通过上下文对象与其所属的{@link ChannelPipeline}进行交互。
 * 通过使用上下文对象，{@link ChannelHandler}可以向上游或下游传递事件，动态修改管道，
 * 或存储特定于处理程序的信息（使用{@link AttributeKey}）。
 *
 *
 * <h3>State management</h3>
 * <h3>状态管理</h3>
 *
 * A {@link ChannelHandler} often needs to store some stateful information.
 * 一个{@link ChannelHandler}通常需要存储一些状态信息。
 * The simplest and recommended approach is to use member variables:
 * 最简单推荐的方法是使用成员变量
 * <pre>
 * public interface Message {
 *     // your methods here
 * }
 *
 * public class DataServerHandler extends {@link SimpleChannelInboundHandler}&lt;Message&gt; {
 *
 *     <b>private boolean loggedIn;</b>
 *
 *     {@code @Override}
 *     public void channelRead0({@link ChannelHandlerContext} ctx, Message message) {
 *         if (message instanceof LoginMessage) {
 *             authenticate((LoginMessage) message);
 *             <b>loggedIn = true;</b>
 *         } else (message instanceof GetDataMessage) {
 *             if (<b>loggedIn</b>) {
 *                 ctx.writeAndFlush(fetchSecret((GetDataMessage) message));
 *             } else {
 *                 fail();
 *             }
 *         }
 *     }
 *     ...
 * }
 * </pre>
 * Because the handler instance has a state variable which is dedicated to
 * one connection, you have to create a new handler instance for each new
 * channel to avoid a race condition where a unauthenticated client can get
 * the confidential information:
 * 因为处理程序实例具有专用于一个连接的状态变量。您必须为每个新通道创建一个新的处理程序实例，
 * 以避免竞争情况，未经身份验证的客户端可以获取机密信息：
 * <pre>
 * // Create a new handler instance per channel.
 * // See {@link ChannelInitializer#initChannel(Channel)}.
 * public class DataServerInitializer extends {@link ChannelInitializer}&lt;{@link Channel}&gt; {
 *     {@code @Override}
 *     public void initChannel({@link Channel} channel) {
 *         channel.pipeline().addLast("handler", <b>new DataServerHandler()</b>);
 *     }
 * }
 *
 * </pre>
 *
 * <h4>Using {@link AttributeKey}s</h4>
 * <h4>使用{@link AttributeKey}s</h4>
 *
 * Although it's recommended to use member variables to store the state of a
 * handler, for some reason you might not want to create many handler instances.
 * In such a case, you can use {@link AttributeKey}s which is provided by
 * {@link ChannelHandlerContext}:
 * 尽管建议使用成员变量来存储处理程序的状态。由于某些原因，您可能不想创建许多处理程序实例。
 * 在这种情况下，您可以使用{@link ChannelHandlerContext}提供的{@link AttributeKey}：
 * <pre>
 * public interface Message {
 *     // your methods here
 * }
 *
 * {@code @Sharable}
 * public class DataServerHandler extends {@link SimpleChannelInboundHandler}&lt;Message&gt; {
 *     private final {@link AttributeKey}&lt;{@link Boolean}&gt; auth =
 *           {@link AttributeKey#valueOf(String) AttributeKey.valueOf("auth")};
 *
 *     {@code @Override}
 *     public void channelRead({@link ChannelHandlerContext} ctx, Message message) {
 *         {@link Attribute}&lt;{@link Boolean}&gt; attr = ctx.attr(auth);
 *         if (message instanceof LoginMessage) {
 *             authenticate((LoginMessage) o);
 *             <b>attr.set(true)</b>;
 *         } else (message instanceof GetDataMessage) {
 *             if (<b>Boolean.TRUE.equals(attr.get())</b>) {
 *                 ctx.writeAndFlush(fetchSecret((GetDataMessage) o));
 *             } else {
 *                 fail();
 *             }
 *         }
 *     }
 *     ...
 * }
 * </pre>
 * Now that the state of the handler is attached to the {@link ChannelHandlerContext}, you can add the
 * same handler instance to different pipelines:
 * 现在，处理程序的状态已附加到{@link ChannelHandlerContext}，您可以将相同的处理程序实例添加到不同的管道中。
 * <pre>
 * public class DataServerInitializer extends {@link ChannelInitializer}&lt;{@link Channel}&gt; {
 *
 *     private static final DataServerHandler <b>SHARED</b> = new DataServerHandler();
 *
 *     {@code @Override}
 *     public void initChannel({@link Channel} channel) {
 *         channel.pipeline().addLast("handler", <b>SHARED</b>);
 *     }
 * }
 * </pre>
 *
 *
 * <h4>The {@code @Sharable} annotation</h4>
 * <h4>{@code @Sharable}注解</h4>
 * <p>
 * In the example above which used an {@link AttributeKey},
 * you might have noticed the {@code @Sharable} annotation.
 * 在上面使用{@link AttributeKey}的示例中，您可能已经注意到了{@code @Sharable}注解。s
 * <p>
 * If a {@link ChannelHandler} is annotated with the {@code @Sharable}
 * annotation, it means you can create an instance of the handler just once and
 * add it to one or more {@link ChannelPipeline}s multiple times without
 * a race condition.
 * 如果{@link ChannelHandler}带有{@code @Sharable}注解，则意味着您可以只创建一次该处理程序的实例，
 * 然后多次将其添加到一个或多个{@link ChannelPipeline}中，而不会出现竞争条件。
 *
 * <p>
 * If this annotation is not specified, you have to create a new handler
 * instance every time you add it to a pipeline because it has unshared state
 * such as member variables.
 * <p>
 * <p>
 * 如果未指定此注解，则每次将其添加到管道时都必须创建一个新的处理程序实例，
 * 因为它具有未共享的状态，例如成员变量。
 * </p>
 * This annotation is provided for documentation purpose, just like
 * <a href="http://www.javaconcurrencyinpractice.com/annotations/doc/">the JCIP annotations</a>.
 *
 * <h3>Additional resources worth reading</h3>
 * <h3>值得阅读的其他资源</h3>
 * <p>
 * Please refer to the {@link ChannelHandler}, and
 * {@link ChannelPipeline} to find out more about inbound and outbound operations,
 * what fundamental differences they have, how they flow in a  pipeline,  and how to handle
 * the operation in your application.
 * 请参阅{@link ChannelHandler}和{@link ChannelPipeline}，
 * 以了解有关入站和出站操作，它们之间有哪些根本区别，
 * 它们如何在管道中流动以及如何在应用程序中处理该操作的更多信息。
 */
public interface ChannelHandler {

    /**
     * Gets called after the {@link ChannelHandler} was added to the actual context and it's ready to handle events.
     * 在将{@link ChannelHandler}添加到实际上下文中并可以处理事件后调用。
     *
     */
    void handlerAdded(ChannelHandlerContext ctx) throws Exception;

    /**
     * Gets called after the {@link ChannelHandler} was removed from the actual context and it doesn't handle events
     * anymore.
     * 在将{@link ChannelHandler}从实际上下文中删除并且不再处理事件之后被调用。
     */
    void handlerRemoved(ChannelHandlerContext ctx) throws Exception;

    /**
     * Gets called if a {@link Throwable} was thrown.
     *
     * @deprecated if you want to handle this event you should implement {@link ChannelInboundHandler} and
     * implement the method there.
     */
    @Deprecated
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;

    /**
     * Indicates that the same instance of the annotated {@link ChannelHandler}
     * can be added to one or more {@link ChannelPipeline}s multiple times
     * without a race condition.
     * 表示可以将相同注解的{@link ChannelHandler}实例添加到一个或多个{@link ChannelPipeline}中，而不会出现竞争条件。
     *
     *
     * <p>
     * If this annotation is not specified, you have to create a new handler
     * instance every time you add it to a pipeline because it has unshared
     * state such as member variables.
     *
     * 如果未指定此注解，则每次将其添加到管道时都必须创建一个新的处理程序实例，因为它具有未共享的状态，例如成员变量。
     * <p>
     * This annotation is provided for documentation purpose, just like
     * <a href="http://www.javaconcurrencyinpractice.com/annotations/doc/">the JCIP annotations</a>.
     */
    @Inherited
    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Sharable {
        // no value
    }
}
