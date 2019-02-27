package org.cloud.wetag.model;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.LayoutInflater;

import org.cloud.wetag.R;
import org.cloud.wetag.ui.adapter.DataObjectCardAdapter;
import org.cloud.wetag.ui.adapter.ImageCardAdapter;
import org.cloud.wetag.ui.adapter.Seq2SeqCardAdapter;
import org.cloud.wetag.ui.adapter.TextCardAdapter;
import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.cloud.wetag.ui.PageFragment.ALL_LABELED;
import static org.cloud.wetag.ui.PageFragment.ALL_UNLABELED;
import static org.cloud.wetag.ui.PageFragment.SINGLE_LABELED;

public class DataSet extends DataSupport {

  private int id;

  // name of the dataset
  @Column(unique = true)
  private String name;

  @Column(nullable = true)
  private String desc;

  // labels that dataset contains
  @Column(nullable = false)
  private List<String> labels = new ArrayList<>();

  @Column(nullable = false)
  private int type;

  private List<DataObject> dataObjects = new ArrayList<>();

  // dataset type for image classification labeling, it can be object detection in future version
  public static final int IMAGE = 0;

  // dataset type for text classification labeling
  public static final int TEXT_CLASSIFICATION = 1;

  // dataset type for text seq2seq labeling
  public static final int SEQ2SEQ = 2;

  public DataSet() {
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  public DataSet(String name, int type) {
    Objects.requireNonNull(name);
    this.name = name;
    this.type = type;
  }

  public static DataSet newImageDataSet(String name) {
    return new DataSet(name, IMAGE);
  }

  public static DataSet newTextClassificationDataSet(String name) {
    return new DataSet(name, TEXT_CLASSIFICATION);
  }

  public static DataSet newSeq2SeqDataSet(String name) {
    return new DataSet(name, SEQ2SEQ);
  }

  public boolean isImageDataSet() {
    return type == IMAGE;
  }

  public boolean isTextClassificationDataSet() {
    return type == TEXT_CLASSIFICATION;
  }

  public boolean isText() {
    return type == TEXT_CLASSIFICATION || type == SEQ2SEQ;
  }

  public int getId() {
    return id;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setLabels(List<String> labels) {
    this.labels.clear();
    this.labels.addAll(labels);
  }

  public List<String> getLabels() {
    return labels;
  }

  public List<DataObject> getOrLoadObjects() {
    if (this.dataObjects.size() == 0) {
      List<DataObject> dataObjects = DataSupport
          .where("dataset_id = ?", String.valueOf(id))
          .find(DataObject.class);
      this.dataObjects = dataObjects;
      for (DataObject dataObject : dataObjects) {
        dataObject.getOrLoadLabels();
      }
    }
    return this.dataObjects;
  }

  public int getObjectCount() {
    return dataObjects.size();
  }

  public List<DataObject> getDataObjects() {
    return dataObjects;
  }

  public DataObject getDataObject(int index) {
    return dataObjects.get(index);
  }

  public void addSource(File file) throws IOException {
    if (type == IMAGE) {
      DataObject dataObject = new DataObject(file.getPath(), true);
      dataObject.saveThrows();
      addObject(dataObject);
    } else if (type == TEXT_CLASSIFICATION) {
      readAllLines(file);
    } else if (type == SEQ2SEQ) {
      readAllLines(file);
    } else {
      throw new UnsupportedEncodingException();
    }
  }

  private void readAllLines(File file) throws IOException {
    FileInputStream in = new FileInputStream(file);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String line;
    while ((line = reader.readLine()) != null) {
      DataObject dataObject = new DataObject(line, true);
      dataObject.saveThrows();
      addObject(dataObject);
    }
    reader.close();
    in.close();
  }

  public void addObject(DataObject dataObject) {
    dataObjects.add(dataObject);
  }

  public void removeObject(DataObject dataObject) {
    dataObjects.remove(dataObject);
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  // default picture shown in the card
  public int getDefaultPictureResourceId() {
    if (isTextClassificationDataSet()) {
      return R.drawable.text_classification;
    }
    return R.drawable.empty_dark;
  }

  public boolean requireLabelDef() {
    switch (type) {
      case IMAGE:
      case TEXT_CLASSIFICATION:
        return true;
      default:
        return false;
    }
  }

  public boolean requireLabelBar() {
    switch (type) {
      case IMAGE:
        return true;
      default:
        return false;
    }
  }

  public boolean requireObjectSelection() {
    switch (type) {
      case IMAGE:
        return true;
      default:
        return false;
    }
  }

  public DataObjectCardAdapter createAdapter(int pageType, String labelName,
                                             ObjectSelection objectSelection) {
    if (type == DataSet.IMAGE) {
      return createImageCardAdapter(pageType, labelName, objectSelection);
    } else if (type == DataSet.TEXT_CLASSIFICATION) {
      return createTextCardAdapter(pageType, labelName, objectSelection);
    } else if (type == DataSet.SEQ2SEQ) {
      return createSeq2SeqCardAdapter(pageType, labelName, objectSelection);
    } else {
      throw new UnsupportedOperationException("dataset type " + type + " is not supported");
    }
  }

  private DataObjectCardAdapter createImageCardAdapter(int pageType, String labelName,
                                                       ObjectSelection objectSelection) {
    if (pageType == ALL_UNLABELED) {
      return new ImageCardAdapter(this, objectSelection, ALL_UNLABELED, null);
    } else if (pageType == ALL_LABELED) {
      return new ImageCardAdapter(this, objectSelection, ALL_LABELED, null);
    } else {
      return new ImageCardAdapter(this, objectSelection, SINGLE_LABELED, labelName);
    }
  }

  private DataObjectCardAdapter createTextCardAdapter(int pageType, String labelName,
                                                      ObjectSelection objectSelection) {
    if (pageType == ALL_UNLABELED) {
      return new TextCardAdapter(this, objectSelection, ALL_UNLABELED, null);
    } else if (pageType == ALL_LABELED) {
      return new TextCardAdapter(this, objectSelection, ALL_LABELED, null);
    } else {
      return new TextCardAdapter(this, objectSelection, SINGLE_LABELED, labelName);
    }
  }

  private DataObjectCardAdapter createSeq2SeqCardAdapter(int pageType, String labelName,
                                                         ObjectSelection objectSelection) {
    if (pageType == ALL_UNLABELED) {
      return new Seq2SeqCardAdapter(this, objectSelection, ALL_UNLABELED, null);
    } else if (pageType == ALL_LABELED) {
      return new Seq2SeqCardAdapter(this, objectSelection, ALL_LABELED, null);
    } else {
      return new Seq2SeqCardAdapter(this, objectSelection, SINGLE_LABELED, labelName);
    }
  }

  public int getCardItemLayoutResource() {
    if (type == DataSet.IMAGE) {
      return R.layout.image_card_item;
    } else if (type == DataSet.TEXT_CLASSIFICATION) {
      return R.layout.text_card_item;
    } else if (type == DataSet.SEQ2SEQ) {
      return R.layout.seq2seq_card_item;
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public int getMenuResource() {
    if (type == DataSet.IMAGE) {
      return R.menu.menu_image_labeling;
    } else if (type == DataSet.TEXT_CLASSIFICATION) {
      return R.menu.menu_text_labeling;
    } else if (type == DataSet.SEQ2SEQ) {
      return R.menu.menu_seq2seq_labeling;
    } else {
      throw new UnsupportedOperationException();
    }
  }
}
