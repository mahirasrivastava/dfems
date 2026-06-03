-- ============================================================
-- DIGITAL FORENSIC EVIDENCE MANAGEMENT SYSTEM (DFEMS)
-- Complete Oracle SQL + PL/SQL Script
-- ============================================================

-- Clean up if re-running
BEGIN
  FOR t IN (SELECT table_name FROM user_tables ORDER BY table_name) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS';
  END LOOP;
  FOR s IN (SELECT sequence_name FROM user_sequences) LOOP
    EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
  END LOOP;
END;
/

-- ============================================================
-- SECTION 1: CREATE TABLES
-- ============================================================

CREATE TABLE CASE_TYPE (
    case_type_id   NUMBER PRIMARY KEY,
    type_name      VARCHAR2(100) NOT NULL UNIQUE
);

CREATE TABLE CASE_STATUS (
    status_id    NUMBER PRIMARY KEY,
    status_name  VARCHAR2(50) NOT NULL UNIQUE
);

CREATE TABLE ROLE (
    role_id    NUMBER PRIMARY KEY,
    role_name  VARCHAR2(100) NOT NULL UNIQUE
);

CREATE TABLE DEPARTMENT (
    dept_id    NUMBER PRIMARY KEY,
    dept_name  VARCHAR2(100) NOT NULL UNIQUE
);

CREATE TABLE USERS (
    user_id   NUMBER PRIMARY KEY,
    name      VARCHAR2(100) NOT NULL,
    role_id   NUMBER NOT NULL,
    dept_id   NUMBER NOT NULL,
    email     VARCHAR2(150) UNIQUE NOT NULL,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES ROLE(role_id),
    CONSTRAINT fk_user_dept FOREIGN KEY (dept_id) REFERENCES DEPARTMENT(dept_id)
);

CREATE TABLE CASES (
    case_id       NUMBER PRIMARY KEY,
    case_number   VARCHAR2(50) NOT NULL UNIQUE,
    case_date     DATE NOT NULL,
    description   VARCHAR2(1000),
    case_type_id  NUMBER NOT NULL,
    status_id     NUMBER NOT NULL,
    location      VARCHAR2(200),
    arrest        CHAR(1) DEFAULT 'N' CHECK (arrest IN ('Y','N')),
    domestic      CHAR(1) DEFAULT 'N' CHECK (domestic IN ('Y','N')),
    created_by    NUMBER,
    created_date  DATE DEFAULT SYSDATE,
    CONSTRAINT fk_case_type   FOREIGN KEY (case_type_id) REFERENCES CASE_TYPE(case_type_id),
    CONSTRAINT fk_case_status FOREIGN KEY (status_id)    REFERENCES CASE_STATUS(status_id),
    CONSTRAINT fk_case_user   FOREIGN KEY (created_by)   REFERENCES USERS(user_id)
);

CREATE TABLE EVIDENCE_TYPE (
    type_id    NUMBER PRIMARY KEY,
    type_name  VARCHAR2(100) NOT NULL UNIQUE
);

CREATE TABLE STORAGE_LOCATION (
    storage_id     NUMBER PRIMARY KEY,
    location_name  VARCHAR2(200) NOT NULL,
    details        VARCHAR2(500)
);

CREATE TABLE EVIDENCE (
    evidence_id   NUMBER PRIMARY KEY,
    case_id       NUMBER NOT NULL,
    type_id       NUMBER NOT NULL,
    storage_id    NUMBER NOT NULL,
    file_name     VARCHAR2(300) NOT NULL,
    file_size     NUMBER,
    file_type     VARCHAR2(50),
    upload_date   DATE DEFAULT SYSDATE,
    hash_md5      VARCHAR2(64),
    hash_sha256   VARCHAR2(128),
    is_verified   CHAR(1) DEFAULT 'N' CHECK (is_verified IN ('Y','N')),
    CONSTRAINT fk_ev_case    FOREIGN KEY (case_id)    REFERENCES CASES(case_id),
    CONSTRAINT fk_ev_type    FOREIGN KEY (type_id)    REFERENCES EVIDENCE_TYPE(type_id),
    CONSTRAINT fk_ev_storage FOREIGN KEY (storage_id) REFERENCES STORAGE_LOCATION(storage_id)
);

CREATE TABLE ACTION_TYPE (
    action_id    NUMBER PRIMARY KEY,
    action_name  VARCHAR2(100) NOT NULL UNIQUE
);

CREATE TABLE CHAIN_OF_CUSTODY (
    custody_id   NUMBER PRIMARY KEY,
    evidence_id  NUMBER NOT NULL,
    user_id      NUMBER NOT NULL,
    action_id    NUMBER NOT NULL,
    timestamp    TIMESTAMP DEFAULT SYSTIMESTAMP,
    remarks      VARCHAR2(500),
    CONSTRAINT fk_coc_evidence FOREIGN KEY (evidence_id) REFERENCES EVIDENCE(evidence_id),
    CONSTRAINT fk_coc_user     FOREIGN KEY (user_id)     REFERENCES USERS(user_id),
    CONSTRAINT fk_coc_action   FOREIGN KEY (action_id)   REFERENCES ACTION_TYPE(action_id)
);

CREATE TABLE FORENSIC_TOOL (
    tool_id    NUMBER PRIMARY KEY,
    tool_name  VARCHAR2(150) NOT NULL,
    version    VARCHAR2(50)
);

CREATE TABLE ANALYSIS_RESULTS (
    analysis_id    NUMBER PRIMARY KEY,
    evidence_id    NUMBER NOT NULL,
    analyst_id     NUMBER NOT NULL,
    tool_id        NUMBER NOT NULL,
    findings       CLOB,
    analysis_date  DATE DEFAULT SYSDATE,
    status         VARCHAR2(50) DEFAULT 'PENDING'
                   CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETE','REVIEWED')),
    CONSTRAINT fk_ar_evidence FOREIGN KEY (evidence_id) REFERENCES EVIDENCE(evidence_id),
    CONSTRAINT fk_ar_analyst  FOREIGN KEY (analyst_id)  REFERENCES USERS(user_id),
    CONSTRAINT fk_ar_tool     FOREIGN KEY (tool_id)     REFERENCES FORENSIC_TOOL(tool_id)
);

