package org.cloud.wetag.ui.adapter;

import android.content.Context;
import android.support.design.chip.Chip;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataObject;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.ObjectSelection;
import org.cloud.wetag.utils.ColorUtils;

import java.util.List;

public class ImageCardAdapter extends DataObjectCardAdapter {
  public ImageCardAdapter(DataSet dataSet, ObjectSelection objectSelection, int type, String filterLabel) {
    super(dataSet, objectSelection, type, filterLabel);
  }

  @Override
  void drawDataObject(Context context, CardItemViewHolder holder, DataObject dataObject,
                      int position) {
    Glide.with(context).load(dataObject.getUri()).into((ImageView)holder.dataObjectView);
    holder.chipGroup.removeAllViews();
    List<String> labels = dataObject.getOrLoadLabels();
    for (String label : labels) {
      Chip chip = new Chip(holder.chipGroup.getContext());
      chip.setText(label);
      chip.setEnabled(true);
      chip.setClickable(false);
      chip.setTextAppearance(R.style.TextAppearance_MaterialComponents_Body1);
      chip.setTextColor(ContextCompat.getColor(context, R.color.white));
      chip.setChipBackgroundColorResource(
          ColorUtils.getLabelBackgroundColor(dataSet.getLabels(), label));
      holder.chipGroup.addView(chip);
      holder.chips.add(chip);
    }
    if (holder.checkView != null) {
      holder.checkView.setChecked(objectSelection.exist(dataObject));
      holder.checkView.setTag(position);
    }
  }

}
