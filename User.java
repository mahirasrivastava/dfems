package com.dfems.model;

public class User {
    private int    userId;
    private String name;
    private String email;
    private int    roleId;
    private String roleName;
    private int    deptId;
    private String deptName;

    public int    getUserId()           { return userId; }
    public void   setUserId(int v)      { this.userId = v; }
    public String getName()             { return name; }
    public void   setName(String v)     { this.name = v; }
    public String getEmail()            { return email; }
    public void   setEmail(String v)    { this.email = v; }
    public int    getRoleId()           { return roleId; }
    public void   setRoleId(int v)      { this.roleId = v; }
    public String getRoleName()         { return roleName; }
    public void   setRoleName(String v) { this.roleName = v; }
    public int    getDeptId()           { return deptId; }
    public void   setDeptId(int v)      { this.deptId = v; }
    public String getDeptName()         { return deptName; }
    public void   setDeptName(String v) { this.deptName = v; }
    @Override public String toString()  { return name + " (" + roleName + ")"; }
}