CREATE TABLE FORENSIC_REPORT (
    report_id     NUMBER PRIMARY KEY,
    case_id       NUMBER NOT NULL,
    created_by    NUMBER NOT NULL,
    report_text   CLOB,
    created_date  DATE DEFAULT SYSDATE,
    report_type   VARCHAR2(50) DEFAULT 'PRELIMINARY'
                  CHECK (report_type IN ('PRELIMINARY','INTERIM','FINAL')),
    CONSTRAINT fk_fr_case FOREIGN KEY (case_id)    REFERENCES CASES(case_id),
    CONSTRAINT fk_fr_user FOREIGN KEY (created_by) REFERENCES USERS(user_id)
);

CREATE TABLE AUDIT_LOG (
    log_id     NUMBER PRIMARY KEY,
    user_id    NUMBER,
    action     VARCHAR2(200) NOT NULL,
    timestamp  TIMESTAMP DEFAULT SYSTIMESTAMP,
    details    VARCHAR2(1000),
    ip_address VARCHAR2(50),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES USERS(user_id)
);

-- ============================================================
-- SECTION 2: SEQUENCES
-- ============================================================

CREATE SEQUENCE seq_case_type    START WITH 1    INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_case_status  START WITH 1    INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_role         START WITH 1    INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_dept         START WITH 1    INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_users        START WITH 1    INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_cases        START WITH 1001 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_ev_type      START WITH 1    INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_storage      START WITH 1    INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_evidence     START WITH 5001 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_action_type  START WITH 1    INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_custody      START WITH 1    INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_tool         START WITH 1    INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_analysis     START WITH 1    INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_report       START WITH 1    INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_audit        START WITH 1    INCREMENT BY 1 NOCACHE;

-- ============================================================
-- SECTION 3: TRIGGERS
-- ============================================================

CREATE OR REPLACE TRIGGER trg_case_type_pk
BEFORE INSERT ON CASE_TYPE FOR EACH ROW
BEGIN IF :NEW.case_type_id IS NULL THEN SELECT seq_case_type.NEXTVAL INTO :NEW.case_type_id FROM DUAL; END IF; END;
/

CREATE OR REPLACE TRIGGER trg_case_status_pk
BEFORE INSERT ON CASE_STATUS FOR EACH ROW
BEGIN IF :NEW.status_id IS NULL THEN SELECT seq_case_status.NEXTVAL INTO :NEW.status_id FROM DUAL; END IF; END;
/

CREATE OR REPLACE TRIGGER trg_role_pk
BEFORE INSERT ON ROLE FOR EACH ROW
BEGIN IF :NEW.role_id IS NULL THEN SELECT seq_role.NEXTVAL INTO :NEW.role_id FROM DUAL; END IF; END;
/

CREATE OR REPLACE TRIGGER trg_dept_pk
BEFORE INSERT ON DEPARTMENT FOR EACH ROW
BEGIN IF :NEW.dept_id IS NULL THEN SELECT seq_dept.NEXTVAL INTO :NEW.dept_id FROM DUAL; END IF; END;
/

CREATE OR REPLACE TRIGGER trg_users_pk
BEFORE INSERT ON USERS FOR EACH ROW
BEGIN IF :NEW.user_id IS NULL THEN SELECT seq_users.NEXTVAL INTO :NEW.user_id FROM DUAL; END IF; END;
/

CREATE OR REPLACE TRIGGER trg_cases_pk
BEFORE INSERT ON CASES FOR EACH ROW
BEGIN
    IF :NEW.case_id IS NULL THEN SELECT seq_cases.NEXTVAL INTO :NEW.case_id FROM DUAL; END IF;
    IF :NEW.case_number IS NULL THEN
        :NEW.case_number := 'CASE-' || TO_CHAR(SYSDATE,'YYYY') || '-' || seq_cases.CURRVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_case_status_change
AFTER UPDATE OF status_id ON CASES FOR EACH ROW
BEGIN
    INSERT INTO AUDIT_LOG(log_id, user_id, action, timestamp, details)
    VALUES(seq_audit.NEXTVAL, :NEW.created_by, 'CASE_STATUS_CHANGED', SYSTIMESTAMP,
           'Case ' || :NEW.case_number || ': Status changed from ' || :OLD.status_id || ' to ' || :NEW.status_id);
END;
/

CREATE OR REPLACE TRIGGER trg_ev_type_pk
BEFORE INSERT ON EVIDENCE_TYPE FOR EACH ROW
BEGIN IF :NEW.type_id IS NULL THEN SELECT seq_ev_type.NEXTVAL INTO :NEW.type_id FROM DUAL; END IF; END;
/

CREATE OR REPLACE TRIGGER trg_storage_pk
BEFORE INSERT ON STORAGE_LOCATION FOR EACH ROW
BEGIN IF :NEW.storage_id IS NULL THEN SELECT seq_storage.NEXTVAL INTO :NEW.storage_id FROM DUAL; END IF; END;
/

CREATE OR REPLACE TRIGGER trg_evidence_pk
BEFORE INSERT ON EVIDENCE FOR EACH ROW
BEGIN IF :NEW.evidence_id IS NULL THEN SELECT seq_evidence.NEXTVAL INTO :NEW.evidence_id FROM DUAL; END IF; END;
/

