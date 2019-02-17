package org.cloud.wetag.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataObject;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.ui.adapter.DataSetCardAdapter;
import org.cloud.wetag.utils.FileUtils;
import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class MainActivity extends BaseActivity {

  private DataSetCardAdapter adapter;

  private RecyclerView recyclerView;

  private static final int REQUEST_CODE_CREATE_DATASET = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LitePal.getDatabase();
    setContentView(R.layout.activity_main);

    setTitle(R.string.main_activity_title);
    getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    FloatingActionButton fab = findViewById(R.id.fab_add_image_dataset);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, CreateDataSetActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CREATE_DATASET);
      }
    });

    recyclerView = findViewById(R.id.recycler_view);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    adapter = new DataSetCardAdapter();
    recyclerView.setAdapter(adapter);

    // when user launch this app for first time, add some example datasets
    if (DataSetCollection.getDataSetList().isEmpty()) {
      addExampleCatDogDataSet();
      addExampleMovieCommentDataSet();
    }
  }

  /**
   * Add an example data set: cat and dog image classification
   */
  private void addExampleCatDogDataSet() {
    DataSet sample = DataSet.newImageDataSet("猫狗图片分类");
    sample.setDesc("样例数据集1：猫狗图片分类");
    sample.setLabels(Arrays.asList("Cat", "Dog"));
    String destDir = createDataSetSourceFolder(sample.getName());
    FileUtils.copyAssetsDir2Phone(this, "ImageClassificationExample", destDir);
    File[] files = new File(destDir).listFiles();
    if (files.length > 0) {
      for (File file : files) {
        if (!file.isDirectory()) {
          try {
            sample.addSource(file);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    DataSetCollection.addDataSet(sample);
  }

  /**
   * Add an example data set: movie comment text classification
   */
  private void addExampleMovieCommentDataSet() {
    DataSet sample = DataSet.newTextClassificationDataSet("《流浪地球》短评文本分类");
    sample.setDesc("样例数据集2：电影评论文本分类");
    sample.setLabels(Arrays.asList("正面", "负面", "中性"));
    String destDir = createDataSetSourceFolder(sample.getName());
    FileUtils.copyAssetsDir2Phone(this, "TextClassificationExample", destDir);
    File[] files = new File(destDir).listFiles();
    if (files.length > 0) {
      try {
        for (File file : files) {
          if (!file.isDirectory()) {
            sample.addSource(file);
          }
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    DataSetCollection.addDataSet(sample);
  }



  private String createDataSetSourceFolder(String dataSetName) {
    File storeDir = new File(getExternalFilesDir(null) + File.separator + dataSetName);
    if (!storeDir.exists()) {
      storeDir.mkdir();
    }
    storeDir = new File(storeDir, "source");
    if (!storeDir.exists()) {
      storeDir.mkdir();
    }
    return storeDir.getPath();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    switch (requestCode) {
      case REQUEST_CODE_CREATE_DATASET:
        if (resultCode == RESULT_OK) {
          String datasetName = data.getStringExtra("dataset_name");
          String datasetLabels = data.getStringExtra("dataset_labels");
          String[] labelArray = datasetLabels.split(",");

          // TODO: add branch for text dataset
          DataSet dataSet = DataSet.newImageDataSet(datasetName);
          dataSet.setLabels(Arrays.asList(labelArray));
          DataSetCollection.addDataSet(dataSet);
          adapter.notifyItemInserted(DataSetCollection.getDataSetList().size() - 1);
          recyclerView.scrollToPosition(DataSetCollection.getDataSetList().size() - 1);
        }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    switch (item.getItemId()) {
      case R.id.item_login:
      case R.id.item_get_job:
      case R.id.item_score:
      case R.id.item_setting:
        new AlertDialog.Builder(this).setMessage("开发中，敬请期待").show();
        break;
      default:
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onPostResume() {
    super.onPostResume();
    adapter.notifyDataSetChanged();
  }
}
