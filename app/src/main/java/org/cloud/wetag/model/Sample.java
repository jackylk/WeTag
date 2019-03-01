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

/**
 * Object that to be labeled. It can be image file, audio file, text, etc
 */
public class Sample extends DataSupport {
  private int id;

  // true if this object is captured in this app (for example, image snapshot in this app),
  // we should delete it only if it is true, otherwise do not delete it
  private boolean capturedInApp;

  // file path of the image, in case of image data object
  // content of the text, in case of text data object
  private String source;

  private List<String> labels = new LinkedList<>();

  // creation time of this sample, by System.currentTimeMillis()
  private long createTime;

  // update time of when any labels is updated, by System.currentTimeMillis()
  private long updateTime;

  /**
   * @param source for file related data (like image and audio), source is the
   *               file path of the object; for text related data, source is the
   *               text content itself
   * @param capturedInApp
   */
  public Sample(String source, boolean capturedInApp) {
    this.source = source;
    this.capturedInApp = capturedInApp;
    this.createTime = System.currentTimeMillis();
    this.updateTime = createTime;
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
          "select * from sample_labels where sample_id = ?", String.valueOf(id));
      if (cursor.moveToFirst()) {
        do {
          labelFromDb.add(cursor.getString(cursor.getColumnIndex("labels")));
        } while (cursor.moveToNext());
      }
      this.labels.addAll(labelFromDb);
    }
    return this.labels;
  }

  void setLabels(List<String> labels) {
    this.labels.clear();
    this.labels.addAll(labels);
  }

  void addLabel(String label) {
    labels.add(label);
  }

  void removeLabel(String label) {
    this.labels.remove(label);
  }

  void setCreateTime(long createTime) {
    this.createTime = createTime;
  }

  void setUpdateTime(long updateTime) {
    this.updateTime = updateTime;
  }

  public long getCreateTime() {
    return createTime;
  }

  public long getUpdateTime() {
    return updateTime;
  }

  public int getId() {
    return id;
  }

}
