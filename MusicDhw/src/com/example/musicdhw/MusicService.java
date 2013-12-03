package com.example.musicdhw;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;

public class MusicService extends Service implements OnCompletionListener {
	private MediaPlayer mediaPlayer;
	private int musicCheck;
	private int musicId;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent.getAction().equals("start")) {
			musicId = intent.getExtras().getInt("musicId");
			if (mediaPlayer == null) {
				mediaPlayer = MediaPlayer.create(this, musicId);
				musicCheck = musicId;
			} else if (musicId != musicCheck) {
				mediaPlayer.release();
				mediaPlayer = null;
				mediaPlayer = MediaPlayer.create(this, musicId);
				musicCheck = musicId;
			}
			mediaPlayer.start();
			mediaPlayer.setOnCompletionListener(this);
		} else if (intent.getAction().equals("pause")) {
			if (mediaPlayer != null) {
				mediaPlayer.pause();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Intent intent = new Intent();
		intent.setAction("toNext");
		sendBroadcast(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mediaPlayer.release();
		mediaPlayer = null;
	}

}
