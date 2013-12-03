package com.example.musicdhw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class MainActivity extends Activity implements OnClickListener {
	private ImageButton playButton;
	private ImageButton stopButton;
	private ImageButton shuffleButton;
	private ListView listView;
	private TextView textView;
	private String[] titles;
	private Map<String, Integer> deriveId;
	private SparseArray<String> deriveTitle;
	private ArrayList<Integer> idList;
	private String musicKey;
	private boolean playCheck;
	private boolean shuffleCheck;
	private int[] resIds;
	private static final int NEXT = 1;
	private static final int CURRENT = 0;
	private static final int PREV = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ImageButton prevButton = (ImageButton) findViewById(R.id.prevButton);
		ImageButton nextButton = (ImageButton) findViewById(R.id.nextButton);
		playButton = (ImageButton) findViewById(R.id.playButton);
		stopButton = (ImageButton) findViewById(R.id.stopButton);
		shuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
		prevButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		playButton.setOnClickListener(this);
		stopButton.setOnClickListener(this);
		shuffleButton.setOnClickListener(this);
		listView = (ListView) findViewById(R.id.listView1);
		textView = (TextView) findViewById(R.id.textView1);
		deriveId = new HashMap<String, Integer>();
		deriveTitle = new SparseArray<String>();
		idList = new ArrayList<Integer>();

		titles = new String[] { "オルゴールBGM", "捨てられた雪原", "ループ用BGM008",
				"ループ用BGM026", "春の陽気", "亡き王女の為のセプテット", "おてんば恋娘", "お茶の時間",
				"Starting Japan" };
		resIds = new int[] { R.raw.nc2422, R.raw.nc7400, R.raw.nc10100,
				R.raw.nc10812, R.raw.nc11577, R.raw.nc13447, R.raw.nc20349,
				R.raw.nc20612, R.raw.nc29204, };
		
		for (int i = 0; i < resIds.length; i++) {
			deriveId.put(titles[i], resIds[i]);
			deriveTitle.put(resIds[i], titles[i]);
			idList.add(resIds[i]);
		}
		textView.setText(titles[0]);
		musicKey = titles[0];

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice, titles);
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setItemChecked(0, true);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				musicKey = listView.getItemAtPosition(position).toString();
				textView.setText(musicKey);
				if (playCheck == true) {
					changeMusic(CURRENT);
					playMusic();
				}
			}
		});

		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				changeMusic(NEXT);
				playMusic();
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction("toNext");
		registerReceiver(receiver, filter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.playButton:
			if (playCheck == false) {
				playCheck = true;
				playButton.setImageResource(R.drawable.pause);
				textView.setText(musicKey);
				changeMusic(CURRENT);
				playMusic();
			} else {
				playCheck = false;
				playButton.setImageResource(R.drawable.play);
				Intent intent = new Intent(MainActivity.this,
						MusicService.class);
				intent.setAction("pause");
				startService(intent);
			}
			break;

		case R.id.stopButton:
			Intent intent = new Intent(MainActivity.this, MusicService.class);
			stopService(intent);
			playButton.setImageResource(R.drawable.play);
			playCheck = false;
			break;

		case R.id.prevButton:
			changeMusic(PREV);
			if (playCheck == true) {
				playMusic();
			}
			break;

		case R.id.nextButton:
			changeMusic(NEXT);
			if (playCheck == true) {
				playMusic();
			}
			break;

		case R.id.shuffleButton:
			if (shuffleCheck == false) {
				shuffleCheck = true;
				shuffleButton.setImageResource(R.drawable.repeat);
				Collections.shuffle(idList);
			} else {
				shuffleCheck = false;
				shuffleButton.setImageResource(R.drawable.shuffle);
				idList = new ArrayList<Integer>();
				for (int i = 0; i < resIds.length; i++) {
					idList.add(resIds[i]);
				}
			}
			break;
		}
	}

	private int serchNumber(ArrayList<Integer> idList, int musicId) {
		int playNumber = 0;
		for (int i = 0; i < idList.size(); i++) {
			if (idList.get(i) == musicId) {
				playNumber = i;
			}
		}
		return playNumber;
	}

	private int checkPosition(String[] titles, String musicKey) {
		int checkPosition = 0;
		for (int i = 0; i < titles.length; i++) {
			if (titles[i].equals(musicKey)) {
				checkPosition = i;
			}
		}
		return checkPosition;
	}

	private void changeMusic(int changeAction) {
		int musicId = deriveId.get(musicKey);
		int playPosition = serchNumber(idList, musicId) + changeAction;
		if (changeAction != CURRENT) {
			if (changeAction == NEXT && playPosition == idList.size()) {
				musicKey = deriveTitle.get(idList.get(0));
			} else if (changeAction == PREV && playPosition == PREV) {
				musicKey = deriveTitle.get(idList.get(idList.size() + PREV));
			} else {
				musicKey = deriveTitle.get(idList.get(playPosition));
			}
		}
		textView.setText(musicKey);
		listView.setItemChecked(checkPosition(titles, musicKey), true);
		listView.setSelection(checkPosition(titles, musicKey));
	}

	private void playMusic() {
		Intent intent = new Intent(MainActivity.this, MusicService.class);
		intent.putExtra("musicId", deriveId.get(musicKey));
		intent.setAction("start");
		startService(intent);
	}
}
