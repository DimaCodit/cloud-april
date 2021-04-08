import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private Callback onMessageReceivedCallback;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       // ChannelFuture future = ctx.writeAndFlush(new FileMessage("Test message!!"));
       // future.addListener(FIRE_EXCEPTION_ON_FAILURE); // Le
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof StorageFile) {

            onMessageReceivedCallback.callback((Message)msg);
        }
    }

    public ClientHandler(Callback onMessageReceivedCallback) {
        this.onMessageReceivedCallback = onMessageReceivedCallback;
    }
}
