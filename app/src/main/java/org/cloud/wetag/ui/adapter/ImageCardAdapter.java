package org.cloud.wetag.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.design.chip.Chip;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.cloud.wetag.R;
import org.cloud.wetag.model.Sample;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.ObjectSelection;
import org.cloud.wetag.utils.ColorUtils;

import java.util.List;

public class ImageCardAdapter extends SampleCardAdapter
    implements View.OnLongClickListener, View.OnTouchListener {

  private boolean inMultiSelectMode;

  public ImageCardAdapter(DataSet dataSet, ObjectSelection objectSelection, int type,
                          String filterLabel) {
    super(dataSet, objectSelection, type, filterLabel);
    inMultiSelectMode = false;
  }

  @Override
  void onBindSample(Context context, CardItemViewHolder holder, Sample sample,
                    int position) {
    ImageView imageView = (ImageView) holder.sampleView;
    Glide.with(context).load(sample.getUri()).into(imageView);
    imageView.setOnLongClickListener(this);
    imageView.setOnTouchListener(this);
    imageView.setTag(R.id.sample_view, position);
    if (inMultiSelectMode) {
      imageView.setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
      holder.checkView.setVisibility(View.VISIBLE);
      if (objectSelection.exist(sample)) {
        imageView.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        holder.checkView.setEnabled(true);
        holder.checkView.setChecked(true);
      } else {
        holder.checkView.setEnabled(false);
        holder.checkView.setChecked(false);
      }
    } else {
      imageView.clearColorFilter();
      holder.checkView.setVisibility(View.GONE);
    }

    holder.chipGroup.removeAllViews();
    List<String> labels = sample.getOrLoadLabels();
    for (String label : labels) {
      Chip chip = makeChip(context, dataSet, label);
      holder.chipGroup.addView(chip);
      holder.chips.add(chip);
    }
  }

  @NonNull
  public static Chip makeChip(Context context, DataSet dataSet, String label) {
    Chip chip = new Chip(context);
    chip.setText(label);
    chip.setEnabled(true);
    chip.setClickable(false);
    chip.setTextAppearance(R.style.TextAppearance_MaterialComponents_Body1);
    chip.setTextColor(ContextCompat.getColor(context, R.color.white));
    chip.setChipBackgroundColorResource(
        ColorUtils.getLabelBackgroundColor(dataSet.getLabels(), label));
    return chip;
  }

  @Override
  public boolean onLongClick(View v) {
    inMultiSelectMode = true;
    onSampleClicked((Integer) v.getTag(R.id.sample_view));
    notifyDataSetChanged();
    return true;
  }

  @Override
  void onSampleClicked(int position) {
    if (inMultiSelectMode) {
      onSampleCheckClicked(position);
    } else {
      super.onSampleClicked(position);
    }
  }

  /**
   * back is pressed by user in parent activity
   * @return true if this class consumed the event
   */
  @Override
  public boolean onBackPressed() {
    if (inMultiSelectMode) {
      inMultiSelectMode = false;
      objectSelection.clear();
      notifyDataSetChanged();
      return true;
    }
    return false;
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    ImageView imageView = v.findViewById(R.id.sample_view);
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        if (!inMultiSelectMode) {
          imageView.setColorFilter(Color.parseColor("#77000000"));
        }
        break;
      default:
        if (!inMultiSelectMode) {
          imageView.clearColorFilter();
        }
        break;
    }
    return false;
  }
}
