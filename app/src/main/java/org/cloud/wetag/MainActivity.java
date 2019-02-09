package org.cloud.wetag;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.cloud.wetag.dataset.DataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends BaseActivity {

  private DataSetAdapter adapter;

  private List<DataSet> dataSetList;

  private RecyclerView recyclerView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
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
    dataSetList = setupSampleDataSet();
    adapter = new DataSetAdapter(dataSetList);
    recyclerView.setAdapter(adapter);
  }

  private List<DataSet> setupSampleDataSet() {
    List<DataSet> dataSets = new ArrayList<>();
    dataSets.add(new DataSet("Pets", new HashSet<>(Arrays.asList("Cat", "Dog"))));
    return dataSets;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    switch (requestCode) {
      case 1:
        if (resultCode == RESULT_OK) {
          String datasetName = data.getStringExtra("dataset_name");
          String datasetLabels = data.getStringExtra("dataset_labels");
          String[] labelArray = datasetLabels.split(",");
          DataSet dataSet = new DataSet(
              datasetName, new HashSet<String>(Arrays.asList(labelArray)));
          addDataSet(dataSet);
          adapter.notifyItemInserted(dataSetList.size() - 1);
          recyclerView.scrollToPosition(dataSetList.size() - 1);
        }
    }
  }

  public void addDataSet(DataSet dataSet) {
    dataSetList.add(dataSet);
  }

  public void removeDataSet(final String dataSetName) {
    for (DataSet dataSet : dataSetList) {
      if (dataSet.getName().equals(dataSetName)) {
        dataSetList.remove(dataSet);
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
