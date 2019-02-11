package org.cloud.wetag.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DataSet {
  // name of the dataset
  private String name;

  // labels that dataset contains
  private Set<String> labels;

  private List<Image> images;

  private List<Set<String>> imageLabels;

  public DataSet(String name, Set<String> labels) {
    this.name = name;
    this.labels = labels;
    images = new ArrayList<>();
    imageLabels = new ArrayList<>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<String> getLabels() {
    return labels;
  }

  public void setLabels(Set<String> labels) {
    this.labels = labels;
  }

  public List<Image> getImages() {
    return images;
  }

  public List<Set<String>> getImageLabels() {
    return imageLabels;
  }

}