CREATE OR REPLACE TRIGGER trg_evidence_initial_custody
AFTER INSERT ON EVIDENCE FOR EACH ROW
DECLARE v_upload_action NUMBER;
BEGIN
    SELECT action_id INTO v_upload_action FROM ACTION_TYPE WHERE action_name = 'UPLOADED' AND ROWNUM = 1;
    INSERT INTO CHAIN_OF_CUSTODY(custody_id, evidence_id, user_id, action_id, timestamp, remarks)
    VALUES(seq_custody.NEXTVAL, :NEW.evidence_id, 1, v_upload_action, SYSTIMESTAMP, 'Initial upload: ' || :NEW.file_name);
EXCEPTION WHEN NO_DATA_FOUND THEN NULL;
END;
/

CREATE OR REPLACE TRIGGER trg_action_type_pk
BEFORE INSERT ON ACTION_TYPE FOR EACH ROW
BEGIN IF :NEW.action_id IS NULL THEN SELECT seq_action_type.NEXTVAL INTO :NEW.action_id FROM DUAL; END IF; END;
/

CREATE OR REPLACE TRIGGER trg_custody_pk
BEFORE INSERT ON CHAIN_OF_CUSTODY FOR EACH ROW
BEGIN IF :NEW.custody_id IS NULL THEN SELECT seq_custody.NEXTVAL INTO :NEW.custody_id FROM DUAL; END IF; END;
/

CREATE OR REPLACE TRIGGER trg_tool_pk
BEFORE INSERT ON FORENSIC_TOOL FOR EACH ROW
BEGIN IF :NEW.tool_id IS NULL THEN SELECT seq_tool.NEXTVAL INTO :NEW.tool_id FROM DUAL; END IF; END;
/

CREATE OR REPLACE TRIGGER trg_analysis_pk
BEFORE INSERT ON ANALYSIS_RESULTS FOR EACH ROW
BEGIN IF :NEW.analysis_id IS NULL THEN SELECT seq_analysis.NEXTVAL INTO :NEW.analysis_id FROM DUAL; END IF; END;
/

CREATE OR REPLACE TRIGGER trg_analysis_complete
AFTER UPDATE OF status ON ANALYSIS_RESULTS FOR EACH ROW
BEGIN
    IF :NEW.status = 'COMPLETE' AND :OLD.status != 'COMPLETE' THEN
        INSERT INTO AUDIT_LOG(log_id, user_id, action, timestamp, details)
        VALUES(seq_audit.NEXTVAL, :NEW.analyst_id, 'ANALYSIS_COMPLETED', SYSTIMESTAMP,
               'Analysis ID ' || :NEW.analysis_id || ' completed for Evidence ID ' || :NEW.evidence_id);
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_report_pk
BEFORE INSERT ON FORENSIC_REPORT FOR EACH ROW
BEGIN IF :NEW.report_id IS NULL THEN SELECT seq_report.NEXTVAL INTO :NEW.report_id FROM DUAL; END IF; END;
/

CREATE OR REPLACE TRIGGER trg_audit_pk
BEFORE INSERT ON AUDIT_LOG FOR EACH ROW
BEGIN IF :NEW.log_id IS NULL THEN SELECT seq_audit.NEXTVAL INTO :NEW.log_id FROM DUAL; END IF; END;
/

CREATE OR REPLACE TRIGGER trg_protect_evidence
BEFORE DELETE ON EVIDENCE FOR EACH ROW
DECLARE v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM ANALYSIS_RESULTS
    WHERE evidence_id = :OLD.evidence_id AND status IN ('PENDING','IN_PROGRESS');
    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Cannot delete evidence with active analysis in progress.');
    END IF;
END;
/

-- ============================================================
-- SECTION 4: FUNCTIONS
-- ============================================================

CREATE OR REPLACE FUNCTION fn_evidence_count(p_case_id IN NUMBER) RETURN NUMBER IS
    v_count NUMBER;
BEGIN SELECT COUNT(*) INTO v_count FROM EVIDENCE WHERE case_id = p_case_id; RETURN v_count; END;
/

CREATE OR REPLACE FUNCTION fn_get_case_status(p_case_id IN NUMBER) RETURN VARCHAR2 IS
    v_status VARCHAR2(50);
BEGIN
    SELECT cs.status_name INTO v_status FROM CASES c JOIN CASE_STATUS cs ON c.status_id = cs.status_id WHERE c.case_id = p_case_id;
    RETURN v_status;
EXCEPTION WHEN NO_DATA_FOUND THEN RETURN 'NOT FOUND';
END;
/

CREATE OR REPLACE FUNCTION fn_format_file_size(p_bytes IN NUMBER) RETURN VARCHAR2 IS
BEGIN
    IF p_bytes IS NULL THEN RETURN 'Unknown';
    ELSIF p_bytes < 1024 THEN RETURN p_bytes || ' B';
    ELSIF p_bytes < 1048576 THEN RETURN ROUND(p_bytes/1024,1) || ' KB';
    ELSIF p_bytes < 1073741824 THEN RETURN ROUND(p_bytes/1048576,1) || ' MB';
    ELSE RETURN ROUND(p_bytes/1073741824,2) || ' GB';
    END IF;
END;
/

CREATE OR REPLACE FUNCTION fn_validate_email(p_email IN VARCHAR2) RETURN NUMBER IS
BEGIN
    IF REGEXP_LIKE(p_email, '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$') THEN RETURN 1; ELSE RETURN 0; END IF;
END;
/

CREATE OR REPLACE FUNCTION fn_coc_count(p_evidence_id IN NUMBER) RETURN NUMBER IS
    v_count NUMBER;
BEGIN SELECT COUNT(*) INTO v_count FROM CHAIN_OF_CUSTODY WHERE evidence_id = p_evidence_id; RETURN v_count; END;
/

CREATE OR REPLACE FUNCTION fn_user_can_access(p_user_id IN NUMBER, p_case_id IN NUMBER) RETURN NUMBER IS
    v_role VARCHAR2(100);
