package org.horaapps.leafpic.model;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import org.horaapps.leafpic.adapters.MediaAdapter;
import org.horaapps.leafpic.model.base.FilterMode;
import org.horaapps.leafpic.model.base.MediaComparators;
import org.horaapps.leafpic.model.base.SortingMode;
import org.horaapps.leafpic.model.base.SortingOrder;
import org.horaapps.leafpic.new_way.CursorHandler;
import org.horaapps.leafpic.util.ContentHelper;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.StringUtils;

import java.io.File;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by dnld on 26/04/16.
 */
public class Album implements Serializable, CursorHandler {

	private String name, path;
	private long id = -1, dateModified;
	private int count = -1, currentMediaIndex = 0;

	private boolean selected = false;
	public AlbumSettings settings = null;

	@Deprecated private ArrayList<Media> media;
	@Deprecated private ArrayList<Media> selectedMedia;


	public Album(String path, String name) {
		media = new ArrayList<>();
		selectedMedia = new ArrayList<>();
		this.name = name;
		this.path = path;
	}

	public Album(String path, String name, long id, int count) {
		this(path, name);
		this.count = count;
		this.id = id;
	}

	public Album(Cursor cur) {
		this(StringUtils.getBucketPathByImagePath(cur.getString(3)),
				cur.getString(1),
				cur.getLong(0),
				cur.getInt(2));
		dateModified = cur.getLong(4);
	}

	public static String[] getProjection() {
		return new String[]{
				MediaStore.Files.FileColumns.PARENT,
				MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
				"count(*)",
				MediaStore.Images.Media.DATA,
				"max(" + MediaStore.Images.Media.DATE_MODIFIED + ")"
		};
	}

	@Override
	public Album handle(Cursor cur) throws SQLException {
		return new Album(cur);
	}

	@Deprecated
	public Album(Context context, String path, long id, String name, int count) {
		this(path, name, id, count);
		settings = AlbumSettings.getDefaults();
	}

	public static Album getEmptyAlbum() {
		Album album = new Album(null, null);
		album.settings = AlbumSettings.getDefaults();
		return album;
	}

	static Album withPath(String path) {
		Album emptyAlbum = getEmptyAlbum();
		emptyAlbum.path = path;
		return emptyAlbum;
	}

	public Album withSettings(AlbumSettings settings) {
		this.settings = settings;
		return this;
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

	public Media getCover() {
		if (hasCover())
			return new Media(settings.coverPath);
		// TODO: 11/20/16 how should i handle this?
		return new Media();
	}

	public void setCover(String path) {
		settings.coverPath = path;
	}

	private boolean hasId() {
		return id != -1;
	}

	public long getId() {
		return  this.id;
	}

	public boolean isHidden() {
		return new File(path, ".nomedia").exists();
	}

	public boolean isPinned(){ return settings.pinned; }

	public boolean hasCover() {
		return settings.coverPath != null;
	}

	private FilterMode getFilterMode() {
		return settings != null ? settings.filterMode : FilterMode.ALL;
	}

	public boolean isSelected() {
		return selected;
	}

	@Override
	public String toString() {
		return "Album{" +
				"name='" + name + '\'' +
				", path='" + path + '\'' +
				", id=" + id +
				", count=" + count +
				'}';
	}
	//endregion

	public ArrayList<String> getParentsFolders() {
		ArrayList<String> result = new ArrayList<>();

		File f = new File(getPath());
		while(f != null && f.canRead()) {
			result.add(f.getPath());
			f = f.getParentFile();
		}
		return result;
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
		ArrayList<Media> mediaArrayList = new ArrayList<>();
		if (hasId()) {
			mediaArrayList.addAll(
					ContentProviderHelper.getMedia(
							context, id, PreferenceUtil.getBool(context, "set_include_video", true)));
		} else {
			mediaArrayList.addAll(ContentProviderHelper.getMedia(
					getPath(), PreferenceUtil.getBool(context, "set_include_video", true)));
		}
		return mediaArrayList;
	}



	public void filterMedias(Context context, FilterMode filter) {
		settings.filterMode = filter;
		updatePhotos(context, filter);
	}

	public boolean addMedia(@Nullable Media media) {
		if(media == null) return false;
		this.media.add(media);
		Log.d("asd", "addMedia: "+media.getPath());
		return true;
	}

	public Media getMedia(int index) { return media.get(index); }

	public void setCurrentMedia(int index){ currentMediaIndex = index; }

	public void setCurrentMedia(Media m){ setCurrentMedia(media.indexOf(m)); }

	public Media getCurrentMedia() { return getMedia(currentMediaIndex); }

	public int getCurrentMediaIndex() { return currentMediaIndex; }

	public void setCurrentMedia(String path) {
		for (int i = 0; i < media.size(); i++)
			if (media.get(i).getPath().equals(path)) {
				currentMediaIndex = i;
				break;
			}
	}



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

	public void setCount(int count) {
		this.count = count;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean setSelected(boolean selected) {
		if (this.selected == selected)
			return false;
		this.selected = selected;
		return true;
	}

	public boolean toggleSelected() {
		selected = !selected;
		return selected;
	}

	public void removeCoverAlbum(Context context) {
		//HandlingAlbums.getInstance(context).setCover(path, null);
		settings.coverPath = null;
	}

	public void setSelectedPhotoAsPreview(Context context) {
		if (selectedMedia.size() > 0) {
			String asd = selectedMedia.get(0).getPath();
			//HandlingAlbums.getInstance(context).setCover(this.path, asd);
			settings.coverPath = asd;
		}
	}

	public void setDefaultSortingMode(Context context, SortingMode column) {
		settings.sortingMode = column.getValue();
		//HandlingAlbums.getInstance(context).setSortingMode(path, column.getValue());
	}

	public void setDefaultSortingAscending(Context context, SortingOrder sortingOrder) {
		settings.sortingOrder = sortingOrder.getValue();
		//HandlingAlbums.getInstance(context).setSortingOrder(path, sortingOrder.getValue());
	}

	public void togglePinAlbum() {
		settings.pinned = !settings.pinned;
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
		File to = new File(targetDir, from.getName());
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
								id = ContentProviderHelper.getAlbumId(context, s);
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
			//id = ContentProviderHelper.getAlbumId(context, media.getValue(0).getPath());

		}
		return success;
	}

	private void scanFile(Context context, String[] path) { scanFile(context, path, null); }

	private void scanFile(Context context, String[] path, MediaScannerConnection.OnScanCompletedListener onScanCompletedListener) {
		MediaScannerConnection.scanFile(context, path, null, onScanCompletedListener);
	}


	public Long getDateModified() {
		return dateModified;
	}
}
