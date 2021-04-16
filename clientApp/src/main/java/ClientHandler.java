import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import messages.Message;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private Callback onMessageReceivedCallback;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       // ChannelFuture future = ctx.writeAndFlush(new messages.FileMessage("Test message!!"));
       // future.addListener(FIRE_EXCEPTION_ON_FAILURE); // Le
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        onMessageReceivedCallback.callback((Message)msg);
    }

    public ClientHandler(Callback onMessageReceivedCallback) {
        this.onMessageReceivedCallback = onMessageReceivedCallback;
    }
}