BEGIN
    SELECT r.role_name INTO v_role FROM USERS u JOIN ROLE r ON u.role_id = r.role_id WHERE u.user_id = p_user_id;
    IF v_role IN ('ADMIN','INVESTIGATOR','FORENSIC_ANALYST') THEN RETURN 1;
    ELSIF v_role = 'VIEWER' THEN
        DECLARE v_status VARCHAR2(50); BEGIN
            v_status := fn_get_case_status(p_case_id);
            IF v_status = 'CLOSED' THEN RETURN 1; ELSE RETURN 0; END IF;
        END;
    ELSE RETURN 0;
    END IF;
EXCEPTION WHEN NO_DATA_FOUND THEN RETURN 0;
END;
/

-- ============================================================
-- SECTION 5: STORED PROCEDURES
-- ============================================================

CREATE OR REPLACE PROCEDURE sp_register_case(
    p_case_number IN VARCHAR2, p_case_date IN DATE, p_description IN VARCHAR2,
    p_type_id IN NUMBER, p_location IN VARCHAR2, p_arrest IN CHAR DEFAULT 'N',
    p_domestic IN CHAR DEFAULT 'N', p_created_by IN NUMBER, p_case_id OUT NUMBER
) IS v_status_id NUMBER;
BEGIN
    SELECT status_id INTO v_status_id FROM CASE_STATUS WHERE status_name = 'OPEN';
    INSERT INTO CASES(case_number,case_date,description,case_type_id,status_id,location,arrest,domestic,created_by)
    VALUES(p_case_number,p_case_date,p_description,p_type_id,v_status_id,p_location,p_arrest,p_domestic,p_created_by)
    RETURNING case_id INTO p_case_id;
    INSERT INTO AUDIT_LOG(user_id,action,timestamp,details)
    VALUES(p_created_by,'CASE_CREATED',SYSTIMESTAMP,'New case registered: '||p_case_number);
    COMMIT;
EXCEPTION WHEN OTHERS THEN ROLLBACK; RAISE;
END;
/

CREATE OR REPLACE PROCEDURE sp_upload_evidence(
    p_case_id IN NUMBER, p_type_id IN NUMBER, p_storage_id IN NUMBER,
    p_file_name IN VARCHAR2, p_file_size IN NUMBER, p_file_type IN VARCHAR2,
    p_hash_md5 IN VARCHAR2, p_uploaded_by IN NUMBER, p_evidence_id OUT NUMBER
) IS
BEGIN
    INSERT INTO EVIDENCE(case_id,type_id,storage_id,file_name,file_size,file_type,hash_md5,upload_date)
    VALUES(p_case_id,p_type_id,p_storage_id,p_file_name,p_file_size,p_file_type,p_hash_md5,SYSDATE)
    RETURNING evidence_id INTO p_evidence_id;
    INSERT INTO AUDIT_LOG(user_id,action,timestamp,details)
    VALUES(p_uploaded_by,'EVIDENCE_UPLOADED',SYSTIMESTAMP,'Evidence: '||p_file_name||' for case: '||p_case_id);
    COMMIT;
EXCEPTION WHEN OTHERS THEN ROLLBACK; RAISE;
END;
/

CREATE OR REPLACE PROCEDURE sp_transfer_custody(
    p_evidence_id IN NUMBER, p_user_id IN NUMBER, p_action_name IN VARCHAR2, p_remarks IN VARCHAR2
) IS v_action_id NUMBER;
BEGIN
    SELECT action_id INTO v_action_id FROM ACTION_TYPE WHERE action_name = p_action_name;
    INSERT INTO CHAIN_OF_CUSTODY(evidence_id,user_id,action_id,timestamp,remarks)
    VALUES(p_evidence_id,p_user_id,v_action_id,SYSTIMESTAMP,p_remarks);
    INSERT INTO AUDIT_LOG(user_id,action,timestamp,details)
    VALUES(p_user_id,'CUSTODY_TRANSFERRED',SYSTIMESTAMP,'Evidence '||p_evidence_id||' action: '||p_action_name);
    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN RAISE_APPLICATION_ERROR(-20002,'Invalid action type: '||p_action_name);
    WHEN OTHERS THEN ROLLBACK; RAISE;
END;
/

CREATE OR REPLACE PROCEDURE sp_create_analysis(
    p_evidence_id IN NUMBER, p_analyst_id IN NUMBER, p_tool_id IN NUMBER,
    p_findings IN CLOB, p_analysis_id OUT NUMBER
) IS
BEGIN
    INSERT INTO ANALYSIS_RESULTS(evidence_id,analyst_id,tool_id,findings,analysis_date,status)
    VALUES(p_evidence_id,p_analyst_id,p_tool_id,p_findings,SYSDATE,'COMPLETE')
    RETURNING analysis_id INTO p_analysis_id;
    INSERT INTO AUDIT_LOG(user_id,action,timestamp,details)
    VALUES(p_analyst_id,'ANALYSIS_CREATED',SYSTIMESTAMP,'Analysis for evidence: '||p_evidence_id);
    COMMIT;
EXCEPTION WHEN OTHERS THEN ROLLBACK; RAISE;
END;
/

CREATE OR REPLACE PROCEDURE sp_generate_report(
    p_case_id IN NUMBER, p_created_by IN NUMBER, p_type IN VARCHAR2 DEFAULT 'FINAL', p_report_id OUT NUMBER
) IS
    v_report_text CLOB; v_case_num VARCHAR2(50); v_ev_count NUMBER; v_analyst_count NUMBER;
