package org.cloud.wetag.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.cloud.wetag.R;
import org.cloud.wetag.model.LoginInfo;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.service.CloudDataSet;
import org.cloud.wetag.model.service.CloudDataSetListResponse;
import org.cloud.wetag.ui.adapter.CloudDataSetCardAdapter;
import org.cloud.wetag.utils.HttpUtil;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.Response;

public class CloudDataSetActivity extends BaseActivity {

  private LoginInfo loginInfo;

  public static void start(Activity activity, LoginInfo loginInfo) {
    Intent intent = new Intent(activity, CloudDataSetActivity.class);
    intent.putExtra("login_info", loginInfo);
    activity.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_cloud_data_set);
    setTitle("云端数据集");

    RecyclerView recyclerView = findViewById(R.id.cloud_dataset_recycler_view);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);

    loginInfo = (LoginInfo) getIntent().getSerializableExtra("login_info");
    List<DataSet> dataSets = null;
    try {
      dataSets = fetchCloudDataSet(loginInfo.getToken());
    } catch (Exception e) {
      fail(e.getMessage());
      finish();
    }
    CloudDataSetCardAdapter adapter = new CloudDataSetCardAdapter(dataSets);
    recyclerView.setAdapter(adapter);
  }

  private List<DataSet> fetchCloudDataSet(String token) throws IOException,
      ExecutionException, InterruptedException {
    List<DataSet> dataSetList = new LinkedList<>();
    Future<Response> future = Executors.newSingleThreadExecutor().submit(
        () -> HttpUtil.getSync(
            "https://modelarts.cn-north-1.myhuaweicloud.com/v1/" +
                loginInfo.getProjectId() + "/datasets",
            token));

    Response response = future.get();
    if (response.isSuccessful()) {
      String body = response.body().string();
      Gson gson = new Gson();
      CloudDataSetListResponse resp = gson.fromJson(body, CloudDataSetListResponse.class);
      for (CloudDataSet cloudDataSet : resp.getDatasets()) {
        DataSet dataSet = new DataSet(cloudDataSet.getDataset_name(), DataSet.UNKNOWN);
        if (cloudDataSet.getDescription().isEmpty()) {
          dataSet.setDesc("无描述");
        } else {
          dataSet.setDesc(cloudDataSet.getDescription());
        }
        dataSet.setCreateTime(cloudDataSet.getCreate_time());
        dataSetList.add(dataSet);
      }
    }

    return dataSetList;
  }

  private void fail(String message) {
    Toast.makeText(CloudDataSetActivity.this.getApplicationContext(),
        "失败:" + message, Toast.LENGTH_LONG).show();
  }
}
