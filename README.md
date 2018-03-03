# Question ([as posted in StackOverflow](https://stackoverflow.com/questions/49084988/clustered-vert-x-not-working-in-docker-if-vert-x-instance-is-created-manually))

I setup a small project for learning the capabilities of Vert.x in a cluster environment but I'm facing some weird issues when I try to create the vertx instance inside a Docker image. 

The project consists just in 2 verticles being deployed in different Docker containers and using the event bus to communicate with each other

If I use the vertx provided launcher:

        Launcher.executeCommand("run", verticleClass, "--cluster")

(or just stating that the main class is `io.vertx.core.Launcher` and putting the right arguments)

Everything works both locally and inside docker images. But if I try to create the vertx instance manually with

    Vertx.rxClusteredVertx(VertxOptions())
            .flatMap { it.rxDeployVerticle(verticleClass) }
            .subscribe()

Then it's not working in Docker (it works locally). Or, more visually

<pre>
|                 	| Local 	| Docker 	|
|:---------------:	|:-----:	|:------:	|
|  Vertx launcher 	|   Y   	|    Y   	|
| Custom launcher 	|   Y   	|    N   	|
</pre>


By checking the Docker logs it seems that everything works. I can see that both verticles know each other:

    Members [2] {
    	Member [172.18.0.2]:5701 - c5e9636d-b3cd-4e24-a8ce-e881218bf3ce
    	Member [172.18.0.3]:5701 - a09ce83d-e0b3-48eb-aad7-fbd818c389bc this
    }

But when I try to send a message through the event bus the following exception is thrown:

    WARNING: Connecting to server localhost:33845 failed
    io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: localhost/127.0.0.1:33845
    	at sun.nio.ch.SocketChannelImpl.checkConnect(Native Method)
    	at sun.nio.ch.SocketChannelImpl.finishConnect(SocketChannelImpl.java:717)
    	at io.netty.channel.socket.nio.NioSocketChannel.doFinishConnect(NioSocketChannel.java:325)
    	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:340)
    	at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:633)
    	at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:580)
    	at io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:497)
    	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:459)
    	at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:886)
    	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
    	at java.lang.Thread.run(Thread.java:748)
    Caused by: java.net.ConnectException: Connection refused
    	... 11 more
    	
    	
# What do the scripts?

- `runVerticleXLocally`: deploy the verticle in its own JVM using the vertx launcher
- `runVerticleXLocallyCustomLauncher`: deploy the verticle in its own JVM instantiating the `vertx` instance manually
- `runVerticleXDockerized`: deploy the verticle in a Docker container using the vertx launcher
- `runVerticleXLocallyCustomLauncher`: deploy the verticle in a Docker container instantiating the `vertx` instance manually
