package com.ashish.getimagesfrommobile;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class BucketsFragment extends Fragment {

    TextView tvSelection;
//    LinearLayout llAlbumbList;
    RelativeLayout rlAlbumList;
    View view;
    ListView lvAlbumbList;
    GridView gvAlbumbPhotos;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_home, null);
        tvSelection = (TextView) view.findViewById(R.id.tvSelection);
//        llAlbumbList = (LinearLayout) view.findViewById(R.id.llAlbumbList);
        rlAlbumList = (RelativeLayout) view.findViewById(R.id.rlAlbumList);
        lvAlbumbList = (ListView) view.findViewById(R.id.lvAlbumbList);
        gvAlbumbPhotos = (GridView) view.findViewById(R.id.gvAlbumbPhotos);


        String[] projection = new String[]{MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_ID};

        Cursor cur = getActivity().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC, "
                        + MediaStore.Images.Media.DATE_MODIFIED + " DESC");

        final List<GridItem> buckets = new ArrayList<GridItem>();
        BucketItem lastBucket = null;

        if (cur != null) {
            if (cur.moveToFirst()) {
                while (!cur.isAfterLast()) {
                    if (lastBucket == null
                            || !lastBucket.name.equals(cur.getString(1))) {
                        lastBucket = new BucketItem(cur.getString(1),
                                cur.getString(0), "", cur.getInt(2));
                        buckets.add(lastBucket);
                    } else {
                        lastBucket.images++;
                    }
                    cur.moveToNext();
                }
            }
            cur.close();
            showAlbumbPhotos(((BucketItem) buckets.get(0)).id);
        }

        tvSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (rlAlbumList.getVisibility() == View.VISIBLE) {
                    // Its visible
                    rlAlbumList.setVisibility(View.GONE);
                } else {
                    // Either gone or invisible
                    rlAlbumList.setVisibility(View.VISIBLE);

                }
            }
        });

        if (buckets.isEmpty()) {
            Toast.makeText(getActivity(), R.string.no_images,
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        } else {
            lvAlbumbList.setAdapter(new AlbumListAdapter(getActivity(), buckets));
            lvAlbumbList.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    /*((SelectPictureActivity) getActivity())
                            .showBucket(((BucketItem) buckets.get(position)).id);*/
                    rlAlbumList.setVisibility(View.GONE);
                    showAlbumbPhotos(((BucketItem) buckets.get(position)).id);
                    tvSelection.setText(buckets.get(position).name);
                    Toast.makeText(getActivity(), "Album Name : " + ((BucketItem) buckets.get(position)).id, Toast.LENGTH_SHORT).show();
                }
            });
        }
        return view;
    }

    private void showAlbumbPhotos(int id) {
        Cursor cur = getActivity().getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Images.Media.DATA,
                                MediaStore.Images.Media.DISPLAY_NAME,
                                MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.SIZE},
                        MediaStore.Images.Media.BUCKET_ID + " = ?",
                        new String[]{String.valueOf(id)},
                        MediaStore.Images.Media.DATE_MODIFIED + " DESC");

        final List<GridItem> images = new ArrayList<GridItem>(cur.getCount());

        if (cur != null) {
            if (cur.moveToFirst()) {
                while (!cur.isAfterLast()) {
                    images.add(new GridItem(cur.getString(1), cur.getString(0), cur.getString(2), cur.getLong(3)));
                    cur.moveToNext();
                }
            }
            cur.close();
        }

        gvAlbumbPhotos.setAdapter(new GalleryAdapter(getActivity(), images));

        gvAlbumbPhotos.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ((SelectPictureActivity) getActivity()).imageSelected(images
                        .get(position).path, images
                        .get(position).imageTaken, images
                        .get(position).imageSize);
            }
        });
    }

}
