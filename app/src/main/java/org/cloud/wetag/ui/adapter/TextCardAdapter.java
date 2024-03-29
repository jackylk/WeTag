package org.cloud.wetag.ui.adapter;

import android.content.Context;
import android.support.design.chip.Chip;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import org.cloud.wetag.MyApplication;
import org.cloud.wetag.R;
import org.cloud.wetag.model.Sample;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.ObjectSelection;
import org.cloud.wetag.utils.ColorUtils;

import java.util.List;

public class TextCardAdapter extends SampleCardAdapter {

  public TextCardAdapter(DataSet dataSet, ObjectSelection objectSelection, int type,
                         String filterLabel) {
    super(dataSet, objectSelection, type, filterLabel);
  }

  @Override
  void onBindSample(Context context, CardItemViewHolder holder, Sample sample,
                    int position) {
    List<String> labels = sample.getLabels();
    for (Chip chip : holder.chips) {
      if (labels.contains(chip.getText().toString())) {
        chip.setChecked(true);
      } else {
        chip.setChecked(false);
      }
      chip.setChipBackgroundColorResource(
          ColorUtils.getLabelBackgroundColor(dataSet.getLabels(), chip.getText().toString()));
      chip.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.white));
    }
    ((TextView) holder.sampleView).setText(sample.getSource());
    if (objectSelection.isSelectEnabled()) {
      holder.checkView.setVisibility(View.VISIBLE);
      if (objectSelection.get().contains(sample)) {
        holder.checkView.setChecked(true);
      } else {
        holder.checkView.setChecked(false);
      }
    } else {
      holder.checkView.setVisibility(View.GONE);
    }
  }

}
