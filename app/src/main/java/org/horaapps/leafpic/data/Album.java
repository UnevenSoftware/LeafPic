package org.horaapps.leafpic.data;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import org.horaapps.leafpic.adapters.MediaAdapter;
import org.horaapps.leafpic.data.base.FilterMode;
import org.horaapps.leafpic.data.base.MediaComparators;
import org.horaapps.leafpic.data.base.SortingMode;
import org.horaapps.leafpic.data.base.SortingOrder;
import org.horaapps.leafpic.data.providers.MediaStoreProvider;
import org.horaapps.leafpic.data.providers.StorageProvider;
import org.horaapps.leafpic.util.ContentHelper;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by dnld on 26/04/16.
 */
public class Album implements Serializable {

	private String name = null;
	private String path = null;
	private long id = -1;
	private int count = -1;
	private int currentMediaIndex = 0;

	private boolean selected = false;
	public AlbumSettings settings = null;

	public ArrayList<Media> media;
	public ArrayList<Media> selectedMedias;

	public Album() {
		media = new ArrayList<Media>();
		selectedMedias = new ArrayList<Media>();
	}

	public Album(Context context, String path, long id, String name, int count) {
		this();
		this.path = path;
		this.name = name;
		this.count = count;
		this.id = id;
		settings = AlbumSettings.getSettings(context, this);
	}

	public Album(Context context, @NotNull File mediaPath) {
		super();
		File folder = mediaPath.getParentFile();
		this.path = folder.getPath();
		this.name = folder.getName();
		settings = AlbumSettings.getSettings(context, this);
		updatePhotos(context);
		setCurrentPhoto(mediaPath.getAbsolutePath());
	}

	/**
	 * used for open an image from an unknown content storage
	 *
	 * @param context context
	 * @param mediaUri uri of the media to display
   */
	public Album(Context context, Uri mediaUri) {
		super();
		media.add(0, new Media(context, mediaUri));
		setCurrentPhotoIndex(0);
	}

	public ArrayList<Media> getMedia() {
		ArrayList<Media> mediaArrayList = new ArrayList<Media>();
		switch (settings.getFilterMode()) {
			case ALL:
				mediaArrayList = media;
			default:
				break;
			case GIF:
				for (Media media1 : media)
					if (media1.isGif()) mediaArrayList.add(media1);
				break;
			case IMAGES:
				for (Media media1 : media)
					if (media1.isImage()) mediaArrayList.add(media1);
				break;
			case VIDEO:
				for (Media media1 : media)
					if (media1.isVideo()) mediaArrayList.add(media1);
				break;
		}
		return mediaArrayList;
	}

	public void updatePhotos(Context context) {

		//CustomAlbumsHelper albumHandler = CustomAlbumsHelper.getInstance(context);
		//Set<String> excludedPhotoPaths = albumHandler.getExcludedPhotos(this.getPath(), this.getId());
		PreferenceUtil SP = PreferenceUtil.getInstance(context);
		ArrayList<Media> mediaArrayList = new ArrayList<Media>();
		// TODO: 18/08/16
		if (isFromMediaStore()) {
			mediaArrayList.addAll(
							MediaStoreProvider.getMedia(
											context, id, SP.getBoolean("set_include_video", true)));
		} else {
			mediaArrayList.addAll(StorageProvider.getMedia(
							getPath(), SP.getBoolean("set_include_video", true)));
		}


		/*for (int i = mediaArrayList.size() - 1; i >= 0; i--) {
			if (excludedPhotoPaths.contains(mediaArrayList.get(i).getPath()))
				mediaArrayList.remove(i);
		}*/

		media = mediaArrayList;
		sortPhotos();
		setCount(media.size());
	}

