package org.cloud.wetag.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.util.JsonWriter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.cloud.wetag.MyApplication;
import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.model.Image;
import org.cloud.wetag.model.ImageSelection;
import org.cloud.wetag.ui.adapter.LabelFragmentPagerAdapter;
import org.cloud.wetag.utils.CaptureStrategy;
import org.cloud.wetag.utils.MediaStoreCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class LabelActivity extends BaseActivity {

  private DataSet dataSet;
  private LabelFragmentPagerAdapter adapter;
  private MediaStoreCompat mediaStoreCompat;
  private ImageSelection imageSelection;

  private Menu menu;
  private boolean isEditingDataSet = false;

  private static final int REQUEST_CODE_CAPTURE = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_label);

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
    initTabs();
  }

  private void initTabs() {
    TabLayout tabLayout = findViewById(R.id.label_tab_layout);
    ViewPager viewPager = findViewById(R.id.view_pager);
    imageSelection = new ImageSelection();
    adapter = new LabelFragmentPagerAdapter(getSupportFragmentManager(), dataSet, imageSelection);
    viewPager.setAdapter(adapter);
    tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override
      public void onTabSelected(TabLayout.Tab tab) {
        int position = tab.getPosition();
        adapter.refreshFragment(position);
      }

      @Override
      public void onTabUnselected(TabLayout.Tab tab) {

      }

      @Override
      public void onTabReselected(TabLayout.Tab tab) {

      }
    });
    tabLayout.setupWithViewPager(viewPager);
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
      case R.id.item_export:
        try {
          final File file = exportLabel();
          Snackbar bar = Snackbar.make(item.getActionView(),
               "标签文件导出成功: " + file.getName(), Snackbar.LENGTH_SHORT);
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
        adapter.setEnableLabelBar(false);
        isEditingDataSet = true;
        break;
      case R.id.item_delete:
        final int num = imageSelection.get().size();
        new AlertDialog.Builder(MyApplication.getContext())
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
                Snackbar.make(item.getActionView(),
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
    adapter.refreshAllFragments();
  }
}
