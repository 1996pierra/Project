package com.example.pierrakimathi.jipeorder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class TestFragmentAdapter extends FragmentPagerAdapter {
    protected static final String[] CONTENT = new String[] { "Starters", "Kenyan", "Main Course", "Desserts"};
    protected static final int[] XML = {R.xml.list_a,R.xml.list_b,R.xml.list_c,R.xml.list_d};
    private int mCount = CONTENT.length;

    public TestFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return TestFragment.newInstance(CONTENT[position % CONTENT.length],XML[position]);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}