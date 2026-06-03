package com.dfems.controller;

import com.dfems.MainApp;
import com.dfems.db.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class AuditController implements Initializable {

    @FXML private TableView<String[]>           auditTable;
    @FXML private TableColumn<String[], String> colLogId;
    @FXML private TableColumn<String[], String> colUser;
    @FXML private TableColumn<String[], String> colAction;
    @FXML private TableColumn<String[], String> colTimestamp;
    @FXML private TableColumn<String[], String> colDetails;
    @FXML private TableColumn<String[], String> colRole;

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> actionFilterCombo;
    @FXML private Label            totalLabel;
    @FXML private Label            statusLabel;

    private final ObservableList<String[]> auditList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadActionTypes();
        loadAllLogs();
    }

    private void setupTable() {
        colLogId.setCellValueFactory(d     -> new SimpleStringProperty(d.getValue()[0]));
        colUser.setCellValueFactory(d      -> new SimpleStringProperty(d.getValue()[1]));
        colRole.setCellValueFactory(d      -> new SimpleStringProperty(d.getValue()[2]));
        colAction.setCellValueFactory(d    -> new SimpleStringProperty(d.getValue()[3]));
        colTimestamp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[4]));
        colDetails.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue()[5]));
        auditTable.setItems(auditList);
        auditTable.setPlaceholder(new Label("No audit logs found."));
    }

    private void loadActionTypes() {
        actionFilterCombo.getItems().add("ALL ACTIONS");
        try {
            ResultSet rs = DBConnection.getConnection().createStatement()
                    .executeQuery("SELECT DISTINCT action FROM AUDIT_LOG ORDER BY action");
            while (rs.next()) actionFilterCombo.getItems().add(rs.getString(1));
        } catch (Exception e) { e.printStackTrace(); }
        actionFilterCombo.setValue("ALL ACTIONS");
    }

    private void loadAllLogs() {
        String sql = "SELECT al.log_id, NVL(u.name,'SYSTEM') AS uname, " +
                     "NVL(r.role_name,'-') AS role_name, al.action, " +
                     "TO_CHAR(al.timestamp,'YYYY-MM-DD HH24:MI:SS') AS ts, " +
                     "NVL(al.details,'-') AS details " +
                     "FROM AUDIT_LOG al " +
                     "LEFT JOIN USERS u ON al.user_id=u.user_id " +
                     "LEFT JOIN ROLE r ON u.role_id=r.role_id " +
                     "ORDER BY al.timestamp DESC";
        loadWithSql(sql);
    }

    @FXML
    public void handleFilter() {
        String keyword = searchField.getText().trim();
        String action  = actionFilterCombo.getValue();
        StringBuilder sql = new StringBuilder(
            "SELECT al.log_id, NVL(u.name,'SYSTEM'), NVL(r.role_name,'-'), al.action, " +
            "TO_CHAR(al.timestamp,'YYYY-MM-DD HH24:MI:SS'), NVL(al.details,'-') " +
            "FROM AUDIT_LOG al LEFT JOIN USERS u ON al.user_id=u.user_id " +
            "LEFT JOIN ROLE r ON u.role_id=r.role_id WHERE 1=1 "
        );
        if (!keyword.isEmpty())
            sql.append("AND (UPPER(u.name) LIKE '%").append(keyword.toUpperCase()).append("%' ")
               .append("OR UPPER(al.details) LIKE '%").append(keyword.toUpperCase()).append("%') ");
        if (action != null && !action.equals("ALL ACTIONS"))
            sql.append("AND al.action='").append(action).append("' ");
        sql.append("ORDER BY al.timestamp DESC");
        loadWithSql(sql.toString());
    }

    private void loadWithSql(String sql) {
        try {
            auditList.clear();
            ResultSet rs = DBConnection.getConnection().createStatement().executeQuery(sql);
            while (rs.next()) {
                auditList.add(new String[]{
                    String.valueOf(rs.getInt(1)),
                    rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getString(6)
                });
            }
            totalLabel.setText("Records: " + auditList.size());
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML public void handleClearFilter() { searchField.clear(); actionFilterCombo.setValue("ALL ACTIONS"); loadAllLogs(); }
    @FXML public void handleRefresh()     { loadAllLogs(); }
    @FXML public void goBack()            { MainApp.switchScene("/fxml/DashboardView.fxml"); }
}
