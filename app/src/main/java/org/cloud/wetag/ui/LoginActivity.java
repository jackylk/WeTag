package org.cloud.wetag.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.cloud.wetag.R;
import org.cloud.wetag.utils.HttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

  TextView userNameInput;
  TextView passwordInput;

  public static void start(Activity activity, int requestCode) {
    Intent intent = new Intent(activity, LoginActivity.class);
    activity.startActivityForResult(intent, requestCode);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    userNameInput = findViewById(R.id.user_name_input);
    passwordInput = findViewById(R.id.password_input);
    findViewById(R.id.login_confirm_button).setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    String username = userNameInput.getText().toString();
    String password = passwordInput.getText().toString();
    if (username.isEmpty() || password.isEmpty()) {
      Toast.makeText(
          this.getApplicationContext(), "请输入账户名和密码", Toast.LENGTH_SHORT).show();
      return;
    }
    userLogin(username, password);
  }


  private void userLogin(String username, String password) {
    String loginRequest = "{\n" +
        "  \"auth\": {\n" +
        "    \"identity\": {\n" +
        "      \"methods\": [\"password\"],\n" +
        "      \"password\": {\n" +
        "        \"user\": {\n" +
        "          \"name\": \"" + username + "\",\n" +
        "          \"password\": \"" + password + "\",\n" +
        "          \"domain\": {\n" +
        "            \"name\": \"" + username + "\"\n" +
        "          }\n" +
        "        }\n" +
        "      }\n" +
        "    },\n" +
        "    \"scope\": {\n" +
        "      \"project\": {\n" +
        "        \"name\": \"cn-north-1\"\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}";

    HttpUtil.postAsync("https://iam.cn-north-1.myhuaweicloud.com/v3/auth/tokens",
        loginRequest,
        new Callback() {
          @Override
          public void onFailure(Call call, IOException e) {
            runOnUiThread(() -> {
              Toast.makeText(LoginActivity.this.getApplicationContext(),
                  "登录失败:" + e.getMessage(), Toast.LENGTH_LONG).show();
            });
            setResult(RESULT_FIRST_USER);
            finish();
          }

          @Override
          public void onResponse(Call call, Response response) {
            if (response.isSuccessful()) {
              runOnUiThread(() -> {
                Toast.makeText(LoginActivity.this.getApplicationContext(),
                    "登录成功", Toast.LENGTH_SHORT).show();
              });
              Intent intent = new Intent();
              intent.putExtra("token", response.header("X-Subject-Token"));
              intent.putExtra("username", username);
              setResult(RESULT_OK, intent);
            } else {
              runOnUiThread(() -> {
                Toast.makeText(LoginActivity.this.getApplicationContext(),
                    "登录失败:" + response.message(), Toast.LENGTH_LONG).show();
              });
              setResult(RESULT_FIRST_USER);
            }
            finish();
          }
        });
  }


}
