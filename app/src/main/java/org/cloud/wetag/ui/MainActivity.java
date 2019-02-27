package org.cloud.wetag.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.ui.adapter.DataSetCardAdapter;
import org.cloud.wetag.utils.FileUtils;
import org.litepal.LitePal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends BaseActivity implements
    DataSetCardAdapter.OnEditDataSetClickListener {

  private DataSetCardAdapter adapter;

  private RecyclerView recyclerView;

  private static final int REQUEST_CODE_CREATE_DATASET = 1;
  private static final int REQUEST_CODE_UPDATE_DATASET = 2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LitePal.getDatabase();
    setContentView(R.layout.activity_main);

    setTitle(R.string.app_name);
    getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    recyclerView = findViewById(R.id.recycler_view);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    String[] dataSetType = getResources().getStringArray(R.array.dataset_type_array);
    adapter = new DataSetCardAdapter(dataSetType);
    adapter.registerEditDataSetClickListener(this);
    recyclerView.setAdapter(adapter);

    // when user launch this app for first time, add some example datasets
    if (DataSetCollection.getDataSetList().isEmpty()) {
      addExampleCatDogDataSet();
      addExampleMovieCommentDataSet();
      addExampleSeq2SeqDataSet();
    }
  }

  /**
   * Add an example data set: cat and dog image classification
   */
  private void addExampleCatDogDataSet() {
    DataSet sample = DataSet.newImageDataSet("样例数据集1");
    sample.setDesc("kaggle上的一个竞赛数据集：Dogs vs. Cats");
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
    DataSet sample = DataSet.newTextClassificationDataSet("样例数据集2");
    sample.setDesc("网上收集的《流浪地球》电影评论，对文本情感分类");
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

  /**
   * Add an example seq2seq data set
   */
  private void addExampleSeq2SeqDataSet() {
    DataSet sample = DataSet.newSeq2SeqDataSet("样例数据集3");
    sample.setDesc("英文文本翻译");
    String destDir = createDataSetSourceFolder(sample.getName());
    FileUtils.copyAssetsDir2Phone(this, "Seq2SeqExample", destDir);
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
    if (requestCode != RESULT_OK) {
      return;
    }
    switch (requestCode) {
      case REQUEST_CODE_CREATE_DATASET:
        adapter.notifyItemInserted(DataSetCollection.getDataSetList().size() - 1);
        recyclerView.scrollToPosition(DataSetCollection.getDataSetList().size() - 1);
        break;
      case REQUEST_CODE_UPDATE_DATASET:
        if (data.hasExtra("position")) {
          int position = data.getIntExtra("position", 0);
          adapter.notifyItemChanged(position);
        }
        break;
      default:
        throw new UnsupportedOperationException();
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
      case R.id.add_dataset:
        String[] choices = getResources().getStringArray(R.array.dataset_type_array);
        final List<Integer> selected = new LinkedList<Integer>() {{add(0);}};
        new AlertDialog.Builder(MainActivity.this)
            .setTitle(R.string.dialog_create_dataset_title)
            .setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                selected.clear();
                selected.add(which);
              }
            })
            .setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                EditDataSetActivity.startCreateDataSetActivity(
                    MainActivity.this, REQUEST_CODE_CREATE_DATASET, selected.get(0));
              }
            })
            .setNegativeButton(R.string.dialog_button_negative, null)
            .show();
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

  @Override
  public void onEditDataSetClicked(DataSet dataSet) {
    EditDataSetActivity.startUpdateDataSetActivity(
        this, REQUEST_CODE_UPDATE_DATASET, dataSet.getName());
  }
}
