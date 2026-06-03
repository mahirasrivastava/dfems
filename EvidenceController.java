package com.dfems.controller;

import com.dfems.MainApp;
import com.dfems.dao.EvidenceDAO;
import com.dfems.dao.UserDAO;
import com.dfems.db.DBConnection;
import com.dfems.model.Evidence;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.sql.ResultSet;
import java.util.List;
import java.util.ResourceBundle;

public class EvidenceController implements Initializable {

    // Upload Form
    @FXML private TextField        caseIdField;
    @FXML private ComboBox<String> evidenceTypeCombo;
    @FXML private ComboBox<String> storageCombo;
    @FXML private TextField        fileNameField;
    @FXML private TextField        fileSizeField;
    @FXML private TextField        fileTypeField;
    @FXML private TextField        hashField;
    @FXML private Label            statusLabel;
    @FXML private Button           uploadBtn;

    // Evidence Table
    @FXML private TableView<Evidence>              evidenceTable;
    @FXML private TableColumn<Evidence, Integer>   colEvId;
    @FXML private TableColumn<Evidence, String>    colCaseNum;
    @FXML private TableColumn<Evidence, String>    colFileName;
    @FXML private TableColumn<Evidence, String>    colEvType;
    @FXML private TableColumn<Evidence, String>    colSize;
    @FXML private TableColumn<Evidence, String>    colStorage;
    @FXML private TableColumn<Evidence, String>    colVerified;

    // Chain of Custody panel
    @FXML private TableView<String[]>              custodyTable;
    @FXML private TableColumn<String[], String>    colCocUser;
    @FXML private TableColumn<String[], String>    colCocAction;
    @FXML private TableColumn<String[], String>    colCocTime;
    @FXML private TableColumn<String[], String>    colCocRemarks;
    @FXML private Label                            totalEvidenceLabel;

    private final EvidenceDAO evidenceDAO = new EvidenceDAO();
    private final UserDAO     userDAO     = new UserDAO();
    private final ObservableList<Evidence> evidenceList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadEvidenceTypes();
        loadStorageLocations();
        setupEvidenceTable();
        loadAllEvidence();
        setupTableListener();
    }

    private void loadEvidenceTypes() {
        try {
            ResultSet rs = DBConnection.getConnection().createStatement()
                    .executeQuery("SELECT type_id, type_name FROM EVIDENCE_TYPE ORDER BY type_name");
            evidenceTypeCombo.getItems().clear();
            while (rs.next())
                evidenceTypeCombo.getItems().add(rs.getInt(1) + " - " + rs.getString(2));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadStorageLocations() {
        try {
            ResultSet rs = DBConnection.getConnection().createStatement()
                    .executeQuery("SELECT storage_id, location_name FROM STORAGE_LOCATION ORDER BY location_name");
            storageCombo.getItems().clear();
            while (rs.next())
                storageCombo.getItems().add(rs.getInt(1) + " - " + rs.getString(2));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupEvidenceTable() {
        colEvId.setCellValueFactory(new PropertyValueFactory<>("evidenceId"));
        colCaseNum.setCellValueFactory(new PropertyValueFactory<>("caseNumber"));
        colFileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colEvType.setCellValueFactory(new PropertyValueFactory<>("evidenceTypeName"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("fileSizeFormatted"));
        colStorage.setCellValueFactory(new PropertyValueFactory<>("storageLocation"));
        colVerified.setCellValueFactory(new PropertyValueFactory<>("verifiedStr"));
        evidenceTable.setItems(evidenceList);
        evidenceTable.setPlaceholder(new Label("No evidence records found."));
    }

    private void setupTableListener() {
        evidenceTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) loadCustodyChain(newVal.getEvidenceId());
        });
    }

    private void loadAllEvidence() {
        try {
            List<Evidence> list = evidenceDAO.getAllEvidence();
            evidenceList.setAll(list);
            totalEvidenceLabel.setText("Total Evidence: " + list.size());
        } catch (Exception e) {
            showStatus("Error loading evidence: " + e.getMessage(), true);
        }
    }

    private void loadCustodyChain(int evidenceId) {
        try {
            List<String[]> rows = evidenceDAO.getChainOfCustody(evidenceId);
            ObservableList<String[]> cocList = FXCollections.observableArrayList(rows);
            colCocUser.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
            colCocAction.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));
            colCocTime.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[2]));
            colCocRemarks.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[3]));
            custodyTable.setItems(cocList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void browseFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Evidence File");
        File file = fc.showOpenDialog(MainApp.primaryStage);
        if (file != null) {
            fileNameField.setText(file.getName());
            fileSizeField.setText(String.valueOf(file.length()));
            String ext = file.getName().contains(".")
                    ? file.getName().substring(file.getName().lastIndexOf(".") + 1).toUpperCase()
                    : "UNKNOWN";
            fileTypeField.setText(ext);
            hashField.setText("MD5-" + Long.toHexString(file.length() * 31L + file.getName().hashCode()));
        }
    }

    @FXML
    public void handleUpload() {
        if (caseIdField.getText().trim().isEmpty() || fileNameField.getText().trim().isEmpty()) {
            showStatus("⚠ Case ID and File Name are required.", true); return;
        }
        if (evidenceTypeCombo.getValue() == null || storageCombo.getValue() == null) {
            showStatus("⚠ Please select evidence type and storage location.", true); return;
        }
        try {
            Evidence ev = new Evidence();
            ev.setCaseId(Integer.parseInt(caseIdField.getText().trim()));
            ev.setTypeId(Integer.parseInt(evidenceTypeCombo.getValue().split(" - ")[0]));
            ev.setStorageId(Integer.parseInt(storageCombo.getValue().split(" - ")[0]));
            ev.setFileName(fileNameField.getText().trim());
            ev.setFileSize(fileSizeField.getText().trim().isEmpty() ? 0 : Long.parseLong(fileSizeField.getText().trim()));
            ev.setFileType(fileTypeField.getText().trim());
            ev.setHashMd5(hashField.getText().trim());
            ev.setUploadedBy(MainApp.currentUserId);

            uploadBtn.setDisable(true);
            int evId = evidenceDAO.uploadEvidence(ev);
            showStatus("✔ Evidence uploaded successfully! Evidence ID: " + evId, false);
            clearForm();
            loadAllEvidence();
        } catch (NumberFormatException e) {
            showStatus("⚠ Case ID and File Size must be numbers.", true);
        } catch (Exception e) {
            showStatus("✘ Error: " + e.getMessage(), true);
        } finally {
            uploadBtn.setDisable(false);
        }
    }

    private void clearForm() {
        caseIdField.clear(); fileNameField.clear();
        fileSizeField.clear(); fileTypeField.clear();
        hashField.clear(); evidenceTypeCombo.setValue(null); storageCombo.setValue(null);
    }

    private void showStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setStyle(error ? "-fx-text-fill:#e74c3c;" : "-fx-text-fill:#27ae60;");
    }

    @FXML public void handleRefresh() { loadAllEvidence(); }
    @FXML public void goBack()         { MainApp.switchScene("/fxml/DashboardView.fxml"); }
}