	private boolean isFromMediaStore() {
		return id != -1;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getId() {
		return  this.id;
	}

	public ArrayList<String> getParentsFolders() {
		ArrayList<String> result = new ArrayList<String>();

		File f = new File(getPath());
		while(f != null && f.canRead()) {
			result.add(f.getPath());
			f = f.getParentFile();
		}
		return result;
	}

	public boolean isPinned(){ return settings.isPinned(); }

	public boolean isFromContentReoslver() {
		return this.id != -1;
	}

	public void filterMedias(FilterMode filter) {
		settings.setFilterMode(filter);
	}

	public boolean addMedia(@Nullable Media media) {
		if(media == null) return false;
		this.media.add(media);
		return true;
	}

	public boolean hasCustomCover() {
		return settings.getCoverPath() != null;
	}

	void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public Media getMedia(int index) { return media.get(index); }

	public void setCurrentPhotoIndex(int index){ currentMediaIndex = index; }

	public void setCurrentPhotoIndex(Media m){ setCurrentPhotoIndex(media.indexOf(m)); }

	public Media getCurrentMedia() { return getMedia(currentMediaIndex); }

	public int getCurrentMediaIndex() { return currentMediaIndex; }

	public String getContentDescription(Context c) {
		return c.getString(org.horaapps.leafpic.R.string.media);
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	private void setCount(int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public boolean isHidden() {
		return new File(getPath(), ".nomedia").exists();
	}

	public Media getCoverAlbum() {
		if (hasCustomCover())
			return new Media(settings.getCoverPath());
		if (media.size() > 0)
			return media.get(0);
		return new Media();
	}

	public void removeCoverAlbum(Context context) {
		settings.changeCoverPath(context, null);
	}

	public void setSelectedPhotoAsPreview(Context context) {
		if (selectedMedias.size() > 0)
			settings.changeCoverPath(context, selectedMedias.get(0).getPath());
	}

	private void setCurrentPhoto(String path) {
		for (int i = 0; i < media.size(); i++)
			if (media.get(i).getPath().equals(path)) currentMediaIndex = i;
	}

	public int getSelectedCount() {
		return selectedMedias.size();
	}

	public boolean areMediaSelected() { return getSelectedCount() != 0;}

	public void selectAllPhotos() {
		for (int i = 0; i < media.size(); i++) {
			if (!media.get(i).isSelected()) {
				media.get(i).setSelected(true);
				selectedMedias.add(media.get(i));
			}
		}
	}

	public int toggleSelectPhoto(int index) {
		if (media.get(index) != null) {
			media.get(index).setSelected(!media.get(index).isSelected());
			if (media.get(index).isSelected())
				selectedMedias.add(media.get(index));
			else
				selectedMedias.remove(media.get(index));
		}
		return index;
	}

	public int toggleSelectPhoto(Media m) {
		return toggleSelectPhoto(media.indexOf(m));
	}

	public void setDefaultSortingMode(Context context, SortingMode column) {
		settings.changeSortingMode(context, column);
	}

	public boolean renameCurrentMedia(Context context, String newName) {
		boolean success = false;
		try {
			File from = new File(getCurrentMedia().getPath());
			File to = new File(StringUtils.getPhotoPathRenamed(getCurrentMedia().getPath(), newName));
			if (success =  ContentHelper.moveFile(context, from, to)) {
				scanFile(context, new String[]{ to.getAbsolutePath(), from.getAbsolutePath() });
				getCurrentMedia().setPath(to.getAbsolutePath());
			}
		} catch (Exception e) { e.printStackTrace(); }
		return success;
	}

	public boolean moveCurrentMedia(Context context, String targetDir) {
		boolean success = false;
		try {
			String from = getCurrentMedia().getPath();
			if (success = moveMedia(context, from, targetDir)) {
				scanFile(context, new String[]{ from, StringUtils.getPhotoPathMoved(getCurrentMedia().getPath(), targetDir) });
				media.remove(getCurrentMediaIndex());
				setCount(media.size());
			}
		} catch (Exception e) { e.printStackTrace(); }
		return success;
	}

	public int moveSelectedMedia(Context context, String targetDir) {
		int n = 0;
		try
		{
			for (int i = 0; i < selectedMedias.size(); i++) {

				if (moveMedia(context, selectedMedias.get(i).getPath(), targetDir)) {
					String from = selectedMedias.get(i).getPath();
					scanFile(context, new String[]{ from, StringUtils.getPhotoPathMoved(selectedMedias.get(i).getPath(), targetDir) },
									new MediaScannerConnection.OnScanCompletedListener() {
										@Override
										public void onScanCompleted(String s, Uri uri) {
											Log.d("scanFile", "onScanCompleted: " + s);
										}
									});
					media.remove(selectedMedias.get(i));
					n++;
				}
			}
		} catch (Exception e) { e.printStackTrace(); }
		setCount(media.size());
		return n;
	}

	private boolean moveMedia(Context context, String source, String targetDir) {
		File from = new File(source);
		File to = new File(targetDir);
		return ContentHelper.moveFile(context, from, to);
	}

	public void setDefaultSortingAscending(Context context, SortingOrder sortingOrder) {
		settings.changeSortingOrder(context, sortingOrder);
	}


	/**
	 * On longpress, it finds the last or the first selected image before or after the targetIndex
	 * and selects them all.
	 *
	 * @param targetIndex
	 * @param adapter
	 */
	public void selectAllPhotosUpTo(int targetIndex, MediaAdapter adapter) {
		int indexRightBeforeOrAfter = -1;
		int indexNow;
		for (Media sm : selectedMedias) {
			indexNow = media.indexOf(sm);
			if (indexRightBeforeOrAfter == -1) indexRightBeforeOrAfter = indexNow;

			if (indexNow > targetIndex) break;
			indexRightBeforeOrAfter = indexNow;
		}

		if (indexRightBeforeOrAfter != -1) {
			for (int index = Math.min(targetIndex, indexRightBeforeOrAfter); index <= Math.max(targetIndex, indexRightBeforeOrAfter); index++) {
				if (media.get(index) != null) {
					if (!media.get(index).isSelected()) {
						media.get(index).setSelected(true);
						selectedMedias.add(media.get(index));
						adapter.notifyItemChanged(index);
					}
				}
			}
		}
	}

	public int getIndex(Media m) { return  media.indexOf(m); }

	public void clearSelectedPhotos() {
		for (Media m : media)
			m.setSelected(false);
		selectedMedias.clear();
	}

	public void sortPhotos() {
		Collections.sort(media, MediaComparators.getComparator(settings.getSortingMode(), settings.getSortingOrder()));
	}

	public boolean copySelectedPhotos(Context context, String folderPath) {
		boolean success = true;
		for (Media media : selectedMedias)
			if(!copyPhoto(context, media.getPath(), folderPath))
				success = false;
		return success;
	}

	public boolean copyPhoto(Context context, String olderPath, String folderPath) {
		boolean success = false;
		try {
			File from = new File(olderPath);
			File to = new File(folderPath);
			if (success = ContentHelper.copyFile(context, from, to))
				scanFile(context, new String[]{ StringUtils.getPhotoPathMoved(getCurrentMedia().getPath(), folderPath) });

		} catch (Exception e) { e.printStackTrace(); }
		return success;
	}

	public boolean deleteCurrentMedia(Context context) {
		boolean success = deleteMedia(context, getCurrentMedia());
		if (success) {
			media.remove(getCurrentMediaIndex());
			setCount(media.size());
		}
		return success;
	}

	private boolean deleteMedia(Context context, Media media) {
		boolean success;
		File file = new File(media.getPath());
	/*if (isFromMediaStore()) {
	  success = context.getContentResolver().delete(media.getUri(), null, null) == 1;
	}*/
		if (success = ContentHelper.deleteFile(context, file))
			scanFile(context, new String[]{ file.getAbsolutePath() });
		return success;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Album) {
			return getPath().equals(((Album) obj).getPath());
		}
		return super.equals(obj);
	}

	public boolean deleteSelectedMedia(Context context) {
		boolean success = true;
		for (Media selectedMedia : selectedMedias) {
			if (deleteMedia(context, selectedMedia))
				media.remove(selectedMedia);
			else success = false;
		}
		if (success) {
			clearSelectedPhotos();
			setCount(media.size());
		}
		return success;
	}
	private boolean found_id_album = false;

	public boolean renameAlbum(final Context context, String newName) {
		found_id_album = false;
		boolean success;
		File dir = new File(StringUtils.getAlbumPathRenamed(getPath(), newName));
		if (success = ContentHelper.mkdir(context, dir)) {
			for (final Media m : media) {
				File from = new File(m.getPath());
				File to = new File(StringUtils.getPhotoPathRenamedAlbumChange(m.getPath(), newName));
				if (ContentHelper.moveFile(context, from, to)) {
					scanFile(context, new String[]{from.getAbsolutePath() });
					scanFile(context, new String[]{ to.getAbsolutePath() }, new MediaScannerConnection.OnScanCompletedListener() {
						@Override
						public void onScanCompleted(String s, Uri uri) {
							// TODO: 05/08/16 it sucks! look for a better solution

							if (!found_id_album) {
								id = MediaStoreProvider.getAlbumId(context, s);
								found_id_album = true;
							}
							Log.d(s, "onScanCompleted: "+s);
							m.setPath(s); m.setUri(uri.toString());
						}
					});

				} else success = false;
			}
		}
		if(success) {
			path = dir.getAbsolutePath();
			name = newName;
			// NOTE: the following line doesn't work
			//id = MediaStoreProvider.getAlbumId(context, media.getValue(0).getPath());

		}
		return success;
	}

	public void scanFile(Context context, String[] path) { MediaScannerConnection.scanFile(context, path, null, null); }

	public void scanFile(Context context, String[] path, MediaScannerConnection.OnScanCompletedListener onScanCompletedListener) {
		MediaScannerConnection.scanFile(context, path, null, onScanCompletedListener);
	}

	public boolean areFiltersActive() {
		return settings.getFilterMode() != FilterMode.ALL;
	}
}
