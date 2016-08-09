package org.horaapps.leafpic.Activities;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import org.horaapps.leafpic.Data.CustomAlbumsHandler;
import org.horaapps.leafpic.MyApplication;
import org.horaapps.leafpic.Views.ThemedActivity;
import org.horaapps.leafpic.utils.PreferenceUtil;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Jibo on 04/04/2016.
 */
public class ExcludedAlbumsActivity extends ThemedActivity {

  private ArrayList<File> excludedFolders = new ArrayList<File>();
  private CustomAlbumsHandler h;

  @Override
  public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(org.horaapps.leafpic.R.layout.activity_excluded);
	h = new CustomAlbumsHandler(getApplicationContext());
	PreferenceUtil SP = PreferenceUtil.getInstance(getApplicationContext());

	excludedFolders = h.getExcludedFolders(SP.getBoolean(getString(org.horaapps.leafpic.R.string.preference_use_alternative_provider), false));

	checkNothing(excludedFolders);
	initUI();
  }

  private void checkNothing(ArrayList<File> asd){
	TextView a = (TextView) findViewById(org.horaapps.leafpic.R.id.nothing_to_show);
	a.setTextColor(getTextColor());
	a.setVisibility(asd.size() == 0 ? View.VISIBLE : View.GONE);
  }

  private void initUI(){

	RecyclerView mRecyclerView;
	Toolbar toolbar;

	/** TOOLBAR **/
	toolbar = (Toolbar) findViewById(org.horaapps.leafpic.R.id.toolbar);
	setSupportActionBar(toolbar);
	getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

	/** RECYCLE VIEW**/
	mRecyclerView = (RecyclerView) findViewById(org.horaapps.leafpic.R.id.excluded_albums);
	mRecyclerView.setHasFixedSize(true);

	mRecyclerView.setAdapter(new ExcludedAlbumsAdapter());
	mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
	mRecyclerView.setItemAnimator(new DefaultItemAnimator());
	mRecyclerView.setBackgroundColor(getBackgroundColor());

	/**SET UP UI COLORS**/
	toolbar.setBackgroundColor(getPrimaryColor());
	toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
	toolbar.setNavigationOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		onBackPressed();
	  }
	});
	setStatusBarColor();
	setNavBarColor();
	setRecentApp(getString(org.horaapps.leafpic.R.string.excluded_albums));

	findViewById(org.horaapps.leafpic.R.id.rl_ea).setBackgroundColor(getBackgroundColor());
  }

  private class ExcludedAlbumsAdapter extends RecyclerView.Adapter<ExcludedAlbumsAdapter.ViewHolder> {

	private View.OnClickListener listener = new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		String path = v.getTag().toString();
		int pos;
		if((pos = getIndex(path)) !=-1) {
		  h.clearAlbumExclude(excludedFolders.remove(pos).getAbsolutePath());
		  new Thread(new Runnable() {
			@Override
			public void run() {
			  ((MyApplication) getApplicationContext()).getAlbums().loadAlbums(getApplicationContext());
			}
		  });
		  notifyItemRemoved(pos);
		  checkNothing(excludedFolders);
		}
	  }
	};

	public ExcludedAlbumsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
	  View v = LayoutInflater.from(parent.getContext()).inflate(org.horaapps.leafpic.R.layout.card_excluded_album, parent, false);
	  v.findViewById(org.horaapps.leafpic.R.id.UnExclude_icon).setOnClickListener(listener);
	  return new ViewHolder(
								   MaterialRippleLayout.on(v)
										   .rippleOverlay(true)
										   .rippleAlpha(0.2f)
										   .rippleColor(0xFF585858)
										   .rippleHover(true)
										   .rippleDuration(1)
										   .create()
	  );
	}

	@Override
	public void onBindViewHolder(final ExcludedAlbumsAdapter.ViewHolder holder, final int position) {
	  File a = excludedFolders.get(position);
	  holder.album_path.setText(a.getAbsolutePath());
	  holder.album_name.setText(a.getName());
	  holder.imgUnExclude.setTag(a.getAbsolutePath());

	  /**SET LAYOUT THEME**/
	  holder.album_name.setTextColor(getTextColor());
	  holder.album_path.setTextColor(getSubTextColor());
	  holder.imgFolder.setColor(getIconColor());
	  holder.imgUnExclude.setColor(getIconColor());
	  holder.card_layout.setBackgroundColor(getCardBackgroundColor());
	}

	public int getItemCount() {
	  return excludedFolders.size();
	}

	int getIndex(String path) {
	  for (int i = 0; i < excludedFolders.size(); i++)
		if (excludedFolders.get(i).getAbsolutePath().equals(path)) return i;
	  return -1;
	}

	class ViewHolder extends RecyclerView.ViewHolder {
	  LinearLayout card_layout;
	  IconicsImageView imgUnExclude;
	  IconicsImageView imgFolder;
	  TextView album_name;
	  TextView album_path;

	  ViewHolder(View itemView) {
		super(itemView);
		card_layout = (LinearLayout) itemView.findViewById(org.horaapps.leafpic.R.id.linear_card_excluded);
		imgUnExclude = (IconicsImageView) itemView.findViewById(org.horaapps.leafpic.R.id.UnExclude_icon);
		imgFolder = (IconicsImageView) itemView.findViewById(org.horaapps.leafpic.R.id.folder_icon);
		album_name = (TextView) itemView.findViewById(org.horaapps.leafpic.R.id.Excluded_Title_Item);
		album_path = (TextView) itemView.findViewById(org.horaapps.leafpic.R.id.Excluded_Path_Item);
	  }
	}
  }
}
