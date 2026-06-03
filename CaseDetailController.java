package com.dfems.controller;

import com.dfems.MainApp;
import com.dfems.dao.AnalysisDAO;
import com.dfems.dao.CaseDAO;
import com.dfems.dao.EvidenceDAO;
import com.dfems.dao.ReportDAO;
import com.dfems.model.Analysis;
import com.dfems.model.Case;
import com.dfems.model.Evidence;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CaseDetailController implements Initializable {

    public static int selectedCaseId = -1;

    @FXML private Label    caseNumberLabel;
    @FXML private Label    caseStatusLabel;
    @FXML private Label    caseTypeLabel;
    @FXML private Label    caseLocationLabel;
    @FXML private Label    caseArrestLabel;
    @FXML private Label    caseDomesticLabel;
    @FXML private Label    caseCreatedByLabel;
    @FXML private TextArea caseDescArea;

    @FXML private TableView<Evidence>            evidenceTable;
    @FXML private TableColumn<Evidence, Integer> colEvId;
    @FXML private TableColumn<Evidence, String>  colEvFile;
    @FXML private TableColumn<Evidence, String>  colEvType;
    @FXML private TableColumn<Evidence, String>  colEvSize;
    @FXML private TableColumn<Evidence, String>  colEvVerified;

    @FXML private TableView<Analysis>            analysisTable;
    @FXML private TableColumn<Analysis, String>  colArAnalyst;
    @FXML private TableColumn<Analysis, String>  colArTool;
    @FXML private TableColumn<Analysis, String>  colArDate;
    @FXML private TableColumn<Analysis, String>  colArStatus;

    @FXML private Label statusLabel;

    private final CaseDAO     caseDAO     = new CaseDAO();
    private final EvidenceDAO evidenceDAO = new EvidenceDAO();
    private final AnalysisDAO analysisDAO = new AnalysisDAO();
    private final ReportDAO   reportDAO   = new ReportDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (selectedCaseId == -1) return;
        loadCaseDetails();
        setupTables();
        loadEvidence();
        loadAnalysis();
    }

    private void loadCaseDetails() {
        try {
            Case c = caseDAO.getCaseById(selectedCaseId);
            if (c != null) {
                caseNumberLabel.setText("Case: " + c.getCaseNumber());
                caseStatusLabel.setText("Status: " + c.getStatusName());
                caseTypeLabel.setText("Type: " + c.getCaseTypeName());
                caseLocationLabel.setText("Location: " + c.getLocation());
                caseArrestLabel.setText("Arrest Made: " + c.getArrest());
                caseDomesticLabel.setText("Domestic: " + c.getDomestic());
                caseCreatedByLabel.setText("Created By: " + c.getCreatedByName());
                caseDescArea.setText(c.getDescription());
            }
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void setupTables() {
        colEvId.setCellValueFactory(new PropertyValueFactory<>("evidenceId"));
        colEvFile.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colEvType.setCellValueFactory(new PropertyValueFactory<>("evidenceTypeName"));
        colEvSize.setCellValueFactory(new PropertyValueFactory<>("fileSizeFormatted"));
        colEvVerified.setCellValueFactory(new PropertyValueFactory<>("verifiedStr"));

        colArAnalyst.setCellValueFactory(new PropertyValueFactory<>("analystName"));
        colArTool.setCellValueFactory(new PropertyValueFactory<>("toolName"));
        colArDate.setCellValueFactory(new PropertyValueFactory<>("analysisDateStr"));
        colArStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadEvidence() {
        try {
            List<Evidence> list = evidenceDAO.getEvidenceByCase(selectedCaseId);
            evidenceTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadAnalysis() {
        try {
            List<Analysis> list = analysisDAO.getAnalysisByCase(selectedCaseId);
            analysisTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void handleCloseCase() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to close this case?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    caseDAO.closeCase(selectedCaseId, MainApp.currentUserId);
                    caseStatusLabel.setText("Status: CLOSED");
                    statusLabel.setText("✔ Case closed successfully.");
                } catch (Exception e) {
                    statusLabel.setText("✘ Error: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    public void handleGenerateReport() {
        try {
            int reportId = reportDAO.generateReport(selectedCaseId, MainApp.currentUserId);
            statusLabel.setText("✔ Report generated! Report ID: " + reportId);
        } catch (Exception e) {
            statusLabel.setText("✘ Error generating report: " + e.getMessage());
        }
    }

    @FXML public void goBack() { MainApp.switchScene("/fxml/DashboardView.fxml"); }
}
