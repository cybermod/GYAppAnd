package com.turkcell.gelecegiyazanlar.adapterlisteners;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.turkcell.gelecegiyazanlar.fragments.BlogEtkinlikListeFragment;

public class ViewPagerAdapterBlogEtkinlik extends FragmentStatePagerAdapter {

    CharSequence titles[];
    int numbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapterBlogEtkinlik(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);

        this.titles = mTitles;
        this.numbOfTabs = mNumbOfTabsumb;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {

        return new BlogEtkinlikListeFragment();

    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return numbOfTabs;
    }
}
