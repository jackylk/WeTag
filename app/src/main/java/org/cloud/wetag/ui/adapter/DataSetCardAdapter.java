package org.cloud.wetag.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.cloud.wetag.model.Image;
import org.cloud.wetag.ui.LabelActivity;
import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;

import java.util.List;

public class DataSetCardAdapter extends RecyclerView.Adapter<DataSetCardAdapter.DataSetViewHolder> {

  private Context context;

  public DataSetCardAdapter() {
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
  public void onBindViewHolder(@NonNull DataSetViewHolder viewHolder, final int position) {
    final DataSet dataSet = DataSetCollection.getDataSetList().get(position);
    viewHolder.dataSetName.setText(dataSet.getName() + "数据集");
    viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // show a dialog to alert user
        new AlertDialog.Builder(v.getContext())
            .setTitle(R.string.dialog_delete_dataset_title)
            .setMessage(R.string.dialog_delete_dataset_message)
            .setPositiveButton(R.string.button_positive, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                // delete the dataset by name
                DataSetCollection.removeDataSet(dataSet.getName());
                notifyItemRemoved(position);
              }
            })
            .setNegativeButton(R.string.button_negative, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                // do nothing
              }
            }).show();
      }
    });
    List<Image> images = dataSet.getOrLoadImages();
    if (images.isEmpty()) {
      // if dataset is empty, load default picture
      Glide.with(context).load(R.drawable.no_image).into(viewHolder.dataSetImage);
    } else {
      // display first image in the dataset
      Glide.with(context).load(images.get(0).getUri()).into(viewHolder.dataSetImage);
    }

    viewHolder.dataSetLabels.removeAllViews();
    for (String label : dataSet.getLabels()) {
      Chip chip = new Chip(viewHolder.dataSetLabels.getContext());
      chip.setText(label);
      chip.setClickable(false);
      chip.setCheckable(false);
      chip.setEnabled(false);
      chip.setTextSize(viewHolder.labelText.getTextSize() / 3);
      viewHolder.dataSetLabels.addView(chip);
    }
  }

  @Override
  public int getItemCount() {
    return DataSetCollection.getDataSetList().size();
  }

  static class DataSetViewHolder extends RecyclerView.ViewHolder {

    CardView cardView;
    TextView dataSetName;
    TextView labelText;
    ImageButton deleteButton;
    ImageView dataSetImage;
    ChipGroup dataSetLabels;

    public DataSetViewHolder(@NonNull final View itemView) {
      super(itemView);
      cardView = (CardView) itemView;
      dataSetName = itemView.findViewById(R.id.dataset_name);
      dataSetImage = itemView.findViewById(R.id.dataset_image);
      deleteButton = itemView.findViewById(R.id.dataset_delete);
      dataSetLabels = itemView.findViewById(R.id.dataset_chipgroup);
      labelText = itemView.findViewById(R.id.label_text);

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(itemView.getContext(), LabelActivity.class);
          intent.putExtra("dataset_name",
              DataSetCollection.getDataSetList().get(getAdapterPosition()).getName());
          itemView.getContext().startActivity(intent);
        }
      });
    }
  }
}
