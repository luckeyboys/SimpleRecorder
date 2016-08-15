package net.sunniwell.simplerecorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.nio.BufferOverflowException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.opengl.Visibility;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.os.IResultReceiver;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import net.sunniwell.simplerecorder.adapter.MyListViewAdapter;
import net.sunniwell.simplerecorder.global.Contants;

/**
 * 功能 : 简易录音器。 1.实现开始录音,结束录音。 2.实现播放录音。 3.实现录音播放,暂停,继续播放录音。
 * 
 * @author 作者 郭勇
 * 
 *         创建时间：2016年7月20日 上午10:31:54
 * 
 */
public class MainActivity extends Activity implements OnClickListener {
	private final static String sTAG = "MainActivity";

	private Button mStartRecorder;
	private Button mStopRecorder;
	private TextView mShowTimer;
	private ProgressBar mPbProgress;
	private Button mBtnPlayer;// 开始播放按钮
	private Button mBtnStop;// 停止播放按钮
	private LinearLayout mLvPlayer;
	private LinearLayout mLvRecoder;
	private ListView mListView;

	private File mFileSaveDir;// 文件保存的文件夹路径
	private String mFileSavePath; // 保存文件的路径
	private boolean mIsRecording;
	private boolean mIsPlaying;
	private Handler mHandler = new mHandler();
	private long mStartTime;
	private List<File> mFiles;
	private String mSavePath;

	private MediaRecorder mMediaRecorder = null;
	private MediaPlayer mMediaPlayer = null;
	private MyListViewAdapter mAdapter;
	private String mPlayerPath;
	private Runnable mUpdateProgress;

	private class mHandler extends Handler {

		SimpleDateFormat format = new SimpleDateFormat("mm:ss");

		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {

				case Contants.UPDATE_TIME_RECORD:
					long currentTimeMillis = System.currentTimeMillis();
					String time = format.format(currentTimeMillis - mStartTime);
					mShowTimer.setText(time);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initData();
	}

	private void initView() {
		mShowTimer = (TextView) findViewById(R.id.tv_show_timer);
		mStartRecorder = (Button) findViewById(R.id.start_recorder);
		mStopRecorder = (Button) findViewById(R.id.stop_recorder);
		mPbProgress = (ProgressBar) findViewById(R.id.pb_progress);
		mBtnPlayer = (Button) findViewById(R.id.btn_player);
		mBtnStop = (Button) findViewById(R.id.btn_stop);
		mListView = (ListView) findViewById(R.id.lv_listview);
		mLvPlayer = (LinearLayout) findViewById(R.id.lv_player);
		mLvRecoder = (LinearLayout) findViewById(R.id.lv_recorder);
		mStartRecorder.setOnClickListener(this);
		mStopRecorder.setOnClickListener(this);
		mBtnPlayer.setOnClickListener(this);
		mBtnStop.setOnClickListener(this);
	}

