package com.xyz.screen.recorder.CoderlyticsFragments;

import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.xyz.screen.recorder.R;
import com.google.android.material.tabs.TabLayout;


public class AllFragmentsTab extends Fragment {

    public static int int_items = 3;
    public TabLayout tabLayout2;
    public ViewPager viewPager;
int vwpgr_pos=0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View x = inflater.inflate(R.layout.contetn_tablayout, null);
        tabLayout2 = x.findViewById(R.id.tabs2);
        viewPager = x.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);

        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));
        tabLayout2.setupWithViewPager(viewPager);

        tabLayout2.getTabAt(0).setIcon(R.drawable.video_gallery);
        tabLayout2.getTabAt(1).setIcon(R.drawable.ic_frame);
        tabLayout2.getTabAt(2).setIcon(R.drawable.app_setiing);

        viewPager.setCurrentItem(vwpgr_pos);

        return x;

    }
public AllFragmentsTab(int vp)
{
    vwpgr_pos=vp;
}
    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case 0:
                    Log.i("iaminaf"," case o: ");
                    return checkFrag(new VideosShowingFragment());
                case 1:
                    return checkFrag(new DisplayScreenshotsFragment());
                case 2:
                    return checkFrag(new SettingsFragment());


            }

            return null;
        }

        @Override
        public int getCount() {
            return int_items;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }
public Fragment checkFrag(final Fragment fragment)
{
                if (fragment instanceof BaseFragment) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                ((BaseFragment) fragment).onVisibleFragment();
                            }
                        });
                    }
                }, 100);
            }
return fragment;
}
}
