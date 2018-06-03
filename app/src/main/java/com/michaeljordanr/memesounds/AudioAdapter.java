package com.michaeljordanr.memesounds;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.RecyclerViewHolder> {
    private List<Audio> audioList;
    private Context context;
    private SoundManager soundManager;
    private RecyclerAdapterOnClickListener callbackOnClick;
    private RecyclerAdapterOnLongListener callbackOnLongClick;

    public interface RecyclerAdapterOnClickListener{
        void onClick(Audio audio);
    }

    public interface RecyclerAdapterOnLongListener{
        void onLongClick(Audio audio);
    }

    public AudioAdapter(Context context, RecyclerAdapterOnClickListener callbackOnClick,
                        RecyclerAdapterOnLongListener callbackOnLongClick,
                        SoundManager soundManager){
        this.context = context;
        this.callbackOnClick = callbackOnClick;
        this.callbackOnLongClick = callbackOnLongClick;
        this.soundManager = soundManager;
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

        soundManager.load(Utils.getRawId(context, audio.getAudioName()));
    }

    @Override
    public int getItemCount() {
        if(audioList == null) return 0;
        return audioList.size();
    }

    public void setData(List<Audio> audioList){
        this.audioList = audioList;
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
    }

}
