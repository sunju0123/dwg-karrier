package com.dwg_karrier.roys;

import static com.dwg_karrier.roys.ContentSwipe.saveSwipeActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

public class ListActivity extends AppCompatActivity {
  public static Activity saveActivity;
  ListView lv;
  Date finTime; // expected finish time
  Date curTime; // current time
  double duration; // time duration between current_time and finish time
  int flag;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.list);
    saveActivity = ListActivity.this;
    Intent getTimeInfo = new Intent(this.getIntent());
    finTime = (Date) getTimeInfo.getSerializableExtra("finTime");
    curTime = (Date) getTimeInfo.getSerializableExtra("curTime");
    flag = getTimeInfo.getCharExtra("FLAG", '2');

    DataBaseOpenHelper dbHelper = new DataBaseOpenHelper(this);
    FloatingActionButton changeMode = (FloatingActionButton) findViewById(R.id.toSwipe);
    changeMode.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        saveSwipeActivity = null;
        Intent openSwipe = new Intent(ListActivity.this, ContentSwipe.class); // open Recommend Lists
        openSwipe.putExtra("finTime", finTime);
        openSwipe.putExtra("curTime", curTime);
        startActivity(openSwipe);
        finish();
      }
    });
    final ArrayList<ScriptedURL> unreadPageList;

    if(flag == '0') {
      changeMode.setVisibility(View.INVISIBLE);
      unreadPageList = dbHelper.getUnreadUrlList();
    } else if(flag == '1') {
      changeMode.setVisibility(View.INVISIBLE);
      unreadPageList = dbHelper.getUnreadRecommededUrlList();
    } else {
      final int minute = 60000;
      duration = (finTime.getTime() - curTime.getTime()) / minute;
      unreadPageList = dbHelper.getScriptedUrlListByTime(0, (int)duration) ;
    }
    lv = (ListView) findViewById(R.id.listView);
    lv.setAdapter(new ListViewAdapter(this, R.layout.item, unreadPageList));

    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent openSelectedPage = new Intent(ListActivity.this, ContentView.class);

        if(flag == '0') {
          openSelectedPage.putExtra("FLAG", '0');
        } else if(flag == '1') {
          openSelectedPage.putExtra("FLAG", '1');
        } else {
          openSelectedPage.putExtra("finTime", finTime);
          openSelectedPage.putExtra("curTime", curTime);
        }
        ScriptedURL pageInfo = unreadPageList.get(position);

        openSelectedPage.putExtra("title", pageInfo.getTitle());
        openSelectedPage.putExtra("content", pageInfo.getContent());
        openSelectedPage.putExtra("url", pageInfo.getUrl());
        startActivity(openSelectedPage);
      }
    });

    // from ContentView
    Intent getReadTime = new Intent(this.getIntent());
    String readTime = getReadTime.getStringExtra("readTime");
    if (readTime != null) {
      Toast checkInfo = Toast.makeText(getApplicationContext(), "Congratulations!" + "\n" +
          "You finished reading in " + readTime + "sec", Toast.LENGTH_LONG);
      checkInfo.setGravity(Gravity.CENTER, 0, 0);
      checkInfo.show();
    }
  }
}
