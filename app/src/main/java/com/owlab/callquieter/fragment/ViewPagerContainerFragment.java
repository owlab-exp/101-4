package com.owlab.callquieter.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.owlab.callquieter.MainActivity;
import com.owlab.callquieter.R;
import com.owlab.callquieter.util.Utils;

import java.io.Serializable;

/**
 * Created by ernest on 6/20/16.
 */
public class ViewPagerContainerFragment extends Fragment {
    public static final String TAG = ViewPagerContainerFragment.class.getSimpleName();

    private final int pageCount = 2;
    private final int pageMarginDp = 2;

    ViewPager viewPager;
    MyFragmentPagerAdapter myFragmentPagerAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ////Log.d(TAG, ">>>>> onCreateView called with savedInstanceState: " + Objects.toString(savedInstanceState));

        View root = inflater.inflate(R.layout.view_pager_container_view, container, false);


        viewPager = (ViewPager) root.findViewById(R.id.pager);
        //
        viewPager.setPageMargin(Utils.convertDip2Pixels(getActivity(), pageMarginDp));
        viewPager.setPageMarginDrawable(R.color.colorPrimary);

        FragmentManager fragmentManager = getChildFragmentManager();

        //if(savedInstanceState != null && savedInstanceState.getSerializable("pagerAdapter") != null) {
        //    myFragmentPagerAdapter = (MyFragmentPagerAdapter)savedInstanceState.getSerializable("pagerAdapter");
        //} else {
            myFragmentPagerAdapter = new MyFragmentPagerAdapter(fragmentManager, pageCount);
        //}

        if (viewPager != null) {
            viewPager.setAdapter(myFragmentPagerAdapter);
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        ////Log.d(TAG, ">>>>> onResume called");
        MainActivity mainActivity = (MainActivity)getActivity();
        ActionBar mainActionBar = mainActivity.getSupportActionBar();
        if(mainActionBar != null) {
            mainActionBar.setTitle(R.string.app_name);
            mainActionBar.setDisplayHomeAsUpEnabled(false);

            //mainActivity.invalidateOptionsMenu();
            Menu mainMenu = mainActivity.getMenu();
            if (mainMenu != null && !mainMenu.findItem(R.id.menuitem_settings).isEnabled()) {
                //mainMenu.findItem(R.id.menuitem_main_onoff_switch_layout).getActionView().findViewById(R.id.action_main_onoff_switch).setVisibility(View.INVISIBLE);
                //mainMenu.findItem(R.id.menuitem_settings).setVisible(false);
                mainMenu.findItem(R.id.menuitem_settings).setEnabled(true);
            }
        }

        ////To handle request for certain page when open
        //Bundle args = getArguments();
        //int pageNo = 0;
        //if(args != null && (pageNo = args.getInt("pageNo")) > 0) {
        //    viewPager.setCurrentItem(pageNo);
        //}
    }

    public void setPage(int pageIndex) {
        if(viewPager != null) {
            viewPager.setCurrentItem(pageIndex);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        ////Log.d(TAG, ">>>>> onSaveInstanceState called");
        //outState.putSerializable("pagerAdapter", myFragmentPagerAdapter);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();

        ////Log.d(TAG, ">>>>> onPause called");
    }

    public static class MyFragmentPagerAdapter extends FragmentPagerAdapter implements Serializable {
        private static final String TAG = MyFragmentPagerAdapter.class.getSimpleName();

        private SparseArrayCompat<Fragment> fragments = new SparseArrayCompat<>();

        private int pageCount;

        public MyFragmentPagerAdapter(FragmentManager fm, int pageCount) {
            super(fm);
            this.pageCount = pageCount;
            ////Log.d(TAG, ">>>>> instantiated");
        }

        public Fragment getFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            ////Log.d(TAG, "Getting item in position: " + position);
            Fragment fragment = fragments.get(position);

            if (fragment != null) {
                return fragment;
            }

            switch (position) {
                    case 0:
                        RegisteredNumberListFragment registeredNumberListFragment = new RegisteredNumberListFragment();
                        fragments.put(position, registeredNumberListFragment);
                        return registeredNumberListFragment;
                    case 1:
                        QuietedCallLogFragment quietedCallLogFragment = new QuietedCallLogFragment();
                        fragments.put(position, quietedCallLogFragment);
                        return quietedCallLogFragment;
            }

            return null;
        }

        @Override
        public int getCount() {
            return pageCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Registered Numbers";
                case 1:
                    return "Quieted Calls";
            }

            return null;
        }
    }
}
