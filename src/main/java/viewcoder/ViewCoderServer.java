package viewcoder;

/**
 * Created by Administrator on 2017/2/8.
 */

import com.aliyun.oss.OSSClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.tool.pool.WebDriverPool;
/**
 * An HTTP server that sends back the content of the received HTTP request
 * in a pretty plaintext form.
 */

public class ViewCoderServer {
    static final Logger logger = LoggerFactory.getLogger(ViewCoderServer.class);
    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080"));

    public static void main(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ViewCoderServerInitializer(sslCtx));

            Channel ch = b.bind(PORT).sync().channel();
            ViewCoderServer.logger.debug("Service access through port on:" + PORT + '/');
            initFunc();
            ch.closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 一些初始化数据加载
     */
    private static void initFunc(){
        //初始化该静态方法中某些信息
        ViewCoderServer.logger.debug("idle: " + WebDriverPool.getPool().getNumIdle() +
                        " num total:" + WebDriverPool.getPool().getNumActive() +
                        " waiter:" + WebDriverPool.getPool().getNumWaiters());
        ViewCoderServer.logger.error("makeer", "error");
    }

}
