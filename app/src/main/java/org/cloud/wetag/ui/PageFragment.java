package org.cloud.wetag.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.model.ImageSelection;
import org.cloud.wetag.ui.adapter.ImageCardAdapter;
import org.cloud.wetag.ui.widget.CheckView;
import org.cloud.wetag.ui.widget.SwipeSelectRecyclerView;

import java.util.LinkedList;
import java.util.List;

public class PageFragment extends Fragment
    implements SwipeSelectRecyclerView.OnDispatchTouchListener {

  public final static int ALL = 0;
  public final static int ALL_UNLABELED = 1;
  public final static int ALL_LABELED = 2;
  public final static int SINGLE_LABELED = 3;

  private ImageCardAdapter adapter;
  private ImageSelection imageSelection;
  private ImageCardAdapter.OnImageCheckChangedListener listener;
  private SwipeSelectRecyclerView recyclerView;

  public static PageFragment newInstance(DataSet dataSet, int pageType, String filterLabel) {
    PageFragment fragment = new PageFragment();
    Bundle args = new Bundle();
    args.putString("dataset_name", dataSet.getName());
    args.putInt("page_type", pageType);
    args.putString("filter_label", filterLabel);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    String datasetName = getArguments().getString("dataset_name");
    int pageType = getArguments().getInt("page_type");
    String labelName = getArguments().getString("filter_label");
    DataSet dataSet = DataSetCollection.getDataSet(datasetName);
    if (pageType == ALL) {
      adapter = new ImageCardAdapter(dataSet, imageSelection, ALL, null);
    } else if (pageType == ALL_UNLABELED) {
      adapter = new ImageCardAdapter(dataSet, imageSelection, ALL_UNLABELED, null);
    } else if (pageType == ALL_LABELED) {
      adapter = new ImageCardAdapter(dataSet, imageSelection, ALL_LABELED, null);
    } else {
      adapter = new ImageCardAdapter(dataSet, imageSelection, SINGLE_LABELED, labelName);
    }
    adapter.registerOnCheckChangedListener(listener);
    super.onCreate(savedInstanceState);
  }

  public void registerOnCheckChangedListener(ImageCardAdapter.OnImageCheckChangedListener listener) {
    this.listener = listener;
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
    View view = inflater.inflate(R.layout.fragment_label, container, false);
    recyclerView = view.findViewById(R.id.images_rv);
    recyclerView.registerOnDispatchTouchListener(this);
    recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 3));
    recyclerView.setAdapter(adapter);
    return view;
  }

  private List<Integer> picked = new LinkedList<>();
  private CheckView currentView;

  @Override
  public void onDispatchTouch(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        picked.clear();
        break;
      case MotionEvent.ACTION_MOVE:
        CardView v = (CardView) recyclerView.findChildViewUnder(event.getX(), event.getY());
        if (v != null) {
          CheckView checkView = v.findViewById(R.id.image_check_view);
          Log.e("TAG", checkView.getTag().toString());
          if (checkView != currentView) {
            Integer position = (Integer) checkView.getTag();
            if (!checkView.isChecked()){
              picked.add(position);
              adapter.onImageCheckClicked(position);
            } else {
              picked.remove(position);
            }
            currentView = checkView;
          }
        }
        break;
      case MotionEvent.ACTION_UP:
        picked.clear();
        break;
    }
  }
}
