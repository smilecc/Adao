package com.hujiang.designsupportlibrarydemo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class FragmentAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> mFragments;
    private List<Forum> mForum;

    public FragmentAdapter(FragmentManager fm, List<Fragment> fragments, List<Forum> Forums) {
        super(fm);
        mFragments = fragments;
        mForum = Forums;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mForum.get(position).Name;
    }

}
