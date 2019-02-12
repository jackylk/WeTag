package org.cloud.wetag.model;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import org.cloud.wetag.MyApplication;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Image extends DataSupport {
  private int id;

  private String fileName;

  private String dataSetName;

  private Set<String> labels = new HashSet<>();

  public Image(String dataSetName, String fileName) {
    this.dataSetName = dataSetName;
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }

  public String getFilePath() {
    File storageDir =
        MyApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    return storageDir.getAbsolutePath() + File.separator + dataSetName +
        File.separator + fileName;
  }

  public Uri getUri() {
    File storageDir =
        MyApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    File imageFile = new File(storageDir.getAbsolutePath() + File.separator + dataSetName +
      File.separator + fileName);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      return FileProvider.getUriForFile(MyApplication.getContext(),
          "org.cloud.wetag.fileprovider", imageFile);
    } else {
      return Uri.fromFile(imageFile);
    }
  }

  public Set<String> getLabels() {
    return labels;
  }

  public Set<String> getOrLoadLabels() {
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

  public void setLabels(Set<String> labels) {
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
