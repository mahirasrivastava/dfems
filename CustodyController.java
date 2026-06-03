package com.dfems.controller;

import com.dfems.MainApp;
import com.dfems.dao.EvidenceDAO;
import com.dfems.dao.UserDAO;
import com.dfems.db.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.ResultSet;
import java.util.List;
import java.util.ResourceBundle;

public class CustodyController implements Initializable {

    @FXML private TextField        evidenceIdField;
    @FXML private ComboBox<String> actionCombo;
    @FXML private ComboBox<String> userCombo;
    @FXML private TextArea         remarksArea;
    @FXML private Label            statusLabel;

    @FXML private TableView<String[]>           custodyTable;
    @FXML private TableColumn<String[], String> colCustodyId;
    @FXML private TableColumn<String[], String> colEvidence;
    @FXML private TableColumn<String[], String> colUser;
    @FXML private TableColumn<String[], String> colAction;
    @FXML private TableColumn<String[], String> colTime;
    @FXML private TableColumn<String[], String> colRemarks;
    @FXML private Label totalLabel;

    private final EvidenceDAO evidenceDAO = new EvidenceDAO();
    private final UserDAO     userDAO     = new UserDAO();
    private final ObservableList<String[]> custodyList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadActionTypes();
        loadUsers();
        loadAllCustody();
    }

    private void setupTable() {
        colCustodyId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        colEvidence.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue()[1]));
        colUser.setCellValueFactory(d      -> new SimpleStringProperty(d.getValue()[2]));
        colAction.setCellValueFactory(d    -> new SimpleStringProperty(d.getValue()[3]));
        colTime.setCellValueFactory(d      -> new SimpleStringProperty(d.getValue()[4]));
        colRemarks.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue()[5]));
        custodyTable.setItems(custodyList);
        custodyTable.setPlaceholder(new Label("No custody records."));
    }

    private void loadActionTypes() {
        try {
            ResultSet rs = DBConnection.getConnection().createStatement()
                    .executeQuery("SELECT action_name FROM ACTION_TYPE ORDER BY action_name");
            actionCombo.getItems().clear();
            while (rs.next()) actionCombo.getItems().add(rs.getString(1));
            actionCombo.setValue("ACCESSED");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadUsers() {
        try {
            ResultSet rs = DBConnection.getConnection().createStatement()
                    .executeQuery("SELECT user_id, name FROM USERS ORDER BY name");
            userCombo.getItems().clear();
            while (rs.next())
                userCombo.getItems().add(rs.getInt(1) + " - " + rs.getString(2));
            userCombo.getItems().stream()
                    .filter(s -> s.startsWith(MainApp.currentUserId + " - "))
                    .findFirst().ifPresent(userCombo::setValue);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadAllCustody() {
        try {
            String sql = "SELECT coc.custody_id, e.file_name, u.name, at.action_name, " +
                         "TO_CHAR(coc.timestamp,'YYYY-MM-DD HH24:MI:SS'), NVL(coc.remarks,'-') " +
                         "FROM CHAIN_OF_CUSTODY coc " +
                         "JOIN EVIDENCE e ON coc.evidence_id=e.evidence_id " +
                         "JOIN USERS u ON coc.user_id=u.user_id " +
                         "JOIN ACTION_TYPE at ON coc.action_id=at.action_id " +
                         "ORDER BY coc.timestamp DESC";
            ResultSet rs = DBConnection.getConnection().createStatement().executeQuery(sql);
            custodyList.clear();
            while (rs.next()) {
                custodyList.add(new String[]{
                    String.valueOf(rs.getInt(1)),
                    rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getString(6)
                });
            }
            totalLabel.setText("Total Records: " + custodyList.size());
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void handleTransfer() {
        if (evidenceIdField.getText().trim().isEmpty()) {
            showStatus("⚠ Evidence ID is required.", true); return;
        }
        if (actionCombo.getValue() == null || userCombo.getValue() == null) {
            showStatus("⚠ Select action and user.", true); return;
        }
        try {
            int evidenceId = Integer.parseInt(evidenceIdField.getText().trim());
            int userId     = Integer.parseInt(userCombo.getValue().split(" - ")[0]);
            String action  = actionCombo.getValue();
            String remarks = remarksArea.getText().trim();

            evidenceDAO.transferCustody(evidenceId, userId, action, remarks);
            showStatus("✔ Custody transfer recorded.", false);
            loadAllCustody();
            evidenceIdField.clear();
            remarksArea.clear();

            userDAO.logAction(MainApp.currentUserId, "CUSTODY_RECORDED",
                    "Evidence " + evidenceId + " action: " + action);
        } catch (NumberFormatException e) {
            showStatus("⚠ Evidence ID must be a number.", true);
        } catch (Exception e) {
            showStatus("✘ Error: " + e.getMessage(), true);
        }
    }

    @FXML public void handleRefresh() { loadAllCustody(); }
    @FXML public void goBack()         { MainApp.switchScene("/fxml/DashboardView.fxml"); }

    private void showStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setStyle(error ? "-fx-text-fill:#e74c3c;-fx-font-weight:bold;"
                                   : "-fx-text-fill:#27ae60;-fx-font-weight:bold;");
    }
}
