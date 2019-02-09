package org.cloud.wetag.dataset;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WorkSpace {
  private static List<DataSet> dataSetList = new ArrayList<>();

  private WorkSpace() {
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
