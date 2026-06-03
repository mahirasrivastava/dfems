package com.dfems.model;

import java.util.Date;

public class Case {
    private int    caseId;
    private String caseNumber;
    private Date   caseDate;
    private String description;
    private int    caseTypeId;
    private String caseTypeName;
    private int    statusId;
    private String statusName;
    private String location;
    private String arrest;
    private String domestic;
    private int    createdBy;
    private String createdByName;
    private int    evidenceCount;

    public int    getCaseId()              { return caseId; }
    public void   setCaseId(int v)         { this.caseId = v; }
    public String getCaseNumber()          { return caseNumber; }
    public void   setCaseNumber(String v)  { this.caseNumber = v; }
    public Date   getCaseDate()            { return caseDate; }
    public void   setCaseDate(Date v)      { this.caseDate = v; }
    public String getDescription()         { return description; }
    public void   setDescription(String v) { this.description = v; }
    public int    getCaseTypeId()          { return caseTypeId; }
    public void   setCaseTypeId(int v)     { this.caseTypeId = v; }
    public String getCaseTypeName()        { return caseTypeName; }
    public void   setCaseTypeName(String v){ this.caseTypeName = v; }
    public int    getStatusId()            { return statusId; }
    public void   setStatusId(int v)       { this.statusId = v; }
    public String getStatusName()          { return statusName; }
    public void   setStatusName(String v)  { this.statusName = v; }
    public String getLocation()            { return location; }
    public void   setLocation(String v)    { this.location = v; }
    public String getArrest()              { return arrest; }
    public void   setArrest(String v)      { this.arrest = v; }
    public String getDomestic()            { return domestic; }
    public void   setDomestic(String v)    { this.domestic = v; }
    public int    getCreatedBy()           { return createdBy; }
    public void   setCreatedBy(int v)      { this.createdBy = v; }
    public String getCreatedByName()       { return createdByName; }
    public void   setCreatedByName(String v){ this.createdByName = v; }
    public int    getEvidenceCount()       { return evidenceCount; }
    public void   setEvidenceCount(int v)  { this.evidenceCount = v; }

    @Override public String toString() { return caseNumber + " [" + statusName + "]"; }
}
