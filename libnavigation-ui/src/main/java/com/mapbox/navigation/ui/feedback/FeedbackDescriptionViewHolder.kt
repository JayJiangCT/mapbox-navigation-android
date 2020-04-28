package com.mapbox.navigation.ui.feedback

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.libnavigation.ui.R
import com.mapbox.navigation.ui.feedback.FeedbackDescriptionAdapter.OnDescriptionItemClickListener

internal class FeedbackDescriptionViewHolder(
    itemView: View,
    private val itemClickListener: OnDescriptionItemClickListener?
) : RecyclerView.ViewHolder(itemView),
    View.OnClickListener {

    private val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
    private val feedbackIssueDetail: TextView = itemView.findViewById(R.id.feedbackIssueDetail)

    init {
        itemView.setOnClickListener(this)
        checkBox.setOnClickListener(this)
    }

    fun setFeedbackIssueDetail(item: FeedbackDescriptionItem) {
        checkBox.isChecked = item.isChecked
        feedbackIssueDetail.setText(item.feedbackDescriptionResourceId)
    }

    override fun onClick(view: View) {
        if (itemClickListener != null) {
            checkBox.isChecked = itemClickListener.onItemClick(adapterPosition)
        }
    }
}
