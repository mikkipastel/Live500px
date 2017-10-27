package com.mikkipastel.live500px.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.inthecheesefactory.thecheeselibrary.manager.Contextor;
import com.mikkipastel.live500px.R;
import com.mikkipastel.live500px.adapter.PhotoListAdapter;
import com.mikkipastel.live500px.dao.PhotoItemCollectionDao;
import com.mikkipastel.live500px.manager.HttpManager;
import com.mikkipastel.live500px.manager.PhotoListManager;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends Fragment {

    ListView listView;
    PhotoListAdapter listAdapter;

    SwipeRefreshLayout swipeRefreshLayout;

    PhotoListManager photoListManager;

    Button btnNewPhoto;

    boolean isLoadMore = false;

    public MainFragment() {
        super();
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        initInstances(rootView);
        return rootView;
    }

    private void initInstances(View rootView) {
        photoListManager = new PhotoListManager();
        btnNewPhoto = rootView.findViewById(R.id.btnNewPhoto);
        btnNewPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.smoothScrollToPosition(0);
                hideButtonNewPhotos();
            }
        });
        // Init 'View' instance(s) with rootView.findViewById here
        listView = rootView.findViewById(R.id.listView);
        listAdapter = new PhotoListAdapter();
        listView.setAdapter(listAdapter);

        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0);
                if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                    if (photoListManager.getCount() > 0) {
                        loadMoreData();
                    }

                }
            }
        });

        refreshData();
    }

    private void refreshData() {
        if (photoListManager.getCount() == 0)
            reloadData();
        else
            reloadDataNewer();
    }

    class PhotoListLoadCallback implements Callback<PhotoItemCollectionDao> {

        public static final int MODE_RELOAD = 1;
        public static final int MODE_RELOAD_NEWER = 2;
        public static final int MODE_RELOAD_MORE = 3;

        int mode;

        public PhotoListLoadCallback(int mode) {
            this.mode = mode;
        }

        @Override
        public void onResponse(Call<PhotoItemCollectionDao> call, Response<PhotoItemCollectionDao> response) {
            swipeRefreshLayout.setRefreshing(false);
            if (response.isSuccessful()) {
                PhotoItemCollectionDao dao = response.body();

                int firstVisiblePosition = listView.getFirstVisiblePosition();
                View child = listView.getChildAt(0);
                int top = (child == null) ? 0 : child.getTop();

                if (mode == MODE_RELOAD_NEWER) {
                    photoListManager.insertDaoAtTopPosition(dao);
                }
                else if (mode == MODE_RELOAD_MORE) {
                    photoListManager.appendDaoAtBottomPosition(dao);
                    isLoadMore = false;
                }
                else {
                    photoListManager.setDao(dao);
                }

                listAdapter.setDao(photoListManager.getDao());
                listAdapter.notifyDataSetChanged();

                if (mode == MODE_RELOAD_NEWER) {
                    int additionalSize = (dao != null) ? dao.getData().size() : 0;
                    listAdapter.increaseLastPosition(additionalSize);
                    listView.setSelectionFromTop(firstVisiblePosition + additionalSize,
                            top);
                    if (additionalSize > 0)
                        showButtonNewPhotos();
                } else {

                }

                Toast.makeText(Contextor.getInstance().getContext(),
                        "Load Complete",
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                if (mode == MODE_RELOAD_MORE)
                    isLoadMore = false;
                try {
                    Toast.makeText(Contextor.getInstance().getContext(),
                            response.errorBody().string(),
                            Toast.LENGTH_SHORT)
                            .show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(Call<PhotoItemCollectionDao> call, Throwable t) {
            if (mode == MODE_RELOAD_MORE)
                isLoadMore = false;

            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(Contextor.getInstance().getContext(),
                    t.toString(),
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void reloadDataNewer() {
        int maxId = photoListManager.getMaximumId();
        Call<PhotoItemCollectionDao> call = HttpManager.getInstance()
                .getService()
                .loadPhotoListAfterId(maxId);
        call.enqueue(new PhotoListLoadCallback(PhotoListLoadCallback.MODE_RELOAD_NEWER));
    }

    private void loadMoreData() {
        if (isLoadMore)
            return;
        isLoadMore = true;

        int minId = photoListManager.getMinimumId();
        Call<PhotoItemCollectionDao> call = HttpManager.getInstance()
                .getService()
                .loadPhotoListBeforeId(minId);
        call.enqueue(new PhotoListLoadCallback(PhotoListLoadCallback.MODE_RELOAD_MORE));
    }

    private void reloadData() {
        Call<PhotoItemCollectionDao> call = HttpManager.getInstance()
                .getService()
                .loadPhotoList();
        call.enqueue(new PhotoListLoadCallback(PhotoListLoadCallback.MODE_RELOAD));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /*
     * Save Instance State Here
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save Instance State here
    }

    /*
     * Restore Instance State Here
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore Instance State here
        }
    }

    public void showButtonNewPhotos() {
        btnNewPhoto.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(Contextor.getInstance().getContext(),
                R.anim.zoom_fade_in);
        btnNewPhoto.setAnimation(anim);
    }

    public void hideButtonNewPhotos() {
        btnNewPhoto.setVisibility(View.GONE);
        Animation anim = AnimationUtils.loadAnimation(Contextor.getInstance().getContext(),
                R.anim.zoom_fade_out);
        btnNewPhoto.setAnimation(anim);
    }
}
