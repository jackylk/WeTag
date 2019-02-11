package org.cloud.wetag.entity;

import android.net.Uri;

import java.util.HashSet;
import java.util.Set;

public class Image {
  private Uri imageUri;
  private Set<String> labels;

  public Image(Uri imageUri) {
    this.imageUri = imageUri;
    this.labels = new HashSet<>();
  }

  public Uri getImageUri() {
    return imageUri;
  }

  public void setImageUri(Uri imageUri) {
    this.imageUri = imageUri;
  }

  public Set<String> getLabels() {
    return labels;
  }

  public void setLabels(Set<String> labels) {
    this.labels = labels;
  }
}
