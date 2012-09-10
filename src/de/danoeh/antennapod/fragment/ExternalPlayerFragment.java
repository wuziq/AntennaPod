package de.danoeh.antennapod.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import de.danoeh.antennapod.AppConfig;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.asynctask.FeedImageLoader;
import de.danoeh.antennapod.feed.FeedMedia;
import de.danoeh.antennapod.service.PlaybackService;
import de.danoeh.antennapod.util.Converter;
import de.danoeh.antennapod.util.PlaybackController;

/**
 * Fragment which is supposed to be displayed outside of the MediaplayerActivity
 * if the PlaybackService is running
 */
public class ExternalPlayerFragment extends SherlockFragment {
	private static final String TAG = "ExternalPlayerFragment";

	private ViewGroup fragmentLayout;
	private ImageView imgvCover;
	private ViewGroup layoutInfo;
	private TextView txtvTitle;
	private TextView txtvPosition;
	private ImageButton butPlay;

	private PlaybackController controller;

	public ExternalPlayerFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.external_player_fragment,
				container, false);
		fragmentLayout = (ViewGroup) root.findViewById(R.id.fragmentLayout);
		imgvCover = (ImageView) root.findViewById(R.id.imgvCover);
		layoutInfo = (ViewGroup) root.findViewById(R.id.layoutInfo);
		txtvTitle = (TextView) root.findViewById(R.id.txtvTitle);
		txtvPosition = (TextView) root.findViewById(R.id.txtvPosition);
		butPlay = (ImageButton) root.findViewById(R.id.butPlay);

		layoutInfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (AppConfig.DEBUG)
					Log.d(TAG, "layoutInfo was clicked");

				if (controller.getMedia() != null) {
					startActivity(PlaybackService.getPlayerActivityIntent(
							getActivity(), controller.getMedia()));
				}
			}
		});
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		controller = new PlaybackController(getActivity()) {

			@Override
			public void setupGUI() {
			}

			@Override
			public void onPositionObserverUpdate() {
				int duration = controller.getDuration();
				int position = controller.getPosition();
				if (duration != PlaybackController.INVALID_TIME
						&& position != PlaybackController.INVALID_TIME) {
					txtvPosition.setText(getPositionString(position, duration));
				}
			}

			@Override
			public void onReloadNotification(int code) {
			}

			@Override
			public void onBufferStart() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onBufferEnd() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onBufferUpdate(float progress) {
			}

			@Override
			public void onSleepTimerUpdate() {
			}

			@Override
			public void handleError(int code) {
			}

			@Override
			public ImageButton getPlayButton() {
				return butPlay;
			}

			@Override
			public void postStatusMsg(int msg) {
			}

			@Override
			public void clearStatusMsg() {
			}

			@Override
			public void loadMediaInfo() {
				ExternalPlayerFragment.this.loadMediaInfo();
			}

			@Override
			public void onAwaitingVideoSurface() {
			}

			@Override
			public void onServiceQueried() {
			}
		};
		butPlay.setOnClickListener(controller.newOnPlayButtonClickListener());
	}

	@Override
	public void onResume() {
		super.onResume();
		controller.init();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (AppConfig.DEBUG)
			Log.d(TAG, "Fragment is about to be destroyed");
		if (controller != null) {
			controller.release();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (controller != null) {
			controller.pause();
		}
	}

	private void loadMediaInfo() {
		if (AppConfig.DEBUG)
			Log.d(TAG, "Loading media info");
		if (controller.serviceAvailable()) {
			FeedMedia media = controller.getMedia();
			if (media != null) {
				txtvTitle.setText(media.getItem().getTitle());
				FeedImageLoader.getInstance().loadThumbnailBitmap(
						media.getItem().getFeed().getImage(),
						imgvCover,
						(int) getActivity().getResources().getDimension(
								R.dimen.external_player_height));

				txtvPosition.setText(getPositionString(media.getPosition(),
						media.getDuration()));
				fragmentLayout.setVisibility(View.VISIBLE);
				if (controller.isPlayingVideo()) {
					butPlay.setVisibility(View.GONE);
				} else {
					butPlay.setVisibility(View.VISIBLE);
				}
			} else {
				Log.w(TAG,
						"loadMediaInfo was called while the media object of playbackService was null!");
			}
		} else {
			Log.w(TAG,
					"loadMediaInfo was called while playbackService was null!");
		}
	}

	private String getPositionString(int position, int duration) {
		return Converter.getDurationStringLong(position) + " / "
				+ Converter.getDurationStringLong(duration);
	}
}
