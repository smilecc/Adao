package com.hujiang.designsupportlibrarydemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.me.lewisdeane.ldialogs.CustomDialog;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private List<Forum> ForumsList;

    public ProgressDialog progressDialog;
    public boolean IsFisrtRun;

    public static final int SHOW_PROGRESS_DIALOG = 1;
    public static final int CANCEL_PROGRESS_DIALOG = 2;
    public static final int CREATE_DATABASE_OVER = 3;
    public static final int GET_COOKIE_FAIL = 4;
    public static final int GET_COOKIE_REFUSE = 5;
    public static final int GET_COOKIE_OVER = 6;
    public static final int HAVE_NEW_VERSION = 7;

    public static RequestQueue Queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Queue = Volley.newRequestQueue(MainActivity.this);

        progressDialog = new ProgressDialog(MainActivity.this);

        String DbPath = getDatabasePath("Adao.db").getPath();
        File dbFile = new File(DbPath);
        if(dbFile.exists())
        {
            IsFisrtRun = false;
        }
        else
        {
            IsFisrtRun = true;
            DbInitThread dbInitThread = new DbInitThread(this,this);
            dbInitThread.start();
            CookieInitThread cookieInitThread = new CookieInitThread(this);
            cookieInitThread.start();
        }

        // 设置Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 顶栏样式设置
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_main_drawer);
        NavigationView navigationView =
                (NavigationView) findViewById(R.id.nv_main_navigation);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        if(!IsFisrtRun) setupViewPager();
    }

    private void ShowFirstRunDialog()
    {
        progressDialog.setTitle("加载数据");
        progressDialog.setMessage("首次运行，正在加载数据...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void CancelFisrtRunDialog()
    {
        setupViewPager();
        progressDialog.cancel();
    }

    Handler myhandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case SHOW_PROGRESS_DIALOG:
                    ShowFirstRunDialog();
                    break;
                case CREATE_DATABASE_OVER:
                    Snackbar.make(getWindow().getDecorView(), "(＾o＾)ﾉ  加载完毕~", Snackbar.LENGTH_SHORT).show();
                    CancelFisrtRunDialog();
                    break;
                case GET_COOKIE_FAIL:
                    Snackbar.make(getWindow().getDecorView(), "(`ε´ )获取饼干(Cookie)失败，程序异常，请联系作者 ", Snackbar.LENGTH_LONG).show();
                    break;
                case GET_COOKIE_REFUSE:
                    Snackbar.make(getWindow().getDecorView(), "(((　ﾟдﾟ)))获取饼干(Cookie)失败，被主站残忍拒绝", Snackbar.LENGTH_LONG).show();
                case GET_COOKIE_OVER:
                    Snackbar.make(getWindow().getDecorView(), "(つд⊂)获取饼干(Cookie)成功，尽情的丧尸吧", Snackbar.LENGTH_LONG).show();
                case HAVE_NEW_VERSION:
                    final CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this, "(｀･ω･)", "更新");
                    builder.content("有新版本，大爷更新吗？");
                    builder.negativeText("不更新");
                    builder.positiveColor("#009900");
                    builder.contentTextSize(18);
                    builder.contentColor("#000000");
                    CustomDialog customDialog = builder.build();

                    customDialog.setClickListener(new CustomDialog.ClickListener() {
                        @Override
                        public void onConfirmClick() {
                            Uri uri = Uri.parse("http://fir.im/acdao");
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelClick() {

                        }
                    });
                    customDialog.show();
                    break;
            }
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overaction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                startActivityForResult(intent, 1);
                break;
        }
        return true;
    }

    private void setupViewPager() {
        VersionCheckThread versionCheckThread = new VersionCheckThread(MainActivity.this);
        versionCheckThread.start();
        this.ForumsList = GetForum();

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        List<Fragment> fragments = new ArrayList<>();

        for (Forum tempForum:this.ForumsList) {
            mTabLayout.addTab(mTabLayout.newTab().setText(tempForum.Name));
            fragments.add(new ListFragment(GetBoard(tempForum.Id)));
        }

        FragmentAdapter adapter =
                new FragmentAdapter(getSupportFragmentManager(), fragments, this.ForumsList);
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabsFromPagerAdapter(adapter);


    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();

                        switch (menuItem.getItemId())
                        {
                            case R.id.nav_setting:
                                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                                startActivityForResult(intent,1);
                                break;
                        }

                        return true;
                    }
                });
    }

    private List<Forum> GetForum()
    {
        SQLiteHelper sqLiteHelper = new SQLiteHelper(this, "Adao.db", null, 1, this);
        SQLiteDatabase DataBase = sqLiteHelper.getReadableDatabase();
        List<Forum> forumList = new ArrayList<Forum>();
        Cursor cursor = DataBase.query("Forum",null,null,null,null,null,"sort");
        if(cursor.moveToFirst())
        {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                Forum tempForum = new Forum(id,name);
                forumList.add(tempForum);
            }while (cursor.moveToNext());
        }
        sqLiteHelper.close();
        return forumList;
    }

    private List<Board> GetBoard(int fgroup)
    {
        SQLiteHelper sqLiteHelper = new SQLiteHelper(this, "Adao.db", null, 1, this);
        SQLiteDatabase DataBase = sqLiteHelper.getReadableDatabase();
        List<Board> boardList = new ArrayList<Board>();
        Cursor cursor = DataBase.query("Board",null,"fgroup="+fgroup,null,null,null,"sort");
        if(cursor.moveToFirst())
        {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String msg = cursor.getString(cursor.getColumnIndex("msg"));
                Board tempBoard = new Board(id,name,fgroup,msg);
                boardList.add(tempBoard);
            }while (cursor.moveToNext());
        }
        sqLiteHelper.close();
        return boardList;
    }

    private class DbInitThread extends Thread{
        Context mContext;
        MainActivity mMainActivity;

        public DbInitThread(Context context,MainActivity mainActivity)
        {
            mContext = context;
            mMainActivity = mainActivity;
        }

        @Override
        public void run() {
            super.run();
            SQLiteHelper helper = new SQLiteHelper(mContext, "Adao.db", null, 1, mMainActivity);
            helper.getWritableDatabase();
        }
    }

    private class VersionCheckThread extends Thread{
        MainActivity mActivity;

        public VersionCheckThread(MainActivity activity)
        {
            mActivity = activity;
        }

        @Override
        public void run() {
            StringRequest request = new StringRequest(
                    Request.Method.GET,
                    "http://adao.smilec.org/vsapi.php?mode=select&uid=1&vsname=adao&res=version",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            try {
                                Context mContext = (Context)mActivity;
                                PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                                int code = info.versionCode;
                                int serverCode = Integer.parseInt(s);
                                if(code < serverCode)
                                {
                                    Message message = new Message();
                                    message.what = MainActivity.HAVE_NEW_VERSION;
                                    mActivity.myhandler.sendMessage(message);
                                }

                            }catch (Exception err)
                            {

                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

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
            Queue.add(request);
        }
    }

    private class CookieInitThread extends Thread{
        MainActivity mMainActivity;
        boolean mTips;

        public CookieInitThread(MainActivity mainActivity,boolean Tips)
        {
            mMainActivity = mainActivity;
            mTips = Tips;
        }


        public CookieInitThread(MainActivity mainActivity)
        {
            mMainActivity = mainActivity;
            mTips = false;
        }

        @Override
        public void run() {
            super.run();
            CookieManager.initCookie(mMainActivity,mTips);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                if(resultCode == RESULT_OK)
                {
                    switch (data.getIntExtra("return", 0))
                    {
                        case 1:
                            String DbPath = getDatabasePath("Adao.db").getPath();
                            File dbFile = new File(DbPath);
                            dbFile.delete();

                            DbInitThread dbInitThread = new DbInitThread(this,this);
                            dbInitThread.start();
                            break;
                        case 2:
                            CookieInitThread cookieInitThread = new CookieInitThread(MainActivity.this,true);
                            cookieInitThread.start();
                        default:
                            break;
                    }
                }
                break;
        }
    }
}
