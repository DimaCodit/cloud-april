import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Network {
    private SocketChannel channel;

    private static final String HOST = "localhost";
    private static final int PORT = 8189;

    public Network(Callback onMessageReceivedCallback) {
        Thread t = new Thread(() -> {
            NioEventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                channel = socketChannel;
                                socketChannel.pipeline().addLast(new ObjectEncoder(),
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)), new ClientHandler(onMessageReceivedCallback));
                            }
                        });
                ChannelFuture future = b.connect(HOST, PORT).sync();
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
            }
        });
        t.start();
    }

    public void close() {
        channel.close();
    }

    public void sendMessage(Message ms) {
        channel.writeAndFlush(ms);
    }

    public void sendFile(File file, String destination) throws IOException {

        RandomAccessFile raf = new RandomAccessFile(file, "rw");

        int ptr = 0;
        int offset = 0;
        byte[] buf = new byte[524288];

        String fileName = file.getName();

        while((ptr = raf.read(buf)) != -1) {

            FileMessage ms = new FileMessage(destination + "\\"+ fileName, buf, offset, ptr);
            offset += ptr;

            sendMessage(ms);

        }

        sendMessage(new RequestMessage(Action.CLOSE_FILE, destination + "\\"+ fileName));

    }

}