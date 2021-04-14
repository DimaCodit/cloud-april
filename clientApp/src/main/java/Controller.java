import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import messages.Action;
import messages.FileMessage;
import messages.Message;
import messages.RequestMessage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private Network network;
    private FileHandler fileHandler;

    @FXML
    TableView<File> filesTableView;
    @FXML
    TableView<StorageFile> storageFilesTableView;
    @FXML
    ComboBox<File> discComboBox;
    @FXML
    MenuItem menuItemGetFromStorage;
    @FXML
    MenuItem menuItemSendToStorage;
    @FXML
    TableColumn<File, String> filesTableViewColumn;
    @FXML
    TableColumn<StorageFile, String> storageTableViewColumn;


    File currentFolder;
    StorageFile currentStoragePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        fileHandler = new FileHandler();

        initDiscComboBox();
        initLocalFilesTable();
        initStorageFilesTable();

        network = new Network(m-> {
            try {
                processMessage(m);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private void getStorageFile(StorageFile storageFile) {

        if (storageFile == null || storageFile.isFile()) {
            return;
        }
        else if (storageFile.getName() == "...") {
            storageFile = currentStoragePath.getParent();
        }

        if (storageFile != null) {
            network.sendMessage(new RequestMessage(Action.GET_STORAGE_PATH, storageFile.getFullName()));
        }
    }

    public List<File> getNodesForDirectory(File directory) {
        List<File> list = new ArrayList<>();
        for(File f : directory.listFiles()) {
            if (!f.isHidden()) {
                list.add(f);
            }
        }
        return list;
    }

    public void exitAction() {
        network.close();
        Platform.exit();
    }

    public void setDisc(ActionEvent actionEvent) {
       File disc = (File) discComboBox.getValue();
       setPath(disc);
    }

    public void setPath(File path) {
        if (path == null || !path.isDirectory()) {
            return;
        }
        if (path.getName() == "...") {
            setPath(currentFolder.getParentFile());
            return;
        }
        List<File> list = getNodesForDirectory(path);

        filesTableView.getItems().clear();
        if (path.getParentFile() != null) {
            filesTableView.getItems().add(new File("..."));
        }
        filesTableView.getItems().addAll(list);
        currentFolder = path;
    }

    public void processMessage(Message msg) throws IOException {
        if (msg instanceof StorageFile) {
            currentStoragePath = (StorageFile) msg;
            storageFilesTableView.getItems().clear();
            if (!currentStoragePath.getFullName().equals("/")) {
                storageFilesTableView.getItems().add(new StorageFile("..."));
            }
            storageFilesTableView.getItems().addAll(currentStoragePath.listStorageFiles());
        }
        else if (msg instanceof FileMessage) {
            fileHandler.acceptFileMessage((FileMessage)msg, "");
        }
        else if (msg instanceof RequestMessage) {
            RequestMessage reqMsg = (RequestMessage) msg;
            if (reqMsg.getAction() == Action.CLOSE_FILE) {
                fileHandler.closeFile((RequestMessage) msg);
                setPath(currentFolder);
            }
        }
    }

    private void initDiscComboBox() {
        ObservableList<File> olist = FXCollections.observableArrayList(File.listRoots());

        discComboBox.setItems(olist);
        discComboBox.setValue(olist.get(0));
    }

    private void initLocalFilesTable() {
        filesTableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2){
                setPath(filesTableView.getSelectionModel().getSelectedItem());
            }
        });

        filesTableViewColumn.setCellValueFactory(entry -> new SimpleObjectProperty<>(entry.getValue().getName()));

    }

    private void initStorageFilesTable() {
        storageFilesTableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2){
                StorageFile file = storageFilesTableView.getSelectionModel().getSelectedItem();
                getStorageFile(file);
            }
        });

        storageTableViewColumn.setCellValueFactory(entry -> new SimpleObjectProperty<>(entry.getValue().getName()));
    }

    public void getFromStorage(ActionEvent actionEvent) {

        StorageFile item = storageFilesTableView.getSelectionModel().getSelectedItem();
        if (item == null || !item.isFile()) {
            return;
        } else {
            network.sendMessage(new RequestMessage(Action.GET_FILE_FROM_STORAGE, item.getFullName(), currentFolder.toString()));
        }

    }

    public void sendToStorage(ActionEvent actionEvent) {

        File item = filesTableView.getSelectionModel().getSelectedItem();
        if (item == null || !item.isFile()) {
            return;
        }
        else {
            try {

                fileHandler.sendFile(item.toString(), currentStoragePath.getFullName(), message -> network.sendMessage(message));
                getStorageFile(currentStoragePath);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void renameStorageFile(ActionEvent actionEvent) {
        StorageFile item = storageFilesTableView.getSelectionModel().getSelectedItem();
        if (item == null || !item.isFile()) {
            return;
        } else {
            TextInputDialog dialog = new TextInputDialog(item.getName());
            dialog.setTitle("Введите новое имя");
            dialog.setHeaderText("Enter your name:");

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(newName -> {
                network.sendMessage(new RequestMessage(Action.RENAME_FILE_IN_STORAGE, item.getFullName(), newName));
            });

        }
    }
}
