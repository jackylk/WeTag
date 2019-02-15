package org.cloud.wetag.model;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import org.cloud.wetag.MyApplication;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Image extends DataSupport {
  private int id;

  // true if this image is captured in this app, we should delete it
  // only if it is true, otherwise do not delete it
  private boolean capturedInApp;

  private String filePath;

  private String dataSetName;

  private List<String> labels = new LinkedList<>();

  public Image(String dataSetName, String filePath, boolean capturedInApp) {
    this.dataSetName = dataSetName;
    this.filePath = filePath;
    this.capturedInApp = capturedInApp;
  }

  public boolean isCapturedInApp() {
    return capturedInApp;
  }

  public String getFilePath() {
    return filePath;
  }

  public Uri getUri() {
    File imageFile = new File(filePath);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      return FileProvider.getUriForFile(MyApplication.getContext(),
          "org.cloud.wetag.fileprovider", imageFile);
    } else {
      return Uri.fromFile(imageFile);
    }
  }

  public List<String> getLabels() {
    return labels;
  }

  public List<String> getOrLoadLabels() {
    if (this.labels.size() == 0) {
      List<String> labelFromDb = new LinkedList<>();
      Cursor cursor = DataSupport.findBySQL(
          "select * from image_labels where image_id = ?", String.valueOf(id));
      if (cursor.moveToFirst()) {
        do {
          labelFromDb.add(cursor.getString(cursor.getColumnIndex("labels")));
        } while (cursor.moveToNext());
      }
      this.labels.addAll(labelFromDb);
    }
    return this.labels;
  }

  public void setLabels(List<String> labels) {
    this.labels.clear();
    this.labels.addAll(labels);
  }

  public String getDataSetName() {
    return dataSetName;
  }

  public void addLabel(String label) {
    labels.add(label);
  }

  public void addLabels(Set<String> labels) {
    labels.addAll(labels);
  }

  public void removeLabel(String lable) {
    labels.remove(lable);
  }
}