BEGIN
    SELECT case_number INTO v_case_num FROM CASES WHERE case_id = p_case_id;
    v_ev_count := fn_evidence_count(p_case_id);
    SELECT COUNT(DISTINCT ar.analyst_id) INTO v_analyst_count
    FROM ANALYSIS_RESULTS ar JOIN EVIDENCE e ON ar.evidence_id = e.evidence_id WHERE e.case_id = p_case_id;

    v_report_text := 'FORENSIC REPORT - '||p_type||CHR(10)
        ||'Case Number  : '||v_case_num||CHR(10)
        ||'Report Date  : '||TO_CHAR(SYSDATE,'YYYY-MM-DD HH24:MI')||CHR(10)
        ||'Total Evidence: '||v_ev_count||CHR(10)
        ||'Analysts: '||v_analyst_count||CHR(10)
        ||'Status: '||fn_get_case_status(p_case_id)||CHR(10)||CHR(10)||'-- Findings --'||CHR(10);

    FOR rec IN (SELECT ar.findings,u.name AS analyst,ft.tool_name,ar.analysis_date
                FROM ANALYSIS_RESULTS ar JOIN USERS u ON ar.analyst_id=u.user_id
                JOIN FORENSIC_TOOL ft ON ar.tool_id=ft.tool_id
                JOIN EVIDENCE e ON ar.evidence_id=e.evidence_id WHERE e.case_id=p_case_id) LOOP
        v_report_text := v_report_text||'Analyst: '||rec.analyst||' | Tool: '||rec.tool_name||CHR(10)||'Findings: '||rec.findings||CHR(10)||CHR(10);
    END LOOP;

    INSERT INTO FORENSIC_REPORT(case_id,created_by,report_text,report_type)
    VALUES(p_case_id,p_created_by,v_report_text,p_type) RETURNING report_id INTO p_report_id;
    INSERT INTO AUDIT_LOG(user_id,action,timestamp,details)
    VALUES(p_created_by,'REPORT_GENERATED',SYSTIMESTAMP,'Report '||p_report_id||' for case '||v_case_num);
    COMMIT;
EXCEPTION WHEN OTHERS THEN ROLLBACK; RAISE;
END;
/

CREATE OR REPLACE PROCEDURE sp_close_case(p_case_id IN NUMBER, p_closed_by IN NUMBER) IS
    v_status_id NUMBER;
BEGIN
    SELECT status_id INTO v_status_id FROM CASE_STATUS WHERE status_name = 'CLOSED';
    UPDATE CASES SET status_id = v_status_id WHERE case_id = p_case_id;
    INSERT INTO AUDIT_LOG(user_id,action,timestamp,details)
    VALUES(p_closed_by,'CASE_CLOSED',SYSTIMESTAMP,'Case ID '||p_case_id||' closed.');
    COMMIT;
END;
/

-- ============================================================
-- SECTION 6: PACKAGE
-- ============================================================

