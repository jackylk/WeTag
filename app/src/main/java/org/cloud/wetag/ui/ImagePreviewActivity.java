package org.cloud.wetag.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.design.chip.ChipGroup;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.cloud.wetag.MyApplication;
import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.model.Image;

import java.io.File;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class ImagePreviewActivity extends AppCompatActivity implements View.OnClickListener {

  private String dataSetName;
  private int imageIndex;

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

    findViewById(R.id.label_confirm).setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    DataSet dataSet = DataSetCollection.getDataSet(dataSetName);
    Image image = dataSet.getImage(imageIndex);

    ChipGroup chipGroup = findViewById(R.id.label_chipgroup);
    finish();
  }
}
