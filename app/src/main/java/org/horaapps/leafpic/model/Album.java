package org.horaapps.leafpic.model;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import org.horaapps.leafpic.adapters.MediaAdapter;
import org.horaapps.leafpic.model.base.FilterMode;
import org.horaapps.leafpic.model.base.MediaComparators;
import org.horaapps.leafpic.model.base.SortingMode;
import org.horaapps.leafpic.model.base.SortingOrder;
import org.horaapps.leafpic.model.providers.MediaStoreProvider;
import org.horaapps.leafpic.model.providers.StorageProvider;
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

	private ArrayList<Media> media;
	private ArrayList<Media> selectedMedia;

	private Album() {
		media = new ArrayList<>();
		selectedMedia = new ArrayList<>();
	}

	public Album(Context context, String path, long id, String name, int count) {
		this();
		this.path = path;
		this.name = name;
		this.count = count;
		this.id = id;
		settings = AlbumSettings.getSettings(context, this);
	}

	public Album(String path, long id, AlbumSettings settings, int count) {
		this();
		this.path = path;
		this.name = StringUtils.getNameByPath(path);
		this.id = id;
		this.settings = settings;
		this.count = count;
	}

	public Album(Context context, @NotNull File mediaPath) {
		this();
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
		this();
		this.path = mediaUri.toString();
		this.name = mediaUri.getPath();
		media.add(0, new Media(context, mediaUri));
		setCurrentMedia(0);
	}

	public static Album getEmptyAlbum() {
		Album album = new Album();
		album.settings = AlbumSettings.getDefaults();
		return album;
	}

	public ArrayList<Media> getMedia() {
		ArrayList<Media> mediaArrayList = new ArrayList<>();
		switch (getFilterMode()) {
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
		media = getMedia(context);
		sortPhotos();
		setCount(media.size());
	}

	private void updatePhotos(Context context, FilterMode filterMode) {

		ArrayList<Media> media = getMedia(context), mediaArrayList = new ArrayList<>();

		switch (filterMode) {
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

		this.media = mediaArrayList;
		sortPhotos();
		setCount(this.media.size());
	}

	private ArrayList<Media> getMedia(Context context) {
		PreferenceUtil SP = PreferenceUtil.getInstance(context);
		ArrayList<Media> mediaArrayList = new ArrayList<>();
		// TODO: 18/08/16
		if (isFromMediaStore()) {
			mediaArrayList.addAll(
					MediaStoreProvider.getMedia(
							context, id, SP.getBoolean("set_include_video", true)));
		} else {
			mediaArrayList.addAll(StorageProvider.getMedia(
					getPath(), SP.getBoolean("set_include_video", true)));
		}
		return mediaArrayList;
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

	public void filterMedias(Context context, FilterMode filter) {
		settings.filterMode = filter;
		updatePhotos(context, filter);
	}

	public boolean addMedia(@Nullable Media media) {
		if(media == null) return false;
		this.media.add(media);
		return true;
	}

	public Media getMedia(int index) { return media.get(index); }

	public void setCurrentMedia(int index){ currentMediaIndex = index; }

	public void setCurrentMedia(Media m){ setCurrentMedia(media.indexOf(m)); }

	public Media getCurrentMedia() { return getMedia(currentMediaIndex); }

	public int getCurrentMediaIndex() { return currentMediaIndex; }

	private void setCurrentPhoto(String path) {
		for (int i = 0; i < media.size(); i++)
			if (media.get(i).getPath().equals(path)) {
				currentMediaIndex = i;
				break;
			}
	}

	//region Album Properties Getters
	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public int getCount() {
		return count;
	}

	public Media getCoverAlbum() {
		if (hasCustomCover())
			return new Media(settings.coverPath);
		if (media.size() > 0)
			return media.get(0);
		// TODO: 11/20/16 how should i handle this?
		return new Media();
	}

	private boolean isFromMediaStore() {
		return id != -1;
	}

	public long getId() {
		return  this.id;
	}

	public boolean isHidden() {
		return new File(path, ".nomedia").exists();
	}

	public boolean isPinned(){ return settings.pinned; }

	public boolean hasCustomCover() {
		return settings.coverPath != null;
	}

	private FilterMode getFilterMode() {
		return settings != null ? settings.filterMode : FilterMode.ALL;
	}

	public boolean isSelected() {
		return selected;
	}

	//endregion

	//region Selected Media

	public ArrayList<Media> getSelectedMedia() {
		return selectedMedia;
	}

	public Media getSelectedMedia(int index) {
		return selectedMedia.get(index);
	}

	public int getSelectedMediaCount() {
		return selectedMedia.size();
	}

	public boolean thereAreMediaSelected() { return getSelectedMediaCount() != 0;}

	public void selectAllMedia() {
		for (int i = 0; i < media.size(); i++) {
			if (!media.get(i).isSelected()) {
				media.get(i).setSelected(true);
				selectedMedia.add(media.get(i));
			}
		}
	}

	public int toggleSelectMedia(Media m) {
		int index = media.indexOf(m);
		if (media.get(index) != null) {
			media.get(index).setSelected(!media.get(index).isSelected());
			if (media.get(index).isSelected())
				selectedMedia.add(media.get(index));
			else
				selectedMedia.remove(media.get(index));
		}
		return index;
	}

	/**
	 * On longpress, it finds the last or the first selected image before or after the targetIndex
	 * and selects them all.
	 *
	 * @param adapter
	 */
	public void selectAllMediaUpTo(Media m, MediaAdapter adapter) {
		int targetIndex = media.indexOf(m);
		int indexRightBeforeOrAfter = -1;
		int indexNow;
		for (Media sm : selectedMedia) {
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
						selectedMedia.add(media.get(index));
						adapter.notifyItemChanged(index);
					}
				}
			}
		}
	}

	public void clearSelectedMedia() {
		for (Media m : media)
			m.setSelected(false);
		selectedMedia.clear();
	}
	//endregion

	//region Album Properties Setters

	private void setCount(int count) {
		this.count = count;
	}

	public void setName(String name) {
		this.name = name;
	}

	void setSelected(boolean selected) {
		this.selected = selected;
	}

	public void removeCoverAlbum(Context context) {
		HandlingAlbums.getInstance(context).setAlbumPhotoPreview(path, null);
		settings.coverPath = null;
	}

	public void setSelectedPhotoAsPreview(Context context) {
		if (selectedMedia.size() > 0) {
			String asd = selectedMedia.get(0).getPath();
			HandlingAlbums.getInstance(context).setAlbumPhotoPreview(this.path, asd);
			settings.coverPath = asd;
		}
	}

	public void setDefaultSortingMode(Context context, SortingMode column) {
		settings.sortingMode = column.getValue();
		HandlingAlbums.getInstance(context).setAlbumSortingMode(path, column.getValue());
	}

	public void setDefaultSortingAscending(Context context, SortingOrder sortingOrder) {
		settings.sortingOrder = sortingOrder.getValue();
		HandlingAlbums.getInstance(context).setAlbumSortingOrder(path, sortingOrder.getValue());
	}

	public void togglePinAlbum(Context context) {
		settings.pinned = !settings.pinned;
		HandlingAlbums.getInstance(context).pinAlbum(path, settings.pinned);
	}

	//endregion

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
			for (int i = 0; i < selectedMedia.size(); i++) {

				if (moveMedia(context, selectedMedia.get(i).getPath(), targetDir)) {
					String from = selectedMedia.get(i).getPath();
					scanFile(context, new String[]{ from, StringUtils.getPhotoPathMoved(selectedMedia.get(i).getPath(), targetDir) },
							new MediaScannerConnection.OnScanCompletedListener() {
								@Override
								public void onScanCompleted(String s, Uri uri) {
									Log.d("scanFile", "onScanCompleted: " + s);
								}
							});
					media.remove(selectedMedia.get(i));
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

	public void sortPhotos() {
		Collections.sort(media, MediaComparators.getComparator(settings.getSortingMode(), settings.getSortingOrder()));
	}

	public boolean copySelectedPhotos(Context context, String folderPath) {
		boolean success = true;
		for (Media media : selectedMedia)
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
		File file = new File(media.getPath());
		boolean  success = ContentHelper.deleteFile(context, file);
		scanFile(context, new String[]{ file.getAbsolutePath() });
		return success;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Album) {
			return path.equals(((Album) obj).getPath());
		}
		return super.equals(obj);
	}

	public boolean deleteSelectedMedia(Context context) {
		boolean success = true;
		for (Media selectedMedia : this.selectedMedia) {
			if (deleteMedia(context, selectedMedia))
				media.remove(selectedMedia);
			else success = false;
		}
		if (success) {
			clearSelectedMedia();
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
							// TODO: 05/08/16 it sucks! look for a better solution!

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

	private void scanFile(Context context, String[] path) { scanFile(context, path, null); }

	private void scanFile(Context context, String[] path, MediaScannerConnection.OnScanCompletedListener onScanCompletedListener) {
		MediaScannerConnection.scanFile(context, path, null, onScanCompletedListener);
	}
}
