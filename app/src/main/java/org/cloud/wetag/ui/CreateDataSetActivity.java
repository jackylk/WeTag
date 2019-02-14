package org.cloud.wetag.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSetCollection;

public class CreateDataSetActivity extends BaseActivity implements View.OnClickListener {

  private EditText name;
  private EditText desc;
  private EditText labels;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_data_set);

    name = findViewById(R.id.dataset_name_input);
    name.setHint("请输入数据集名字");
    desc = findViewById(R.id.dataset_desc_input);
    desc.setHint("一句话描述数据集");
    labels = findViewById(R.id.dataset_labels_input);
    labels.setHint("用于打标签的标签名，以逗号分隔");

    Button button = findViewById(R.id.create_dataset_button);
    button.setOnClickListener(this);
    setTitle("创建数据集");
  }

  private boolean isValidInput(View v) {
    if (name.getText().toString().isEmpty() || labels.getText().toString().isEmpty()) {
      Toast.makeText(v.getContext(), "必须填写名称和标签", Toast.LENGTH_LONG).show();
      return false;
    }
    if (DataSetCollection.getDataSet(name.getText().toString()) != null) {
      Toast.makeText(v.getContext(), "数据集名称已存在", Toast.LENGTH_LONG).show();
      return false;
    }
    String[] labelArray = labels.getText().toString().split(",");
    if (labelArray.length < 2) {
      Toast.makeText(v.getContext(), "标签必须要有两个或以上，以英文逗号分隔", Toast.LENGTH_LONG).show();
      return false;
    }
    return true;
  }

  @Override
  public void onClick(View v) {
    if (!isValidInput(v)) {
      return;
    }
    Intent intent = new Intent();
    intent.putExtra("dataset_name", name.getText().toString());
    intent.putExtra("dataset_labels", labels.getText().toString());
    setResult(RESULT_OK, intent);
    finish();
  }
}
