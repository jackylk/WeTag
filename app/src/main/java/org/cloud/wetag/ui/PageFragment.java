package org.cloud.wetag.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.model.ObjectSelection;
import org.cloud.wetag.ui.adapter.DataObjectCardAdapter;
import org.cloud.wetag.ui.adapter.ImageCardAdapter;
import org.cloud.wetag.ui.adapter.TextCardAdapter;

public class PageFragment extends Fragment {

  public final static int ALL = 0;
  public final static int ALL_UNLABELED = 1;
  public final static int ALL_LABELED = 2;
  public final static int SINGLE_LABELED = 3;

  private DataObjectCardAdapter adapter;
  private ObjectSelection objectSelection;
  private DataObjectCardAdapter.OnDataObjectCheckChangedListener listener;
  private RecyclerView recyclerView;
  private DataSet dataSet;

  public static PageFragment newInstance(DataSet dataSet, int pageType, String filterLabel) {
    PageFragment fragment = new PageFragment();
    Bundle args = new Bundle();
    args.putString("dataset_name", dataSet.getName());
    args.putInt("page_type", pageType);
    args.putString("filter_label", filterLabel);
    fragment.setArguments(args);
    fragment.dataSet = dataSet;
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    String datasetName = getArguments().getString("dataset_name");
    int pageType = getArguments().getInt("page_type");
    String labelName = getArguments().getString("filter_label");
    DataSet dataSet = DataSetCollection.getDataSet(datasetName);
    if (dataSet.getType() == DataSet.IMAGE) {
      adapter = createImageCardAdapter(pageType, labelName, dataSet);
    } else if (dataSet.getType() == DataSet.TEXT_CLASSIFICATION) {
      adapter = createTextCardAdapter(pageType, labelName, dataSet);
    } else {
      throw new UnsupportedOperationException("dataset type " +
          dataSet.getType() + " is not supported");
    }
    adapter.registerOnCheckChangedListener(listener);
    super.onCreate(savedInstanceState);
  }

  private DataObjectCardAdapter createImageCardAdapter(int pageType, String labelName, DataSet dataSet) {
    if (pageType == ALL) {
      return new ImageCardAdapter(dataSet, objectSelection, ALL, null);
    } else if (pageType == ALL_UNLABELED) {
      return new ImageCardAdapter(dataSet, objectSelection, ALL_UNLABELED, null);
    } else if (pageType == ALL_LABELED) {
      return new ImageCardAdapter(dataSet, objectSelection, ALL_LABELED, null);
    } else {
      return new ImageCardAdapter(dataSet, objectSelection, SINGLE_LABELED, labelName);
    }
  }

  private DataObjectCardAdapter createTextCardAdapter(int pageType, String labelName, DataSet dataSet) {
    if (pageType == ALL) {
      return new TextCardAdapter(dataSet, objectSelection, ALL, null);
    } else if (pageType == ALL_UNLABELED) {
      return new TextCardAdapter(dataSet, objectSelection, ALL_UNLABELED, null);
    } else if (pageType == ALL_LABELED) {
      return new TextCardAdapter(dataSet, objectSelection, ALL_LABELED, null);
    } else {
      return new TextCardAdapter(dataSet, objectSelection, SINGLE_LABELED, labelName);
    }
  }

  public void registerOnCheckChangedListener(DataObjectCardAdapter.OnDataObjectCheckChangedListener listener) {
    this.listener = listener;
  }

  public DataObjectCardAdapter getAdapter() {
    return adapter;
  }

  public void setObjectSelection(ObjectSelection objectSelection) {
    this.objectSelection = objectSelection;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_labeling_page, container, false);
    recyclerView = view.findViewById(R.id.recycler_view);
    if (dataSet.isImageDataSet()) {
      recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 3));
    } else {
      recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(),
          LinearLayoutManager.VERTICAL, false));
    }
    recyclerView.setAdapter(adapter);
    return view;
  }

}