	private void initData() {
		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			mSavePath = Environment.getExternalStorageDirectory().getPath() + "/SimpleRecorder";
		} else {
			// 手机自身内存的路径
			mSavePath = Environment.getDataDirectory().getPath() + "/SimpleRecorder";
		}
		mFileSaveDir = new File(mSavePath);
		if (!mFileSaveDir.exists()) {
			mFileSaveDir.mkdir();
		}
		mFiles = new ArrayList<>();
		mFiles.addAll(Arrays.asList(mFileSaveDir.listFiles()));
		mAdapter = new MyListViewAdapter(this, mFiles);
		mListView.setAdapter(mAdapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mLvPlayer.setVisibility(View.VISIBLE);
				mLvRecoder.setVisibility(View.GONE);
				mIsPlaying = true;
				mBtnPlayer.setText("暂停");
				mPlayerPath = mFiles.get(position).getAbsolutePath();
				play(mPlayerPath);
			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				final File file = mFiles.get(position);
				AlertDialog.Builder builder = new Builder(MainActivity.this);
				builder.setMessage("你是否要删除“" + file.getName() + "”?");
				builder.setTitle("删除");
				builder.setIcon(android.R.drawable.ic_menu_delete);
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
				builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						file.delete();
						if (mFiles.size() != 0) {
							mFiles.clear();
						}
						mFiles.addAll(Arrays.asList(mFileSaveDir.listFiles()));
						mAdapter.notifyDataSetChanged();
						if (mMediaPlayer != null) {
							mMediaPlayer.reset();
						}
						mPbProgress.setProgress(0);
					}
				});
				builder.create().show();
				return true;
			}
		});

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_recorder:
			mStartTime = System.currentTimeMillis();
			mIsRecording = true;
			mStartRecorder.setEnabled(false);
			mStopRecorder.setEnabled(true);
			startRecorder();
			break;
		case R.id.stop_recorder:
			if (mFiles.size() != 0) {
				mFiles.clear();
			}
			mFiles.addAll(Arrays.asList(mFileSaveDir.listFiles()));
			mAdapter.notifyDataSetChanged();
			mIsRecording = false;
			mStartRecorder.setEnabled(true);
			mStopRecorder.setEnabled(false);
			stopRecorder();
			break;
		case R.id.btn_player:
			if (mIsPlaying) {
				pause();
				mBtnPlayer.setText("播放");
			} else {
				start();
				mBtnPlayer.setText("暂停");
			}
			mIsPlaying = !mIsPlaying;
			break;
		case R.id.btn_stop:
			ceasePlayer();
			break;
		default:
			break;
		}
	}

	/**
	 * 功能 : 开始录音的方法。
	 * 
	 * @author 作者 郭勇 创建时间：2016年8月11日
	 */
	private void startRecorder() {
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			// 设置保存路径
			long currentTime = System.currentTimeMillis();
			Log.i(sTAG, currentTime + "");
			mFileSavePath = mSavePath + "/gy" + currentTime + ".amr";
			File file = new File(mFileSavePath);
			mMediaRecorder.setOutputFile(file.getAbsolutePath());
			mMediaRecorder.prepare();
			mMediaRecorder.start();
			new UpdateThread().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void stopRecorder() {
		if (mFileSavePath != null && mMediaRecorder != null) {
			mMediaRecorder.stop();
			mMediaRecorder.release();
		}
	}

	/**
	 * 功能: 开始播放录音.
	 * 
	 * @author 作者 郭勇 创建时间：2016年8月12日
	 */
	private void play(String path) {
		if (mMediaPlayer != null) {
			stopPlayer();
		}
		mMediaPlayer = new MediaPlayer();
		try {
			mMediaPlayer.setDataSource(path);// 设置多媒体数据来源
			mMediaPlayer.prepare(); // 准备
			mMediaPlayer.start(); // 开始
			updateProgress();
		} catch (IOException e) {
			Log.e(sTAG, "播放录音失败");
		}
		// 播放完成，改变按钮状态
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				mIsPlaying = !mIsPlaying;
				mBtnPlayer.setText("播放");
				mLvPlayer.setVisibility(View.GONE);
				mLvRecoder.setVisibility(View.VISIBLE);
			}
		});
	}

	private void updateProgress() {
		mPbProgress.setMax(mMediaPlayer.getDuration());// 设置进度条最大值
		mUpdateProgress = new Runnable() {
			@Override
			public void run() {
				mPbProgress.setProgress(mMediaPlayer.getCurrentPosition());
				if (mMediaPlayer.getCurrentPosition() <= mMediaPlayer.getDuration()) {
					mHandler.postDelayed(mUpdateProgress, 10);// 发送异步消息，实现实时更新进度条
				}
			}
		};
		mHandler.post(mUpdateProgress);// 开启子线程更新进度条
	}

	/**
	 * 功能 : 暂停播放录音
	 * 
	 * @author 作者 郭勇 创建时间：2016年8月12日
	 */
	private void pause() {
		mMediaPlayer.pause();
	}

	private void start() {
		mMediaPlayer.start();
	}

	private void stopPlayer() {
		mMediaPlayer.release();
		mMediaPlayer = null;
		mHandler.removeCallbacksAndMessages(null);
		mPbProgress.setProgress(0);
	}

	/**
	 * 功能 ： 停止播放录音的方法
	 * 
	 * @author 作者 郭勇 创建时间：2016年8月12日
	 */
	private void ceasePlayer() {
		mMediaPlayer.reset();
		mPbProgress.setProgress(0);
		mLvRecoder.setVisibility(View.VISIBLE);
		mLvPlayer.setVisibility(View.GONE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		if (mMediaRecorder != null) {
			mMediaRecorder.stop();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}

	/**
	 * 功能 : 更新录音的时间
	 * 
	 * @author 作者 郭勇 创建时间：2016年8月12日
	 */
	private class UpdateThread extends Thread {

		public void run() {
			while (mIsRecording) {
				try {
					new Thread().sleep(Contants.THREAD_DELAYED);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mHandler.sendEmptyMessage(Contants.UPDATE_TIME_RECORD);
			}
		}
	}

}
