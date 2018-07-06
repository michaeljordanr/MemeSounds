package com.michaeljordanr.memesounds;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.RecyclerViewHolder> implements
        Filterable{
    private AudioFilter filter;
    private List<Audio> audioList;
    private List<Audio> audioFilteredList;
    private Context context;
    private RecyclerAdapterOnClickListener callbackOnClick;
    private RecyclerAdapterOnLongListener callbackOnLongClick;
    private int lastPosition = -1;

    @Override
    public Filter getFilter() {
        if(filter == null) {
            filter = new AudioFilter(audioFilteredList, this);
        }
        return filter;
    }

    public interface RecyclerAdapterOnClickListener{
        void onClick(Audio audio);
    }

    public interface RecyclerAdapterOnLongListener{
        void onLongClick(Audio audio);
    }

    public AudioAdapter(Context context, RecyclerAdapterOnClickListener callbackOnClick,
                        RecyclerAdapterOnLongListener callbackOnLongClick){
        this.context = context;
        this.callbackOnClick = callbackOnClick;
        this.callbackOnLongClick = callbackOnLongClick;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int idLayoutForListItem = R.layout.item_recyclerview;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(idLayoutForListItem, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        Audio audio = audioList.get(position);

        holder.audioButton.setTag(audio.getAudioName());

        if(!audio.getAudioThumb().isEmpty()) {
            holder.audioButton.setBackgroundResource(Utils.getDrawableId(context, audio.getAudioThumb()));

        }else{
            holder.audioButton.setText(audio.getAudioDescription());
        }

        setAnimation(holder.itemView, position);

    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            animation.setDuration(500);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerViewHolder holder) {
        holder.clearAnimation();
    }

    @Override
    public int getItemCount() {
        if(audioList == null) return 0;
        return audioList.size();
    }

    public void setData(List<Audio> audioList){
        this.audioList = audioList;
        this.audioFilteredList = audioList;
        notifyDataSetChanged();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener{

        @BindView(R.id.bt_sound)
        Button audioButton;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            audioButton.setOnClickListener(this);
            audioButton.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            callbackOnClick.onClick(audioList.get(position));
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            callbackOnLongClick.onLongClick(audioList.get(position));
            return true;
        }

        public void clearAnimation()
        {
            itemView.clearAnimation();
        }
    }

}
