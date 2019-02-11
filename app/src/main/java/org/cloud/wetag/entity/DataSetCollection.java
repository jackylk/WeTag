package org.cloud.wetag.entity;

import java.util.ArrayList;
import java.util.List;

public class DataSetCollection {
  private static List<DataSet> dataSetList = new ArrayList<>();

  private DataSetCollection() {
  }

  public static List<DataSet> getDataSetList() {
    return dataSetList;
  }

  public static void addDataSet(DataSet dataSet) {
    dataSetList.add(dataSet);
  }

  public static void removeDataSet(final String dataSetName) {
    for (DataSet dataSet : dataSetList) {
      if (dataSet.getName().equals(dataSetName)) {
        dataSetList.remove(dataSet);
      }
    }
  }

  public static void removeDataSet(int index) {
    if (dataSetList.size() > index) {
      dataSetList.remove(index);
    }
  }
}
