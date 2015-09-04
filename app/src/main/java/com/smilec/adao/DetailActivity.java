package com.smilec.adao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
//http://h.nimingban.com/Api/showf?id=2&page=0
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RequestQueue mQueue;
    private boolean isLoadingMore = false;
    private int ThisPage = 0;
    private int BoardId;
    private RecyclerView.Adapter adapter;
    private List<Chuan> ChuanList;
    private int mode;
    private boolean LoadOver = false;
    private Chuan chuanHead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final FloatingActionsMenu FActionMenu = (FloatingActionsMenu) findViewById(R.id.fab_menu);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(R.color.orange, R.color.green, R.color.blue);
        swipeRefreshLayout.setProgressViewOffset(false, -70, 50);

        Intent intent = getIntent();
        String ActionBarTitle = intent.getStringExtra("name");
        BoardId = intent.getIntExtra("id", 0);
        mode = intent.getIntExtra("mode", 0);

        if(mode == 1)
        {
            chuanHead = new Chuan();
            chuanHead.id = intent.getIntExtra("id", 0);
            chuanHead.userid = intent.getStringExtra("userid") + " - " + intent.getIntExtra("id", 0);
            chuanHead.title = intent.getStringExtra("name");
            chuanHead.time = intent.getStringExtra("time");
            chuanHead.content = intent.getStringExtra("content");
            chuanHead.count = intent.getIntExtra("count", 0);
            chuanHead.img = intent.getStringExtra("img");
            chuanHead.isEndPage = true;
        }

        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(ActionBarTitle);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_detail);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
                int totalItemCount = mLayoutManager.getItemCount();
                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
                // dy>0 表示向下滑动
                FActionMenu.collapse();
                if (lastVisibleItem >= totalItemCount - 5 && dy > 0) {
                    if (isLoadingMore) {
                        Log.d("adao", "ignore manually update!");
                    } else {
                        if(!LoadOver) {
                            isLoadingMore = true;
                            swipeRefreshLayout.setRefreshing(true);
                            LoadNextPage();
                        }
                    }
                }
            }
        });

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(mRecyclerView.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        Log.d("adao","setAdpter");
        try {
            ChuanList = new ArrayList<Chuan>();
            adapter = new DetailRecyclerAdapter(this,ChuanList);
            mRecyclerView.setAdapter(adapter);
        }catch (Exception err)
        {
            Log.e("adao","error:"+err.toString());
        }


        FloatingActionButton ScrollToUpBtn = (FloatingActionButton)findViewById(R.id.fab_scroll_to_up);
        FloatingActionButton BackBtn = (FloatingActionButton)findViewById(R.id.fab_bak);
        FloatingActionButton AddContentBtn = (FloatingActionButton)findViewById(R.id.fab_add);

        ScrollToUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.scrollToPosition(0);
                FActionMenu.collapse();
            }
        });

        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        AddContentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this,ReplyActivity.class);
                if(mode == 1)
                {
                    intent.putExtra("isBoard",false);
                }
                else
                {
                    intent.putExtra("isBoard",true);
                }
                intent.putExtra("id",BoardId);
                startActivityForResult(intent,1);
            }
        });

        mQueue = Volley.newRequestQueue(this);
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                if(resultCode == RESULT_OK)
                {
                    if(data.getBooleanExtra("data_return",true))
                    {
                        Snackbar.make(getWindow().getDecorView(),"已送出",Snackbar.LENGTH_LONG).show();
                        Chuan chuan = new Chuan();
                        String s = data.getStringExtra("replyContent").replaceAll("\n", "<br/>");
                        chuan.content = s;
                        chuan.time = "刚刚";
                        chuan.userid = "我";
                        chuan.id = -1;
                        chuan.isEndPage = true;
                        ChuanList.add(chuan);
                        adapter.notifyDataSetChanged();
                    }
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private SwipeRefreshLayout swipeRefreshLayout;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    @Override
    public void onRefresh() {
        ThisPage = 0;
        LoadOver = false;
        ChuanList.clear();
        if(mode == 1) ChuanList.add(chuanHead);
        LoadNextPage();
    }

    private final static String ChuanListUrl = "http://h.nimingban.com/Api/showf?id=%d&page=%d";
    private final static String ChuanDetailUrl = "http://h.nimingban.com/Api/thread?id=%d&page=%d";

    public void LoadNextPage()
    {
        isLoadingMore = true;

        ++ThisPage;
        StringRequest request = new StringRequest(
                Request.Method.GET,
                String.format((mode == 0 ? ChuanListUrl:ChuanDetailUrl),BoardId,ThisPage),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("adao",s);
                        try
                        {
                            JSONArray jsonArray;
                            if(mode == 0)
                            {
                                jsonArray = new JSONArray(s);
                            }
                            else
                            {
                                JSONObject tempjo = new JSONObject(s);
                                jsonArray = tempjo.getJSONArray("replys");
                            }

                            int i;
                            for (i = 0; i < jsonArray.length(); ++i) {
                                JSONObject chuanJsonObj = (JSONObject) jsonArray.get(i);

                                Chuan tChuan = new Chuan();
                                tChuan.id = chuanJsonObj.getInt("id");

                                if(mode == 0) tChuan.userid = chuanJsonObj.getString("userid");
                                else tChuan.userid = chuanJsonObj.getString("userid") +" - "+ chuanJsonObj.getString("id");

                                if(mode == 0) tChuan.count = chuanJsonObj.getInt("replyCount");
                                tChuan.time = chuanJsonObj.getString("now");
                                tChuan.title = chuanJsonObj.getString("title");
                                tChuan.content = chuanJsonObj.getString("content");
                                tChuan.img = chuanJsonObj.getString("img") + chuanJsonObj.getString("ext");

                                if(mode == 0) tChuan.isEndPage = false;
                                else
                                {
                                    tChuan.isEndPage = true;
                                    tChuan.formChuanId = chuanHead.id;
                                }

                                ChuanList.add(tChuan);
                            }
                            adapter.notifyDataSetChanged();
                            isLoadingMore = false;
                            swipeRefreshLayout.setRefreshing(false);

                            if(i == 0)
                            {
                                LoadOver = true;
                                Snackbar.make(getWindow().getDecorView(),"全部内容已加载完毕 (￣^￣)ゞ",Snackbar.LENGTH_SHORT).show();
                            }
                        }
                        catch (Exception err)
                        {
                            Log.e("adao",err.toString());
                            Snackbar.make(getWindow().getDecorView(),"出了一些问题，串不存在、没有网络或程序异常",Snackbar.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("adao",volleyError.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {  //设置头信息
                Map<String, String> map = new HashMap<String, String>();
                map.put("User-Agent", "HavfunClient-Android");
                return map;
            }
        };

        mQueue.add(request);
    }
}
