package org.cloud.wetag.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
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
import org.cloud.wetag.utils.FileUtils;
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

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

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

  public static final int REQUEST_CODE_CAPTURE = 1;
  public static final int REQUEST_CODE_PREVIEW = 2;
  public static final int REQUEST_CODE_ALBUM = 3;
  public static final int REQUEST_CODE_CHOOSE_FILE = 4;

  public static void start(Context context, DataSet dataSet) {
    Intent intent = new Intent(context, DataObjectLabelingActivity.class);
    intent.putExtra("dataset_name", dataSet.getName());
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
    if (dataSet.isTextClassificationDataSet()) {
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
    if (isEditingDataSet) {
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
      case R.id.item_edit:
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_labeling_edit, menu);
        labelBar.setEnableLabelBar(false);
        isEditingDataSet = true;
        if (dataSet.isTextClassificationDataSet()) {
          // make the check box visible in UI so that user can select text to delete
          objectSelection.setSelectEnabled(true);
          adapter.refreshAllFragments();
        }
        break;
      case R.id.item_delete:
        showDeleteDataObjectConfirmDialog();
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
    isEditingDataSet = false;
    if (dataSet.isTextClassificationDataSet()) {
      // make the check box invisible in UI
      objectSelection.setSelectEnabled(false);
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
        .setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // do nothing
          }
        })
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
  private void showDeleteDataObjectConfirmDialog() {
    new AlertDialog.Builder(tabLayout.getContext())
        .setTitle(R.string.dialog_delete_dataobject_title)
        .setMessage(R.string.dialog_delete_image_message)
        .setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (dataSet.isTextClassificationDataSet()) {
              objectSelection.setSelectEnabled(true);
            }
          }
        })
        .setPositiveButton(R.string.dialog_delete_dataobject_button_positive, new DialogInterface.OnClickListener() {
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
            String msg = "";
            if (dataSet.isImageDataSet()) {
              if (ignored != 0) {
                msg = "删除了" + deleted + "张图片, 忽略了" + ignored + "张图片（来自相册）";
              } else {
                msg = "删除了" + deleted + "张图片";
              }
            } else if (dataSet.isTextClassificationDataSet()){
              msg = "删除了" + deleted + "个文本";
            }

            if (dataSet.isTextClassificationDataSet()) {
              objectSelection.setSelectEnabled(false);
            }
            Snackbar.make(tabLayout.getRootView(), msg, Snackbar.LENGTH_LONG).show();
            refreshView();
            drawMainMenu();
            isEditingDataSet = false;
          }
        })
        .show();
  }

  private void startMatisseActivity() {
    RxPermissions rxPermissions = new RxPermissions(this);
    rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
        .subscribe(new Observer<Boolean>() {
          @Override
          public void onSubscribe(Disposable d) {

          }

          @Override
          public void onNext(Boolean aBoolean) {
            Matisse.from(DataObjectLabelingActivity.this)
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

          @Override
          public void onError(Throwable e) {
            Log.e(DataObjectLabelingActivity.this.getClass().getName(), e.toString());
          }

          @Override
          public void onComplete() {

          }
        });
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
    if (dataSet.isImageDataSet()) {
      getMenuInflater().inflate(R.menu.menu_image_labeling, menu);
    } else if (dataSet.isTextClassificationDataSet()) {
      getMenuInflater().inflate(R.menu.menu_text_labeling, menu);
    }
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
        DataObject dataObject = new DataObject(dataSet.getName(), mediaStoreCompat.getCurrentPhotoPath(), true);
        dataObject.saveThrows();
        dataSet.addObject(dataObject);
        dataSet.saveThrows();
        refreshView();
      }
    } else if (requestCode == REQUEST_CODE_PREVIEW) {
      refreshView();
    } else if (requestCode == REQUEST_CODE_ALBUM) {
      // returned from image chooser, add the image to the dataset and refresh UI
      List<String> pathList = Matisse.obtainPathResult(data);
      for (String path : pathList) {
        DataObject dataObject = new DataObject(dataSet.getName(), path, false);
        dataObject.saveThrows();
        dataSet.addObject(dataObject);
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

  // dataObject clicked
  @Override
  public void onDataObjectClicked(DataObject dataObject) {
    if (dataSet.isImageDataSet()) {
      ImagePreviewActivity.start(this, dataSet, dataObject, REQUEST_CODE_PREVIEW);
    }
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
