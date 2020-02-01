package com.michaeljordanr.memesounds

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.flurry.android.FlurryAgent
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.FirebaseAnalytics
import com.microsoft.appcenter.analytics.Analytics


class MainActivity : AppCompatActivity(), AudioListFragment.AudioListFragmentListener {

    var selectedTab = AudioListFragment.AudioListType.ALL
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    lateinit var viewPager: ViewPager2
    lateinit var tabs: TabLayout

    private val viewPagerAdapter by lazy {
        ViewPagerAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setTitle(R.string.app_name)
        }

        viewPager = findViewById(R.id.view_pager)
        tabs = findViewById(R.id.tab_layout)

        viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(tabs, viewPager,
                TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                    when (position) {
                        0 -> {
                            tab.text = getString(R.string.all)
                        }
                        1 -> {
                            tab.text = getString(R.string.bookmarks)
                        }
                    }
                }).attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectedTab = if (position == AudioListFragment.AudioListType.FAVORITES.value) {
                    AudioListFragment.AudioListType.FAVORITES
                } else {
                    AudioListFragment.AudioListType.ALL
                }
            }
        })

        val openAppEvent = "open_app"
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseAnalytics.logEvent(openAppEvent, null)

        FlurryAgent.logEvent(openAppEvent)
        Analytics.trackEvent(openAppEvent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.search)

        val searchView = searchItem.actionView as SearchView
        searchView.isFocusable = false
        searchView.queryHint = getText(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                viewPagerAdapter.setQuery(query)
                viewPager.adapter = viewPagerAdapter
                goToPage(selectedTab)

                val params = Bundle()
                val logFilter = "filter"
                params.putString("query", query)
                firebaseAnalytics.logEvent(logFilter, params)

                val logParams = HashMap<String, String>()
                logParams["query"] = query

                FlurryAgent.logEvent(logFilter, logParams)
                Analytics.trackEvent(logFilter, logParams)

                return false
            }
        })
        return true
    }

    companion object {
        const val JSON_CONFIG_PATH = "config.json"
    }

    override fun goToPage(audioListType: AudioListFragment.AudioListType) {
        if (audioListType == AudioListFragment.AudioListType.FAVORITES) {
            viewPager.currentItem = 1
        } else {
            viewPager.currentItem = 0
        }
        viewPager.adapter = viewPagerAdapter
    }
}
