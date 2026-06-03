package com.dfems.controller;

import com.dfems.MainApp;
import com.dfems.dao.CaseDAO;
import com.dfems.db.DBConnection;
import com.dfems.model.Case;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class NewCaseController implements Initializable {

    @FXML private TextField        caseNumberField;
    @FXML private TextArea         descriptionArea;
    @FXML private ComboBox<String> caseTypeCombo;
    @FXML private TextField        locationField;
    @FXML private DatePicker       caseDatePicker;
    @FXML private CheckBox         arrestCheck;
    @FXML private CheckBox         domesticCheck;
    @FXML private Label            statusLabel;
    @FXML private Button           submitBtn;

    private final CaseDAO caseDAO = new CaseDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCaseTypes();
        caseDatePicker.setValue(java.time.LocalDate.now());
        caseNumberField.setText("CASE-" + java.time.Year.now().getValue() + "-");
    }

    private void loadCaseTypes() {
        try {
            ResultSet rs = DBConnection.getConnection()
                    .createStatement()
                    .executeQuery("SELECT type_id, type_name FROM CASE_TYPE ORDER BY type_name");
            caseTypeCombo.getItems().clear();
            while (rs.next()) {
                caseTypeCombo.getItems().add(rs.getInt("type_id") + " - " + rs.getString("type_name"));
            }
        } catch (Exception e) {
            statusLabel.setText("Error loading case types: " + e.getMessage());
        }
    }

    @FXML
    public void handleSubmit() {
        if (caseNumberField.getText().trim().isEmpty()) {
            statusLabel.setText("⚠ Case number is required.");
            return;
        }
        if (caseTypeCombo.getValue() == null) {
            statusLabel.setText("⚠ Please select a case type.");
            return;
        }
        if (caseDatePicker.getValue() == null) {
            statusLabel.setText("⚠ Please select a date.");
            return;
        }
        try {
            Case c = new Case();
            c.setCaseNumber(caseNumberField.getText().trim());
            c.setDescription(descriptionArea.getText().trim());
            c.setCaseTypeId(Integer.parseInt(caseTypeCombo.getValue().split(" - ")[0]));
            c.setLocation(locationField.getText().trim());
            c.setCaseDate(java.sql.Date.valueOf(caseDatePicker.getValue()));
            c.setArrest(arrestCheck.isSelected() ? "Y" : "N");
            c.setDomestic(domesticCheck.isSelected() ? "Y" : "N");
            c.setCreatedBy(MainApp.currentUserId);

            submitBtn.setDisable(true);
            int newId = caseDAO.registerCase(c);
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
            statusLabel.setText("✔ Case registered! ID: " + newId + ". You may register another.");
            clearForm();
        } catch (Exception e) {
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            statusLabel.setText("✘ Error: " + e.getMessage());
        } finally {
            submitBtn.setDisable(false);
        }
    }

    private void clearForm() {
        caseNumberField.setText("CASE-" + java.time.Year.now().getValue() + "-");
        descriptionArea.clear();
        locationField.clear();
        caseDatePicker.setValue(java.time.LocalDate.now());
        arrestCheck.setSelected(false);
        domesticCheck.setSelected(false);
        caseTypeCombo.setValue(null);
    }

    @FXML public void handleClear() { clearForm(); statusLabel.setText(""); }
    @FXML public void goBack()       { MainApp.switchScene("/fxml/DashboardView.fxml"); }
}
