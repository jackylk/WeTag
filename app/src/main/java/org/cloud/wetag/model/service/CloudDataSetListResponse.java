package org.cloud.wetag.model.service;

import java.util.List;

public class CloudDataSetListResponse {

  private int dataset_count;
  private List<CloudDataSet> datasets;
  private String error_code;
  private String error_msg;

  public int getDataset_count() {
    return dataset_count;
  }

  public void setDataset_count(int dataset_count) {
    this.dataset_count = dataset_count;
  }

  public List<CloudDataSet> getDatasets() {
    return datasets;
  }

  public void setDatasets(List<CloudDataSet> datasets) {
    this.datasets = datasets;
  }

  public String getError_code() {
    return error_code;
  }

  public void setError_code(String error_code) {
    this.error_code = error_code;
  }

  public String getError_msg() {
    return error_msg;
  }

  public void setError_msg(String error_msg) {
    this.error_msg = error_msg;
  }
}
