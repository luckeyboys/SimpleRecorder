package net.sunniwell.simplerecorder.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import net.sunniwell.simplerecorder.R;
import net.sunniwell.simplerecorder.global.Contants;

/**
 * 功能 : listview的Adapter。
 *
 * @author 作者 郭勇 创建时间：2016年8月11日
 * 
 */
public class MyListViewAdapter extends BaseAdapter {
	private Context mContext;
	private List<File> mFiles;
	private SimpleDateFormat format;
	private MediaPlayer mMediaPlayer;

	public MyListViewAdapter(Context context, List<File> files) {
		this.mContext = context;
		this.mFiles = files;
		format = new SimpleDateFormat("mm:ss:S");
	}

	@Override
	public int getCount() {
		return mFiles.size();
	}

	@Override
	public Object getItem(int position) {
		return mFiles.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.listview_item, null);
			holder.mTvFileName = (TextView) convertView.findViewById(R.id.tv_filename);
			holder.mTvSize = (TextView) convertView.findViewById(R.id.tv_size);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mTvFileName.setText(mFiles.get(position).getName());
		long timer = getFileTimer(mFiles.get(position).toString());
		holder.mTvSize.setText(format.format(timer));
		return convertView;
	}

	class ViewHolder {
		TextView mTvFileName;
		TextView mTvSize;
	}

	/**
	 * 功能 ：获得文件的时长
	 * 
	 * @author 作者 郭勇 创建时间：2016年8月11日
	 */
	private long getFileTimer(String file) {
		int duration = 0;
		MediaPlayer mediaPlayer = null;
		try {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setDataSource(new FileInputStream(new File(file)).getFD());
			mediaPlayer.prepare();
			duration = mediaPlayer.getDuration();
			mediaPlayer.release();
			mediaPlayer = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return duration;
	}
}
