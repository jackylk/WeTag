package org.cloud.wetag.model.service;

public class Token {
  private String expires_at;
  private String issued_at;
  private Project project;

  public String getExpires_at() {
    return expires_at;
  }

  public void setExpires_at(String expires_at) {
    this.expires_at = expires_at;
  }

  public String getIssued_at() {
    return issued_at;
  }

  public void setIssued_at(String issued_at) {
    this.issued_at = issued_at;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }
}
