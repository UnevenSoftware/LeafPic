public class AlbumMedia {

	public ArrayList<Media> media;
	public ArrayList<Media> selectedMedias;
	private int filter_photos;

    int currentPhotoIndex = -1;

	SharedPreferences SP;
    MediaComparators mediaComparators;

	public AlbumMedia(){
		media = new ArrayList<Media>();
		selectedMedias = new ArrayList<Media>();
	}

	public void updatePhotos(Context context) {
        SP = PreferenceManager.getDefaultSharedPreferences(context);
        ArrayList<Media> mediaArrayList = new ArrayList<Media>();
        ImageFileFilter filter = new ImageFileFilter(filter_photos, SP.getBoolean("set_include_video",true));
        File[] images = new File(getPath()).listFiles(filter);

        for (File image : images)
            mediaArrayList.add(0, new Media(image.getAbsolutePath(), image.lastModified(), image.length()));
        media = mediaArrayList;
        sortPhotos();
        setCount(media.size());
    }

    public void filterMedias(Context context, int filter) {
        filter_photos = filter;
        updatePhotos(context);
    }

    public boolean areFiltersActive(){
    	return filter_photos != ImageFileFilter.FILTER_ALL;
    }

    public Media getMedia(int index) { return media.get(index); }

    public Media getCurrentMedia() { return media.get(currentPhotoIndex); }

    public void setCurrentPhotoIndex(int index){ currentPhotoIndex = index; }

    public int getCurrentMediaIndex() { return currentPhotoIndex; }

    public int getMediaSize(){ return media.size();}

    public int getSelectedMediaSize() {
        return selectedMedias.size();
    }

    public Media getCoverAlbum() {
        if (media.size() > 0)
            return media.get(0);
        return new Media();
    }

    public void setSelectedPhotoAsPreview(Context context) {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        h.setAlbumPhotPreview(getPath(), selectedMedias.get(0).getPath());
    }
    public String getFirstSelectedMediaPath(){
    	return this.selectedMedias.get(0).getPath();
    }

    public boolean hasMediaSelected(){
    	return selectedMedias.size() > 0;
    }

    public void setCurrentPhoto(String path) {
    	int mediaSize = media.size();
        for (int i = 0; i < mediaSize; i++)
            if (media.get(i).getPath().equals(path)) currentPhotoIndex = i;
    }
    public void selectAllPhotos() {
        selectedMedia.clear();
        for (Media m : media){
            m.setSelected(true);
            selectedMedias.add(m);
        }
    }

     public int toggleSelectPhoto(int index) {
     	Media mediaItem = media.get(index);
        if (mediaItem != null) {
            mediaItem.setSelected(!mediaItem.isSelected());
            if (mediaItem.isSelected())
                selectedMedias.add(mediaItem);
            else
                selectedMedias.remove(mediaItem);
        }
        return index;
    }

    public void renameCurrentMedia(Context context, String newName) {
        try {
            File from = new File(getCurrentMedia().getPath());
            File to = new File(StringUtils.getPhotoPathRenamed(getCurrentMedia().getPath(), newName));
            if (from.renameTo(to)) {
                scanFile(context, new String[]{ to.getAbsolutePath(), from.getAbsolutePath() });
                getCurrentMedia().path = to.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     public int moveCurrentPhoto(Context context, String newName) {
        try {
            File from = new File(getCurrentMedia().getPath());
            File to = new File(StringUtils.getPhotoPathMoved(getCurrentMedia().getPath(), newName));
            if (from.renameTo(to)) {
                scanFile(context, new String[]{ to.getAbsolutePath(), from.getAbsolutePath() });
                getCurrentMedia().path = to.getAbsolutePath();
                media.remove(getCurrentMediaIndex());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return getCurrentMediaIndex();
    }

    /**
     * On longpress, it finds the last or the first selected image before or after the targetIndex
     * and selects them all.
     *
     * @param targetIndex
     * @param adapter
     */
    public void selectAllPhotosUpTo(int targetIndex, PhotosAdapter adapter) {
        int indexRightBeforeOrAfter = -1;
        int indexNow;
        for (Media sm : selectedMedias) {
            indexNow = media.indexOf(sm);
            if (indexRightBeforeOrAfter == -1) {
                indexRightBeforeOrAfter = indexNow;
            }

            if (indexNow > targetIndex) {
                break;
            }
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

      public void clearSelectedPhotos() {
        for (Media m : media) {
            m.setSelected(false);
        }
        selectedMedias.clear();
    }

    public void copySelectedPhotos(Context context, String folderPath) {
        for (Media media : selectedMedias)
            copyPhoto(context, media.getPath(), folderPath);
    }

    public void copyPhoto(Context context, String olderPath, String folderPath) {
        try {
            File from = new File(olderPath);
            File to = new File(StringUtils.getPhotoPathMoved(olderPath, folderPath));

            InputStream in = new FileInputStream(from);
            OutputStream out = new FileOutputStream(to);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);

            in.close();
            out.close();

            scanFile(context, new String[]{to.getAbsolutePath()});
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void deleteCurrentMedia(Context context) {
        deleteMedia(context, media.get(getCurrentMediaIndex()));
        media.remove(getCurrentMediaIndex());
    }

    public void deleteMedia(Context context, Media media) {
        File file = new File(media.getPath());
        if (file.delete())
            scanFile(context, new String[]{ file.getAbsolutePath() });
    }

    public void deleteSelectedMedia(Context context) {
        for (Media selectedMedia : selectedMedias) {
            deleteMedia(context, selectedMedia);
            media.remove(selectedMedia);
        }
        clearSelectedPhotos();
    }

    public void scanFile(Context context, String[] path) {   MediaScannerConnection.scanFile(context, path, null, null); }