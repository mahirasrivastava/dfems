package com.dfems.controller;

import com.dfems.MainApp;
import com.dfems.dao.CaseDAO;
import com.dfems.dao.UserDAO;
import com.dfems.model.Case;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label                      welcomeLabel;
    @FXML private Label                      roleLabel;
    @FXML private TableView<Case>            caseTable;
    @FXML private TableColumn<Case, Integer> colId;
    @FXML private TableColumn<Case, String>  colNumber;
    @FXML private TableColumn<Case, String>  colType;
    @FXML private TableColumn<Case, String>  colStatus;
    @FXML private TableColumn<Case, String>  colLocation;
    @FXML private TableColumn<Case, Integer> colEvidence;
    @FXML private TableColumn<Case, String>  colCreatedBy;
    @FXML private TextField                  searchField;
    @FXML private Label                      totalCasesLabel;

    private final CaseDAO caseDAO = new CaseDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ObservableList<Case> caseList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        welcomeLabel.setText("Welcome, " + MainApp.currentUserName);
        roleLabel.setText("Role: " + MainApp.currentUserRole);
        setupColumns();
        loadAllCases();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("caseId"));
        colNumber.setCellValueFactory(new PropertyValueFactory<>("caseNumber"));
        colType.setCellValueFactory(new PropertyValueFactory<>("caseTypeName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusName"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colEvidence.setCellValueFactory(new PropertyValueFactory<>("evidenceCount"));
        colCreatedBy.setCellValueFactory(new PropertyValueFactory<>("createdByName"));

        // Color-code status
        colStatus.setCellFactory(col -> new TableCell<Case, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
                setText(status);
                switch (status) {
                    case "OPEN":               setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;"); break;
                    case "UNDER_INVESTIGATION": setStyle("-fx-text-fill:#e67e22;-fx-font-weight:bold;"); break;
                    case "CLOSED":             setStyle("-fx-text-fill:#e74c3c;-fx-font-weight:bold;"); break;
                    case "PENDING_REVIEW":     setStyle("-fx-text-fill:#2980b9;-fx-font-weight:bold;"); break;
                    default:                   setStyle("-fx-text-fill:#7f8c8d;"); break;
                }
            }
        });
        caseTable.setItems(caseList);
    }

    private void loadAllCases() {
        try {
            List<Case> cases = caseDAO.getAllCases();
            caseList.setAll(cases);
            totalCasesLabel.setText("Total Cases: " + cases.size());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error loading cases: " + e.getMessage());
        }
    }

    @FXML public void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { loadAllCases(); return; }
        try {
            List<Case> results = caseDAO.searchCases(keyword);
            caseList.setAll(results);
            totalCasesLabel.setText("Found: " + results.size() + " cases");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search error: " + e.getMessage());
        }
    }

    @FXML public void handleRefresh()  { searchField.clear(); loadAllCases(); }
    @FXML public void openNewCase()    { MainApp.switchScene("/fxml/NewCaseView.fxml"); }
    @FXML public void openEvidence()   { MainApp.switchScene("/fxml/EvidenceView.fxml"); }
    @FXML public void openAnalysis()   { MainApp.switchScene("/fxml/AnalysisView.fxml"); }
    @FXML public void openReports()    { MainApp.switchScene("/fxml/ReportView.fxml"); }
    @FXML public void openAuditLog()   { MainApp.switchScene("/fxml/AuditView.fxml"); }
    @FXML public void openCustody()    { MainApp.switchScene("/fxml/CustodyView.fxml"); }

    @FXML public void handleViewCase() {
        Case selected = caseTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "Please select a case."); return; }
        CaseDetailController.selectedCaseId = selected.getCaseId();
        MainApp.switchScene("/fxml/CaseDetailView.fxml");
    }

    @FXML public void handleLogout() {
        try {
            userDAO.logAction(MainApp.currentUserId, "USER_LOGOUT",
                    "User " + MainApp.currentUserName + " logged out");
        } catch (Exception ignored) {}
        MainApp.currentUserId = -1;
        MainApp.currentUserName = "";
        MainApp.currentUserRole = "";
        MainApp.switchScene("/fxml/LoginView.fxml");
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type); a.setContentText(msg); a.showAndWait();
    }
}
