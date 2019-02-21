package org.cloud.wetag.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.cloud.wetag.MyApplication;
import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;

import java.util.Arrays;
import java.util.List;

public class EditDataSetActivity extends BaseActivity implements View.OnClickListener {

  private EditText name;
  private EditText desc;
  private EditText labels;
  private int datasetType;
  private int mode;

  public static final int CREATE_DATASET = 0;
  public static final int UPDATE_DATASET = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_data_set);

    Intent intent = getIntent();
    mode = intent.getIntExtra("mode", CREATE_DATASET);

    name = findViewById(R.id.dataset_name_input);
    desc = findViewById(R.id.dataset_desc_input);
    labels = findViewById(R.id.dataset_labels_input);

    if (mode == CREATE_DATASET) {
      datasetType = intent.getIntExtra("dataset_type", 0);
      name.setHint("请输入数据集名字");
      desc.setHint("可选项，一句话描述数据集");
      labels.setHint("用于打标签的标签名，以逗号分隔");
      setTitle("新建数据集");
    } else {
      String dataSetName = intent.getStringExtra("dataset_name");
      DataSet dataSet = DataSetCollection.getDataSet(dataSetName);
      name.setText(dataSet.getName());
      desc.setText(dataSet.getDesc());
      List<String> labelList = dataSet.getLabels();
      StringBuilder builder = new StringBuilder();
      for (String label : labelList) {
        builder.append(label).append(" ");
      }
      labels.setText(builder.toString());
      setTitle("修改数据集");
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

  @Override
  public void onClick(View v) {
    saveResultAndReturnActivity(v.getContext());
  }

  private void saveResultAndReturnActivity(Context context) {
    if (!isValidInput(context)) {
      return;
    }

    String datasetName = name.getText().toString();
    String datasetDesc = desc.getText().toString();
    String allLabelString = labels.getText().toString();
    String[] labelArray = allLabelString.split(",");
    if (labelArray.length < 2) {
      labelArray = allLabelString.split("，");
    }

    DataSet dataSet = new DataSet(datasetName, datasetType);
    if (datasetDesc.isEmpty()) {
      datasetDesc = "无描述";
    }
    dataSet.setDesc(datasetDesc);
    dataSet.setLabels(Arrays.asList(labelArray));
    DataSetCollection.addDataSet(dataSet);

    Intent intent = new Intent();
    setResult(RESULT_OK, intent);

    finish();
  }

  private boolean isValidInput(Context context) {
    if (name.getText().toString().isEmpty() || labels.getText().toString().isEmpty()) {
      Toast.makeText(context, "必须填写名称和标签", Toast.LENGTH_LONG).show();
      return false;
    }
    if (DataSetCollection.getDataSet(name.getText().toString()) != null) {
      Toast.makeText(context, "数据集名称已存在", Toast.LENGTH_LONG).show();
      return false;
    }
    String[] labelArray = labels.getText().toString().split(",");
    if (labelArray.length < 2) {
      labelArray = labels.getText().toString().split("，");
      if (labelArray.length < 2) {
        Toast.makeText(context, "标签必须要有两个或以上，以逗号分隔", Toast.LENGTH_LONG).show();
        return false;
      }
    }
    return true;
  }

  public static void startCreateDataSetActivity(Activity parent, int requestCode,
                                                int datasetType) {
    Intent intent = new Intent(parent, EditDataSetActivity.class);
    intent.putExtra("mode", EditDataSetActivity.CREATE_DATASET);
    if (datasetType == DataSet.IMAGE) {
      intent.putExtra("dataset_type", DataSet.IMAGE);
    } else if (datasetType == DataSet.TEXT_CLASSIFICATION) {
      intent.putExtra("dataset_type", DataSet.TEXT_CLASSIFICATION);
    } else {
      throw new UnsupportedOperationException();
    }
    parent.startActivityForResult(intent, requestCode);
  }

  public static void startUpdateDataSetActivity(Context context, String datasetName) {
    Intent intent = new Intent(context, EditDataSetActivity.class);
    intent.putExtra("mode", EditDataSetActivity.UPDATE_DATASET);
    intent.putExtra("dataset_name", datasetName);
    context.startActivity(intent);
  }
}
