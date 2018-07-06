package com.michaeljordanr.memesounds;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

public class AudioFilter extends Filter {

    private List<Audio> filterAudioList;
    private AudioAdapter adapter;

    public AudioFilter(List<Audio> filterAudioList, AudioAdapter adapter){
        this.filterAudioList = filterAudioList;
        this.adapter = adapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if(constraint != null && constraint.length() > 0){
            constraint = constraint.toString().toLowerCase();

            List<Audio> filteredAudioList = new ArrayList<>();

            for(Audio item : filterAudioList){
                if(item.getAudioDescription().toLowerCase().contains(constraint)){
                    filteredAudioList.add(item);
                }
            }
            results.count = filteredAudioList.size();
            results.values = filteredAudioList;
        }else{
            results.count = filterAudioList.size();
            results.values = filterAudioList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.setData((List<Audio>)results.values);
        adapter.notifyDataSetChanged();
    }
}
