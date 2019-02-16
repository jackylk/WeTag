package org.cloud.wetag.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.JsonWriter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;

import org.cloud.wetag.MyApplication;
import org.cloud.wetag.R;
import org.cloud.wetag.model.DataObject;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.model.ObjectSelection;
import org.cloud.wetag.ui.adapter.DataObjectCardAdapter;
import org.cloud.wetag.ui.adapter.LabelFragmentPagerAdapter;
import org.cloud.wetag.ui.widget.LabelBar;
import org.cloud.wetag.utils.CaptureStrategy;
import org.cloud.wetag.utils.GifSizeFilter;
import org.cloud.wetag.utils.Glide4Engine;
import org.cloud.wetag.utils.MediaStoreCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataObjectLabelingActivity extends BaseActivity implements View.OnClickListener,
    DataObjectCardAdapter.OnDataObjectCheckChangedListener, TabLayout.OnTabSelectedListener {

  private DataSet dataSet;
  private TabLayout tabLayout;
  private LabelFragmentPagerAdapter adapter;
  private MediaStoreCompat mediaStoreCompat;
  private ObjectSelection objectSelection;

  private Menu menu;
  private LabelBar labelBar;
  private boolean isEditingDataSet = false;

  private static final int REQUEST_CODE_CAPTURE = 1;
  public static final int REQUEST_CODE_PREVIEW = 2;
  private static final int REQUEST_CODE_ALBUM = 3;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_labeling);

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

    setTitle(dataSet.getName() + "数据集");
    initLabelBar();
    initTabs();
  }

  private void initLabelBar() {
    LinearLayout linearLayout = findViewById(R.id.label_bar);
    labelBar = new LabelBar(linearLayout, dataSet, this);
    labelBar.setEnableLabelBar(false);
    if (dataSet.isTextClassificationDataSet()) {
      labelBar.setVisible(false);
    }
  }

  private void initTabs() {
    tabLayout = findViewById(R.id.label_tab_layout);
    ViewPager viewPager = findViewById(R.id.view_pager);
    objectSelection = new ObjectSelection();
    adapter = new LabelFragmentPagerAdapter(getSupportFragmentManager(), dataSet, objectSelection,
        this);
    viewPager.setAdapter(adapter);
    tabLayout.addOnTabSelectedListener(this);
    tabLayout.setupWithViewPager(viewPager, true);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    this.menu = menu;
    getMenuInflater().inflate(R.menu.menu_labeling, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.item_camera:
        if (mediaStoreCompat != null) {
          mediaStoreCompat.dispatchCaptureIntent(this, REQUEST_CODE_CAPTURE);
        }
        break;
      case R.id.item_import_album:
        startMatisseActivity();
        break;
      case R.id.item_export:
        try {
          final File file = exportLabel();
          Snackbar bar = Snackbar.make(tabLayout.getRootView(),
               "标签文件导出成功: " + file.getName(), Snackbar.LENGTH_LONG);
          bar.setAction("打开", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              startViewFileActivity(file);
            }
          }).show();
        } catch (IOException e) {
            e.printStackTrace();
            Snackbar.make(item.getActionView(),
                "标签文件导出失败, 原因：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
        break;
      case R.id.item_edit:
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_labeling_edit, menu);
        labelBar.setEnableLabelBar(false);
        isEditingDataSet = true;
        break;
      case R.id.item_delete:
        showDeleteImageConfirmDialog();
        break;
      case R.id.item_cancel:
        redrawMainMenu();
        isEditingDataSet = false;
        break;
      default:
    }
    return super.onOptionsItemSelected(item);
  }

  private void showDeleteImageConfirmDialog() {
    new AlertDialog.Builder(tabLayout.getContext())
        .setTitle(R.string.dialog_delete_image_title)
        .setMessage(R.string.dialog_delete_image_message)
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // do nothing
          }
        })
        .setPositiveButton(R.string.button_positive, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // delete all selected images, delete it only if it is captured in this app.
            int ignored = 0;
            int deleted = 0;
            for (DataObject dataObject : objectSelection.get()) {
              dataSet.removeObject(dataObject);
              dataObject.delete();
              if (dataObject.isCapturedInApp()) {
                File imageFile = new File(dataObject.getSource());
                imageFile.delete();
                deleted++;
              } else {
                ignored++;
              }
            }
            String msg;
            if (ignored != 0) {
              msg = "删除了" + deleted + "张图片, 忽略了" + ignored + "张图片（来自相册）";
            } else {
              msg = "删除了" + deleted + "张图片";
            }
            Snackbar.make(tabLayout.getRootView(), msg, Snackbar.LENGTH_LONG).show();
            refreshView();
            redrawMainMenu();
            isEditingDataSet = false;
          }
        })
        .show();
  }

  private void startMatisseActivity() {
    Matisse.from(this)
        .choose(new HashSet<MimeType>(){{add(MimeType.JPEG);}})
        .countable(true)
        .maxSelectable(100)
        .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
        .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        .thumbnailScale(0.85f)
        .imageEngine(new Glide4Engine())    // for glide-V4
        .forResult(REQUEST_CODE_ALBUM);
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
    File storeDir = new File(getExternalFilesDir(null), dataSet.getName());
    if (!storeDir.exists()) {
      storeDir.mkdir();
    }
    storeDir = new File(storeDir, "output");
    if (!storeDir.exists()) {
      storeDir.mkdir();
    }
    File file = new File(storeDir, "manifest.json");
    if (file.exists()) {
      file.delete();
    }
    file.createNewFile();
    Writer out = new FileWriter(file);
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    for (int i = 0; i < dataSet.getObjectCount(); i++) {
      DataObject dataObject = dataSet.getDataObject(i);
      if (dataObject.getLabels().size() > 0) {
        writer.beginObject();
        writer.name("file");
        writer.value(dataObject.getSource());
        writer.name("label");
        writer.value(dataObject.getLabels().toString());
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
        DataObject dataObject = new DataObject(dataSet.getName(), mediaStoreCompat.getCurrentPhotoPath(), true);
        dataObject.saveThrows();
        dataSet.addObject(dataObject);
        dataSet.saveThrows();
        refreshView();
      }
    } else if (requestCode == REQUEST_CODE_PREVIEW) {
      refreshView();
    } else if (requestCode == REQUEST_CODE_ALBUM) {
      List<String> pathList = Matisse.obtainPathResult(data);
      for (String path : pathList) {
        DataObject dataObject = new DataObject(dataSet.getName(), path, false);
        dataObject.saveThrows();
        dataSet.addObject(dataObject);
        dataSet.saveThrows();
      }
      refreshView();
    }
  }

  private void refreshView() {
    adapter.refreshAllFragments();
    adapter.notifyDataSetChanged();
  }

  // dataObject clicked
  @Override
  public void onDataObjectClicked(DataObject dataObject) {
      ImagePreviewActivity.start(this, dataSet, dataObject, REQUEST_CODE_PREVIEW);
  }

  @Override
  public void onDataObjectChipClicked(Chip chip, DataObject dataObject) {
    if (dataObject.getLabels().contains(chip.getText().toString())) {
      dataObject.removeLabel(chip.getText().toString());
    } else {
      dataObject.addLabel(chip.getText().toString());
    }
    refreshView();
  }

  // dataObject check clicked
  @Override
  public void onDataObjectCheckClicked(DataObject dataObject, boolean check) {
    if (dataObject.getLabels().size() > 0) {
      // gather labels from all select images
      Set<String> labels = new HashSet<>();
      for (DataObject img : objectSelection.get()) {
        labels.addAll(img.getLabels());
      }
      // set chips status in label bar
      for (Map.Entry<String, Chip> chipEntry : labelBar.getChipMap().entrySet()) {
        if (labels.contains(chipEntry.getKey())) {
          chipEntry.getValue().setChecked(true);
        } else {
          chipEntry.getValue().setChecked(false);
        }
      }
    }
    if (objectSelection.get().size() > 0 && !isEditingDataSet) {
      labelBar.setEnableLabelBar(true);
    } else {
      labelBar.setCheckedLabelBar(false);
      labelBar.setEnableLabelBar(false);
    }
  }

  // confirm button clicked
  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.label_confirm:
        for (DataObject dataObject : objectSelection.get()) {
          dataObject.setLabels(labelBar.getLabelSelection());
          dataObject.saveThrows();
        }
        objectSelection.clear();
        labelBar.clear();
        labelBar.setEnableLabelBar(false);
        refreshView();
        break;
      default:
        break;
    }
  }

  // tab selected
  @Override
  public void onTabSelected(TabLayout.Tab tab) {
    int position = tab.getPosition();
    adapter.refreshFragment(position);
  }

  @Override
  public void onTabUnselected(TabLayout.Tab tab) { }

  @Override
  public void onTabReselected(TabLayout.Tab tab) { }


}
