package org.cloud.wetag.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cloud.wetag.R;
import org.cloud.wetag.model.Sample;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.ObjectSelection;
import org.cloud.wetag.ui.widget.CheckView;

import java.util.LinkedList;
import java.util.List;

import static org.cloud.wetag.ui.PageFragment.ALL_LABELED;
import static org.cloud.wetag.ui.PageFragment.ALL_UNLABELED;

public abstract class SampleCardAdapter extends
    RecyclerView.Adapter<SampleCardAdapter.CardItemViewHolder> {

  Context context;
  ObjectSelection objectSelection;
  OnSampleStateChangedListener listener;

  DataSet dataSet;
  List<Sample> samples;

  // type can be constant value in PageFragment
  private int type;
  private String filterLabel;

  public SampleCardAdapter(DataSet dataSet, ObjectSelection objectSelection, int type,
                           String filterLabel) {
    this.objectSelection = objectSelection;
    this.dataSet = dataSet;
    this.type = type;
    this.filterLabel = filterLabel;

    // avoid blinking when refreshing the cards
    setHasStableIds(true);
    refreshAllCards();
  }

  public void refreshAllCards() {
    if (type == ALL_UNLABELED) {
      samples = new LinkedList<>();
      for (Sample sample : dataSet.getSamples()) {
        if (sample.getLabels().size() == 0) {
          samples.add(sample);
        }
      }
    } else if (type == ALL_LABELED) {
      samples = new LinkedList<>();
      for (Sample sample : dataSet.getSamples()) {
        if (sample.getLabels().size() > 0) {
          samples.add(sample);
        }
      }
    } else {
      samples = new LinkedList<>();
      for (Sample sample : dataSet.getSamples()) {
        if (sample.getLabels().contains(filterLabel)) {
          samples.add(sample);
        }
      }
    }
    notifyDataSetChanged();
  }

  @Override
  public long getItemId(int position) {
    return samples.get(position).getId();
  }

  @NonNull
  @Override
  public CardItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
    if (context == null) {
      context = viewGroup.getContext();
    }
    View view = LayoutInflater.from(context).inflate(
        dataSet.getCardItemLayoutResource(), viewGroup, false);
    return new CardItemViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull CardItemViewHolder holder, int position) {
    Sample sample = samples.get(position);
    onBindSample(context, holder, sample, position);
  }

  abstract void onBindSample(Context context, CardItemViewHolder holder,
                             Sample sample, int position);

  @Override
  public int getItemCount() {
    return samples.size();
  }

  public void registerOnCheckChangedListener(OnSampleStateChangedListener listener) {
    this.listener = listener;
  }

  public interface OnSampleStateChangedListener {
    void onSampleCheckClicked(Sample sample, boolean check);
    void onSampleClicked(Sample sample);
    void onSampleChipClicked(Chip chip, Sample sample);
    void refreshTab();
  }

  void onSampleCheckClicked(int position) {
    Sample sample = samples.get(position);
    if (!objectSelection.exist(sample)) {
      objectSelection.add(sample);
      listener.onSampleCheckClicked(sample,true);
    } else {
      objectSelection.remove(sample);
      listener.onSampleCheckClicked(sample,false);
    }
    notifyItemChanged(position);
  }

  void onSampleClicked(int position) {
    listener.onSampleClicked(samples.get(position));
  }

  void onSampleChipClicked(Chip chip, int position) {
    listener.onSampleChipClicked(chip, samples.get(position));
  }

  class CardItemViewHolder extends RecyclerView.ViewHolder {

    CardView cardView;
    ChipGroup chipGroup;
    List<Chip> chips;
    CheckView checkView;
    View sampleView;

    public CardItemViewHolder(@NonNull View itemView) {
      super(itemView);
      cardView = (CardView) itemView;
      checkView = cardView.findViewById(R.id.sample_check_view);
      if (checkView != null) {
        checkView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            onSampleCheckClicked(getAdapterPosition());
          }
        });
      }
      sampleView = cardView.findViewById(R.id.sample_view);
      sampleView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          onSampleClicked(getAdapterPosition());
        }
      });
      chipGroup = cardView.findViewById(R.id.sample_chipgroup);
      if (chipGroup != null) {
        chips = new LinkedList<>();
        List<String> labelDefinition = dataSet.getLabels();
        for (String labelDef : labelDefinition) {
          final Chip chip = new Chip(chipGroup.getContext());
          chip.setText(labelDef);
          chip.setEnabled(true);
          chip.setClickable(true);
          chip.setCheckable(true);
          chip.setChipIconResource(R.drawable.ic_radio_button_unchecked_black_24dp);
          chip.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
          chip.setTextColor(ContextCompat.getColor(context, R.color.white));
          chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              onSampleChipClicked(chip, getAdapterPosition());
            }
          });
          chip.setTag(getAdapterPosition());
          chipGroup.addView(chip);
          chips.add(chip);
        }
      }
    }
  }

  /**
   * back is pressed by user in parent activity
   * @return true if this class consumed the event
   */
  public boolean onBackPressed() {
    return false;
  }
}
