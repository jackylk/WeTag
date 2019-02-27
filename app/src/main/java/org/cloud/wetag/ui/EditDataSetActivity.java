package org.cloud.wetag.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.cloud.wetag.MyApplication;
import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;

public class EditDataSetActivity extends BaseActivity {

  private EditText name;
  private EditText desc;

  // only for create case
  private int datasetType;

  // only for update case
  private DataSet dataSet;
  private int position;

  // can be create or update
  private int mode;

  public static final int CREATE_DATASET = 0;
  public static final int UPDATE_DATASET = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_data_set);

    Intent intent = getIntent();
    mode = intent.getIntExtra("mode", CREATE_DATASET);

    name = findViewById(R.id.dataset_name_input);
    desc = findViewById(R.id.dataset_desc_input);

    if (mode == CREATE_DATASET) {
      datasetType = intent.getIntExtra("dataset_type", 0);
      name.setHint("请输入数据集名字");
      desc.setHint("可选项，一句话描述数据集");
      setTitle("新建数据集");
    } else if (mode == UPDATE_DATASET) {
      String dataSetName = intent.getStringExtra("dataset_name");
      dataSet = DataSetCollection.getDataSet(dataSetName);
      name.setText(dataSet.getName());
      desc.setText(dataSet.getDesc());
      position = DataSetCollection.getDataSetList().indexOf(dataSet);
      setTitle("修改数据集");
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_edit_dataset, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.item_save) {
      saveResultAndReturnActivity(MyApplication.getContext());
    }
    return super.onOptionsItemSelected(item);
  }

  private void saveResultAndReturnActivity(Context context) {
    if (!isValidInput(context)) {
      return;
    }

    String datasetName = name.getText().toString();
    String datasetDesc = desc.getText().toString();
    if (datasetDesc.isEmpty()) {
      datasetDesc = "无描述";
    }

    Intent intent = new Intent();
    if (mode == CREATE_DATASET) {
      DataSet dataSet = new DataSet(datasetName, datasetType);
      dataSet.setDesc(datasetDesc);
      DataSetCollection.addDataSet(dataSet);
    } else if (mode == UPDATE_DATASET) {
      int id = dataSet.getId();
      dataSet.setName(datasetName);
      dataSet.setDesc(datasetDesc);
      dataSet.update(id);
      intent.putExtra("position", position);
    } else {
      throw new UnsupportedOperationException();
    }

    setResult(RESULT_OK, intent);
    finish();
  }

  private boolean isValidInput(Context context) {
    if (name.getText().toString().isEmpty()) {
      Toast.makeText(context, "必须填写名称和标签", Toast.LENGTH_LONG).show();
      return false;
    }
    if (mode == CREATE_DATASET && DataSetCollection.getDataSet(name.getText().toString()) != null) {
      Toast.makeText(context, "数据集名称已存在", Toast.LENGTH_LONG).show();
      return false;
    }
    return true;
  }

  public static void startCreateDataSetActivity(Activity parent, int requestCode, int datasetType) {
    Intent intent = new Intent(parent, EditDataSetActivity.class);
    intent.putExtra("mode", EditDataSetActivity.CREATE_DATASET);
    if (datasetType == DataSet.IMAGE) {
      intent.putExtra("dataset_type", DataSet.IMAGE);
    } else if (datasetType == DataSet.TEXT_CLASSIFICATION) {
      intent.putExtra("dataset_type", DataSet.TEXT_CLASSIFICATION);
    } else if (datasetType == DataSet.SEQ2SEQ) {
      intent.putExtra("dataset_type", DataSet.SEQ2SEQ);
    } else {
      throw new UnsupportedOperationException();
    }
    parent.startActivityForResult(intent, requestCode);
  }

  public static void startUpdateDataSetActivity(Activity parent, int requestCode,
                                                String datasetName) {
    Intent intent = new Intent(parent, EditDataSetActivity.class);
    intent.putExtra("mode", EditDataSetActivity.UPDATE_DATASET);
    intent.putExtra("dataset_name", datasetName);
    parent.startActivityForResult(intent, requestCode);
  }
}
