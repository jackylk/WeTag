package org.cloud.wetag.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.JsonWriter;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;

import org.cloud.wetag.MyApplication;
import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.model.Image;
import org.cloud.wetag.model.ImageSelection;
import org.cloud.wetag.ui.adapter.ImageCardAdapter;
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

public class LabelActivity extends BaseActivity implements View.OnClickListener,
    ImageCardAdapter.OnImageCheckChangedListener, TabLayout.OnTabSelectedListener {

  private DataSet dataSet;
  private TabLayout tabLayout;
  private LabelFragmentPagerAdapter adapter;
  private MediaStoreCompat mediaStoreCompat;
  private ImageSelection imageSelection;

  private Menu menu;
  private LabelBar labelBar;
  private boolean isEditingDataSet = false;

  private static final int REQUEST_CODE_CAPTURE = 1;
  private static final int REQUEST_CODE_PREVIEW = 2;
  private static final int REQUEST_CODE_ALBUM = 3;

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

    LinearLayout linearLayout = findViewById(R.id.label_bar);
    labelBar = new LabelBar(linearLayout, dataSet, this);
    labelBar.setEnableLabelBar(false);
    setTitle(dataSet.getName() + "数据集");
    initTabs();
  }

  private void initTabs() {
    tabLayout = findViewById(R.id.label_tab_layout);
    ViewPager viewPager = findViewById(R.id.view_pager);
    imageSelection = new ImageSelection();
    adapter = new LabelFragmentPagerAdapter(getSupportFragmentManager(), dataSet, imageSelection,
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
            for (Image image : imageSelection.get()) {
              dataSet.removeImage(image);
              image.delete();
              if (image.isCapturedInApp()) {
                File imageFile = new File(image.getFilePath());
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
        writer.name("file");
        writer.value(image.getFilePath());
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
        Image image = new Image(dataSet.getName(), mediaStoreCompat.getCurrentPhotoPath(), true);
        image.saveThrows();
        dataSet.addImage(image);
        dataSet.saveThrows();
        refreshView();
      }
    } else if (requestCode == REQUEST_CODE_PREVIEW) {
      refreshView();
    } else if (requestCode == REQUEST_CODE_ALBUM) {
      List<String> pathList = Matisse.obtainPathResult(data);
      for (String path : pathList) {
        Image image = new Image(dataSet.getName(), path, false);
        image.saveThrows();
        dataSet.addImage(image);
        dataSet.saveThrows();
      }
      refreshView();
    }
  }

  private void refreshView() {
    adapter.refreshAllFragments();
    adapter.notifyDataSetChanged();
  }

  // image clicked
  @Override
  public void onImageClicked(Image image) {
    ImagePreviewActivity.start(this, dataSet, image, REQUEST_CODE_PREVIEW);
  }

  // image check clicked
  @Override
  public void onImageCheckClicked(Image image, boolean check) {
    if (image.getLabels().size() > 0) {
      // gather labels from all select images
      Set<String> labels = new HashSet<>();
      for (Image img : imageSelection.get()) {
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
    if (imageSelection.get().size() > 0 && !isEditingDataSet) {
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
        for (Image image : imageSelection.get()) {
          image.setLabels(labelBar.getLabelSelection());
          image.saveThrows();
        }
        imageSelection.clear();
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
