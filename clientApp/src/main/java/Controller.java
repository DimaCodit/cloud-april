import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private Network network;

    @FXML
    TableView<File> filesTableView;
    @FXML
    TableView<StorageFile> storageFilesTableView;
    @FXML
    ComboBox<File> discComboBox;

    File currentFolder;
    StorageFile currentStoragePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ObservableList<File> olist = FXCollections.observableArrayList(File.listRoots());

        filesTableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2){
                setPath(filesTableView.getSelectionModel().getSelectedItem());
            }
        });

        storageFilesTableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2){
                StorageFile file = storageFilesTableView.getSelectionModel().getSelectedItem();
                getStorageFile(file);
            }
        });

        discComboBox.setItems(olist);
        discComboBox.setValue(olist.get(0));

        TableColumn<File, String> col = new TableColumn("Файл");
        col.setCellValueFactory(entry -> new SimpleObjectProperty<>(entry.getValue().getName()));

        filesTableView.getColumns().add(col);

        MenuItem sent = new MenuItem("Отправить в хранилище");
        sent.setOnAction((ActionEvent event) -> {
            File item = filesTableView.getSelectionModel().getSelectedItem();
            if (item == null || !item.isFile()) {
                return;
            }
            else {
                try {
                    network.sendFile(item);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });

        ContextMenu menu = new ContextMenu();
        menu.getItems().add(sent);
        filesTableView.setContextMenu(menu);

        TableColumn<StorageFile, String> storCol = new TableColumn("Файл");
        storCol.setCellValueFactory(entry -> new SimpleObjectProperty<>(entry.getValue().getName()));

        storageFilesTableView.getColumns().add(storCol);

        network = new Network(m->processMessage(m));

    }

    private void getStorageFile(StorageFile storageFile) {

        if (storageFile.isFile()) {
            return;
        }
        else if (storageFile.getName() == "...") {
            storageFile = currentStoragePath.getParent();
        }

        if (storageFile != null) {
            network.sendMessage(new RequestMessage(Action.GET_STORAGE_FILE, storageFile.getFullName()));
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

    public void processMessage(Message storageFile) {
        if (storageFile instanceof StorageFile) {
            currentStoragePath = (StorageFile) storageFile;
            storageFilesTableView.getItems().clear();
            if (currentStoragePath.getParent() != null) {
                storageFilesTableView.getItems().add(new StorageFile("..."));
            }
            storageFilesTableView.getItems().addAll(currentStoragePath.listStorageFiles());
        }
    }

}
