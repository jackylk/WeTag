package org.cloud.wetag.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.chip.Chip;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.cloud.wetag.MyApplication;
import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSetUpdateDBHelper;
import org.cloud.wetag.model.Sample;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.ui.widget.LabelBar;

import java.io.File;
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
    labelBar.setEnabled(true);

    Sample sample = dataSet.getSample(imageIndex);
    for (Map.Entry<String, Chip> chipEntry : labelBar.getChipMap().entrySet()) {
      if (sample.getLabels().contains(chipEntry.getKey())) {
        chipEntry.getValue().setChecked(true);
      }
    }
  }

  @Override
  public void onClick(View v) {
    Sample sample = dataSet.getSample(imageIndex);
    DataSetUpdateDBHelper.setLabels(dataSet, sample, labelBar.getLabelSelection());
    Intent intent = new Intent();
    setResult(RESULT_OK, intent);
    finish();
  }

  public static void start(AppCompatActivity activity,
                           DataSet dataSet, Sample sample, int requestCode) {
    Intent intent = new Intent(MyApplication.getContext(), ImagePreviewActivity.class);
    intent.putExtra("dataset_name", dataSet.getName());
    intent.putExtra("image_index", dataSet.getSamples().indexOf(sample));
    intent.putExtra("image_path", sample.getSource());
    activity.startActivityForResult(intent, requestCode);
  }

}
