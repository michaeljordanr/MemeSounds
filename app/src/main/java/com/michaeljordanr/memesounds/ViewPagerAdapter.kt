package com.michaeljordanr.memesounds

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
    private var query = ""

    fun setQuery(query: String) {
        this.query = query
    }

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AudioListFragment.newInstance(AudioListFragment.AudioListType.ALL, query)
            else -> AudioListFragment.newInstance(AudioListFragment.AudioListType.FAVORITES, query)
        }
    }
}