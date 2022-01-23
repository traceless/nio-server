# nio-server
代码简单易懂就没必要说明了。进入目录后，mvn clean install -Dmaven.test.skip 编译一遍即可。

## 一、LOG日志导致吞吐量上不去的原因

这个知识点非常的重要，我认为不亚于前面的NIO问题。log日志是同步写入文件的，造成线程等待，我以为这个问题在springboot的服务中会没问题，线程数量增加不就行了吗？没想到居然也不行（还好测试过了），因为多个线程无法同时进行写入的，还是要竞争等待。。。在webflux和vert.x的服务中，就更不用说了，严重降低了服务的吞吐量。因为它们的默认线程数量就是等于cpu核心数，而且还不知道在那里改，当然不建议大家去改，我估计它设计的时候肯定不会让你去改动这个数量。而且改了也没用，遇到同步写入log的问题，依旧无解。刚在springboot测试过了，无解。

因为它的输入到`console`和写入文件都是同步的，导致IO时间增加，所以吞吐量就上不去了，而且多线程也无法解决。必须要把日志输出改成异步输入，且设置好任务队列大小，queueSize > 10000。

```
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LogPattern}</pattern>
            <charset>utf8</charset>
        </encoder>
    </appender>
    <!-- 控制台输出也要用异步，因为CONSOLE也是要竞争的 -->
    <appender name ="ASYNC_CONSOLE" class= "ch.qos.logback.classic.AsyncAppender">
        <discardingThreshold >0</discardingThreshold>
        <queueSize>12345</queueSize>
        <appender-ref ref = "CONSOLE"/>
    </appender>

    <root level="INFO">
        <appender-ref ref="ASYNC_CONSOLE" />
        <appender-ref ref="ASYNC_FILE" />
    </root>

```

这里的queueSize队列非常的重要，建议设置一万以上，之前设置1000+，吞吐量依然上不去。因为队列满了，就会使用主线程进行写入阻塞（目前通过测试可以得出的结论），所以说线程池多么重要，到处都是它的坑。另外若discardingThreshold > 0，也只丢弃≤INFO级日志，出现大量错误日志时，还是会阻塞。一般不建议discardingThreshold > 0，并发大的时候会丢失INFO日志。

那么可以增加这个AsyncAppender的线程数量吗？但是我目前还没有找到配置项啊，它默认是单线程的，我还以为它设计缺陷呢。。为啥不能增加AsyncAppender线程数呀？刚不是说了多个线程同步写入文件依然需要竞争等待。如果有迫切的高并发需求，那么可以找一下其他的方案，比如可以修改不同日志级别Level写入不同的文件。比如多条日志并入再写入，减少写入磁盘次数，当然这个风险就是会死机的时候丢失日志。更多的问题不在这里讨论了，给了线索你们了，自己摸索吧。
        
## 二、为啥客户端要大于服务接口的性能呢

1、主要是为了控制连接数这变量对于客户端性能影响的问题，如果连接数很大会导致客户端需要吃更多的cpu，就会影响到服务的接口性能，毕竟在同一个服务器上（其实实际影响不大），那么就不容易得到服务器接口真实的水平，就会影响我们验证IO和连接、和吞吐量的关系。


## nodejs模块
- node版本 14以上即可。
- 进入nodejs目录，执行npm i，即可安装相关模块依赖。默认端口7080，启动后就可以进行测试啦。

## springboot模块

- 如果要使用tomcat进行测试，可以在pom文件中去掉 undertow依赖，聪明如你，肯定知道怎么搞啦。
- VertxHttpClientUtil 是使用vert.x相关核心包，NIO客户端。如果你有需要大量并发的请求外部接口，那么这个是一个非常不错的选择。

## vert.x 模块

- 兴趣挺大，暂时没想到有什么可写的。实在要讲的话，可能会讲它的分布式系统方案，它设计非常特别，天生就支持微服务开发，天生就具备分布式部署。
- vert.x在IO密集型的服务中优势非常大。
- 这个项目有一个分布式模式部署例子，分别启动 Manager, User , Order，Controller，类即可运行整个集群了，看看它是怎么进行微服务调用。actor模型跟go的csp 有的一拼。个人感觉actor更加牛逼，天生为分布式而生。

## webflux 模块

- 非常恐怖的技术，难以理解的响应式编程，处理数据和业务像面条一样的，像流水线一样处理你的业务。非常恐怖，比C语言还要恐怖的技术。
- 活不久的技术，千万不要入坑，协程的到来应该就是它死亡的那天。建议入坑vert.x，java协程的到来应该会为它赋予更强大的生命力。






