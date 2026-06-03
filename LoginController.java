package com.dfems.controller;

import com.dfems.MainApp;
import com.dfems.dao.UserDAO;
import com.dfems.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginBtn;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            errorLabel.setText("⚠ Please enter your email.");
            return;
        }
        loginBtn.setDisable(true);
        loginBtn.setText("Logging in...");
        try {
            User user = userDAO.authenticate(email);
            if (user != null) {
                MainApp.currentUserId   = user.getUserId();
                MainApp.currentUserName = user.getName();
                MainApp.currentUserRole = user.getRoleName();

                userDAO.logAction(user.getUserId(), "USER_LOGIN",
                        "User logged in: " + email + " | Role: " + user.getRoleName());

                MainApp.switchScene("/fxml/DashboardView.fxml");
            } else {
                errorLabel.setText("✘ Email not found. Access denied.");
                loginBtn.setDisable(false);
                loginBtn.setText("Login");
            }
        } catch (Exception e) {
            errorLabel.setText("✘ Database error: " + e.getMessage());
            loginBtn.setDisable(false);
            loginBtn.setText("Login");
        }
    }

    @FXML
    public void handleEnter(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            handleLogin();
        }
    }
}
