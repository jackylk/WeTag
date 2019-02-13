package org.cloud.wetag.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabItem;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonWriter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.Toast;

import org.cloud.wetag.MyApplication;
import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.model.Image;
import org.cloud.wetag.model.ImageSelection;
import org.cloud.wetag.ui.adapter.ImageCardAdapter;
import org.cloud.wetag.utils.CaptureStrategy;
import org.cloud.wetag.utils.MediaStoreCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
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
  private Menu menu;
  private boolean isEditingDataSet = false;

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

    initLabelBar();
    setEnableLabelBar(false);

    findViewById(R.id.label_confirm).setOnClickListener(this);
    setTitle(dataSet.getName() + "数据集");
    initTabs();
  }

  private void initTabs() {
    TableLayout tableLayout = findViewById(R.id.label_tab_layout);
    TabItem tab1 = new TabItem(tableLayout.getContext());
    tab1.
    tableLayout.addView(new TabItem());
  }
  private void initLabelBar() {
    chipGroup = findViewById(R.id.label_chipgroup);
    chipMap = new HashMap<>();
    for (String label : dataSet.getLabels()) {
      Chip chip = new Chip(chipGroup.getContext());
      chip.setText(label);
      chip.setEnabled(false);
      chip.setClickable(true);
      chip.setCheckable(true);
      chip.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
      chip.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.white));
      chip.setChipBackgroundColorResource(dataSet.getLabelBackgroundColor(label));
      chip.setOnCheckedChangeListener(this);
      chipGroup.addView(chip);
      chipMap.put(label, chip);
    }
  }

  private void setEnableLabelBar(boolean enabled) {
    findViewById(R.id.label_confirm).setEnabled(enabled);
    for (Chip chip : chipMap.values()) {
      chip.setEnabled(enabled);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    this.menu = menu;
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
      case R.id.item_export:
        try {
          final File file = exportLabel();
          Snackbar bar = Snackbar.make(chipGroup.getRootView(),
               "标签文件导出成功: " + file.getName(), Snackbar.LENGTH_SHORT);
          bar.setAction("打开", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              startViewFileActivity(file);
            }
          }).show();
        } catch (IOException e) {
            e.printStackTrace();
            Snackbar.make(chipGroup.getRootView(),
                "标签文件导出失败, 原因：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
        break;
      case R.id.item_edit:
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_labeling_edit, menu);
        setEnableLabelBar(false);
        isEditingDataSet = true;
        break;
      case R.id.item_delete:
        final int num = imageSelection.get().size();
        new AlertDialog.Builder(chipGroup.getContext())
            .setTitle(R.string.dialog_delete_image_title)
            .setMessage(R.string.dialog_delete_image_message)
            .setPositiveButton(R.string.button_positive, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                // delete the dataset by name
                for (Image image : imageSelection.get()) {
                  dataSet.removeImage(image);
                  File imageFile = new File(image.getFilePath());
                  imageFile.delete();
                  image.delete();
                }
                Snackbar.make(chipGroup.getRootView(),
                    "删除了" + num + "张图片", Snackbar.LENGTH_SHORT).show();
                refreshView();
                redrawMainMenu();
                isEditingDataSet = false;
              }
            })
            .setNegativeButton(R.string.button_negative, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                // do nothing
              }
            }).show();
        break;
      case R.id.item_cancel:
        redrawMainMenu();
        isEditingDataSet = false;
        break;
      default:
    }
    return super.onOptionsItemSelected(item);
  }

  private void startViewFileActivity(File file) {
    Uri uri = null;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      uri = FileProvider.getUriForFile(
          MyApplication.getContext(), "org.cloud.wetag.fileprovider", file);
    } else {
      uri = Uri.fromFile(file);
    }
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.setDataAndType(uri, "text/plain");
    startActivity(intent);
  }

  /**
   * write label to file
   * @return the output file
   * @throws IOException if IO errors
   */
  private File exportLabel() throws IOException {
    File storeDir = MyApplication.getContext()
        .getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
    storeDir = new File(storeDir, dataSet.getName());
    if (!storeDir.exists()) {
      storeDir.mkdir();
    }
    File file = new File(storeDir, dataSet.getName() + ".json");
    if (file.exists()) {
      file.delete();
    }
    file.createNewFile();
    Writer out = new FileWriter(file);
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    for (int i = 0; i < dataSet.getImageCount(); i++) {
      Image image = dataSet.getImage(i);
      if (image.getLabels().size() > 0) {
        writer.beginObject();
        writer.name("file_name");
        writer.value(image.getFileName());
        writer.name("label");
        writer.value(image.getLabels().toString());
        writer.endObject();
      }
    }
    writer.endArray();
    writer.flush();
    writer.close();
    out.close();
    return file;
  }

  private void redrawMainMenu() {
    menu.clear();
    getMenuInflater().inflate(R.menu.menu_labeling, menu);
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
    if (imageSelection.get().size() > 0 && !isEditingDataSet) {
      setEnableLabelBar(true);
    } else {
      setEnableLabelBar(false);
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.label_confirm:
        for (Image image : imageSelection.get()) {
          image.setLabels(labelSelection);
          image.saveThrows();
        }
        imageSelection.clear();
        labelSelection.clear();
        chipGroup.clearCheck();
        refreshView();
        break;
      default:
        break;
    }
  }
}
