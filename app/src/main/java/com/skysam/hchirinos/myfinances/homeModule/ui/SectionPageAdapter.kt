package com.skysam.hchirinos.myfinances.homeModule.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SectionPageAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment.newInstance()
            1 -> CronologiaFragment.newInstance()
            else -> HomeFragment.newInstance()
        }
    }

}