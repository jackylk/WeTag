package org.cloud.wetag.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSetCollection;

public class CreateDataSetActivity extends BaseActivity {

  EditText name;
  EditText labels;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_data_set);

    name = findViewById(R.id.dataset_name_input);
    labels = findViewById(R.id.dataset_labels_input);

    Button button = findViewById(R.id.create_dataset_button);
    button.setOnClickListener(new View.OnClickListener() {
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
    });
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
      Toast.makeText(v.getContext(), "标签必须要有两个或以上，以逗号分隔", Toast.LENGTH_LONG).show();
      return false;
    }
    return true;
  }

}
