package com.webmy.core_sdk.presentation.adapters.subscriptions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.webmy.core_sdk.databinding.ListItemSubscriptionBinding

class PlansAdapter(
    private val callback: Callback
) : androidx.recyclerview.widget.ListAdapter<SubscriptionsUiModel, PlanViewHolder>(PlanDiffCallback()) {

    interface Callback {
        fun onItemClick(id: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val binding = ListItemSubscriptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PlanViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PlanViewHolder(
    private val binding: ListItemSubscriptionBinding,
    private val callback: PlansAdapter.Callback
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SubscriptionsUiModel) {
        binding.apply {
            tvTitle.text = item.title
            tvPrice.text = item.formattedPrice
            tvWeekPrice.text = item.formattedPriceWeek
            tvTrial.isVisible = item.freeFormatted != null
            tvTrial.text = item.freeFormatted

            clNestedRoot.alpha = if (item.isSelected) 1f else 0.4f
            root.setOnClickListener {
                callback.onItemClick(item.productId)
            }
        }
    }
}

private class PlanDiffCallback : DiffUtil.ItemCallback<SubscriptionsUiModel>() {
    override fun areItemsTheSame(
        oldItem: SubscriptionsUiModel,
        newItem: SubscriptionsUiModel
    ): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(
        oldItem: SubscriptionsUiModel,
        newItem: SubscriptionsUiModel
    ): Boolean {
        return oldItem == newItem
    }
}
