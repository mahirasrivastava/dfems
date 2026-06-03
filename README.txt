================================================================
  DIGITAL FORENSIC EVIDENCE MANAGEMENT SYSTEM (DFEMS)
  Setup & Run Guide
================================================================

QUICK START — 3 STEPS
-----------------------
1. Run SQL script in Oracle
2. Set DB credentials in DBConnection.java
3. Run MainApp.java in IntelliJ

================================================================
STEP 1 — ORACLE DATABASE SETUP
================================================================

A) Install Oracle Database 21c XE (free):
   https://www.oracle.com/database/technologies/xe-downloads.html

B) Open SQL*Plus or SQL Developer. Connect as SYSDBA:
   sqlplus sys/YOUR_PASSWORD as sysdba

C) Create the DFEMS user:
   CREATE USER dfems_user IDENTIFIED BY dfems_pass;
   GRANT CONNECT, RESOURCE, DBA TO dfems_user;
   GRANT UNLIMITED TABLESPACE TO dfems_user;

D) Run the SQL script as dfems_user:
   CONNECT dfems_user/dfems_pass@XE
   @path/to/sql/dfems_database.sql

   OR in SQL Developer:
   → File → Open → sql/dfems_database.sql → F5 (Run Script)

E) Verify (should see 15 tables, 5 views):
   SELECT table_name FROM user_tables;
   SELECT view_name  FROM user_views;

================================================================
STEP 2 — INTELLIJ PROJECT SETUP
================================================================

A) Install Java 17 or 21:
   https://adoptium.net

B) Download JavaFX SDK 21:
   https://gluonhq.com/products/javafx/
   Extract to a folder, e.g. C:\javafx-sdk-21\

C) Download Oracle JDBC Driver (ojdbc11.jar):
   https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html
   Place ojdbc11.jar in the lib/ folder of this project.

D) Open this project in IntelliJ IDEA:
   File → Open → select the DFEMS folder

E) Add Libraries:
   File → Project Structure → Libraries → "+" → Java
   Add ALL .jar files from:
     • javafx-sdk-21/lib/
     • lib/ojdbc11.jar

F) Configure Run:
   Run → Edit Configurations → Application
   Main class: com.dfems.MainApp
   VM Options:
     --module-path "C:/javafx-sdk-21/lib" --add-modules javafx.controls,javafx.fxml

   (Adjust path to where you extracted JavaFX SDK)

G) Set Sources Root:
   Right-click src/ → Mark Directory as → Sources Root
   Right-click resources/ → Mark Directory as → Resources Root

================================================================
STEP 3 — CONFIGURE DB CONNECTION
================================================================

Open: src/com/dfems/db/DBConnection.java
Change lines 8–10 to match your Oracle setup:

  private static final String URL      = "jdbc:oracle:thin:@localhost:1521:XE";
  private static final String USERNAME = "dfems_user";
  private static final String PASSWORD = "dfems_pass";

Common URL formats:
  Oracle XE (older):  jdbc:oracle:thin:@localhost:1521:XE
  Oracle XE (newer):  jdbc:oracle:thin:@localhost:1521/XEPDB1
  Oracle Standard:    jdbc:oracle:thin:@hostname:1521:ORCL

================================================================
PROJECT STRUCTURE
================================================================

DFEMS/
├── sql/
│   └── dfems_database.sql          ← Run this first
├── src/com/dfems/
│   ├── MainApp.java                ← Entry point
│   ├── db/
│   │   └── DBConnection.java       ← JDBC connection
│   ├── model/
│   │   ├── Case.java
│   │   ├── Evidence.java
│   │   ├── Analysis.java
│   │   └── User.java
│   ├── dao/
│   │   ├── CaseDAO.java
│   │   ├── EvidenceDAO.java
│   │   ├── AnalysisDAO.java
│   │   ├── UserDAO.java
│   │   ├── ReportDAO.java
│   │   └── (+ AuditController reads DB directly)
│   └── controller/
│       ├── LoginController.java
│       ├── DashboardController.java
│       ├── NewCaseController.java
│       ├── EvidenceController.java
│       ├── AnalysisController.java
│       ├── CaseDetailController.java
│       ├── ReportController.java
│       ├── AuditController.java
│       └── CustodyController.java
├── resources/
│   ├── fxml/
│   │   ├── LoginView.fxml
│   │   ├── DashboardView.fxml
│   │   ├── NewCaseView.fxml
│   │   ├── EvidenceView.fxml
│   │   ├── AnalysisView.fxml
│   │   ├── CaseDetailView.fxml
│   │   ├── ReportView.fxml
│   │   ├── AuditView.fxml
│   │   └── CustodyView.fxml
│   └── css/
│       └── dfems.css
└── lib/
    └── ojdbc11.jar                 ← Add manually (download separately)

================================================================
TEST LOGIN EMAILS (from sample data)
================================================================

  admin@dfems.gov          → ADMIN
  arjun.sharma@dfems.gov   → INVESTIGATOR
  priya.nair@dfems.gov     → FORENSIC_ANALYST
  rahul.singh@dfems.gov    → INVESTIGATOR
  meena.pillai@dfems.gov   → SUPERVISOR
  vikram.das@dfems.gov     → FORENSIC_ANALYST

================================================================
DATABASE SUMMARY (for VIVA)
================================================================

Tables    : 15
Sequences : 15
Triggers  : 19 (auto-PK + audit + guards)
Functions : 6
Procedures: 6
Package   : 1 (pkg_dfems with 6 members)
Views     : 5

================================================================
Project Report

The complete project report can be viewed here:

[📄 DFEMS Project Report](dfems_report.pdf)
