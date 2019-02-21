package org.cloud.wetag.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.ui.ui.managelabel.ManageLabelFragment;

public class ManageLabelActivity extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.manage_label_activity);
    if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.container, ManageLabelFragment.newInstance())
          .commitNow();
    }
  }

  public static void start(Context context, DataSet dataSet) {
    Intent intent = new Intent(context, ManageLabelActivity.class);
    context.startActivity(intent);
  }
}
