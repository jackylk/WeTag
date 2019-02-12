package org.cloud.wetag.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.model.Image;
import org.cloud.wetag.model.ImageSelection;
import org.cloud.wetag.ui.adapter.ImageCardAdapter;
import org.cloud.wetag.utils.CaptureStrategy;
import org.cloud.wetag.utils.MediaStoreCompat;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LabelActivity extends BaseActivity implements View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, ImageCardAdapter.OnCheckChangedListener {

  private DataSet dataSet;
  private ImageCardAdapter adapter;
  private MediaStoreCompat mediaStoreCompat;
  private ChipGroup chipGroup;
  private ImageSelection imageSelection;
  private Set<String> labelSelection;
  private Map<String, Chip> chipMap;

  private static final int REQUEST_CODE_CAPTURE = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_label);

    imageSelection = new ImageSelection();
    labelSelection = new HashSet<>();
    String datasetName = getIntent().getStringExtra("dataset_name");
    if (datasetName == null) {
      Toast.makeText(getApplicationContext(), "error: should pass dataset_name",
          Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    dataSet = DataSetCollection.getDataSet(datasetName);
    mediaStoreCompat = new MediaStoreCompat(this);
    mediaStoreCompat.setCaptureStrategy(
        new CaptureStrategy(false, "org.cloud.wetag.fileprovider", datasetName));

    RecyclerView recyclerView = findViewById(R.id.images_rv);
    RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
    recyclerView.setLayoutManager(layoutManager);
    adapter = new ImageCardAdapter(dataSet, imageSelection);
    adapter.registerOnCheckChangedListener(this);
    recyclerView.setAdapter(adapter);

    chipGroup = findViewById(R.id.label_chipgroup);
    chipMap = new HashMap<>();
    for (String label : dataSet.getLabels()) {
      Chip chip = new Chip(chipGroup.getContext());
      chip.setText(label);
      chip.setEnabled(true);
      chip.setClickable(true);
      chip.setCheckable(true);
      chip.setOnCheckedChangeListener(this);
      chipGroup.addView(chip);
      chipMap.put(label, chip);
    }
    chipGroup.setEnabled(false);

    findViewById(R.id.label_confirm).setOnClickListener(this);
    setTitle(dataSet.getName());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_labeling, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.item_camera:
        if (mediaStoreCompat != null) {
          mediaStoreCompat.dispatchCaptureIntent(this, REQUEST_CODE_CAPTURE);
        }
        break;
      default:
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != RESULT_OK) {
      return;
    }
    if (requestCode == REQUEST_CODE_CAPTURE) {
      File imageFile = new File(mediaStoreCompat.getCurrentPhotoPath());
      if (imageFile.exists() && imageFile.length() > 0) {
        Image image = new Image(dataSet.getName(), mediaStoreCompat.getImageFileName());
        image.saveThrows();
        dataSet.addImage(image);
        dataSet.saveThrows();
        refreshView();
      }
    }
  }

  private void refreshView() {
    adapter.notifyDataSetChanged();
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (imageSelection.get().size() > 0) {
      String label = buttonView.getText().toString();
      if (isChecked) {
        labelSelection.add(label);
      } else {
        labelSelection.remove(label);
      }
    }
  }

  @Override
  public void onImageCheckedChanged(Image image, boolean checked) {
    Set<String> labels = new HashSet<>();
    for (Image img : imageSelection.get()) {
      labels.addAll(img.getLabels());
    }
    for (Map.Entry<String, Chip> chipEntry : chipMap.entrySet()) {
      if (labels.contains(chipEntry.getKey())) {
        chipEntry.getValue().setChecked(true);
      } else {
        chipEntry.getValue().setChecked(false);
      }
    }
    if (imageSelection.get().size() > 0) {
      chipGroup.setEnabled(true);
    } else {
      chipGroup.setEnabled(false);
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.label_confirm:
        for (Image image : imageSelection.get()) {
          image.setLabels(labelSelection);
        }
        if (imageSelection.get().size() > 0) {
          imageSelection.clear();
        }
        if (labelSelection.size() > 0) {
          labelSelection.clear();
          chipGroup.clearCheck();
        }
        refreshView();
        break;
      default:
        break;
    }
  }
}
