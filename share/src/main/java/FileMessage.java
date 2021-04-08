import java.io.Serializable;

public class FileMessage implements Serializable, Message {

    private byte[] bytes;
    private int offset;
    private int ptr;
    private String name;
    private String destinationPath;

    public FileMessage(String name, byte[] bytes, int offset, int ptr) {
        this.bytes = bytes;
        this.offset = offset;
        this.ptr = ptr;
        this.name = name;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getOffset() {
        return offset;
    }

    public int getPtr() {
        return ptr;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public String getName() {
        return name;
    }
}
