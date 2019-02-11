package org.cloud.wetag.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
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
import java.util.HashSet;

public class MainActivity extends BaseActivity {

  private DataSetCardAdapter adapter;

  private RecyclerView recyclerView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LitePal.getDatabase();
    setContentView(R.layout.activity_main);
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
    sample.setLabels(new HashSet<>(Arrays.asList("Cat", "Dog")));
    String descDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator +
        sample.getName();
    FileUtils.copyAssetsDir2Phone(this, "Example", descDir);
    File[] imageFiles = new File(descDir).listFiles();
    if (imageFiles.length > 0) {
      for (File imageFile : imageFiles) {
        Image image = new Image(sample.getName(), imageFile.getName());
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
          dataSet.setLabels(new HashSet<>(Arrays.asList(labelArray)));
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
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
