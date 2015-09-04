package com.smilec.adao;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import uk.me.lewisdeane.ldialogs.CustomListDialog;

/**
 * Created by sxcui on 2015/9/4.
 */
public class SettingActivity extends AppCompatActivity {
    private ActionBar ab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);

        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("设置");

        Button reloadBtn = (Button)findViewById(R.id.setting_reload_btn);
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomListDialog.Builder builder = new CustomListDialog.Builder(SettingActivity.this, "选择你的操作项", new String[]{"重载版块列表","重新获取Cookie"});
                CustomListDialog customListDialog = builder.build();
                customListDialog.setListClickListener(new CustomListDialog.ListClickListener() {
                    @Override
                    public void onListItemSelected(int i, String[] strings, String s) {
                        Intent intent = new Intent();
                        switch (i) {
                            case 0:
                                intent.putExtra("return", 1);
                                break;
                            case 1:
                                intent.putExtra("return", 2);
                                break;
                        }
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
                customListDialog.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
        }
        return true;
    }
}
