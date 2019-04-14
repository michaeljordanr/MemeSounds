package com.michaeljordanr.memesounds

import android.widget.Filter

class AudioFilter(private val filterAudioList: List<Audio>, private val adapter: AudioAdapter) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var query = constraint
        val results = FilterResults()

        if (query != null && query.isNotEmpty()) {
            query = query.toString().toLowerCase()

            val filteredAudioList = ArrayList<Audio>()

            for (item in filterAudioList) {
                if (item.audioDescription.toLowerCase().contains(query)) {
                    filteredAudioList.add(item)
                }
            }
            results.count = filteredAudioList.size
            results.values = filteredAudioList
        } else {
            results.count = filterAudioList.size
            results.values = filterAudioList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        adapter.setData((results.values as List<*>).filterIsInstance<Audio>())
    }
}
