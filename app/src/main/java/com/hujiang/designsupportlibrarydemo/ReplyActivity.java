package com.hujiang.designsupportlibrarydemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.me.lewisdeane.ldialogs.CustomDialog;

/**
 * Created by sxcui on 2015/9/2.
 */
public class ReplyActivity extends AppCompatActivity {
    private ActionBar ab;
    private EditText titleEditText;
    private EditText contentEditText;
    private CustomDialog exitCustomDialog;
    private boolean isBoard;
    private int OperateId;
    private RequestQueue mQueue;
    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reply_activity);

        Intent intent = getIntent();
        isBoard = intent.getBooleanExtra("isBoard",false);
        OperateId = intent.getIntExtra("id",-1);
        int replyId = intent.getIntExtra("replyId",-1);

        progressDialog = new ProgressDialog(this);

        titleEditText = (EditText)findViewById(R.id.et_title);
        contentEditText = (EditText)findViewById(R.id.et_content);
        if(replyId != -1) contentEditText.setText(">>No."+replyId+"\n");


        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("添加串");

        final CustomDialog.Builder builder = new CustomDialog.Builder(this, "确认", "确定");
        builder.content("确定直接退出吗？");
        builder.negativeText("返回");
        builder.positiveColor("#FF0000");
        builder.contentTextSize(18);
        builder.contentColor("#000000");
        exitCustomDialog = builder.build();
        exitCustomDialog.setClickListener(new CustomDialog.ClickListener() {
            @Override
            public void onConfirmClick() {
                finish();
            }

            @Override
            public void onCancelClick() {

            }
        });
        mQueue = Volley.newRequestQueue(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reply_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case android.R.id.home:
                if(titleEditText.getText().length() != 0 || contentEditText.getText().length() != 0) {
                    exitCustomDialog.show();
                }
                else finish();
                break;
            case R.id.menu_submit:
                Submit();
                break;
        }
        return true;
    }

    private void Submit()
    {
        progressDialog.setTitle("发送数据");
        progressDialog.setMessage("正在提交，请稍后...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        HashMap<String, String> mMap = new HashMap<String, String>();
        if(isBoard) mMap.put("fid", OperateId+"");
        else mMap.put("resto", OperateId+"");
        if(titleEditText.getText().length() != 0) mMap.put("title", titleEditText.getText().toString());
        mMap.put("content", contentEditText.getText().toString());
        JsonObjectPostRequest jsonObjectPostRequest = new JsonObjectPostRequest(
                (isBoard?"http://h.nimingban.com/Home/Forum/doPostThread.html":"http://h.nimingban.com/Home/Forum/doReplyThread.html"),
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                progressDialog.cancel();

                //从服务器响应response中的jsonObject中取出cookie的值，存到本地sharePreference
                try {
                    String resContent = jsonObject.getString("Content");

                    Pattern p = Pattern.compile("<p class=\"error\">(.*?)</p>");
                    Matcher m = p.matcher(resContent);

                    if(m.find())
                    {
                        CustomDialog.Builder builder = new CustomDialog.Builder(ReplyActivity.this, "发生了一些错误", "朕已阅");
                        builder.content(m.group(1));
                        CustomDialog customDialog = builder.build();
                        customDialog.show();
                    }else{
                        Intent intent = new Intent();
                        intent.putExtra("data_return",true);
                        intent.putExtra("replyContent",contentEditText.getText().toString());
                        setResult(RESULT_OK,intent);
                        finish();
                    }

                    Log.d("adao",jsonObject.getString("Content"));
                }catch (Exception err)
                {

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Snackbar.make(getWindow().getDecorView(),"网络错误，发送失败",Snackbar.LENGTH_LONG);
            }
        }, mMap);

        String localCookieStr = CookieManager.Get(this);
        if(!localCookieStr.equals("")){
            jsonObjectPostRequest.setSendCookie(localCookieStr);//向服务器发起post请求时加上cookie字段
        }
        mQueue.add(jsonObjectPostRequest);

    }

    @Override
    public void onBackPressed() {
        if(titleEditText.getText().length() != 0 || contentEditText.getText().length() != 0) {
            exitCustomDialog.show();
        }
        else finish();
    }

}

