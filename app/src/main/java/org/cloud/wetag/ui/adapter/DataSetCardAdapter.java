package org.cloud.wetag.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.cloud.wetag.MyApplication;
import org.cloud.wetag.model.DataObject;
import org.cloud.wetag.ui.DataObjectLabelingActivity;
import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.utils.ColorUtils;

import java.util.List;

public class DataSetCardAdapter extends RecyclerView.Adapter<DataSetCardAdapter.DataSetViewHolder> {

  private Context context;
  private String[] dataSetType;

  public DataSetCardAdapter(String[] dataSetType) {
    this.dataSetType = dataSetType;
  }

  @NonNull
  @Override
  public DataSetViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
    if (context == null) {
      context = viewGroup.getContext();
    }
    View view = LayoutInflater.from(context).inflate(R.layout.dataset_item, viewGroup, false);
    return new DataSetViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull final DataSetViewHolder viewHolder, final int position) {
    final DataSet dataSet = DataSetCollection.getDataSetList().get(position);
    viewHolder.dataSetName.setText(dataSet.getName());
    viewHolder.dataSetType.setText(dataSetType[dataSet.getType()]);
    if (dataSet.getDesc() != null) {
      viewHolder.dataSetDesc.setText(dataSet.getDesc());
    } else {
      viewHolder.dataSetDesc.setText("无描述");
    }

    viewHolder.moreButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showPopupMenu(position, v);
      }
    });
    List<DataObject> dataObjects = dataSet.getOrLoadObjects();
    if (dataObjects.isEmpty() || !dataSet.isImageDataSet()) {
      // load default picture
      Glide.with(context).load(R.drawable.empty_dark).into(viewHolder.dataSetImage);
    } else {
      // display first image in the dataset
      Glide.with(context).load(dataObjects.get(0).getUri()).into(viewHolder.dataSetImage);
    }

    viewHolder.dataSetLabels.removeAllViews();
    for (String label : dataSet.getLabels()) {
      Chip chip = new Chip(viewHolder.dataSetLabels.getContext());
      chip.setText(label);
      chip.setClickable(false);
      chip.setCheckable(false);
      chip.setEnabled(false);
      chip.setTextAppearance(R.style.TextAppearance_AppCompat_Medium);
      chip.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.white));
      chip.setChipBackgroundColorResource(
          ColorUtils.getLabelBackgroundColor(dataSet.getLabels(), label));
      viewHolder.dataSetLabels.addView(chip);
    }
  }

  @Override
  public int getItemCount() {
    return DataSetCollection.getDataSetList().size();
  }

  private void showPopupMenu(final int position, final View view) {
    // inflate menu
    PopupMenu popup = new PopupMenu(view.getContext(), view);
    MenuInflater inflater = popup.getMenuInflater();
    inflater.inflate(R.menu.menu_dataset_item, popup.getMenu());
    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
          case R.id.dataset_modify:
            updateDataSet(position, view);
            break;
          case R.id.dataset_delete:
            deletDataSet(position, view);
            break;
        }
        return false;
      }
    });
    popup.show();
  }

  private void updateDataSet(final int position, View view) {
    final DataSet dataSet = DataSetCollection.getDataSetList().get(position);
  }

  private void deletDataSet(final int position, View view) {
    final DataSet dataSet = DataSetCollection.getDataSetList().get(position);
    // show a dialog to alert user
    new AlertDialog.Builder(view.getContext())
        .setTitle(R.string.dialog_delete_dataset_title)
        .setMessage(R.string.dialog_delete_dataset_message)
        .setPositiveButton(R.string.dialog_delete_dataobject_button_positive, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // delete the dataset by name
            DataSetCollection.removeDataSet(dataSet.getName());
            notifyItemRemoved(position);
          }
        })
        .setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // do nothing
          }
        }).show();
  }

  static class DataSetViewHolder extends RecyclerView.ViewHolder {

    CardView cardView;
    TextView dataSetName;
    TextView dataSetType;
    TextView dataSetDesc;
    TextView labelText;
    ImageButton moreButton;
    ImageView dataSetImage;
    ChipGroup dataSetLabels;

    public DataSetViewHolder(@NonNull final View itemView) {
      super(itemView);
      cardView = (CardView) itemView;
      dataSetName = itemView.findViewById(R.id.dataset_name);
      dataSetType = itemView.findViewById(R.id.dataset_type);
      dataSetDesc = itemView.findViewById(R.id.dataset_desc);
      dataSetImage = itemView.findViewById(R.id.dataset_image);
      moreButton = itemView.findViewById(R.id.dataset_menu_dot);
      dataSetLabels = itemView.findViewById(R.id.dataset_chipgroup);
      labelText = itemView.findViewById(R.id.label_text);
      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          DataObjectLabelingActivity.start(itemView.getContext(),
              DataSetCollection.getDataSetList().get(getAdapterPosition()));
        }
      });
    }
  }
}
