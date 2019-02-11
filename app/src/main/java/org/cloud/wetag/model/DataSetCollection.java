package org.cloud.wetag.model;

import org.litepal.crud.DataSupport;

import java.util.List;

public class DataSetCollection {

  private final static DataSetCollection INSTANCE = new DataSetCollection();

  private List<DataSet> dataSets;

  private DataSetCollection() {
    dataSets = DataSupport.findAll(DataSet.class);
  }

  public static List<DataSet> getDataSetList() {
    return INSTANCE.dataSets;
  }

  public static void addDataSet(DataSet dataSet) {
    INSTANCE.dataSets.add(dataSet);
    dataSet.saveThrows();
  }

  public static void removeDataSet(final String dataSetName) {
    for (DataSet dataSet : INSTANCE.dataSets) {
      if (dataSet.getName().equals(dataSetName)) {
        INSTANCE.dataSets.remove(dataSet);
      }
    }
  }

  public static void removeDataSet(int index) {
    if (INSTANCE.dataSets.size() > index) {
      INSTANCE.dataSets.remove(index);
    }
  }

  public static DataSet getDataSet(String dataSetName) {
    for (DataSet dataSet : INSTANCE.dataSets) {
      if (dataSet.getName().equals(dataSetName)) {
        return dataSet;
      }
    }
    return null;
  }
}
