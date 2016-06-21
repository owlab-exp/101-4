package com.owlab.callblocker.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.owlab.callblocker.R;
import com.owlab.callblocker.Utils;

/**
 * Created by ernest on 6/20/16.
 */
public class ViewPagerContainerFragment extends Fragment {
    private static final String TAG = ViewPagerContainerFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.view_pager_container_view, container, false);

        ViewPager viewPager = (ViewPager) root.findViewById(R.id.pager);
        //
        viewPager.setPageMargin(Utils.convertDip2Pixels(getActivity(), 20));
        viewPager.setPageMarginDrawable(R.color.colorPrimarylight);

        FragmentManager fragmentManager = getChildFragmentManager();
        MyFragmentPagerAdapter myFragmentPagerAdapter = new MyFragmentPagerAdapter(fragmentManager);
        if (viewPager != null) {
            viewPager.setAdapter(myFragmentPagerAdapter);
        }

        return root;
    }

    public static class MyFragmentPagerAdapter  extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "Getting item in position: " + position);
            switch (position) {
                case 0:
                    BlockedNumberListFragment blockedNumberListFragment = new BlockedNumberListFragment();
                    return blockedNumberListFragment;
                case 1:
                    BlockedCallLogFragment blockedCallLogFragment = new BlockedCallLogFragment();
                    return blockedCallLogFragment;
            }

            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0:
                    return "Blocked Numbers";
                case 1:
                    return "Blocked Calls";
            }

            return null;
        }
    }
}
