package org.cloud.wetag.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
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
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.model.DataSetUpdateDBHelper;
import org.cloud.wetag.model.ObjectSelection;
import org.cloud.wetag.model.Sample;
import org.cloud.wetag.ui.adapter.LabelFragmentPagerAdapter;
import org.cloud.wetag.ui.adapter.SampleCardAdapter;
import org.cloud.wetag.ui.widget.LabelBar;
import org.cloud.wetag.utils.CaptureStrategy;
import org.cloud.wetag.utils.FileUtils;
import org.cloud.wetag.utils.GifSizeFilter;
import org.cloud.wetag.utils.Glide4Engine;
import org.cloud.wetag.utils.MediaStoreCompat;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LabelingActivity extends BaseActivity implements View.OnClickListener,
    SampleCardAdapter.OnSampleStateChangedListener, TabLayout.OnTabSelectedListener {

  private DataSet dataSet;
  private TabLayout tabLayout;
  private ViewPager viewPager;
  private LabelFragmentPagerAdapter adapter;
  private MediaStoreCompat mediaStoreCompat;
  private ObjectSelection objectSelection;

  private Menu menu;
  private LabelBar labelBar;

  // whether it is in deleting mode currently. (user is selecting data object for deletion)
  private boolean inDeletingMode = false;

  public static final int REQUEST_CODE_CAPTURE = 1;
  public static final int REQUEST_CODE_PREVIEW = 2;
  public static final int REQUEST_CODE_ALBUM = 3;
  public static final int REQUEST_CODE_CHOOSE_FILE = 4;

  public static void start(final Context context, final DataSet dataSet) {
    if (dataSet.getLabels().isEmpty() && dataSet.requireLabelDef()) {
      new AlertDialog.Builder(context)
          .setTitle("标签未定义，打标签前请先添加标签")
          .setPositiveButton("好的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              EditLabelActivity.start(context, dataSet);
            }
          })
          .show();
    } else {
      Intent intent = new Intent(context, LabelingActivity.class);
      intent.putExtra("dataset_name", dataSet.getName());
      context.startActivity(intent);
    }
  }

  public static void start(final Context context, final DataSet dataSet, String gotoLabel) {
    Intent intent = new Intent(context, LabelingActivity.class);
    intent.putExtra("dataset_name", dataSet.getName());
    intent.putExtra("dataset_goto_label", gotoLabel);
    context.startActivity(intent);
  }

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

    setTitle(dataSet.getName());
    initLabelBar();
    initTabs();

    if (getIntent().hasExtra("dataset_goto_label")) {
      String gotoLabel = getIntent().getStringExtra("dataset_goto_label");
      int position = dataSet.getLabels().indexOf(gotoLabel) + 2;
      tabLayout.setScrollPosition(position, 0f, true);
      viewPager.setCurrentItem(position);
    }
  }

  private void initLabelBar() {
    LinearLayout linearLayout = findViewById(R.id.label_bar);
    labelBar = new LabelBar(linearLayout, dataSet, this);
    labelBar.setEnabled(false);
    if (!dataSet.requireLabelBar()) {
      labelBar.setVisible(false);
    }
  }

  private void initTabs() {
    tabLayout = findViewById(R.id.label_tab_layout);
    viewPager = findViewById(R.id.view_pager);
    objectSelection = new ObjectSelection();
    if (!dataSet.requireObjectSelection()) {
      objectSelection.setSelectEnabled(false);
    }
    adapter = new LabelFragmentPagerAdapter(getSupportFragmentManager(), dataSet, objectSelection,
        this);
    viewPager.setAdapter(adapter);
    tabLayout.addOnTabSelectedListener(this);
    tabLayout.setupWithViewPager(viewPager, true);
  }

  @Override
  public void onBackPressed() {
    labelBar.setEnabled(false);
    if (adapter != null) {
      boolean consumed = adapter.onBackPressed();
      if (consumed) {
        return;
      }
    }
    if (inDeletingMode) {
      cancelEdit();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    this.menu = menu;
    drawMainMenu();
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
        showExportLabelDialog();
        break;
      case R.id.item_delete_data_object:
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_labeling_page_delete_object, menu);
        labelBar.setEnabled(false);
        inDeletingMode = true;
        if (dataSet.getType() == DataSet.TEXT_CLASSIFICATION ||
            dataSet.getType() == DataSet.SEQ2SEQ) {
          // make the check box visible in UI so that user can select text to delete
          objectSelection.setSelectEnabled(true);
          adapter.refreshAllFragments();
        }
        break;
      case R.id.item_delete:
        showDeleteSampleConfirmDialog();
        break;
      case R.id.item_cancel:
        cancelEdit();
        break;
      case R.id.item_add_text:
        startFileChooseActivity();
        break;
      default:
    }
    return super.onOptionsItemSelected(item);
  }

  private void cancelEdit() {
    drawMainMenu();
    inDeletingMode = false;
    if (dataSet.isText()) {
      // make the check box invisible in UI
      objectSelection.setSelectEnabled(false);
      objectSelection.clear();
      adapter.refreshAllFragments();
    }
  }

  private void startFileChooseActivity() {
    Intent intent = new Intent("android.intent.action.GET_CONTENT");
    intent.setType("test/*");
    startActivityForResult(intent, REQUEST_CODE_CHOOSE_FILE);
  }

  private void showExportLabelDialog() {
    final String filePath = FileUtils.getExportLableFilePath(getExternalFilesDir(null).getPath(), dataSet);
    String message = getResources().getString(R.string.dialog_export_label_message) + filePath;
    new AlertDialog.Builder(tabLayout.getContext())
        .setTitle(R.string.dialog_export_label_title)
        .setMessage(message)
        .setNegativeButton(R.string.dialog_button_negative, null)
        .setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            try {
              FileUtils.exportLabel(filePath, dataSet);
              Snackbar bar = Snackbar.make(tabLayout.getRootView(),
                  "导出成功", Snackbar.LENGTH_LONG);
              bar.setAction("打开", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  startViewFileActivity(filePath);
                }
              }).show();
            } catch (IOException e) {
              e.printStackTrace();
              Snackbar.make(findViewById(R.id.recycler_view),
                  "导出失败, 原因：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
          }
        })
        .show();
  }

  /**
   * show dialog and delete data object
   */
  private void showDeleteSampleConfirmDialog() {
    new AlertDialog.Builder(tabLayout.getContext())
        .setTitle(R.string.dialog_delete_sample_title)
        .setMessage(R.string.dialog_delete_image_message)
        .setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (dataSet.isText()) {
              objectSelection.setSelectEnabled(true);
            }
          }
        })
        .setPositiveButton(R.string.dialog_delete_sample_button_positive, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // delete all selected images, delete it only if it is captured in this app.
            int ignored = 0;
            int deleted = 0;
            for (Sample sample : objectSelection.get()) {
              dataSet.removeObject(sample);
              sample.delete();
              if (sample.isCapturedInApp()) {
                File imageFile = new File(sample.getSource());
                imageFile.delete();
                deleted++;
              } else {
                ignored++;
              }
            }
            String msg = "";
            if (dataSet.isImageDataSet()) {
              if (ignored != 0) {
                msg = "删除了" + deleted + "张图片, 忽略了" + ignored + "张图片（来自相册）";
              } else {
                msg = "删除了" + deleted + "张图片";
              }
            } else if (dataSet.isText()){
              msg = "删除了" + deleted + "个文本";
            }

            if (dataSet.isText()) {
              objectSelection.setSelectEnabled(false);
            }
            Snackbar.make(tabLayout.getRootView(), msg, Snackbar.LENGTH_LONG).show();
            refreshView();
            drawMainMenu();
            inDeletingMode = false;
          }
        })
        .show();
  }

  private void startMatisse() {
    Matisse.from(LabelingActivity.this)
        .choose(new HashSet<MimeType>(){{add(MimeType.JPEG);}})
        .theme(R.style.Matisse_Dracula)
        .countable(true)
        .maxSelectable(100)
        .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
        .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        .thumbnailScale(0.85f)
        .imageEngine(new Glide4Engine())    // for glide-V4
        .forResult(REQUEST_CODE_ALBUM);
  }

  private void startMatisseActivity() {
    if (ContextCompat.checkSelfPermission(LabelingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(
          LabelingActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    } else {
      startMatisse();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch (requestCode) {
      case 1:
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          startMatisse();
        } else {
          Toast.makeText(this, R.string.error_permission_denied, Toast.LENGTH_SHORT).show();
        }
        break;
      default:
    }
  }

  private void startViewFileActivity(String filePath) {
    File file = new File(filePath);
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

  private void drawMainMenu() {
    menu.clear();
    getMenuInflater().inflate(dataSet.getMenuResource(), menu);
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != RESULT_OK) {
      return;
    }
    if (requestCode == REQUEST_CODE_CAPTURE) {
      File imageFile = new File(mediaStoreCompat.getCurrentPhotoPath());
      if (imageFile.exists() && imageFile.length() > 0) {
        Sample sample = new Sample(mediaStoreCompat.getCurrentPhotoPath(), true);
        sample.saveThrows();
        dataSet.addObject(sample);
        refreshView();
      }
    } else if (requestCode == REQUEST_CODE_PREVIEW) {
      refreshView();
    } else if (requestCode == REQUEST_CODE_ALBUM) {
      // returned from image chooser, add the image to the dataset and refresh UI
      List<String> pathList = Matisse.obtainPathResult(data);
      for (String path : pathList) {
        Sample sample = new Sample(path, false);
        sample.saveThrows();
        dataSet.addObject(sample);
        dataSet.saveThrows();
      }
      refreshView();
    } else if (requestCode == REQUEST_CODE_CHOOSE_FILE) {
      // returned from file chooser, add the text to the dataset and refresh UI
      String filePath = null;
      Uri uri = data.getData();
      if (DocumentsContract.isDocumentUri(this, uri)) {
        String docId = DocumentsContract.getDocumentId(uri);
        if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
          String id = docId.split(":")[1];
          String selection = MediaStore.Images.Media._ID + "=" + id;
          filePath = getFilePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
        } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
          Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
          filePath = getFilePath(contentUri, null);
        }
      } else if ("content".equalsIgnoreCase(uri.getScheme())) {
        filePath = getFilePath(uri, null);
      } else if ("file".equalsIgnoreCase(uri.getScheme())) {
        filePath = uri.getPath();
      }
      try {
        dataSet.addSource(new File(filePath));
      } catch (IOException e) {
        e.printStackTrace();
      }
      refreshView();
    }
  }

  private String getFilePath(Uri uri, String selection) {
    String path = null;
    Cursor c = getContentResolver().query(uri, null, selection, null, null);
    if (c != null) {
      if (c.moveToFirst()) {
        path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
      }
      c.close();
    }
    return path;
  }

  private void refreshView() {
    adapter.refreshAllFragments();
    adapter.notifyDataSetChanged();
  }

  // sample clicked
  @Override
  public void onSampleClicked(Sample sample) {
    if (dataSet.isImageDataSet()) {
      ImagePreviewActivity.start(this, dataSet, sample, REQUEST_CODE_PREVIEW);
    }
  }

  @Override
  public void onSampleChipClicked(Chip chip, Sample sample) {
    if (sample.getLabels().contains(chip.getText().toString())) {
      DataSetUpdateDBHelper.removeLabel(dataSet, sample, chip.getText().toString());
    } else {
      DataSetUpdateDBHelper.addLabel(dataSet, sample, chip.getText().toString());
    }
    refreshView();
  }

  @Override
  public void refreshTab() {
    refreshView();
  }

  // sample check clicked
  @Override
  public void onSampleCheckClicked(Sample sample, boolean check) {
    if (sample.getLabels().size() > 0) {
      // gather labels from all select images
      Set<String> labels = new HashSet<>();
      for (Sample img : objectSelection.get()) {
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
    if (objectSelection.get().size() > 0 && !inDeletingMode) {
      labelBar.setEnabled(true);
    } else {
      labelBar.setChecked(false);
      labelBar.setEnabled(false);
    }
  }

  // confirm button clicked
  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.button_label_confirm:
        for (Sample sample : objectSelection.get()) {
          DataSetUpdateDBHelper.setLabels(dataSet, sample, labelBar.getLabelSelection());
        }
        objectSelection.clear();
        labelBar.clear();
        labelBar.setEnabled(false);
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
