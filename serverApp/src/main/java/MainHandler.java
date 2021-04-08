import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class MainHandler extends ChannelInboundHandlerAdapter {

    public static ConcurrentHashMap<String, RandomAccessFile> activeFiles = new ConcurrentHashMap();
    public static String storagePath;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected.");

        StorageFile rootPath = getStorageFile("/");

        ctx.writeAndFlush(rootPath);

    }

    public MainHandler(String storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileMessage) {
            FileMessage fileMsg = (FileMessage) msg;
            String fileName = fileMsg.getName();
            RandomAccessFile rnfile = activeFiles.get(fileName);
            if (rnfile == null) {
                rnfile = new RandomAccessFile(storagePath + fileName, "rw");
                activeFiles.put(fileName, rnfile);
            }
            rnfile.seek(fileMsg.getOffset());
            rnfile.write(fileMsg.getBytes(),0, fileMsg.getPtr());

        }
        else if (msg instanceof RequestMessage) {
            RequestMessage reqMsg = (RequestMessage) msg;
            if (reqMsg.getAction() == Action.CLOSE_FILE) {
                String fileName = reqMsg.getParam();
                RandomAccessFile rnfile = activeFiles.get(fileName);
                if (rnfile != null) {
                    rnfile.close();
                    activeFiles.remove(fileName);
                }
            }
            else if (reqMsg.getAction() == Action.GET_STORAGE_FILE) {

                StorageFile rootPath = getStorageFile(reqMsg.getParam());
                ctx.writeAndFlush(rootPath);

            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("Эксепш на сервере");
        cause.printStackTrace();
        ctx.close();
    }

    public StorageFile getStorageFile(String relativePath) throws IOException {

        Path parentPath;
        StorageFile parentStorageFile;

        Path absolutePath = Paths.get(storagePath + relativePath);

        if (absolutePath.equals(Paths.get(storagePath))) {
            parentPath = null;
        }
        else {
            parentPath = absolutePath.getParent();
        }

        if (parentPath == null) {
            parentStorageFile = null;
        }
        else if (parentPath.equals(Paths.get(storagePath))) {
            parentStorageFile = new StorageFile("/", "/");
        }
        else
        {
            parentStorageFile = new StorageFile(parentPath.getFileName().toString(), parentPath.getFileName().toString());
        }

        StorageFile storageFile = new StorageFile( relativePath, relativePath, parentStorageFile, false, null);

        List<StorageFile> subFolders = Files.walk(absolutePath,1).map(p-> {
            return new StorageFile(p.getFileName().toString(), absolutePath.relativize(p).toString(), storageFile, !Files.isDirectory(p), null);
        }).collect(Collectors.toList());

        subFolders.remove(0);

        storageFile.setListStorageFiles(subFolders);

        return storageFile;
    }
}