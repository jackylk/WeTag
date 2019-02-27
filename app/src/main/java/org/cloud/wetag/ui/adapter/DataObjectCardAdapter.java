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
import org.cloud.wetag.model.DataObject;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.ObjectSelection;
import org.cloud.wetag.ui.widget.CheckView;

import java.util.LinkedList;
import java.util.List;

import static org.cloud.wetag.ui.PageFragment.ALL_LABELED;
import static org.cloud.wetag.ui.PageFragment.ALL_UNLABELED;

public abstract class DataObjectCardAdapter extends
    RecyclerView.Adapter<DataObjectCardAdapter.CardItemViewHolder> {

  Context context;
  ObjectSelection objectSelection;
  OnDataObjectStateChangedListener listener;

  DataSet dataSet;
  List<DataObject> dataObjects;

  // type can be constant value in PageFragment
  private int type;
  private String filterLabel;

  public DataObjectCardAdapter(DataSet dataSet, ObjectSelection objectSelection, int type,
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
      dataObjects = new LinkedList<>();
      for (DataObject dataObject : dataSet.getDataObjects()) {
        if (dataObject.getLabels().size() == 0) {
          dataObjects.add(dataObject);
        }
      }
    } else if (type == ALL_LABELED) {
      dataObjects = new LinkedList<>();
      for (DataObject dataObject : dataSet.getDataObjects()) {
        if (dataObject.getLabels().size() > 0) {
          dataObjects.add(dataObject);
        }
      }
    } else {
      dataObjects = new LinkedList<>();
      for (DataObject dataObject : dataSet.getDataObjects()) {
        if (dataObject.getLabels().contains(filterLabel)) {
          dataObjects.add(dataObject);
        }
      }
    }
    notifyDataSetChanged();
  }

  @Override
  public long getItemId(int position) {
    return dataObjects.get(position).getId();
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
    DataObject dataObject = dataObjects.get(position);
    onBindDataObject(context, holder, dataObject, position);
  }

  abstract void onBindDataObject(Context context, CardItemViewHolder holder,
                                 DataObject dataObject, int position);

  @Override
  public int getItemCount() {
    return dataObjects.size();
  }

  public void registerOnCheckChangedListener(OnDataObjectStateChangedListener listener) {
    this.listener = listener;
  }

  public interface OnDataObjectStateChangedListener {
    void onDataObjectCheckClicked(DataObject dataObject, boolean check);
    void onDataObjectClicked(DataObject dataObject);
    void onDataObjectChipClicked(Chip chip, DataObject dataObject);
    void refreshTab();
  }

  void onDataObjectCheckClicked(int position) {
    DataObject dataObject = dataObjects.get(position);
    if (!objectSelection.exist(dataObject)) {
      objectSelection.add(dataObject);
      listener.onDataObjectCheckClicked(dataObject,true);
    } else {
      objectSelection.remove(dataObject);
      listener.onDataObjectCheckClicked(dataObject,false);
    }
    notifyItemChanged(position);
  }

  void onDataObjectClicked(int position) {
    listener.onDataObjectClicked(dataObjects.get(position));
  }

  void onDataObjectChipClicked(Chip chip, int position) {
    listener.onDataObjectChipClicked(chip, dataObjects.get(position));
  }

  class CardItemViewHolder extends RecyclerView.ViewHolder {

    CardView cardView;
    ChipGroup chipGroup;
    List<Chip> chips;
    CheckView checkView;
    View dataObjectView;

    public CardItemViewHolder(@NonNull View itemView) {
      super(itemView);
      cardView = (CardView) itemView;
      checkView = cardView.findViewById(R.id.dataobject_check_view);
      if (checkView != null) {
        checkView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            onDataObjectCheckClicked(getAdapterPosition());
          }
        });
      }
      dataObjectView = cardView.findViewById(R.id.dataobject_view);
      dataObjectView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          onDataObjectClicked(getAdapterPosition());
        }
      });
      chipGroup = cardView.findViewById(R.id.dataobject_chipgroup);
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
              onDataObjectChipClicked(chip, getAdapterPosition());
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
