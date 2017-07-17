package com.onceapps.m.ui.activites;

import android.content.Intent;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ViewFlipper;

import com.onceapps.core.util.Logger;
import com.onceapps.m.R;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.models.MagazineStatus;
import com.onceapps.m.ui.AllBrandsMagazineListItemViewHolder;
import com.onceapps.m.ui.adapters.AllBrandsMagazineListAnimatedViewAdapter;
import com.onceapps.m.ui.adapters.MyIssuesAnimatedViewAdapter;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.security.GeneralSecurityException;

import io.apptik.multiview.layoutmanagers.SnapperLinearLayoutManager;


@EActivity(R.layout.activity_my_issues)
public class MyIssuesActivity extends BaseActivity implements AllBrandsMagazineListAnimatedViewAdapter.OnItemClickListener {

    public static final String KEY_MAGAZINE_TO_ADD = "mgta";

    @Extra
    protected Magazine magazineToAdd;

    @ViewById(R.id.isempty_flipper)
    protected ViewFlipper mIsEmptyFlipper;

    @ViewById(R.id.recycler)
    protected RecyclerView recyclerView;

    @ViewById(R.id.add_issues_fab)
    protected FloatingActionButton mAddIssuesFAB;

    @InstanceState
    protected Parcelable mListViewState;

    private MyIssuesAnimatedViewAdapter mAdapter;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        downloadMagazineToAdd();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mListViewState = recyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mListViewState != null && mAdapter != null && mMagazineHandler.getMagazineRegistry().size() == mAdapter.getMagazinesItemCount()) {
            recyclerView.getLayoutManager().onRestoreInstanceState(mListViewState);
            mAdapter.setMagazineHandler(mMagazineHandler);
        } else {
            updateUI();
        }
    }

    private void downloadMagazineToAdd() {
        if (magazineToAdd != null) {
            try {
                MagazineStatus status = mMagazineHandler.download(magazineToAdd);
                if (mAdapter != null) {
                    mAdapter.addMagazineStatus(status);
                }
            } catch (IllegalStateException e) {
                Logger.debug("magazine already in download queue");
            } catch (GeneralSecurityException e) {
                Logger.debug("storage permission not granted");
                StoragePermissionRequestActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP).start();
            }
        }
    }

    private void downloadMagazine(Magazine magazine) {
        if (magazine != null) {
            try {
                MagazineStatus status = mMagazineHandler.download(magazine);
                if (mAdapter != null) {
                    mAdapter.addMagazineStatus(status);
                }
            } catch (IllegalStateException e) {
                Logger.debug("magazine already in download queue");
            } catch (GeneralSecurityException e) {
                Logger.debug("storage permission not granted");
                StoragePermissionRequestActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP).start();
            }
        }
    }

    @AfterViews
    protected void afterViews() {

        setTitle(getString(R.string.menu_downloads));
        initRecyclerView();

        downloadMagazineToAdd();
    }

    @UiThread
    protected void updateUI() {
        if (mMagazineHandler.getMagazineRegistry() == null || mMagazineHandler.getMagazineRegistry().isEmpty()) {
            mIsEmptyFlipper.setDisplayedChild(0);
            mAddIssuesFAB.setVisibility(View.GONE);
        } else {
            mIsEmptyFlipper.setDisplayedChild(1);
            mAddIssuesFAB.setVisibility(View.VISIBLE);
            setRecyclerAdapter(recyclerView);
            recyclerView.scheduleLayoutAnimation();
        }
    }

    private void initRecyclerView() {
        final LinearLayoutManager layoutManager = new SnapperLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                for (int i = 0; i < recyclerView.getChildCount(); ++i) {
                    RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
                    if (viewHolder instanceof AllBrandsMagazineListItemViewHolder) {
                        AllBrandsMagazineListItemViewHolder cellViewHolder = ((AllBrandsMagazineListItemViewHolder) viewHolder);
                        cellViewHolder.newPosition(i);
                    }
                }
            }
        });

        DefaultItemAnimator animator = new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
                return true;
            }
        };
        recyclerView.setItemAnimator(animator);
    }

    private void setRecyclerAdapter(RecyclerView recyclerView) {
        mAdapter = new MyIssuesAnimatedViewAdapter(mMagazineHandler, this);
        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                if (mMagazineHandler.getMagazineRegistry() == null || mAdapter.getMagazinesItemCount() == 0) {
                    mIsEmptyFlipper.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mIsEmptyFlipper.setDisplayedChild(0);
                            mAddIssuesFAB.setVisibility(View.GONE);

                        }
                    }, 500);
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                mIsEmptyFlipper.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsEmptyFlipper.setDisplayedChild(1);
                        mAddIssuesFAB.setVisibility(View.VISIBLE);
                    }
                }, 500);
            }
        });
    }

    @Override
    public void onItemClick(View view, Magazine magazine) {
        MagazinePreviewActivity_.intent(this).magazine(magazine).start();
    }

    @Click({R.id.add_issues_fab, R.id.add_issues_layout})
    protected void addIssuesClicked() {
        AllBrandsMagazineListActivity_.intent(this).start();
    }

    @Subscribe
    public void statusUpdated(MagazineStatus status) {
        if (mAdapter != null)
            mAdapter.updateMagazineStatus(status);

        if (status.getDownloadStatus() == MagazineStatus.DownloadStatus.DOWNLOAD_FAILED) {
            showDownloadFailedDialog(status.getMagazine());
            mAdapter.removeMagazineStatus(status);
            mMagazineHandler.cancel(status.getMagazine());
        }
    }

    private void showDownloadFailedDialog(final Magazine magazine) {

        showAlertDialog(
                getString(R.string.error),
                getString(R.string.magazine_download_failed_message, magazine.getName()),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMagazineHandler.delete(magazine);
                        downloadMagazine(magazine);
                        dismissAlertDialog();
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissAlertDialog();
                    }
                }
        );
    }
}