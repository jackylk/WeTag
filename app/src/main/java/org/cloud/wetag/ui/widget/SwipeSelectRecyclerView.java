package org.cloud.wetag.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class SwipeSelectRecyclerView extends RecyclerView {

  private OnDispatchTouchListener onDispatchTouchListener;

  public SwipeSelectRecyclerView(@NonNull Context context) {
    super(context);
  }

  public SwipeSelectRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public SwipeSelectRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs,
                                 int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    if (onDispatchTouchListener != null) {
      Log.e("TAG", ev.toString());
      onDispatchTouchListener.onDispatchTouch(ev);
    }
    return super.dispatchTouchEvent(ev);
  }

  public void registerOnDispatchTouchListener(OnDispatchTouchListener listener) {
    this.onDispatchTouchListener = listener;
  }

  public interface OnDispatchTouchListener {
    void onDispatchTouch(MotionEvent event);
  }
}
