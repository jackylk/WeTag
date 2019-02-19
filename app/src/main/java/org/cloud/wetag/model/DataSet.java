package org.cloud.wetag.model;

import android.annotation.TargetApi;
import android.os.Build;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DataSet extends DataSupport {

  private int id;

  // name of the dataset
  @Column(unique = true)
  private String name;

  @Column(nullable = true)
  private String desc;

  // labels that dataset contains
  @Column(nullable = false)
  private List<String> labels = new ArrayList<>();

  @Column(nullable = false)
  private int type;

  private List<DataObject> dataObjects = new ArrayList<>();

  // dataset type for image classification labeling, it can be object detection in future version
  public static final int IMAGE = 0;

  // dataset type for text classification labeling
  public static final int TEXT_CLASSIFICATION = 1;

  @TargetApi(Build.VERSION_CODES.KITKAT)
  public DataSet(String name, int type) {
    Objects.requireNonNull(name);
    this.name = name;
    this.type = type;
  }

  public static DataSet newImageDataSet(String name) {
    return new DataSet(name, IMAGE);
  }

  public static DataSet newTextClassificationDataSet(String name) {
    return new DataSet(name, TEXT_CLASSIFICATION);
  }

  public boolean isImageDataSet() {
    return type == IMAGE;
  }

  public boolean isTextClassificationDataSet() {
    return type == TEXT_CLASSIFICATION;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
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

  public List<DataObject> getOrLoadObjects() {
    if (this.dataObjects.size() == 0) {
      List<DataObject> dataObjects = DataSupport.where("dataSetName = ?", name).find(DataObject.class);
      this.dataObjects = dataObjects;
      for (DataObject dataObject : dataObjects) {
        dataObject.getOrLoadLabels();
      }
    }
    return this.dataObjects;
  }

  public int getObjectCount() {
    return dataObjects.size();
  }

  public List<DataObject> getDataObjects() {
    return dataObjects;
  }

  public DataObject getDataObject(int index) {
    return dataObjects.get(index);
  }

  public void addSource(File file) throws IOException {
    if (isImageDataSet()) {
      DataObject dataObject = new DataObject(getName(), file.getPath(), true);
      dataObject.saveThrows();
      addObject(dataObject);
    } else if (isTextClassificationDataSet()) {
      FileInputStream in = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String line;
      while ((line = reader.readLine()) != null) {
        DataObject dataObject = new DataObject(getName(), line, true);
        dataObject.saveThrows();
        addObject(dataObject);
      }
      reader.close();
      in.close();
    }
  }

  public void addObject(DataObject dataObject) {
    dataObjects.add(dataObject);
  }

  public void removeObject(DataObject dataObject) {
    dataObjects.remove(dataObject);
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }
}
