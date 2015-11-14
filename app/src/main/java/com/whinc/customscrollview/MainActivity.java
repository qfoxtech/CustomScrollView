package com.whinc.customscrollview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.whinc.widget.CustomScrollView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    @Bind(R.id.custom_scrollView) CustomScrollView mCustomScrollView;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    private boolean mAutoScroll = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final int count = 3;
        mCustomScrollView.setInterpolator(new OvershootInterpolator());
        mCustomScrollView.setOnItemChangedListener(new CustomScrollView.OnItemChangedListener() {
            @Override
            public void onChanged(final CustomScrollView view, int prev, int cur) {
                Log.i(TAG, String.format("prev:%d, cur:%d", prev, cur));
                if (mAutoScroll) {
                    Log.i(TAG, "auto scroll");
                    if (cur == 0) {
                        // ScrollView is scrolling, so you need delaying call scrollTo()
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                view.scrollTo(view.getChildCount() - 1);
                            }
                        });
                    }
                    if (cur == view.getChildCount() - 1) {
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                view.scrollTo(0);
                            }
                        });
                    }
                }
            }
        });
        mCustomScrollView.setAdapter(new MyScrollViewAdapter(this, count));

        setSupportActionBar(mToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_fold:
                foldScrollView();
                break;
            case R.id.action_unfold:
                unfoldScrollView();
                break;
            case R.id.action_print_large_item_index:
                Log.i(TAG, "large item index:" + mCustomScrollView.getItemLargeIndex());
                break;
            case R.id.action_print_item_tag:
                printItemTags();
                break;
            case R.id.action_set_3_items:
                showScrollView(3);
                break;
            case R.id.action_set_8_items:
                showScrollView(8);
                break;
            case R.id.action_set_22_items:
                showScrollView(22);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void printItemTags() {
        for (int i = 0; i < mCustomScrollView.getItemCount(); ++i) {
            Integer tag = (Integer) mCustomScrollView.getItem(i).getTag();
            Log.i(TAG, "Tag of view " + i + ":" + tag);
        }
    }

    private void foldScrollView() {
        int parentW = ((ViewGroup)mCustomScrollView.getParent()).getWidth();
        int width = mCustomScrollView.getWidth();
        ObjectAnimator unfoldXAnimator = ObjectAnimator.ofInt(mCustomScrollView, new Property<CustomScrollView, Integer>(Integer.class, "") {
            @Override
            public Integer get(CustomScrollView object) {
                return mCustomScrollView.getLayoutParams().width;
            }

            @Override
            public void set(CustomScrollView object, Integer value) {
                ViewGroup.LayoutParams lp = mCustomScrollView.getLayoutParams();
                lp.width = value;
                mCustomScrollView.setLayoutParams(lp);
            }
        }, width, width / 3);
        unfoldXAnimator.setDuration(1000);
        unfoldXAnimator.start();
    }

    private void unfoldScrollView() {
        int parentW = ((ViewGroup)mCustomScrollView.getParent()).getWidth();
        int width = mCustomScrollView.getWidth();
        ObjectAnimator foldXAnimator = ObjectAnimator.ofInt(mCustomScrollView, new Property<CustomScrollView, Integer>(Integer.class, "") {
            @Override
            public Integer get(CustomScrollView object) {
                return mCustomScrollView.getLayoutParams().width;
            }

            @Override
            public void set(CustomScrollView object, Integer value) {
                ViewGroup.LayoutParams lp = mCustomScrollView.getLayoutParams();
                lp.width = value;
                mCustomScrollView.setLayoutParams(lp);
            }
        }, width, parentW);
        foldXAnimator.setDuration(1000);
        foldXAnimator.start();
    }

    private void showScrollView(final int n) {
        mCustomScrollView.setAdapter(new MyScrollViewAdapter(this, n));
        if (mCustomScrollView.getVisibility() != View.VISIBLE) {
            mCustomScrollView.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.hide_button)
    protected void hideScrollView() {
        if (mCustomScrollView.getVisibility() == View.VISIBLE) {
            mCustomScrollView.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.scroll_left_button)
    protected void scrollLeft() {
        if (!mCustomScrollView.isScrolling()) {
            mCustomScrollView.scrollBy(-1);
        }
    }

    @OnClick(R.id.scroll_right_button)
    protected void scrollRight() {
        if (!mCustomScrollView.isScrolling()) {
            mCustomScrollView.scrollBy(1);
        }
        Log.i(TAG, "large item index:" + mCustomScrollView.getItemLargeIndex());
    }

    @OnClick(R.id.clear_item_button)
    protected void clearItems() {
        mCustomScrollView.clearItems();
    }

    @OnClick(R.id.scroll_to_button)
    protected void scrollTo() {
        if (!mCustomScrollView.isScrolling()) {
            mCustomScrollView.scrollTo(0);
        }
    }

    @OnClick(R.id.stop_scroll_button)
    protected void stopScroll() {
        mCustomScrollView.stopScroll();
    }

    @OnClick(R.id.auto_scroll_button)
    protected void autoScroll() {
        mAutoScroll = true;
        if (mCustomScrollView.getItemLargeIndex() == 0) {
            mCustomScrollView.scrollTo(mCustomScrollView.getChildCount() - 1);
        } else {
            mCustomScrollView.scrollTo(0);
        }
    }

    @OnClick(R.id.stop_auto_scroll_button)
    protected void stopAutoScroll() {
        mAutoScroll = false;
    }

    @OnClick(R.id.goto_second_activity_button)
    protected void startSencodActivity() {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    static class MyScrollViewAdapter implements CustomScrollView.Adapter {
        private final Context mContext;
        private final int mCount;
        private @IntegerRes int [] mDrawableList = new int[] {
                R.drawable.img1,
                R.drawable.img2,
                R.drawable.img3,
                R.drawable.img4,
                R.drawable.img5,
                R.drawable.img6,
                R.drawable.img7,
                R.drawable.img8,
                R.drawable.img9,
                R.drawable.img10,
                R.drawable.img11,
                R.drawable.img12,
                R.drawable.img13,
                R.drawable.img14,
                R.drawable.img15,
                R.drawable.img16,
                R.drawable.img17,
                R.drawable.img18,
                R.drawable.img19,
                R.drawable.img20,
                R.drawable.img21,
                R.drawable.img22,
        };

        public MyScrollViewAdapter(Context context, int count) {
            mContext = context;
            mCount = count;
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public View getView(CustomScrollView parent, int pos) {
            if (pos >= mDrawableList.length) {
                throw new IndexOutOfBoundsException("size:" + mDrawableList.length + ", index:" + pos);
            }
            View view = LayoutInflater.from(mContext).inflate(R.layout.scrollview_item, parent, false);
            ImageView imgView = (ImageView)view.findViewById(R.id.imageView);
            imgView.setImageResource(mDrawableList[pos]);
            Log.i(TAG, "pos:" + pos);
            view.setTag(pos);
            return view;
        }
    }
}
