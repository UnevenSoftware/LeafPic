package org.horaapps.leafpic.activities;

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

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.MyApplication;
import org.horaapps.leafpic.activities.base.ThemedActivity;
import org.horaapps.leafpic.model.CustomAlbumsHelper;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Jibo on 04/04/2016.
 */
public class ExcludedAlbumsActivity extends ThemedActivity {

	private ArrayList<File> excludedFolders = new ArrayList<File>();
	private CustomAlbumsHelper h;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(org.horaapps.leafpic.R.layout.activity_excluded);

		h = CustomAlbumsHelper.getInstance(getApplicationContext());

		excludedFolders = h.getExcludedFolders();

		checkNothing();
		initUI();
	}

	private void checkNothing() {
		TextView a = (TextView) findViewById(org.horaapps.leafpic.R.id.nothing_to_show);
		a.setTextColor(getTextColor());
		a.setVisibility(excludedFolders.size() == 0 ? View.VISIBLE : View.GONE);
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

		mRecyclerView.setAdapter(new ExcludedItemsAdapter());
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

	private class ExcludedItemsAdapter extends RecyclerView.Adapter<ExcludedItemsAdapter.ViewHolder> {

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
					checkNothing();
				}
			}
		};

		public ExcludedItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(org.horaapps.leafpic.R.layout.card_excluded_album, parent, false);
			v.findViewById(org.horaapps.leafpic.R.id.tracked_status).setOnClickListener(listener);
			return new ViewHolder(v);
		}

		@Override
		public void onBindViewHolder(final ExcludedItemsAdapter.ViewHolder holder, final int position) {

			File excludedFolder = excludedFolders.get(position);
			holder.album_path.setText(excludedFolder.getAbsolutePath());
			holder.album_name.setText(excludedFolder.getName());
			holder.imgUnExclude.setTag(excludedFolder.getAbsolutePath());
			holder.imgFolder.setIcon(GoogleMaterial.Icon.gmd_folder);

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
				imgUnExclude = (IconicsImageView) itemView.findViewById(org.horaapps.leafpic.R.id.tracked_status);
				imgFolder = (IconicsImageView) itemView.findViewById(org.horaapps.leafpic.R.id.folder_icon);
				album_name = (TextView) itemView.findViewById(org.horaapps.leafpic.R.id.folder_name);
				album_path = (TextView) itemView.findViewById(org.horaapps.leafpic.R.id.folder_path);
			}
		}
	}
}
