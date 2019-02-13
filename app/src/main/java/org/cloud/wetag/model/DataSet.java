package org.cloud.wetag.model;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;

import org.cloud.wetag.R;
import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DataSet extends DataSupport {

  private int id;

  // name of the dataset
  @Column(unique = true)
  private String name;

  // labels that dataset contains
  @Column(nullable = false)
  private List<String> labels = new ArrayList<>();

  private List<Image> images = new ArrayList<>();

  @TargetApi(Build.VERSION_CODES.KITKAT)
  public DataSet(String name) {
    Objects.requireNonNull(name);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setLabels(List<String> labels) {
    this.labels.clear();
    this.labels.addAll(labels);
  }

  public List<String> getLabels() {
    return labels;
  }

  public List<Image> getOrLoadImages() {
    if (this.images.size() == 0) {
      List<Image> images = DataSupport.select("id", "fileName", "dataSetName")
          .where("dataSetName = ?", name)
          .find(Image.class);
      this.images = images;
    }
    return this.images;
  }

  public int getImageCount() {
    return images.size();
  }

  public List<Image> getImages() {
    return images;
  }

  public Image getImage(int index) {
    return images.get(index);
  }

  public void addImage(Image image) {
    images.add(image);
  }

  public void removeImage(Image image) {
    images.remove(image);
  }


}
