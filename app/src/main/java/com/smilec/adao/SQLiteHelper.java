package com.smilec.adao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Message;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sxcui on 2015/8/30.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    private Context mContext;
    private RequestQueue mQueue;
    private MainActivity mMainActivity;
    private SQLiteDatabase mDb;

    // 建立版块区列表
    private static final String CREATE_FORUM = "create table Forum ("
            + "id integer primary key, "
            + "name text, "
            + "sort integer)";
    // 插入Forum的格式化语句
    private static final String INSERT_INTO_FORUM = "INSERT INTO Forum (id,name,sort) VALUES (%d,'%s',%d)";
    // 建立版块列表
    private static final String CREATE_BOARD = "create table Board ("
            + "id integer primary key, "
            + "name text, "
            + "sort integer, "
            + "msg text, "
            + "fgroup integer)";
    // 插入Board的格式化语句
    private static final String INSERT_INTO_BOARD = "INSERT INTO Board (id,name,sort,fgroup,msg) VALUES (%d,'%s',%d,%d,'%s')";

    private static final String CREATE_SETTING = "create table Setting ("
            + "name text, "
            + "value text)";
    public static final String INSERT_INTO_SETTING = "INSERT INTO Setting (name,value) VALUES ('%s','%s')";

    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, MainActivity argMainActivity) {
        super(context, name, factory, version);
        mContext = context;
        mQueue = Volley.newRequestQueue(mContext);
        mMainActivity = argMainActivity;

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        mDb = db;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        mDb = db;

        Message message = new Message();
        message.what = MainActivity.SHOW_PROGRESS_DIALOG;
        mMainActivity.myhandler.sendMessage(message);

        // Create table
        db.execSQL(CREATE_FORUM);
        db.execSQL(CREATE_BOARD);
        db.execSQL(CREATE_SETTING);

        //initCookie();

        // Create Http,to get the forum list
        StringRequest request = new StringRequest(
                Request.Method.GET,
                "http://h.nimingban.com/Api/getForumList",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        s = s.replaceAll("(<font.*?font>)", "");
                        s = s.replaceAll("(<.*?>)", "");
                        s = s.replaceAll("(&.*?;)", "");
                        s = s.replaceAll("msg\":\"((\\\\r\\\\n){1,4})", "msg\":\"");
                        s = s.replaceAll("((\\\\r\\\\n){1,2})\"", "\"");
                        s = s.replaceAll("(\\\\r\\\\n){2}", "\\\\r\\\\n");
                        try {
                            // parse string to json array
                            JSONArray jsonArray = new JSONArray(s);
                            // parse 'forums' form json array
                            for (int i = 0; i < jsonArray.length(); ++i) {
                                JSONObject forumJsonObj = (JSONObject) jsonArray.get(i);

                                // get data
                                int id = forumJsonObj.getInt("id");
                                String name = forumJsonObj.getString("name");
                                int sort = forumJsonObj.getInt("sort");

                                // insert to sqlite table
                                db.execSQL(String.format(INSERT_INTO_FORUM, id, name, sort));

                                // parse boards from this forum
                                JSONArray boardJsonArr = forumJsonObj.getJSONArray("forums");

                                for (int j = 0; j < boardJsonArr.length(); ++j) {
                                    JSONObject boardsJsonObj = (JSONObject) boardJsonArr.get(j);

                                    int boardId = boardsJsonObj.getInt("id");

                                    // if had 'showName',save the showName,else save 'name'
                                    String boardShowName = boardsJsonObj.getString("showName");
                                    if (boardShowName.equals("")) {
                                        boardShowName = boardsJsonObj.getString("name");
                                    }
                                    int boardSort = boardsJsonObj.getInt("sort");
                                    int boardFgroup = boardsJsonObj.getInt("fgroup");
                                    String boardMsg = boardsJsonObj.getString("msg");

                                    // insert into 'board' table
                                    db.execSQL(String.format(INSERT_INTO_BOARD, boardId, boardShowName, boardSort, boardFgroup, boardMsg));
                                }
                            }
                            db.close();
                            Message message = new Message();
                            message.what = MainActivity.CREATE_DATABASE_OVER;
                            mMainActivity.myhandler.sendMessage(message);
                        } catch (Exception err) {
                            Log.e("adao", "catch exception: " + err.toString());
                            Log.e("adao", "catch exception message: " + err.getMessage());
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

        mQueue.add(request);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
