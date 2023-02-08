package com.dev.smsspamdetector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

class Installation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installation)
        val vpPager : ViewPager = findViewById(R.id.pager)
        val adapterViewPager = SwipAdapter(supportFragmentManager)
        vpPager.adapter = adapterViewPager
        val tabLayout : TabLayout = findViewById(R.id.dots)
        tabLayout.setupWithViewPager(vpPager, true)
    }
}