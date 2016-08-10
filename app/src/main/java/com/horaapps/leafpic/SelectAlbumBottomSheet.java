package com.horaapps.leafpic;

/**
 * Created by Jibo on 18/04/2016.
 */

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.horaapps.leafpic.Data.Album;
import com.horaapps.leafpic.Data.FoldersFileFilter;
import com.horaapps.leafpic.Views.ThemedActivity;
import com.horaapps.leafpic.utils.AlertDialogsHelper;
import com.horaapps.leafpic.utils.ContentHelper;
import com.horaapps.leafpic.utils.ThemeHelper;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class SelectAlbumBottomSheet extends BottomSheetDialogFragment {

  private String title;
  private ArrayList<File> folders;

  private BottomSheetAlbumsAdapter adapter;
  private ThemeHelper theme;

  private boolean exploreMode = false;
  private boolean canGoBack = false;
  private IconicsImageView imgExploreMode;
  private LinearLayout exploreModePanel;
  private String currentFolderPath;

	final int INTERNAL_STORAGE = 0;

  private SelectAlbumInterface selectAlbumInterface;

  private boolean canGoBack() {
	return canGoBack;
  }

  interface SelectAlbumInterface {
	void folderSelected(String path);
  }

  void setSelectAlbumInterface(SelectAlbumInterface selectAlbumInterface) {
	this.selectAlbumInterface = selectAlbumInterface;
  }


  private View.OnClickListener onClickListener = new View.OnClickListener() {
	@Override
	public void onClick(View view) {
	  String path = view.findViewById(R.id.name_folder).getTag().toString();
	  if (isExploreMode()) displayContentFolder(new File(path));
	  else selectAlbumInterface.folderSelected(path);
	}
  };


  @Override
  public void setupDialog(Dialog dialog, int style) {
	super.setupDialog(dialog, style);

	View contentView = View.inflate(getContext(), R.layout.select_folder_bottom_sheet, null);
	theme = new ThemeHelper(getContext());

	RecyclerView mRecyclerView = (RecyclerView) contentView.findViewById(R.id.folders);
	mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
	adapter = new BottomSheetAlbumsAdapter();
	mRecyclerView.setAdapter(adapter);

	exploreModePanel = (LinearLayout) contentView.findViewById(R.id.ll_explore_mode_panel);
	imgExploreMode = (IconicsImageView) contentView.findViewById(R.id.toggle_hidden_icon);
	imgExploreMode.setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		toggleExplorerMode(!exploreMode);
	  }
	});


	toggleExplorerMode(false);

	final Spinner spinner = (Spinner) contentView.findViewById(R.id.volume_spinner);
	spinner.setAdapter(new VolumeSpinnerAdapter(contentView.getContext()));
	spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
			switch(pos){
				case INTERNAL_STORAGE:
					displayContentFolder(Environment.getExternalStorageDirectory());
					break;
				default:
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
						DocumentFile documentFile = ContentHelper.getDocumentFile(getContext(), new File(ContentHelper.getExtSdCardPaths(getContext())[pos - 1]), true, false);
						if(documentFile != null){
							displayContentFolder(new File(ContentHelper.getExtSdCardPaths(getContext())[pos - 1]));
						} else {
							Toast.makeText(getContext(), getString(R.string.no_permission), Toast.LENGTH_LONG).show();
							spinner.setSelection(0);
						}
					} else {
						displayContentFolder(new File(ContentHelper.getExtSdCardPaths(getContext())[pos - 1]));
					}
					break;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {}
	});

	/**SET UP THEME**/
	theme.setColorScrollBarDrawable(ContextCompat.getDrawable(dialog.getContext(), R.drawable.ic_scrollbar));
	contentView.findViewById(R.id.ll_bottom_sheet_title).setBackgroundColor(theme.getPrimaryColor());
	exploreModePanel.setBackgroundColor(theme.getPrimaryColor());
	contentView.findViewById(R.id.ll_select_folder).setBackgroundColor(theme.getCardBackgroundColor());
	((TextView) contentView.findViewById(R.id.bottom_sheet_title)).setText(title);

	((IconicsImageView) contentView.findViewById(R.id.create_new_folder)).setColor(Color.WHITE);
	((IconicsImageView) contentView.findViewById(R.id.done)).setColor(Color.WHITE);

	contentView.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View view) {
			selectAlbumInterface.folderSelected(currentFolderPath);
	  }
	});

	contentView.findViewById(R.id.create_new_folder).setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View view) {
		final EditText editText = new EditText(getContext());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), theme.getDialogStyle());
		AlertDialogsHelper.getInsertTextDialog(((ThemedActivity) getActivity()), builder,
				editText, R.string.new_folder);
		builder.setPositiveButton(R.string.ok_action, new DialogInterface.OnClickListener() {
		  @Override
		  public void onClick(DialogInterface dialogInterface, int i) {
				File folderPath = new File(currentFolderPath + File.separator + editText.getText().toString());
				if (folderPath.mkdir()){
					displayContentFolder(folderPath);
				}
		  }
		});
		builder.show();
	  }
	});


	dialog.setContentView(contentView);
	CoordinatorLayout.LayoutParams layoutParams =
			(CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
	CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
	if (behavior != null && behavior instanceof BottomSheetBehavior) {
	  ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
	}
	adapter.notifyDataSetChanged();
  }

	private void requestSdCardPermissions() {
		ThemeHelper themeHelper = new ThemeHelper(getActivity());
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), themeHelper.getDialogStyle());

		AlertDialogsHelper.getTextDialog((ThemedActivity) getActivity(), dialogBuilder,
						R.string.sd_card_write_permission_title, R.string.sd_card_permissions_message);

		dialogBuilder.setPositiveButton(R.string.ok_action, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
					startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 42);
			}
		});
		dialogBuilder.show();
	}

  private void displayContentFolder(File dir) {
		canGoBack = false;
		if(dir.canRead()) {
			folders = new ArrayList<File>();
			File parent = dir.getParentFile();
			try {
				if (parent.canRead() && !Arrays.asList(ContentHelper.getExtSdCardPaths(getContext())).contains(dir.getCanonicalPath())) {
          canGoBack = true;
          folders.add(0, parent);
        }
			} catch (IOException e) {
				e.printStackTrace();
			}
			File[] files = dir.listFiles(new FoldersFileFilter());
			if (files != null && files.length > 0) {
				folders.addAll(new ArrayList<File>(Arrays.asList(files)));
			}
			Collections.sort(folders, new Comparator<File>() {
				@Override
				public int compare(File file, File t1) {
					return file.getName().compareTo(t1.getName());
				}
			});
		  currentFolderPath = dir.getAbsolutePath();
			adapter.notifyDataSetChanged();
		}
  }

  private void setExploreMode(boolean exploreMode) {
	this.exploreMode = exploreMode;
  }

  private boolean isExploreMode() { return  exploreMode; }

  public void setTitle(String title) {
	this.title = title;
  }

  private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
		@Override
		public void onStateChanged(@NonNull View bottomSheet, int newState) {
			if (newState == BottomSheetBehavior.STATE_HIDDEN) {
			dismiss();
			}
		}
		@Override
		public void onSlide(@NonNull View bottomSheet, float slideOffset) {
		}
  };

  private void toggleExplorerMode(boolean enabled) {
		folders = new ArrayList<File>();
		setExploreMode(enabled);
		if(enabled) {
			displayContentFolder(Environment.getExternalStorageDirectory());
			imgExploreMode.setIcon(theme.getIcon(GoogleMaterial.Icon.gmd_folder));
			exploreModePanel.setVisibility(View.VISIBLE);
		} else {
			for (Album album : ((MyApplication) getActivity().getApplicationContext()).getAlbums().dispAlbums) {
				folders.add(new File(album.getPath()));
			}
			Collections.sort(folders, new Comparator<File>() {
				@Override
				public int compare(File file, File t1) {
					return file.getName().compareTo(t1.getName());
				}
			});
			imgExploreMode.setIcon(theme.getIcon(GoogleMaterial.Icon.gmd_explore));
			exploreModePanel.setVisibility(View.GONE);
		}
		adapter.notifyDataSetChanged();
  }

	class VolumeSpinnerAdapter extends ArrayAdapter {

		Context c;

		public VolumeSpinnerAdapter(Context context) {
			super(context, R.layout.spinner_item_with_pic, R.id.volume_name);

			c = context;

			insert("Internal Storage", INTERNAL_STORAGE);
			for(int i = 0; i < ContentHelper.getExtSdCardPaths(getContext()).length; i++){
				if(ContentHelper.getExtSdCardPaths(getContext()).length == 1){
					add("External Storage");
				} else {
					add("External Storage " + (i + 1));
				}
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			GoogleMaterial.Icon icon;

			switch (position){
				case INTERNAL_STORAGE:
					icon = GoogleMaterial.Icon.gmd_storage;
					break;
				default:
					icon = GoogleMaterial.Icon.gmd_sd_card;
					break;
			}

			((ImageView)view.findViewById(R.id.volume_image)).setImageDrawable(new IconicsDrawable(c).icon(icon).sizeDp(24).color(Color.WHITE));
			return view;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View view = super.getDropDownView(position, convertView, parent);
			GoogleMaterial.Icon icon;

			switch (position){
				case INTERNAL_STORAGE:
					icon = GoogleMaterial.Icon.gmd_storage;
					break;
				default:
					icon = GoogleMaterial.Icon.gmd_sd_card;
					break;
			}

			((ImageView)view.findViewById(R.id.volume_image)).setImageDrawable(new IconicsDrawable(c).icon(icon).sizeDp(24).color(Color.WHITE));
			view.setBackgroundColor(ThemeHelper.getPrimaryColor(c));
			return view;
		}
	}

  class BottomSheetAlbumsAdapter extends RecyclerView.Adapter<BottomSheetAlbumsAdapter.ViewHolder> {

	BottomSheetAlbumsAdapter() {}

	public BottomSheetAlbumsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
	  View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_folder_bottom_sheet_item, parent, false);
	  v.setOnClickListener(onClickListener);
	  return new ViewHolder(MaterialRippleLayout.on(v)
								 .rippleOverlay(true)
								 .rippleAlpha(0.2f)
								 .rippleColor(0xFF585858)
								 .rippleHover(true)
								 .rippleDuration(1)
								 .create()
	  );
	}

	@Override
	public void onBindViewHolder(final BottomSheetAlbumsAdapter.ViewHolder holder, final int position) {

	  File f = folders.get(position);
	  String[] list = f.list();
	  int count = list == null ? 0 : list.length;
	  holder.folderName.setText(f.getName());
	  holder.folderName.setTag(f.getPath());

	  /** SET UP THEME**/
	  holder.folderName.setTextColor(theme.getTextColor());
	  String hexAccentColor = String.format("#%06X", (0xFFFFFF & theme.getAccentColor()));
	  holder.folderCount.setText(Html.fromHtml("<b><font color='" + hexAccentColor + "'>" + count + "</font></b>" + "<font " + "color='" + theme.getSubTextColor() + "'> Media</font>"));
	  holder.imgFolder.setColor(theme.getIconColor());
	  holder.imgFolder.setIcon(theme.getIcon(GoogleMaterial.Icon.gmd_folder));

	  if(canGoBack() && position == 0) { // go to parent folder
		holder.folderName.setText("..");
		holder.folderCount.setText(Html.fromHtml("<font color='" + theme.getSubTextColor() + "'>Go to parent</font>"));
		holder.imgFolder.setIcon(theme.getIcon(GoogleMaterial.Icon.gmd_keyboard_arrow_up));
	  }
	}

	public int getItemCount() {
	  return folders.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {
	  TextView folderName;
	  TextView folderCount;
	  IconicsImageView imgFolder;
	  ViewHolder(View itemView) {
		super(itemView);
		folderName = (TextView) itemView.findViewById(R.id.name_folder);
		folderCount = (TextView) itemView.findViewById(R.id.count_folder);
		imgFolder = (IconicsImageView) itemView.findViewById(R.id.folder_icon_bottom_sheet_item);
	  }
	}
  }
}

