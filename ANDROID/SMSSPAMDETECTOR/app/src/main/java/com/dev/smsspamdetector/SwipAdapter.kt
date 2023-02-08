package com.dev.smsspamdetector


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


class SwipAdapter(fm : FragmentManager): FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Fragment {
        return when(position){
            0-> step1()
            1 -> step2()
            else -> step3()
        }

    }


}