package org.horaapps.leafpic.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.activities.SingleMediaActivity;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.StorageHelper;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedFragment;

import java.util.List;

/**
 * Created by dnld on 18/02/16.
 */

public class VideoFragment extends ThemedFragment {
	
	IconicsImageView videoInd;
	
	private Media video;
	
	public static VideoFragment newInstance(Media media) {
		VideoFragment videoFragment = new VideoFragment();
		
		Bundle args = new Bundle();
		args.putParcelable("video", media);
		videoFragment.setArguments(args);
		
		return videoFragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		video = getArguments().getParcelable("video");
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		
		
		View view =
				inflater.inflate(org.horaapps.leafpic.R.layout.fragment_video, container, false);
		
		ImageView picture = view.findViewById(org.horaapps.leafpic.R.id.media_view);
		videoInd = view.findViewById(org.horaapps.leafpic.R.id.icon);
		videoInd.setOnClickListener(v -> {
			Uri uri = StorageHelper.getUriForFile(getContext(), video.getFile());
			Intent intent =
					new Intent(Intent.ACTION_VIEW).setDataAndType(uri, video.getMimeType());
			List<ResolveInfo> resInfoList =
					getContext().getPackageManager().queryIntentActivities(intent,
							PackageManager.MATCH_DEFAULT_ONLY);
			for (ResolveInfo resolveInfo : resInfoList) {
				String packageName = resolveInfo.activityInfo.packageName;
				getContext().grantUriPermission(packageName, uri,
						Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent
								.FLAG_GRANT_READ_URI_PERMISSION);
			}
			startActivity(intent);
		});
		
		
		RequestOptions options =
				new RequestOptions().signature(video.getSignature()).centerCrop()
						.diskCacheStrategy(
						DiskCacheStrategy.AUTOMATIC);
		
		
		Glide.with(getContext()).load(video.getUri()).apply(options).into(picture);
		
		picture.setOnClickListener(v -> ((SingleMediaActivity) getActivity()).toggleSystemUI());
		return view;
	}
	
	@Override
	public void refreshTheme(ThemeHelper themeHelper) {
		videoInd.setIcon(
				new IconicsDrawable(getContext(), GoogleMaterial.Icon.gmd_play_circle_filled)
						.color(
						getThemeHelper().getPrimaryColor()));
	}
}