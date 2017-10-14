package com.gg.imagedemo;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.gg.imagedemo.model.ImageBean;
import com.gg.imagedemo.utils.GlideApp;
import com.gg.imagedemo.utils.GlideRequest;
import com.gg.imagedemo.utils.GlideRequests;

import java.util.ArrayList;
import java.util.List;

/**
 * Creator : GG
 * Time    : 2017/10/13
 * Mail    : gg.jin.yu@gmail.com
 * Explain :
 */

public class SelectImage1Activity extends AppCompatActivity {

    RecyclerView mRecyclerView;

    private ArrayList<ImageBean> mImages = new ArrayList<>();

    ImageAdapter mImageAdapter;
    NewAdapter mNewAdapter;

    private GlideRequest<Drawable> fullRequest;
    private int photoSize;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_image);

        final GlideRequests glideRequests = GlideApp.with(this);
        fullRequest = glideRequests
                .asDrawable()
                .centerCrop();

//        thumbRequest = glideRequests
//                .asDrawable()
////                .diskCacheStrategy(DiskCacheStrategy.DATA)
//                .override(Api.SQUARE_THUMB_SIZE)
//                .transition(withCrossFade());


        photoSize = getPageSize(R.dimen.medium_photo_side);


        final int gridMargin = getResources().getDimensionPixelOffset(R.dimen.grid_margin);
        int spanCount = getResources().getDisplayMetrics()
                .widthPixels / (photoSize + (2 * gridMargin));
        mRecyclerView = (RecyclerView) findViewById(R.id.image_rv);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        mImageAdapter = new ImageAdapter(this, mImages,fullRequest);

        mRecyclerView.setAdapter(mImageAdapter);

//        mNewAdapter = new NewAdapter(mImages);
//
//        mRecyclerView.setAdapter(mNewAdapter);

        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                       RecyclerView.State state) {
                outRect.set(gridMargin, gridMargin, gridMargin, gridMargin);
            }
        });

        mRecyclerView.setRecyclerListener(new RecyclerView.RecyclerListener() {
            @Override
            public void onViewRecycled(RecyclerView.ViewHolder holder) {
                ImageAdapter.ImageViewHolder viewHolder = (ImageAdapter.ImageViewHolder) holder;
                glideRequests.clear(viewHolder.mImageView);
            }
        });

        int heightCount = getResources().getDisplayMetrics().heightPixels / photoSize;
        mRecyclerView.getRecycledViewPool().setMaxRecycledViews(0, 3 * heightCount * 2);
        mRecyclerView.setItemViewCacheSize(0);


        FixedPreloadSizeProvider<ImageBean> preloadSizeProvider =
                new FixedPreloadSizeProvider<>(photoSize, photoSize);
        RecyclerViewPreloader<ImageBean> preLoader = new RecyclerViewPreloader<>(
                GlideApp.with(this), mImageAdapter, preloadSizeProvider, 30);

        mRecyclerView.addOnScrollListener(preLoader);

        getSupportLoaderManager().initLoader(0, null, mCallbacks);

    }
    private int getPageSize(int id) {
        return getResources().getDimensionPixelSize(id);
    }
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media._ID

        };

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            CursorLoader cursorLoader = new CursorLoader(SelectImage1Activity.this,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    IMAGE_PROJECTION,
                    IMAGE_PROJECTION[4] + ">0 AND " + IMAGE_PROJECTION[3]
                            + "=? OR " + IMAGE_PROJECTION[3] + "=? ",
                    new String[]{"image/jpeg", "image/png"},
                    IMAGE_PROJECTION[2] + " DESC");
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null && data.getCount() > 0) {
                data.moveToFirst();
                while (data.moveToNext()) {
                    String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                    String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                    long time = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                    mImages.add(new ImageBean(path, name, time));
                }
            }

            mImageAdapter.notifyDataSetChanged();
//            mNewAdapter.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };


    public class ImageAdapter<ImageViewHolder extends RecyclerView.ViewHolder> extends RecyclerView.Adapter  implements ListPreloader.PreloadModelProvider<ImageBean>{
        private GlideRequest<Drawable> fullRequest;

        private ArrayList<ImageBean> mData;
        private Context mContext;

        public ImageAdapter(Context context, ArrayList<ImageBean> list,GlideRequest<Drawable> fullRequest) {
            mData = list;
            mContext = context;
            this.fullRequest = fullRequest;

        }


        @Override
        public ImageAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageAdapter.ImageViewHolder viewHolder = new ImageAdapter.ImageViewHolder(LayoutInflater.from(mContext).inflate(R
                    .layout.item_image, parent, false));
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ImageAdapter.ImageViewHolder viewHolder = (ImageAdapter.ImageViewHolder) holder;
            fullRequest.load(mData.get(position).getPath())
//                        .thumbnail(thumbRequest.load(item.getPath()))
                    .into(viewHolder.mImageView);
//            Glide.with(mContext).load(mData.get(position)).into(viewHolder.mImageView);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        private class ImageViewHolder extends RecyclerView.ViewHolder {

            private ImageView mImageView;

            public ImageViewHolder(View itemView) {
                super(itemView);
                mImageView = (ImageView) itemView.findViewById(R.id.image);
            }
        }

        @Override
        public List<ImageBean> getPreloadItems(int position) {
            return mData.subList(position, position + 1);
        }

        @Override
        public RequestBuilder getPreloadRequestBuilder(ImageBean item) {
            return fullRequest
                    /*.thumbnail(thumbRequest.load(item.getPath()))*/
                    .load(item.getPath());
        }

    }

    public class NewAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        public NewAdapter(@Nullable List<String> data) {
            super(R.layout.item_image, data);
        }

        protected void convert(BaseViewHolder helper, String item) {
            GlideApp.with(mContext).load(item).into((ImageView) helper.getView(R.id.image));
        }
    }
}
