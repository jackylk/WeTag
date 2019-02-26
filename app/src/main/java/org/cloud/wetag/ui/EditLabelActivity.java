package org.cloud.wetag.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataObject;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.utils.ColorUtils;

import java.util.List;

public class EditLabelActivity extends BaseActivity implements View.OnClickListener {

  private DataSet dataSet;
  private ChipGroup chipGroup;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_label);
    setTitle(R.string.button_edit_label);

    String datasetName = getIntent().getStringExtra("dataset_name");
    dataSet = DataSetCollection.getDataSet(datasetName);

    findViewById(R.id.button_add_label).setOnClickListener(this);
    chipGroup = findViewById(R.id.edit_label_chipgroup);

    for (String label : dataSet.getLabels()) {
      Chip chip = makeChip(chipGroup, dataSet, label);
      chipGroup.addView(chip);
    }
  }

  public static Chip makeChip(final ChipGroup chipGroup, final DataSet dataSet,
                              final String label) {
    final Chip chip = new Chip(chipGroup.getContext());
    int count = 0;
    for (DataObject dataObject : dataSet.getDataObjects()) {
      if (dataObject.getLabels().contains(label)) {
        count++;
      }
    }
    chip.setText(label + "(" + count + ")");
    chip.setTextAppearance(R.style.TextAppearance_AppCompat_Medium);
    chip.setChipBackgroundColorResource(R.color.darkgray);
    chip.setTextColor(Color.WHITE);
    chip.setCloseIconVisible(true);
    chip.setOnCloseIconClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        new AlertDialog.Builder(v.getContext())
            .setTitle("删除标签")
            .setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                for (DataObject dataObject : dataSet.getDataObjects()) {
                  if (dataObject.getLabels().contains(label)) {
                    dataObject.getLabels().remove(label);
                    dataObject.save();
                  }
                }
                dataSet.getLabels().remove(label);
                dataSet.save();
                chipGroup.removeView(chip);
              }
            })
            .setNegativeButton(R.string.dialog_button_negative, null)
            .show();
      }
    });
    return chip;
  }

  public static void start(Context context, DataSet dataSet) {
    Intent intent = new Intent(context, EditLabelActivity.class);
    intent.putExtra("dataset_name", dataSet.getName());
    context.startActivity(intent);
  }

  @Override
  public void onClick(final View v) {
    final EditText et = new EditText(v.getContext());
    new AlertDialog.Builder(v.getContext())
        .setTitle("添加标签")
        .setView(et, 50, 0, 50, 0)
        .setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            final String label = et.getText().toString();
            if (label.isEmpty()) {
              Toast.makeText(v.getContext(), "标签名字不能为空！", Toast.LENGTH_LONG).show();
            } else {
              List<String> labelDef = dataSet.getLabels();
              if (!labelDef.contains(label)) {
                labelDef.add(label);
                dataSet.save();
                Chip chip = makeChip(chipGroup, dataSet, label);
                chipGroup.addView(chip);
              } else {
                Toast.makeText(v.getContext(), "标签名字已存在！", Toast.LENGTH_LONG).show();
              }
            }
          }
        })
        .setNegativeButton(R.string.dialog_button_negative, null)
        .show();
  }
}
