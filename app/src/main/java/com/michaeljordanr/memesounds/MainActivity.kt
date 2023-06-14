package com.michaeljordanr.memesounds

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task


class MainActivity : AppCompatActivity(), AudioListFragment.AudioListFragmentListener {

    companion object {
        const val JSON_CONFIG_PATH = "config.json"
        const val APP_UPDATE_REQUEST_CODE = 1137
    }

    var selectedTab = AudioListFragment.AudioListType.ALL

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

        checkUpdate()
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

                val params = HashMap<String, String>()
                params["query"] = query

                return false
            }
        })
        return true
    }

    override fun goToPage(audioListType: AudioListFragment.AudioListType) {
        if (audioListType == AudioListFragment.AudioListType.FAVORITES) {
            viewPager.currentItem = 1
        } else {
            viewPager.currentItem = 0
        }
        viewPager.adapter = viewPagerAdapter
    }

    private fun checkUpdate() {
        // Creates instance of the manager.
        val appUpdateManager = AppUpdateManagerFactory.create(baseContext)

        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // For a flexible update, use AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                // Request the update.
                appUpdateManager.startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                        AppUpdateType.FLEXIBLE,
                        // The current activity making the update request.
                        this,
                        // Include a request code to later monitor this update request.
                        APP_UPDATE_REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == APP_UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(baseContext, "Por que você fez isso, sua lambisgóia?!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
