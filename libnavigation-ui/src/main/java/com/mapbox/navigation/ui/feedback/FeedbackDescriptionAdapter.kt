package com.mapbox.navigation.ui.feedback

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.libnavigation.ui.R

/**
 * FeedbackDescriptionAdapter provides a binding from [FeedbackBottomSheet] data set
 * to [FeedbackViewHolder] that are displayed within a [RecyclerView].
 */
internal class FeedbackDescriptionAdapter internal constructor(
    private val feedbackDescriptionItems: List<FeedbackDescriptionItem>,
    private val itemClickListener: OnDescriptionItemClickListener
) : RecyclerView.Adapter<FeedbackDescriptionViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FeedbackDescriptionViewHolder {
        return FeedbackDescriptionViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.mapbox_feedback_detail_viewholder_layout, parent, false),
            itemClickListener
        )
    }

    override fun onBindViewHolder(
        holder: FeedbackDescriptionViewHolder,
        position: Int
    ) {
        holder.setFeedbackIssueDetail(feedbackDescriptionItems[position])
    }

    override fun getItemCount(): Int {
        return feedbackDescriptionItems.size
    }

    fun getFeedbackDescriptionItem(position: Int): FeedbackDescriptionItem {
        return feedbackDescriptionItems[position]
    }

    internal interface OnDescriptionItemClickListener {
        fun onItemClick(position: Int): Boolean
    }
}
