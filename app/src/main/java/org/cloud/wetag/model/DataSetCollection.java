package org.cloud.wetag.model;

import org.litepal.crud.DataSupport;

import java.util.Iterator;
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
    Iterator<DataSet> iterator = INSTANCE.dataSets.iterator();
    while (iterator.hasNext()) {
      DataSet dataSet = iterator.next();
      if (dataSet.getName().equals(dataSetName)) {
        iterator.remove();
        dataSet.delete();
        break;
      }
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
