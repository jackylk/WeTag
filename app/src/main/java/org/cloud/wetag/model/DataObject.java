package org.cloud.wetag.model;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import org.cloud.wetag.MyApplication;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Object that to be labeled. It can be image file, audio file, text, etc
 */
public class DataObject extends DataSupport {
  private int id;

  // true if this object is captured in this app (for example, image snapshot in this app),
  // we should delete it only if it is true, otherwise do not delete it
  private boolean capturedInApp;

  private String source;

  private List<String> labels = new LinkedList<>();

  /**
   *  @param source for file related data (like image and audio), source is the
   *               file path of the object; for text related data, source is the
   *               text content itself
   * @param capturedInApp
   */
  public DataObject(String source, boolean capturedInApp) {
    this.source = source;
    this.capturedInApp = capturedInApp;
  }

  public boolean isCapturedInApp() {
    return capturedInApp;
  }

  public String getSource() {
    return source;
  }

  public Uri getUri() {
    File imageFile = new File(source);
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
          "select * from dataobject_labels where dataobject_id = ?", String.valueOf(id));
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
