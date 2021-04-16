import messages.Message;

import java.io.Serializable;
import java.util.List;

public class StorageFile implements Serializable, Message {

    private String name;
    private String fullName;
    private StorageFile parent;
    private boolean isFile;
    private List<StorageFile> listStorageFiles;

    public StorageFile(String name, String fullName, StorageFile parent, boolean isFile, List<StorageFile> listStorageFiles) {
        this.name = name;
        this.fullName = fullName;
        this.parent = parent;
        this.isFile = isFile;
    }

    public StorageFile(String name) {
        this.name = name;
    }

    public StorageFile(String name, String fullName) {
        this.name = name;
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public StorageFile getParent() {
        return parent;
    }

    public boolean isFile() {
        return isFile;
    }

    public List<StorageFile> listStorageFiles() {
        return listStorageFiles;
    }

    public void setListStorageFiles(List<StorageFile> listStorageFiles) {
        this.listStorageFiles = listStorageFiles;
    }
}
