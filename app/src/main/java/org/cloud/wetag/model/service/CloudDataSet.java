package org.cloud.wetag.model.service;

public class CloudDataSet {
  private String dataset_id;

  private String dataset_name;

  private String description;

  private String data_url;

  private int file_count;

  private int create_time;

  public String getDataset_id() {
    return dataset_id;
  }

  public void setDataset_id(String dataset_id) {
    this.dataset_id = dataset_id;
  }

  public String getDataset_name() {
    return dataset_name;
  }

  public void setDataset_name(String dataset_name) {
    this.dataset_name = dataset_name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getData_url() {
    return data_url;
  }

  public void setData_url(String data_url) {
    this.data_url = data_url;
  }

  public int getFile_count() {
    return file_count;
  }

  public void setFile_count(int file_count) {
    this.file_count = file_count;
  }

  public int getCreate_time() {
    return create_time;
  }

  public void setCreate_time(int create_time) {
    this.create_time = create_time;
  }
}
