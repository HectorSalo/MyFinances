package com.skysam.hchirinos.myfinances.homeModule.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class SectionPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment.newInstance()
            1 -> DetallesHomeFragment.newInstance()
            else -> HomeFragment.newInstance()
        }
    }
}