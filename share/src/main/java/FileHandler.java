import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class FileHandler {

    private ConcurrentHashMap<String, RandomAccessFile> activeFiles = new ConcurrentHashMap();

    public void acceptFileMessage(FileMessage msg, String destinationPath) throws IOException {

        String fileName = msg.getName();
        RandomAccessFile file = activeFiles.get(fileName);
        if (file == null) {
            file = new RandomAccessFile(destinationPath + fileName, "rw");
            activeFiles.put(fileName, file);
        }
        file.seek(msg.getOffset());
        file.write(msg.getBytes(),0, msg.getPtr());

    }

    public void sendFile(String fileName, String destination, Consumer<Message> messageSender) throws IOException {

        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        String relativePath = destination + "\\"+ Paths.get(fileName).getFileName().toString() ;

        int ptr = 0;
        int offset = 0;
        byte[] buf = new byte[524288];

        while((ptr = raf.read(buf)) != -1) {

            FileMessage ms = new FileMessage(relativePath, buf, offset, ptr);
            offset += ptr;

            messageSender.accept(ms);

        }

        messageSender.accept(new RequestMessage(Action.CLOSE_FILE, relativePath));

    }

    public void closeFile (RequestMessage reqMsg) throws IOException {

        String fileName = reqMsg.getParam();
        RandomAccessFile file = activeFiles.get(fileName);
        if (file != null) {
            file.close();
            activeFiles.remove(fileName);
        }

    }

}
