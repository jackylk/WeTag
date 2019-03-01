package org.cloud.wetag.model;

import java.util.List;

// This helper update the dataset/sample and the litepal storage
public class DataSetUpdateDBHelper {

  public static void setLabels(DataSet dataSet, Sample sample, List<String> labels) {
    sample.setLabels(labels);
    long time = System.currentTimeMillis();
    sample.setUpdateTime(time);
    sample.saveThrows();
    dataSet.setUpdateTime(time);
    dataSet.saveThrows();
  }

  public static void addLabel(DataSet dataSet, Sample sample, String label) {
    sample.addLabel(label);
    long time = System.currentTimeMillis();
    sample.setUpdateTime(time);
    sample.saveThrows();
    dataSet.setUpdateTime(time);
    dataSet.saveThrows();
  }

  public static void removeLabel(DataSet dataSet, Sample sample, String label) {
    sample.removeLabel(label);
    long time = System.currentTimeMillis();
    sample.setUpdateTime(time);
    sample.saveThrows();
    dataSet.setUpdateTime(time);
    dataSet.saveThrows();
  }

}
