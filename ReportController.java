package com.dfems.controller;

import com.dfems.MainApp;
import com.dfems.dao.ReportDAO;
import com.dfems.dao.UserDAO;
import com.dfems.db.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.net.URL;
import java.sql.*;
import java.util.List;
import java.util.ResourceBundle;

public class ReportController implements Initializable {

    @FXML private TableView<String[]>           reportTable;
    @FXML private TableColumn<String[], String> colReportId;
    @FXML private TableColumn<String[], String> colCaseNum;
    @FXML private TableColumn<String[], String> colCreatedBy;
    @FXML private TableColumn<String[], String> colDate;
    @FXML private TableColumn<String[], String> colType;

    @FXML private TextField  genCaseIdField;
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private TextArea   reportPreviewArea;
    @FXML private Label      statusLabel;
    @FXML private Label      totalLabel;

    private final ReportDAO reportDAO = new ReportDAO();
    private final UserDAO   userDAO   = new UserDAO();
    private final ObservableList<String[]> reportList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        reportTypeCombo.getItems().addAll("PRELIMINARY", "INTERIM", "FINAL");
        reportTypeCombo.setValue("FINAL");
        loadAllReports();
        setupSelectionListener();
    }

    private void setupTable() {
        colReportId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        colCaseNum.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue()[1]));
        colCreatedBy.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        colDate.setCellValueFactory(d     -> new SimpleStringProperty(d.getValue()[3]));
        colType.setCellValueFactory(d     -> new SimpleStringProperty(d.getValue()[4]));

        colType.setCellFactory(col -> new TableCell<String[], String>() {
            @Override protected void updateItem(String t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) { setText(null); setStyle(""); return; }
                setText(t);
                switch (t) {
                    case "FINAL":       setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;"); break;
                    case "PRELIMINARY": setStyle("-fx-text-fill:#e67e22;-fx-font-weight:bold;"); break;
                    case "INTERIM":     setStyle("-fx-text-fill:#2980b9;-fx-font-weight:bold;"); break;
                }
            }
        });
        reportTable.setItems(reportList);
        reportTable.setPlaceholder(new Label("No reports found."));
    }

    private void setupSelectionListener() {
        reportTable.getSelectionModel().selectedItemProperty().addListener((obs, old, row) -> {
            if (row != null) loadReportPreview(Integer.parseInt(row[0]));
        });
    }

    private void loadAllReports() {
        try {
            List<String[]> list = reportDAO.getAllReports();
            reportList.setAll(list);
            totalLabel.setText("Total Reports: " + list.size());
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    private void loadReportPreview(int reportId) {
        try {
            String text = reportDAO.getReportText(reportId);
            reportPreviewArea.setText(text);
        } catch (Exception e) {
            reportPreviewArea.setText("Error loading report: " + e.getMessage());
        }
    }

    @FXML
    public void handleGenerate() {
        if (genCaseIdField.getText().trim().isEmpty()) {
            showStatus("⚠ Enter a Case ID.", true); return;
        }
        try {
            int caseId = Integer.parseInt(genCaseIdField.getText().trim());
            int reportId = reportDAO.generateReport(caseId, MainApp.currentUserId);
            showStatus("✔ Report generated! ID: " + reportId, false);
            loadAllReports();
            userDAO.logAction(MainApp.currentUserId, "REPORT_GENERATED",
                    "Report " + reportId + " for case " + caseId);
        } catch (NumberFormatException e) {
            showStatus("⚠ Case ID must be a number.", true);
        } catch (Exception e) {
            showStatus("✘ Error: " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleExport() {
        String[] selected = reportTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("⚠ Select a report to export.", true); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Report");
        fc.setInitialFileName("Report_" + selected[0] + "_" + selected[1] + ".txt");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        java.io.File file = fc.showSaveDialog(MainApp.primaryStage);
        if (file != null) {
            try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                pw.print(reportPreviewArea.getText());
                showStatus("✔ Exported to: " + file.getName(), false);
            } catch (Exception e) {
                showStatus("✘ Export failed: " + e.getMessage(), true);
            }
        }
    }

    @FXML public void handleRefresh() { loadAllReports(); reportPreviewArea.clear(); }
    @FXML public void goBack()         { MainApp.switchScene("/fxml/DashboardView.fxml"); }

    private void showStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setStyle(error ? "-fx-text-fill:#e74c3c;-fx-font-weight:bold;"
                                   : "-fx-text-fill:#27ae60;-fx-font-weight:bold;");
    }
}
