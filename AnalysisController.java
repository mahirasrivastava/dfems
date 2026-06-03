package com.dfems.controller;

import com.dfems.MainApp;
import com.dfems.dao.AnalysisDAO;
import com.dfems.dao.UserDAO;
import com.dfems.db.DBConnection;
import com.dfems.model.Analysis;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.net.URL;
import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AnalysisController implements Initializable {

    // Filter Bar
    @FXML private TextField        caseIdFilterField;
    @FXML private ComboBox<String> statusFilterCombo;

    // Analysis Table
    @FXML private TableView<Analysis>              analysisTable;
    @FXML private TableColumn<Analysis, Integer>   colAnalysisId;
    @FXML private TableColumn<Analysis, String>    colCaseNumber;
    @FXML private TableColumn<Analysis, String>    colEvidenceFile;
    @FXML private TableColumn<Analysis, String>    colAnalyst;
    @FXML private TableColumn<Analysis, String>    colTool;
    @FXML private TableColumn<Analysis, String>    colDate;
    @FXML private TableColumn<Analysis, String>    colStatus;

    // New Analysis Form
    @FXML private TextField        evidenceIdField;
    @FXML private ComboBox<String> analystCombo;
    @FXML private ComboBox<String> toolCombo;
    @FXML private TextArea         findingsArea;
    @FXML private ComboBox<String> analysisStatusCombo;
    @FXML private Button           submitBtn;

    // Detail Panel
    @FXML private TextArea detailFindingsArea;
    @FXML private Label    detailAnalystLabel;
    @FXML private Label    detailToolLabel;
    @FXML private Label    detailDateLabel;
    @FXML private Label    detailStatusLabel;
    @FXML private Label    detailEvidenceLabel;
    @FXML private Label    detailCaseLabel;

    // Status / Count
    @FXML private Label statusLabel;
    @FXML private Label totalCountLabel;

    private final AnalysisDAO analysisDAO = new AnalysisDAO();
    private final UserDAO     userDAO     = new UserDAO();
    private final ObservableList<Analysis> analysisList = FXCollections.observableArrayList();
    private Analysis selectedAnalysis = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupStatusFilter();
        loadAnalysts();
        loadTools();
        setupFormStatusCombo();
        loadAllAnalysis();
        setupTableSelectionListener();
    }

    // ── Setup ──────────────────────────────────────────────

    private void setupTable() {
        colAnalysisId.setCellValueFactory(new PropertyValueFactory<>("analysisId"));
        colCaseNumber.setCellValueFactory(new PropertyValueFactory<>("caseNumber"));
        colEvidenceFile.setCellValueFactory(new PropertyValueFactory<>("evidenceFileName"));
        colAnalyst.setCellValueFactory(new PropertyValueFactory<>("analystName"));
        colTool.setCellValueFactory(new PropertyValueFactory<>("toolName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("analysisDateStr"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(col -> new TableCell<Analysis, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
                setText(status);
                switch (status) {
                    case "PENDING":     setStyle("-fx-text-fill:#e67e22;-fx-font-weight:bold;"); break;
                    case "IN_PROGRESS": setStyle("-fx-text-fill:#2980b9;-fx-font-weight:bold;"); break;
                    case "COMPLETE":    setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;"); break;
                    case "REVIEWED":    setStyle("-fx-text-fill:#8e44ad;-fx-font-weight:bold;"); break;
                    default:            setStyle("-fx-text-fill:#7f8c8d;"); break;
                }
            }
        });

        analysisTable.setItems(analysisList);
        analysisTable.setPlaceholder(new Label("No analysis records found."));
    }

    private void setupStatusFilter() {
        statusFilterCombo.getItems().addAll("ALL", "PENDING", "IN_PROGRESS", "COMPLETE", "REVIEWED");
        statusFilterCombo.setValue("ALL");
    }

    private void setupFormStatusCombo() {
        analysisStatusCombo.getItems().addAll("PENDING", "IN_PROGRESS", "COMPLETE", "REVIEWED");
        analysisStatusCombo.setValue("COMPLETE");
    }

    private void loadAnalysts() {
        try {
            String sql = "SELECT u.user_id, u.name FROM USERS u " +
                         "JOIN ROLE r ON u.role_id = r.role_id " +
                         "WHERE r.role_name IN ('FORENSIC_ANALYST','INVESTIGATOR','ADMIN') " +
                         "ORDER BY u.name";
            ResultSet rs = DBConnection.getConnection().createStatement().executeQuery(sql);
            analystCombo.getItems().clear();
            while (rs.next())
                analystCombo.getItems().add(rs.getInt("user_id") + " - " + rs.getString("name"));
            analystCombo.getItems().stream()
                    .filter(s -> s.startsWith(MainApp.currentUserId + " - "))
                    .findFirst().ifPresent(analystCombo::setValue);
        } catch (SQLException e) {
            showStatus("Error loading analysts: " + e.getMessage(), true);
        }
    }

    private void loadTools() {
        try {
            String sql = "SELECT tool_id, tool_name, version FROM FORENSIC_TOOL ORDER BY tool_name";
            ResultSet rs = DBConnection.getConnection().createStatement().executeQuery(sql);
            toolCombo.getItems().clear();
            while (rs.next())
                toolCombo.getItems().add(rs.getInt("tool_id") + " - " + rs.getString("tool_name") + " v" + rs.getString("version"));
        } catch (SQLException e) {
            showStatus("Error loading tools: " + e.getMessage(), true);
        }
    }

    private void setupTableSelectionListener() {
        analysisTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, newVal) -> {
                    if (newVal != null) { selectedAnalysis = newVal; populateDetailPanel(newVal); }
                });
    }

    // ── Load Data ──────────────────────────────────────────

    private void loadAllAnalysis() {
        try {
            List<Analysis> list = analysisDAO.getAllAnalysis();
            analysisList.setAll(list);
            updateCount();
        } catch (SQLException e) {
            showStatus("Error loading records: " + e.getMessage(), true);
        }
    }

    // ── Filter ─────────────────────────────────────────────

    @FXML
    public void handleFilter() {
        String keyword  = caseIdFilterField.getText().trim();
        String statusVal = statusFilterCombo.getValue();
        try {
            StringBuilder sql = new StringBuilder(
                "SELECT ar.analysis_id, ar.evidence_id, ar.analyst_id, ar.tool_id, ar.findings, " +
                "TO_CHAR(ar.analysis_date,'YYYY-MM-DD') AS analysis_date_str, ar.status, " +
                "u.name AS analyst_name, ft.tool_name, e.file_name AS evidence_file_name, " +
                "c.case_number, c.case_id " +
                "FROM ANALYSIS_RESULTS ar " +
                "JOIN USERS u ON ar.analyst_id=u.user_id " +
                "JOIN FORENSIC_TOOL ft ON ar.tool_id=ft.tool_id " +
                "JOIN EVIDENCE e ON ar.evidence_id=e.evidence_id " +
                "JOIN CASES c ON e.case_id=c.case_id WHERE 1=1 "
            );
            if (!keyword.isEmpty()) {
                sql.append("AND (UPPER(c.case_number) LIKE '%").append(keyword.toUpperCase()).append("%' ");
                try { sql.append("OR c.case_id=").append(Integer.parseInt(keyword)); } catch (NumberFormatException ignored) {}
                sql.append(") ");
            }
            if (statusVal != null && !statusVal.equals("ALL"))
                sql.append("AND ar.status='").append(statusVal).append("' ");
            sql.append("ORDER BY ar.analysis_date DESC");

            ResultSet rs = DBConnection.getConnection().createStatement().executeQuery(sql.toString());
            analysisList.clear();
            while (rs.next()) analysisList.add(mapRow(rs));
            updateCount();
            showStatus("Filter applied. " + analysisList.size() + " record(s) found.", false);
        } catch (SQLException e) {
            showStatus("Filter error: " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleClearFilter() {
        caseIdFilterField.clear();
        statusFilterCombo.setValue("ALL");
        loadAllAnalysis();
        showStatus("Filter cleared.", false);
    }

    // ── Create Analysis ────────────────────────────────────

    @FXML
    public void handleSubmit() {
        if (evidenceIdField.getText().trim().isEmpty()) {
            showStatus("⚠ Evidence ID is required.", true); return;
        }
        if (analystCombo.getValue() == null) {
            showStatus("⚠ Please select an analyst.", true); return;
        }
        if (toolCombo.getValue() == null) {
            showStatus("⚠ Please select a forensic tool.", true); return;
        }
        if (findingsArea.getText().trim().isEmpty()) {
            showStatus("⚠ Findings cannot be empty.", true); return;
        }
        try {
            int evidenceId = Integer.parseInt(evidenceIdField.getText().trim());
            if (!evidenceExists(evidenceId)) {
                showStatus("⚠ Evidence ID " + evidenceId + " does not exist.", true); return;
            }
            Analysis a = new Analysis();
            a.setEvidenceId(evidenceId);
            a.setAnalystId(Integer.parseInt(analystCombo.getValue().split(" - ")[0]));
            a.setToolId(Integer.parseInt(toolCombo.getValue().split(" - ")[0]));
            a.setFindings(findingsArea.getText().trim());
            a.setStatus(analysisStatusCombo.getValue());

            submitBtn.setDisable(true);
            int newId = analysisDAO.createAnalysis(a);
            showStatus("✔ Analysis created! ID: " + newId, false);
            clearForm();
            loadAllAnalysis();
            userDAO.logAction(MainApp.currentUserId, "ANALYSIS_CREATED",
                    "Analysis ID " + newId + " for evidence " + evidenceId);
        } catch (NumberFormatException e) {
            showStatus("⚠ Evidence ID must be a number.", true);
        } catch (SQLException e) {
            showStatus("✘ Database error: " + e.getMessage(), true);
        } finally {
            submitBtn.setDisable(false);
        }
    }

    @FXML public void handleClearForm() { clearForm(); showStatus("Form cleared.", false); }

    private void clearForm() {
        evidenceIdField.clear();
        findingsArea.clear();
        analysisStatusCombo.setValue("COMPLETE");
        toolCombo.setValue(null);
        analystCombo.getItems().stream()
                .filter(s -> s.startsWith(MainApp.currentUserId + " - "))
                .findFirst().ifPresent(analystCombo::setValue);
    }

    // ── Status Updates ─────────────────────────────────────

    @FXML public void handleMarkComplete() { updateStatus("COMPLETE"); }
    @FXML public void handleMarkReviewed() { updateStatus("REVIEWED"); }

    private void updateStatus(String newStatus) {
        if (selectedAnalysis == null) { showStatus("⚠ Select a record first.", true); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Mark Analysis ID " + selectedAnalysis.getAnalysisId() + " as " + newStatus + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                analysisDAO.updateStatus(selectedAnalysis.getAnalysisId(), newStatus);
                detailStatusLabel.setText("Status    : " + newStatus);
                showStatus("✔ Analysis marked as " + newStatus, false);
                loadAllAnalysis();
                userDAO.logAction(MainApp.currentUserId, "ANALYSIS_STATUS_UPDATED",
                        "Analysis " + selectedAnalysis.getAnalysisId() + " → " + newStatus);
            } catch (SQLException e) {
                showStatus("✘ Error: " + e.getMessage(), true);
            }
        }
    }

    // ── Delete ─────────────────────────────────────────────

    @FXML
    public void handleDelete() {
        if (selectedAnalysis == null) { showStatus("⚠ Select a record to delete.", true); return; }
        if (!MainApp.currentUserRole.equals("ADMIN")) {
            showStatus("✘ Only ADMIN can delete analysis records.", true); return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete Analysis ID " + selectedAnalysis.getAnalysisId() + "? This cannot be undone.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    analysisDAO.deleteAnalysis(selectedAnalysis.getAnalysisId());
                    showStatus("✔ Analysis deleted.", false);
                    clearDetailPanel();
                    loadAllAnalysis();
                    userDAO.logAction(MainApp.currentUserId, "ANALYSIS_DELETED",
                            "Analysis " + selectedAnalysis.getAnalysisId() + " deleted.");
                    selectedAnalysis = null;
                } catch (SQLException e) {
                    showStatus("✘ Error: " + e.getMessage(), true);
                }
            }
        });
    }

    // ── Export ─────────────────────────────────────────────

    @FXML
    public void handleExportFindings() {
        if (selectedAnalysis == null) { showStatus("⚠ Select a record to export.", true); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Findings");
        fc.setInitialFileName("Analysis_" + selectedAnalysis.getAnalysisId() + "_Findings.txt");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        java.io.File file = fc.showSaveDialog(MainApp.primaryStage);
        if (file != null) {
            try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                pw.println("DFEMS - Forensic Analysis Findings Export");
                pw.println("==========================================");
                pw.println("Analysis ID : " + selectedAnalysis.getAnalysisId());
                pw.println("Case        : " + selectedAnalysis.getCaseNumber());
                pw.println("Evidence    : " + selectedAnalysis.getEvidenceFileName());
                pw.println("Analyst     : " + selectedAnalysis.getAnalystName());
                pw.println("Tool        : " + selectedAnalysis.getToolName());
                pw.println("Date        : " + selectedAnalysis.getAnalysisDateStr());
                pw.println("Status      : " + selectedAnalysis.getStatus());
                pw.println();
                pw.println("FINDINGS:");
                pw.println("---------");
                pw.println(selectedAnalysis.getFindings());
                pw.println();
                pw.println("Exported by : " + MainApp.currentUserName);
                pw.println("Exported on : " + new java.util.Date());
                showStatus("✔ Exported to: " + file.getName(), false);
                userDAO.logAction(MainApp.currentUserId, "FINDINGS_EXPORTED",
                        "Analysis " + selectedAnalysis.getAnalysisId() + " exported.");
            } catch (Exception e) {
                showStatus("✘ Export failed: " + e.getMessage(), true);
            }
        }
    }

    // ── Detail Panel ───────────────────────────────────────

    private void populateDetailPanel(Analysis a) {
        detailAnalystLabel.setText("Analyst   : " + a.getAnalystName());
        detailToolLabel.setText("Tool      : " + a.getToolName());
        detailDateLabel.setText("Date      : " + a.getAnalysisDateStr());
        detailStatusLabel.setText("Status    : " + a.getStatus());
        detailEvidenceLabel.setText("Evidence  : " + a.getEvidenceFileName());
        detailCaseLabel.setText("Case      : " + a.getCaseNumber());
        detailFindingsArea.setText(a.getFindings() != null ? a.getFindings() : "No findings recorded.");
    }

    private void clearDetailPanel() {
        detailAnalystLabel.setText("Analyst   :");
        detailToolLabel.setText("Tool      :");
        detailDateLabel.setText("Date      :");
        detailStatusLabel.setText("Status    :");
        detailEvidenceLabel.setText("Evidence  :");
        detailCaseLabel.setText("Case      :");
        detailFindingsArea.clear();
    }

    // ── Helpers ────────────────────────────────────────────

    private boolean evidenceExists(int evidenceId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM EVIDENCE WHERE evidence_id = ?";
        PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
        ps.setInt(1, evidenceId);
        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    private Analysis mapRow(ResultSet rs) throws SQLException {
        Analysis a = new Analysis();
        a.setAnalysisId(rs.getInt("analysis_id"));
        a.setEvidenceId(rs.getInt("evidence_id"));
        a.setAnalystId(rs.getInt("analyst_id"));
        a.setToolId(rs.getInt("tool_id"));
        a.setFindings(rs.getString("findings"));
        a.setAnalysisDateStr(rs.getString("analysis_date_str"));
        a.setStatus(rs.getString("status"));
        a.setAnalystName(rs.getString("analyst_name"));
        a.setToolName(rs.getString("tool_name"));
        a.setEvidenceFileName(rs.getString("evidence_file_name"));
        a.setCaseNumber(rs.getString("case_number"));
        a.setCaseId(rs.getInt("case_id"));
        return a;
    }

    private void showStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setStyle(error ? "-fx-text-fill:#e74c3c;-fx-font-weight:bold;"
                                   : "-fx-text-fill:#27ae60;-fx-font-weight:bold;");
    }

    private void updateCount() {
        totalCountLabel.setText("Total Records: " + analysisList.size());
    }

    @FXML public void handleRefresh() { loadAllAnalysis(); clearDetailPanel(); selectedAnalysis = null; showStatus("Refreshed.", false); }
    @FXML public void goBack()         { MainApp.switchScene("/fxml/DashboardView.fxml"); }
}
