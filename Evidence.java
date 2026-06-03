package com.dfems.model;

import java.util.Date;

public class Evidence {
    private int    evidenceId;
    private int    caseId;
    private String caseNumber;
    private int    typeId;
    private String evidenceTypeName;
    private int    storageId;
    private String storageLocation;
    private String fileName;
    private long   fileSize;
    private String fileSizeFormatted;
    private String fileType;
    private String hashMd5;
    private Date   uploadDate;
    private boolean verified;
    private int    uploadedBy;

    public int     getEvidenceId()              { return evidenceId; }
    public void    setEvidenceId(int v)         { this.evidenceId = v; }
    public int     getCaseId()                  { return caseId; }
    public void    setCaseId(int v)             { this.caseId = v; }
    public String  getCaseNumber()              { return caseNumber; }
    public void    setCaseNumber(String v)      { this.caseNumber = v; }
    public int     getTypeId()                  { return typeId; }
    public void    setTypeId(int v)             { this.typeId = v; }
    public String  getEvidenceTypeName()        { return evidenceTypeName; }
    public void    setEvidenceTypeName(String v){ this.evidenceTypeName = v; }
    public int     getStorageId()               { return storageId; }
    public void    setStorageId(int v)          { this.storageId = v; }
    public String  getStorageLocation()         { return storageLocation; }
    public void    setStorageLocation(String v) { this.storageLocation = v; }
    public String  getFileName()                { return fileName; }
    public void    setFileName(String v)        { this.fileName = v; }
    public long    getFileSize()                { return fileSize; }
    public void    setFileSize(long v)          { this.fileSize = v; }
    public String  getFileSizeFormatted()       { return fileSizeFormatted; }
    public void    setFileSizeFormatted(String v){ this.fileSizeFormatted = v; }
    public String  getFileType()                { return fileType; }
    public void    setFileType(String v)        { this.fileType = v; }
    public String  getHashMd5()                 { return hashMd5; }
    public void    setHashMd5(String v)         { this.hashMd5 = v; }
    public Date    getUploadDate()              { return uploadDate; }
    public void    setUploadDate(Date v)        { this.uploadDate = v; }
    public boolean isVerified()                 { return verified; }
    public void    setVerified(boolean v)       { this.verified = v; }
    public String  getVerifiedStr()             { return verified ? "✔ Yes" : "✘ No"; }
    public int     getUploadedBy()              { return uploadedBy; }
    public void    setUploadedBy(int v)         { this.uploadedBy = v; }
}
