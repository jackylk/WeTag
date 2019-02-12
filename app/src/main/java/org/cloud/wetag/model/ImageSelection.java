package org.cloud.wetag.model;

import java.util.ArrayList;
import java.util.List;

public class ImageSelection {
  private List<Image> selectedImage = new ArrayList<>();

  public void add(Image image) {
    selectedImage.add(image);
  }

  public void remove(Image image) {
    selectedImage.remove(image);
  }

  public List<Image> get() {
    return selectedImage;
  }

  public void clear() {
    selectedImage.clear();
  }

  public boolean exist(Image image) {
    return selectedImage.contains(image);
  }
}
