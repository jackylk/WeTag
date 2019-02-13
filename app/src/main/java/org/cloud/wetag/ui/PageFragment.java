package org.cloud.wetag.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import org.cloud.wetag.MyApplication;
import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.model.Image;
import org.cloud.wetag.model.ImageSelection;
import org.cloud.wetag.ui.adapter.ImageCardAdapter;
import org.cloud.wetag.ui.adapter.LabelFragmentPagerAdapter;
import org.cloud.wetag.utils.ColorUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PageFragment extends Fragment implements ImageCardAdapter.OnCheckChangedListener,
  View.OnClickListener, CompoundButton.OnCheckedChangeListener {

  public final static int ALL = 0;
  public final static int ALL_UNLABELED = 1;
  public final static int ALL_LABELED = 2;
  public final static int SINGLE_LABELED = 3;

  private ImageCardAdapter adapter;
  private DataSet dataSet;
  private ImageSelection imageSelection;
  private List<String> labelSelection;
  private int pageType;
  private String labelName;
  private View view;
  private ChipGroup chipGroup;
  private Map<String, Chip> chipMap;
  private LabelFragmentPagerAdapter parentAdapter;

  public static PageFragment newInstance(DataSet dataSet, int pageType, String filterLabel) {
    PageFragment fragment = new PageFragment();
    Bundle args = new Bundle();
    args.putString("dataset_name", dataSet.getName());
    args.putInt("page_type", pageType);
    args.putString("filter_label", filterLabel);
    fragment.setArguments(args);
    return fragment;
  }

  public void setParentAdapter(LabelFragmentPagerAdapter parentAdapter) {
    this.parentAdapter = parentAdapter;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    String datasetName = getArguments().getString("dataset_name");
    pageType = getArguments().getInt("page_type");
    labelName = getArguments().getString("filter_label");
    dataSet = DataSetCollection.getDataSet(datasetName);
    labelSelection = new LinkedList<>();
    super.onCreate(savedInstanceState);
  }

  public ImageCardAdapter getAdapter() {
    return adapter;
  }

  public void setImageSelection(ImageSelection imageSelection) {
    this.imageSelection = imageSelection;
  }


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_label, container, false);

    RecyclerView recyclerView = view.findViewById(R.id.images_rv);
    RecyclerView.LayoutManager layoutManager = new GridLayoutManager(view.getContext(), 2);
    recyclerView.setLayoutManager(layoutManager);

    if (pageType == ALL) {
      adapter = new ImageCardAdapter(dataSet, imageSelection, ALL, null);
    } else if (pageType == ALL_UNLABELED) {
      adapter = new ImageCardAdapter(dataSet, imageSelection, ALL_UNLABELED, null);
    } else if (pageType == ALL_LABELED) {
      adapter = new ImageCardAdapter(dataSet, imageSelection, ALL_LABELED, null);
    } else {
      adapter = new ImageCardAdapter(dataSet, imageSelection, SINGLE_LABELED, labelName);
    }

    adapter.registerOnCheckChangedListener(this);
    recyclerView.setAdapter(adapter);

    initLabelBar();
    setEnableLabelBar(false);
    view.findViewById(R.id.label_confirm).setOnClickListener(this);

    return view;
  }

  public void setEnableLabelBar(boolean enabled) {
    view.findViewById(R.id.label_confirm).setEnabled(enabled);
    for (Chip chip : chipMap.values()) {
      chip.setEnabled(enabled);
    }
  }

  private void initLabelBar() {
    chipGroup = view.findViewById(R.id.label_chipgroup);
    chipMap = new HashMap<>();
    for (String label : dataSet.getLabels()) {
      Chip chip = new Chip(chipGroup.getContext());
      chip.setText(label);
      chip.setEnabled(false);
      chip.setClickable(true);
      chip.setCheckable(true);
      chip.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
      chip.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.white));
      chip.setChipBackgroundColorResource(
          ColorUtils.getLabelBackgroundColor(dataSet.getLabels(), label));
      chip.setOnCheckedChangeListener(this);
      chipGroup.addView(chip);
      chipMap.put(label, chip);
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (imageSelection.get().size() > 0) {
      String label = buttonView.getText().toString();
      if (isChecked) {
        labelSelection.add(label);
      } else {
        labelSelection.remove(label);
      }
    }
  }

  @Override
  public void onImageCheckedChanged(Image image, boolean checked) {
    Set<String> labels = new HashSet<>();
    for (Image img : imageSelection.get()) {
      labels.addAll(img.getLabels());
    }
    for (Map.Entry<String, Chip> chipEntry : chipMap.entrySet()) {
      if (labels.contains(chipEntry.getKey())) {
        chipEntry.getValue().setChecked(true);
      } else {
        chipEntry.getValue().setChecked(false);
      }
    }
    if (imageSelection.get().size() > 0) {
      setEnableLabelBar(true);
    } else {
      setEnableLabelBar(false);
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.label_confirm:
        for (Image image : imageSelection.get()) {
          image.setLabels(labelSelection);
          image.saveThrows();
        }
        imageSelection.clear();
        labelSelection.clear();
        chipGroup.clearCheck();
        refreshView();
        break;
      default:
        break;
    }
  }

  private void refreshView() {
    adapter.refreshImages();
  }

}
