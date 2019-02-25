package org.cloud.wetag.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.PictureDrawable;
import android.graphics.drawable.VectorDrawable;
import android.support.annotation.NonNull;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataObject;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.ui.DataObjectLabelingActivity;
import org.cloud.wetag.ui.EditLabelActivity;
import org.cloud.wetag.utils.ColorUtils;

import java.util.List;

public class DataSetCardAdapter extends RecyclerView.Adapter<DataSetCardAdapter.DataSetViewHolder> {

  private Context context;
  private String[] dataSetType;
  private OnEditDataSetClickListener listener;

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

    viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        DataObjectLabelingActivity.start(viewHolder.cardView.getContext(), dataSet);
      }
    });
    viewHolder.startLabelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        DataObjectLabelingActivity.start(viewHolder.cardView.getContext(), dataSet);
      }
    });
    viewHolder.editLabelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        EditLabelActivity.start(viewHolder.cardView.getContext(), dataSet);
      }
    });
    viewHolder.moreButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showPopupMenu(position, v);
      }
    });
    List<DataObject> dataObjects = dataSet.getOrLoadObjects();
    GridLayout gridLayout = viewHolder.cardView.findViewById(R.id.dataset_preview_grid);
    gridLayout.removeAllViews();
    if (dataSet.isImageDataSet()) {
      // for image dataset, show an image grid of 2 rows * 3 columns
      gridLayout.setRowCount(2);
      gridLayout.setColumnCount(3);
      for (int i = 0; i < 6; i++) {
        GridLayout.LayoutParams gridParam = new GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED, 1f), GridLayout.spec(GridLayout.UNDEFINED, 1f));
        gridParam.setMargins(4, 4, 4, 4);

        ImageView imageView = new ImageView(viewHolder.cardView.getContext());
        imageView.setAdjustViewBounds(true);
        imageView.setMinimumHeight(300);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(gridParam);

//        LinearLayout relativeLayout = new LinearLayout(viewHolder.cardView.getContext());
//        relativeLayout.setLayoutParams(gridParam);
//        relativeLayout.addView(imageView, gridParam);

        if (dataObjects.size() > i) {
          // display the image in the dataset
//          Glide.with(context).load(dataObjects.get(i).getUri()).into(imageView);
          Glide.with(context).load(dataObjects.get(i).getUri()).into(imageView);

//          DataObject object = dataObjects.get(i);
//          if (!object.getLabels().isEmpty()) {
            // add the label chip at the bottom of the image
//            ChipGroup chipGroup = new ChipGroup(imageView.getContext());
//            for (String label : object.getLabels()) {
//              Chip chip = ImageCardAdapter.makeChip(gridLayout.getContext(), dataSet, label);
//              chip.setTextAppearance(R.style.TextAppearance_AppCompat_Small);
//              chipGroup.addView(chip);
//            }
//
//            RelativeLayout.LayoutParams relativeParam = new RelativeLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//            relativeParam.addRule(RelativeLayout.ALIGN_BOTTOM, imageView.getId());
//            relativeLayout.setMinimumHeight(200);
//            relativeLayout.addView(chipGroup, relativeParam);
//          }

          gridLayout.addView(imageView);
        } else {
          // load default picture
          Glide.with(context).load(dataSet.getDefaultPictureResourceId()).into(imageView);
          gridLayout.addView(imageView);
        }
      }
    } else {
      // for text dataset, show an text grid of 6 rows * 1 column
      gridLayout.removeAllViews();
      gridLayout.setRowCount(6);
      gridLayout.setColumnCount(1);
      for (int i = 0; i < 6; i++) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(
            GridLayout.spec(i, 1f), GridLayout.spec(0, 1f));
        params.setMargins(4, 4, 4, 4);

        TextView textView = new TextView(viewHolder.cardView.getContext());
        textView.setMaxLines(1);
        textView.setMinimumWidth(viewHolder.cardView.getWidth());
        textView.setLeft(30);
        textView.setRight(30);
        Drawable icon = viewHolder.cardView.getResources().getDrawable(R.drawable.ic_text_fields_black_24dp);
        icon.setBounds(0, 0, 30, 30);
        textView.setCompoundDrawables(icon, null, null, null);
        textView.setIncludeFontPadding(true);
        textView.setLetterSpacing(0.05f);
        textView.setPadding(10, 0, 0, 0);
        textView.setBackgroundResource(R.color.card_item_surface);
        textView.setTextColor(
            ColorStateList.valueOf(viewHolder.cardView.getResources().getColor(R.color.lightgray)));
        if (dataObjects.size() > i) {
          textView.setText(dataObjects.get(i).getSource());
          if (!dataObjects.get(i).getLabels().isEmpty()) {
            textView.setTextColor(
                textView.getResources().getColor(
                ColorUtils.getLabelBackgroundColor(
                    dataSet.getLabels(), dataObjects.get(i).getLabels().get(0))));
          }
        } else {
          // load default text
          textView.setText("待添加文本");
        }
        gridLayout.addView(textView);
      }
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
        DataSet dataSet = DataSetCollection.getDataSetList().get(position);
        switch (menuItem.getItemId()) {
          case R.id.dataset_modify:
            if (listener != null) {
              listener.onEditDataSetClicked(dataSet);
            }
            break;
          case R.id.dataset_delete:
            deleteDataSet(position, view);
            break;
        }
        return false;
      }
    });
    popup.show();
  }

  private void deleteDataSet(final int position, View view) {
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
        .setNegativeButton(R.string.dialog_button_negative, null)
        .show();
  }

  public void registerEditDataSetClickListener(OnEditDataSetClickListener listener) {
    this.listener = listener;
  }

  public interface OnEditDataSetClickListener {
    void onEditDataSetClicked(DataSet dataSet);
  }

  static class DataSetViewHolder extends RecyclerView.ViewHolder {

    CardView cardView;
    TextView dataSetName;
    TextView dataSetType;
    TextView dataSetDesc;
    ImageButton moreButton;
    Button startLabelButton;
    Button editLabelButton;

    public DataSetViewHolder(@NonNull final View itemView) {
      super(itemView);
      cardView = (CardView) itemView;
      dataSetName = itemView.findViewById(R.id.dataset_name);
      dataSetType = itemView.findViewById(R.id.dataset_type);
      dataSetDesc = itemView.findViewById(R.id.dataset_desc);
      moreButton = itemView.findViewById(R.id.dataset_menu_dot);
      startLabelButton = itemView.findViewById(R.id.button_start_label);
      editLabelButton = itemView.findViewById(R.id.button_edit_label);
    }
  }
}
