package org.cloud.wetag.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import org.cloud.wetag.MyApplication;
import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.model.Image;
import org.cloud.wetag.ui.widget.LabelBar;

import java.io.File;
import java.util.List;
import java.util.Map;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class ImagePreviewActivity extends AppCompatActivity implements View.OnClickListener {

  private String dataSetName;
  private int imageIndex;
  private LabelBar labelBar;
  private DataSet dataSet;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_preview);

    Intent intent = getIntent();
    String imagePath = intent.getStringExtra("image_path");
    dataSetName = intent.getStringExtra("dataset_name");
    imageIndex = intent.getIntExtra("image_index", 0);
    File file = new File(imagePath);
    Uri imageUri;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      imageUri = FileProvider.getUriForFile(MyApplication.getContext(),
          "org.cloud.wetag.fileprovider", file);
    } else {
      imageUri = Uri.fromFile(file);
    }
    ImageViewTouch imageViewTouch = findViewById(R.id.image_preview_pager);
    imageViewTouch.setImageURI(imageUri);

    dataSet = DataSetCollection.getDataSet(dataSetName);
    labelBar = new LabelBar(findViewById(R.id.label_bar), dataSet, this);
    labelBar.setEnableLabelBar(true);

    Image image = dataSet.getImage(imageIndex);
    for (Map.Entry<String, Chip> chipEntry : labelBar.getChipMap().entrySet()) {
      if (image.getLabels().contains(chipEntry.getKey())) {
        chipEntry.getValue().setChecked(true);
      }
    }
  }

  @Override
  public void onClick(View v) {
    Image image = dataSet.getImage(imageIndex);
    image.setLabels(labelBar.getLabelSelection());
    Intent intent = new Intent();
    setResult(RESULT_OK, intent);
    finish();
  }

  public static void start(AppCompatActivity activity,
                           DataSet dataSet, Image image, int requestCode) {
    Intent intent = new Intent(MyApplication.getContext(), ImagePreviewActivity.class);
    intent.putExtra("dataset_name", dataSet.getName());
    intent.putExtra("image_index", dataSet.getImages().indexOf(image));
    intent.putExtra("image_path", image.getFilePath());
    activity.startActivityForResult(intent, requestCode);
  }

}
