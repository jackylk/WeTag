package org.cloud.wetag.model;

import java.util.ArrayList;
import java.util.List;

public class ObjectSelection {

  private boolean selectEnabled = true;

  private List<Sample> selectedSample = new ArrayList<>();

  public boolean isSelectEnabled() {
    return selectEnabled;
  }

  public void setSelectEnabled(boolean selectEnabled) {
    this.selectEnabled = selectEnabled;
  }

  public void add(Sample sample) {
    selectedSample.add(sample);
  }

  public void remove(Sample sample) {
    selectedSample.remove(sample);
  }

  public List<Sample> get() {
    return selectedSample;
  }

  public void clear() {
    selectedSample.clear();
  }

  public boolean exist(Sample sample) {
    return selectedSample.contains(sample);
  }
}
