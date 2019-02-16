package org.cloud.wetag.model;

import java.util.ArrayList;
import java.util.List;

public class ObjectSelection {
  private List<DataObject> selectedDataObject = new ArrayList<>();

  public void add(DataObject dataObject) {
    selectedDataObject.add(dataObject);
  }

  public void remove(DataObject dataObject) {
    selectedDataObject.remove(dataObject);
  }

  public List<DataObject> get() {
    return selectedDataObject;
  }

  public void clear() {
    selectedDataObject.clear();
  }

  public boolean exist(DataObject dataObject) {
    return selectedDataObject.contains(dataObject);
  }
}
