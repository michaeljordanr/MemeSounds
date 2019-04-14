package com.michaeljordanr.memesounds

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable

class AudioAdapter(private var context: Context, private val callbackOnClick: RecyclerAdapterOnClickListener,
                   private val callbackOnLongClick: RecyclerAdapterOnLongListener) : RecyclerView.Adapter<AudioAdapter.RecyclerViewHolder>(), Filterable {
    private var filter: AudioFilter? = null
    private var audioList: List<Audio>? = null
    private var audioFilteredList: List<Audio>? = null
    private var lastPosition = -1

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = AudioFilter(audioFilteredList!!, this)
        }
        return filter as AudioFilter
    }

    interface RecyclerAdapterOnClickListener {
        fun onClick(audio: Audio)
    }

    interface RecyclerAdapterOnLongListener {
        fun onLongClick(audio: Audio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        context = parent.context
        val idLayoutForListItem = R.layout.item_recyclerview
        val inflater = LayoutInflater.from(context)

        val view = inflater.inflate(idLayoutForListItem, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val audio = audioList!![position]

        holder.audioButton.tag = audio.audioName

        if (audio.audioThumb.isNotEmpty()) {
            holder.audioButton.setBackgroundResource(Utils.getDrawableId(context, audio.audioThumb))

        } else {
            holder.audioButton.text = audio.audioDescription
        }

        setAnimation(holder.itemView, position)

    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            animation.duration = 500
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerViewHolder) {
        holder.clearAnimation()
    }

    override fun getItemCount(): Int {
        return if (audioList == null) 0 else audioList!!.size
    }

    fun setData(audioList: List<Audio>) {
        this.audioList = audioList
        this.audioFilteredList = audioList
        notifyDataSetChanged()
    }

    inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val audioButton: Button = itemView.findViewById(R.id.bt_sound)

        init {
            audioButton.setOnClickListener(this)
            audioButton.setOnLongClickListener(this)
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            callbackOnClick.onClick(audioList!![position])
        }

        override fun onLongClick(v: View): Boolean {
            val position = adapterPosition
            callbackOnLongClick.onLongClick(audioList!![position])
            return true
        }

        fun clearAnimation() {
            itemView.clearAnimation()
        }
    }

}