CREATE OR REPLACE PACKAGE pkg_dfems AS
    c_max_file_size CONSTANT NUMBER := 5368709120;
    PROCEDURE log_user_action(p_user_id IN NUMBER, p_action IN VARCHAR2, p_details IN VARCHAR2);
    PROCEDURE verify_evidence(p_evidence_id IN NUMBER, p_verified_by IN NUMBER);
    PROCEDURE get_case_summary(p_case_id IN NUMBER, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE search_cases(p_keyword IN VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    FUNCTION  get_user_name(p_user_id IN NUMBER) RETURN VARCHAR2;
    FUNCTION  case_exists(p_case_number IN VARCHAR2) RETURN NUMBER;
END pkg_dfems;
/

CREATE OR REPLACE PACKAGE BODY pkg_dfems AS
    PROCEDURE log_user_action(p_user_id IN NUMBER, p_action IN VARCHAR2, p_details IN VARCHAR2) IS
    BEGIN INSERT INTO AUDIT_LOG(user_id,action,timestamp,details) VALUES(p_user_id,p_action,SYSTIMESTAMP,p_details); COMMIT; END;

    PROCEDURE verify_evidence(p_evidence_id IN NUMBER, p_verified_by IN NUMBER) IS
    BEGIN UPDATE EVIDENCE SET is_verified='Y' WHERE evidence_id=p_evidence_id;
          log_user_action(p_verified_by,'EVIDENCE_VERIFIED','Evidence ID: '||p_evidence_id); END;

    PROCEDURE get_case_summary(p_case_id IN NUMBER, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN OPEN p_cursor FOR
        SELECT c.case_id,c.case_number,c.case_date,c.description,ct.type_name,cs.status_name,
               c.location,c.arrest,c.domestic,fn_evidence_count(c.case_id) AS evidence_count
        FROM CASES c JOIN CASE_TYPE ct ON c.case_type_id=ct.case_type_id
        JOIN CASE_STATUS cs ON c.status_id=cs.status_id WHERE c.case_id=p_case_id; END;

    PROCEDURE search_cases(p_keyword IN VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN OPEN p_cursor FOR
        SELECT c.case_id,c.case_number,c.case_date,cs.status_name,ct.type_name
        FROM CASES c JOIN CASE_TYPE ct ON c.case_type_id=ct.case_type_id
        JOIN CASE_STATUS cs ON c.status_id=cs.status_id
        WHERE UPPER(c.case_number) LIKE '%'||UPPER(p_keyword)||'%'
           OR UPPER(c.description) LIKE '%'||UPPER(p_keyword)||'%'
           OR UPPER(c.location)    LIKE '%'||UPPER(p_keyword)||'%'
        ORDER BY c.case_date DESC; END;

    FUNCTION get_user_name(p_user_id IN NUMBER) RETURN VARCHAR2 IS v_name VARCHAR2(100);
    BEGIN SELECT name INTO v_name FROM USERS WHERE user_id=p_user_id; RETURN v_name;
    EXCEPTION WHEN NO_DATA_FOUND THEN RETURN 'Unknown'; END;

    FUNCTION case_exists(p_case_number IN VARCHAR2) RETURN NUMBER IS v_cnt NUMBER;
    BEGIN SELECT COUNT(*) INTO v_cnt FROM CASES WHERE case_number=p_case_number; RETURN v_cnt; END;
END pkg_dfems;
/

-- ============================================================
-- SECTION 7: VIEWS
-- ============================================================

CREATE OR REPLACE VIEW vw_case_details AS
SELECT c.case_id,c.case_number,c.case_date,c.description,ct.type_name AS case_type,
       cs.status_name AS case_status,c.location,c.arrest,c.domestic,u.name AS created_by,
       fn_evidence_count(c.case_id) AS evidence_count
FROM CASES c JOIN CASE_TYPE ct ON c.case_type_id=ct.case_type_id
JOIN CASE_STATUS cs ON c.status_id=cs.status_id LEFT JOIN USERS u ON c.created_by=u.user_id;

CREATE OR REPLACE VIEW vw_evidence_details AS
SELECT e.evidence_id,e.file_name,e.file_type,fn_format_file_size(e.file_size) AS file_size,
       e.upload_date,e.is_verified,e.hash_md5,et.type_name AS evidence_type,
       sl.location_name AS storage_location,c.case_number,c.case_id
FROM EVIDENCE e JOIN EVIDENCE_TYPE et ON e.type_id=et.type_id
JOIN STORAGE_LOCATION sl ON e.storage_id=sl.storage_id JOIN CASES c ON e.case_id=c.case_id;

CREATE OR REPLACE VIEW vw_chain_of_custody AS
SELECT coc.custody_id,coc.timestamp,coc.remarks,e.file_name AS evidence_file,
       u.name AS handled_by,at.action_name AS action,c.case_number
FROM CHAIN_OF_CUSTODY coc JOIN EVIDENCE e ON coc.evidence_id=e.evidence_id
JOIN USERS u ON coc.user_id=u.user_id JOIN ACTION_TYPE at ON coc.action_id=at.action_id
JOIN CASES c ON e.case_id=c.case_id ORDER BY coc.timestamp;

CREATE OR REPLACE VIEW vw_analysis_summary AS
SELECT ar.analysis_id,ar.analysis_date,ar.status,ar.findings,u.name AS analyst_name,
       ft.tool_name,ft.version AS tool_version,e.file_name AS evidence_file,c.case_number,
       e.evidence_id,c.case_id
FROM ANALYSIS_RESULTS ar JOIN USERS u ON ar.analyst_id=u.user_id
JOIN FORENSIC_TOOL ft ON ar.tool_id=ft.tool_id
JOIN EVIDENCE e ON ar.evidence_id=e.evidence_id JOIN CASES c ON e.case_id=c.case_id;

CREATE OR REPLACE VIEW vw_user_audit AS
SELECT al.log_id,al.action,al.timestamp,al.details,u.name AS user_name,r.role_name,d.dept_name
FROM AUDIT_LOG al LEFT JOIN USERS u ON al.user_id=u.user_id
LEFT JOIN ROLE r ON u.role_id=r.role_id LEFT JOIN DEPARTMENT d ON u.dept_id=d.dept_id
ORDER BY al.timestamp DESC;

-- ============================================================
-- SECTION 8: SAMPLE DATA
-- ============================================================

INSERT INTO CASE_TYPE(type_name) VALUES('Cybercrime');
INSERT INTO CASE_TYPE(type_name) VALUES('Financial Fraud');
INSERT INTO CASE_TYPE(type_name) VALUES('Identity Theft');
INSERT INTO CASE_TYPE(type_name) VALUES('Data Breach');
INSERT INTO CASE_TYPE(type_name) VALUES('Homicide');
INSERT INTO CASE_TYPE(type_name) VALUES('Theft');
INSERT INTO CASE_TYPE(type_name) VALUES('Drug Trafficking');

INSERT INTO CASE_STATUS(status_name) VALUES('OPEN');
INSERT INTO CASE_STATUS(status_name) VALUES('UNDER_INVESTIGATION');
INSERT INTO CASE_STATUS(status_name) VALUES('CLOSED');
INSERT INTO CASE_STATUS(status_name) VALUES('ARCHIVED');
INSERT INTO CASE_STATUS(status_name) VALUES('PENDING_REVIEW');

INSERT INTO ROLE(role_name) VALUES('ADMIN');
INSERT INTO ROLE(role_name) VALUES('INVESTIGATOR');
INSERT INTO ROLE(role_name) VALUES('FORENSIC_ANALYST');
INSERT INTO ROLE(role_name) VALUES('SUPERVISOR');
INSERT INTO ROLE(role_name) VALUES('VIEWER');

INSERT INTO DEPARTMENT(dept_name) VALUES('Cybercrime Unit');
INSERT INTO DEPARTMENT(dept_name) VALUES('Homicide Division');
INSERT INTO DEPARTMENT(dept_name) VALUES('Forensic Lab');
INSERT INTO DEPARTMENT(dept_name) VALUES('Administration');
INSERT INTO DEPARTMENT(dept_name) VALUES('Intelligence Wing');

INSERT INTO USERS(name,role_id,dept_id,email) VALUES('Admin System',  1,4,'admin@dfems.gov');
INSERT INTO USERS(name,role_id,dept_id,email) VALUES('Arjun Sharma',  2,1,'arjun.sharma@dfems.gov');
INSERT INTO USERS(name,role_id,dept_id,email) VALUES('Priya Nair',    3,3,'priya.nair@dfems.gov');
INSERT INTO USERS(name,role_id,dept_id,email) VALUES('Rahul Singh',   2,2,'rahul.singh@dfems.gov');
INSERT INTO USERS(name,role_id,dept_id,email) VALUES('Meena Pillai',  4,4,'meena.pillai@dfems.gov');
INSERT INTO USERS(name,role_id,dept_id,email) VALUES('Vikram Das',    3,3,'vikram.das@dfems.gov');
INSERT INTO USERS(name,role_id,dept_id,email) VALUES('Sunita Rao',    5,5,'sunita.rao@dfems.gov');
INSERT INTO USERS(name,role_id,dept_id,email) VALUES('Deepak Kumar',  2,1,'deepak.kumar@dfems.gov');

INSERT INTO EVIDENCE_TYPE(type_name) VALUES('Image');
INSERT INTO EVIDENCE_TYPE(type_name) VALUES('Video');
INSERT INTO EVIDENCE_TYPE(type_name) VALUES('Audio');
INSERT INTO EVIDENCE_TYPE(type_name) VALUES('Document');
INSERT INTO EVIDENCE_TYPE(type_name) VALUES('Email');
INSERT INTO EVIDENCE_TYPE(type_name) VALUES('Database Dump');
INSERT INTO EVIDENCE_TYPE(type_name) VALUES('Network Capture');
INSERT INTO EVIDENCE_TYPE(type_name) VALUES('Memory Dump');

INSERT INTO STORAGE_LOCATION(location_name,details) VALUES('Secure Server Room A','RAID-5 encrypted, Climate controlled');
INSERT INTO STORAGE_LOCATION(location_name,details) VALUES('Cloud Storage - AWS S3','AES-256 encrypted, us-east-1');
INSERT INTO STORAGE_LOCATION(location_name,details) VALUES('Forensic Lab NAS','Isolated network, write-protected');
INSERT INTO STORAGE_LOCATION(location_name,details) VALUES('Physical Evidence Locker 3','USB drives and physical media');
INSERT INTO STORAGE_LOCATION(location_name,details) VALUES('Offsite Backup Center','Disaster recovery location');

INSERT INTO ACTION_TYPE(action_name) VALUES('UPLOADED');
INSERT INTO ACTION_TYPE(action_name) VALUES('ACCESSED');
INSERT INTO ACTION_TYPE(action_name) VALUES('TRANSFERRED');
INSERT INTO ACTION_TYPE(action_name) VALUES('ANALYZED');
INSERT INTO ACTION_TYPE(action_name) VALUES('COPIED');
INSERT INTO ACTION_TYPE(action_name) VALUES('ARCHIVED');
INSERT INTO ACTION_TYPE(action_name) VALUES('RETURNED');
INSERT INTO ACTION_TYPE(action_name) VALUES('VERIFIED');

INSERT INTO FORENSIC_TOOL(tool_name,version) VALUES('Autopsy','4.21');
INSERT INTO FORENSIC_TOOL(tool_name,version) VALUES('Volatility','3.0.1');
INSERT INTO FORENSIC_TOOL(tool_name,version) VALUES('Wireshark','4.2');
INSERT INTO FORENSIC_TOOL(tool_name,version) VALUES('FTK Imager','4.7');
INSERT INTO FORENSIC_TOOL(tool_name,version) VALUES('Cellebrite UFED','7.67');
INSERT INTO FORENSIC_TOOL(tool_name,version) VALUES('Magnet AXIOM','7.3');
INSERT INTO FORENSIC_TOOL(tool_name,version) VALUES('Sleuth Kit','4.12');
COMMIT;

INSERT INTO CASES(case_number,case_date,description,case_type_id,status_id,location,arrest,domestic,created_by)
VALUES('CASE-2024-001',DATE '2024-01-15','Unauthorized access to banking system via SQL injection. 45,000 customer PII records exfiltrated.',1,2,'Mangalore, Karnataka','N','N',2);
INSERT INTO CASES(case_number,case_date,description,case_type_id,status_id,location,arrest,domestic,created_by)
VALUES('CASE-2024-002',DATE '2024-02-20','Employee stole confidential financial records and sold to competitor.',2,1,'Bangalore, Karnataka','Y','N',4);
INSERT INTO CASES(case_number,case_date,description,case_type_id,status_id,location,arrest,domestic,created_by)
VALUES('CASE-2024-003',DATE '2024-03-05','Ransomware attack on hospital network. Patient data encrypted.',4,2,'Udupi, Karnataka','N','N',2);
INSERT INTO CASES(case_number,case_date,description,case_type_id,status_id,location,arrest,domestic,created_by)
VALUES('CASE-2024-004',DATE '2024-04-10','Suspect created fake profiles to defraud victims online.',3,1,'Manipal, Karnataka','Y','N',8);
INSERT INTO CASES(case_number,case_date,description,case_type_id,status_id,location,arrest,domestic,created_by)
VALUES('CASE-2023-015',DATE '2023-11-01','Drug trafficking network used encrypted messaging apps.',7,3,'Hubli, Karnataka','Y','N',4);
COMMIT;

INSERT INTO EVIDENCE(case_id,type_id,storage_id,file_name,file_size,file_type,hash_md5,upload_date)
VALUES(1001,6,1,'db_dump_banking_jan2024.sql',52428800,'SQL','a1b2c3d4e5f6789012345678901234ab',DATE '2024-01-16');
INSERT INTO EVIDENCE(case_id,type_id,storage_id,file_name,file_size,file_type,hash_md5,upload_date)
VALUES(1001,7,3,'network_capture_jan15.pcap',10485760,'PCAP','b2c3d4e5f678901234567890abcd1234',DATE '2024-01-16');
INSERT INTO EVIDENCE(case_id,type_id,storage_id,file_name,file_size,file_type,hash_md5,upload_date)
VALUES(1002,4,1,'stolen_records_spreadsheet.xlsx',2097152,'XLSX','c3d4e5f67890123456789abcde234567',DATE '2024-02-21');
INSERT INTO EVIDENCE(case_id,type_id,storage_id,file_name,file_size,file_type,hash_md5,upload_date)
VALUES(1002,5,2,'email_thread_export.eml',1048576,'EML','d4e5f6789012345678abcdef3456789a',DATE '2024-02-21');
INSERT INTO EVIDENCE(case_id,type_id,storage_id,file_name,file_size,file_type,hash_md5,upload_date)
VALUES(1003,8,3,'server_memory_dump.raw',8589934592,'RAW','e5f67890123456789abcdef45678901b',DATE '2024-03-06');
INSERT INTO EVIDENCE(case_id,type_id,storage_id,file_name,file_size,file_type,hash_md5,upload_date)
VALUES(1004,1,4,'suspect_phone_screenshot.jpg',524288,'JPEG','f6789012345678abcdef567890123456',DATE '2024-04-11');
INSERT INTO EVIDENCE(case_id,type_id,storage_id,file_name,file_size,file_type,hash_md5,upload_date)
VALUES(1005,3,3,'phone_call_recording.mp3',15728640,'MP3','67890abcdef12345678901234abcdef7',DATE '2023-11-02');
COMMIT;

INSERT INTO CHAIN_OF_CUSTODY(evidence_id,user_id,action_id,timestamp,remarks)
VALUES(5001,2,1,TIMESTAMP '2024-01-16 09:00:00','Initial evidence upload from crime scene');
INSERT INTO CHAIN_OF_CUSTODY(evidence_id,user_id,action_id,timestamp,remarks)
VALUES(5001,3,2,TIMESTAMP '2024-01-17 10:30:00','Accessed for forensic analysis');
INSERT INTO CHAIN_OF_CUSTODY(evidence_id,user_id,action_id,timestamp,remarks)
VALUES(5001,3,4,TIMESTAMP '2024-01-18 14:00:00','Completed initial analysis');
INSERT INTO CHAIN_OF_CUSTODY(evidence_id,user_id,action_id,timestamp,remarks)
VALUES(5002,2,1,TIMESTAMP '2024-01-16 09:15:00','Network capture uploaded');
INSERT INTO CHAIN_OF_CUSTODY(evidence_id,user_id,action_id,timestamp,remarks)
VALUES(5002,6,4,TIMESTAMP '2024-01-19 11:00:00','Analyzed with Wireshark');
INSERT INTO CHAIN_OF_CUSTODY(evidence_id,user_id,action_id,timestamp,remarks)
VALUES(5003,4,1,TIMESTAMP '2024-02-21 08:00:00','Confiscated from suspect laptop');
INSERT INTO CHAIN_OF_CUSTODY(evidence_id,user_id,action_id,timestamp,remarks)
VALUES(5004,4,3,TIMESTAMP '2024-02-22 09:00:00','Transferred to forensic lab');
COMMIT;

INSERT INTO ANALYSIS_RESULTS(evidence_id,analyst_id,tool_id,findings,analysis_date,status)
VALUES(5001,3,1,'Database dump shows 45,000 customer records extracted. SQL injection pattern identified. Attacker IP traced to VPN exit node in Netherlands. Timestamp: 2024-01-15 02:34:17 UTC.',DATE '2024-01-18','COMPLETE');
INSERT INTO ANALYSIS_RESULTS(evidence_id,analyst_id,tool_id,findings,analysis_date,status)
VALUES(5002,6,3,'Network capture shows 3 suspicious connections to C2 server (45.132.x.x). Data exfiltration via HTTPS port 443. Total data transferred: ~48MB.',DATE '2024-01-19','COMPLETE');
INSERT INTO ANALYSIS_RESULTS(evidence_id,analyst_id,tool_id,findings,analysis_date,status)
VALUES(5003,3,4,'Excel file contains 12,000 rows of CONFIDENTIAL employee data. Metadata: last modified by john.doe at 2024-02-18 23:45. Emailed to competitor 3 hours later.',DATE '2024-02-25','REVIEWED');
INSERT INTO ANALYSIS_RESULTS(evidence_id,analyst_id,tool_id,findings,analysis_date,status)
VALUES(5005,6,2,'Memory dump reveals active ransomware: RansomEXX v2.1. AES-256 encryption key found in memory. 847 files encrypted. Decryption key extracted successfully.',DATE '2024-03-08','COMPLETE');
INSERT INTO ANALYSIS_RESULTS(evidence_id,analyst_id,tool_id,findings,analysis_date,status)
VALUES(5007,3,5,'Audio recording confirms drug shipment details. Speakers identified via voiceprint. Delivery location: Warehouse 7, Industrial Area.',DATE '2023-11-05','REVIEWED');
COMMIT;

INSERT INTO FORENSIC_REPORT(case_id,created_by,report_text,created_date,report_type)
VALUES(1001,5,'PRELIMINARY REPORT - CASE-2024-001'||CHR(10)||'Banking system breach confirmed via SQL injection.'||CHR(10)||'Evidence: 2 artifacts. Suspect IP identified. Recommend immediate patching.',DATE '2024-01-25','PRELIMINARY');
INSERT INTO FORENSIC_REPORT(case_id,created_by,report_text,created_date,report_type)
VALUES(1002,5,'FINAL REPORT - CASE-2024-002'||CHR(10)||'Employee data theft confirmed. Suspect: Finance Dept.'||CHR(10)||'Evidence handed to prosecutor. Case closed.',DATE '2024-03-15','FINAL');

INSERT INTO AUDIT_LOG(user_id,action,timestamp,details) VALUES(1,'SYSTEM_START',TIMESTAMP '2024-01-01 00:00:00','DFEMS initialized');
INSERT INTO AUDIT_LOG(user_id,action,timestamp,details) VALUES(2,'USER_LOGIN',TIMESTAMP '2024-01-15 08:30:00','Arjun Sharma logged in');
INSERT INTO AUDIT_LOG(user_id,action,timestamp,details) VALUES(3,'USER_LOGIN',TIMESTAMP '2024-01-17 10:00:00','Priya Nair logged in');
COMMIT;
