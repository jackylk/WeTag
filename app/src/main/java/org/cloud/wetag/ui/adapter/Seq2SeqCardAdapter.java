package org.cloud.wetag.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSetUpdateDBHelper;
import org.cloud.wetag.model.Sample;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.ObjectSelection;

public class Seq2SeqCardAdapter extends SampleCardAdapter implements View.OnClickListener {

  public Seq2SeqCardAdapter(DataSet dataSet, ObjectSelection objectSelection, int type,
                            String filterLabel) {
    super(dataSet, objectSelection, type, filterLabel);
  }

  @Override
  void onBindSample(Context context, CardItemViewHolder holder, Sample sample,
                    int position) {
    ((TextView) holder.sampleView).setText(sample.getSource());
    if (objectSelection.isSelectEnabled()) {
      holder.checkView.setVisibility(View.VISIBLE);
      if (objectSelection.get().contains(sample)) {
        holder.checkView.setChecked(true);
      } else {
        holder.checkView.setChecked(false);
      }
    } else {
      holder.checkView.setVisibility(View.GONE);
    }
    LinearLayout labelView = holder.cardView.findViewById(R.id.seq2seq_label_view);
    labelView.removeAllViews();
    if (!sample.getLabels().isEmpty()) {
      for (String label : sample.getLabels()) {
        addNewLabelView(labelView, label, position);
      }
    }
    Button confirmButton = holder.cardView.findViewById(R.id.seq2seq_confirm_button);
    confirmButton.setTag(R.string.tag_key_position, position);
    confirmButton.setOnClickListener(this);

    EditText editText = holder.cardView.findViewById(R.id.seq2seq_label_input);
    editText.setHint("请输入文本");
    editText.setHintTextColor(Color.GRAY);
  }

  /**
   * Add a new linear layout with label text and button.
   * If label is null, it is the last input in this card item
   */
  private void addNewLabelView(LinearLayout labelView, String label, int position) {
    TextView textView = new TextView(context);
    textView.setText(label);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.leftMargin = 40;
    params.rightMargin = 40;
    params.topMargin = 20;
    params.bottomMargin = 20;
    textView.setLayoutParams(params);
    textView.setTextAppearance(R.style.TextAppearance_AppCompat_Medium);
    textView.setTextColor(Color.LTGRAY);
    textView.setOnClickListener(this);
    textView.setTag(R.string.tag_key_position, position);
    labelView.addView(textView);
  }

  @Override
  public void onClick(final View v) {
    final int position = (int) v.getTag(R.string.tag_key_position);
    final Sample sample = samples.get(position);
    if (v instanceof Button) {
      LinearLayout layout = (LinearLayout) v.getParent().getParent();
      CardView cardView = (CardView) layout.getParent();
      EditText editText = cardView.findViewById(R.id.seq2seq_label_input);
      if (!editText.getText().toString().isEmpty()) {
        DataSetUpdateDBHelper.addLabel(dataSet, sample, editText.getText().toString());
        editText.setText("");
        listener.refreshTab();
        notifyItemChanged(position);
      }
    } else {
      // it must be label text, show dialog for changing label
      TextView textView = (TextView) v;
      final String originLabel = textView.getText().toString();
      final EditText editText = new EditText(v.getContext());
      editText.setText(originLabel);
      new AlertDialog.Builder(v.getContext())
          .setTitle("修改标签")
          .setView(editText, 50, 0, 50, 0)
          .setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              final String newLabel = editText.getText().toString();
              if (newLabel.isEmpty()) {
                Toast.makeText(v.getContext(), "输入不能为空！", Toast.LENGTH_LONG).show();
              } else {
                sample.getLabels().remove(originLabel);
                sample.getLabels().add(newLabel);
                sample.saveThrows();
                notifyItemChanged(position);
              }
            }
          })
          .setNegativeButton(R.string.dialog_button_negative, null)
          .setNeutralButton(R.string.dialog_delete_sample_button_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              sample.getLabels().remove(originLabel);
              sample.saveThrows();
              listener.refreshTab();
              notifyItemChanged(position);
            }
          })
          .show();
    }
  }

}
