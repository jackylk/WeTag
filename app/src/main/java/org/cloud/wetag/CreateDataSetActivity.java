package org.cloud.wetag;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateDataSetActivity extends BaseActivity {

  EditText name;
  EditText desc;
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
    if (name.getText() == null || labels.getText() == null) {
      Toast.makeText(v.getContext(), "必须填写名称和标签", Toast.LENGTH_SHORT).show();
      return false;
    }
    String[] labelArray = labels.getText().toString().split(",");
    if (labelArray.length == 0) {
      Toast.makeText(v.getContext(), "标签要以逗号分隔", Toast.LENGTH_SHORT).show();
      return false;
    }
    return true;
  }

}
