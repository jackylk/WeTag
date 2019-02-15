package org.cloud.wetag.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.model.Image;
import org.cloud.wetag.ui.adapter.DataSetCardAdapter;
import org.cloud.wetag.utils.FileUtils;
import org.litepal.LitePal;

import java.io.File;
import java.util.Arrays;

public class MainActivity extends BaseActivity {

  private DataSetCardAdapter adapter;

  private RecyclerView recyclerView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LitePal.getDatabase();
    setContentView(R.layout.activity_main);

    setTitle(R.string.main_activity_title);
    getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    FloatingActionButton fab = findViewById(R.id.fab_add_dataset);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, CreateDataSetActivity.class);
        startActivityForResult(intent, 1);
      }
    });

    recyclerView = findViewById(R.id.recycler_view);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    adapter = new DataSetCardAdapter();
    recyclerView.setAdapter(adapter);

    if (DataSetCollection.getDataSetList().isEmpty()) {
      addExampleDataSet();
    }
  }

  /**
   * Add an Example data set
   */
  private void addExampleDataSet() {
    DataSet sample = new DataSet("Example");
    sample.setDesc("猫狗图片分类数据集");
    sample.setLabels(Arrays.asList("Cat", "Dog"));
    String destDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator +
        sample.getName();
    FileUtils.copyAssetsDir2Phone(this, "Example", destDir);
    File[] imageFiles = new File(destDir).listFiles();
    if (imageFiles.length > 0) {
      for (File imageFile : imageFiles) {
        Image image = new Image(sample.getName(), imageFile.getPath(), true);
        image.saveThrows();
        sample.addImage(image);
      }
    }
    DataSetCollection.addDataSet(sample);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    switch (requestCode) {
      case 1:
        if (resultCode == RESULT_OK) {
          String datasetName = data.getStringExtra("dataset_name");
          String datasetLabels = data.getStringExtra("dataset_labels");
          String[] labelArray = datasetLabels.split(",");
          DataSet dataSet = new DataSet(datasetName);
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
