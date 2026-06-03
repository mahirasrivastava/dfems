package com.dfems.model;

import java.util.Date;

public class Analysis {
    private int    analysisId;
    private int    evidenceId;
    private String evidenceFileName;
    private int    analystId;
    private String analystName;
    private int    toolId;
    private String toolName;
    private String findings;
    private Date   analysisDate;
    private String analysisDateStr;   // formatted "YYYY-MM-DD" string for table display
    private String status;
    private String caseNumber;
    private int    caseId;

    public int    getAnalysisId()              { return analysisId; }
    public void   setAnalysisId(int v)         { this.analysisId = v; }
    public int    getEvidenceId()              { return evidenceId; }
    public void   setEvidenceId(int v)         { this.evidenceId = v; }
    public String getEvidenceFileName()        { return evidenceFileName; }
    public void   setEvidenceFileName(String v){ this.evidenceFileName = v; }
    public int    getAnalystId()               { return analystId; }
    public void   setAnalystId(int v)          { this.analystId = v; }
    public String getAnalystName()             { return analystName; }
    public void   setAnalystName(String v)     { this.analystName = v; }
    public int    getToolId()                  { return toolId; }
    public void   setToolId(int v)             { this.toolId = v; }
    public String getToolName()                { return toolName; }
    public void   setToolName(String v)        { this.toolName = v; }
    public String getFindings()                { return findings; }
    public void   setFindings(String v)        { this.findings = v; }
    public Date   getAnalysisDate()            { return analysisDate; }
    public void   setAnalysisDate(Date v)      { this.analysisDate = v; }
    public String getAnalysisDateStr()         { return analysisDateStr; }
    public void   setAnalysisDateStr(String v) { this.analysisDateStr = v; }
    public String getStatus()                  { return status; }
    public void   setStatus(String v)          { this.status = v; }
    public String getCaseNumber()              { return caseNumber; }
    public void   setCaseNumber(String v)      { this.caseNumber = v; }
    public int    getCaseId()                  { return caseId; }
    public void   setCaseId(int v)             { this.caseId = v; }
}
