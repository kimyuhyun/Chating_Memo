package com.hongslab.chating_memo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hongslab.chating_memo.models.GlobalVO
import com.hongslab.chating_memo.models.MyViewType
import com.hongslab.chating_memo.BR


class GlobalAdapter(
    private val headID: Int,
    private val bodyID: Int,
    private val footID: Int,
    private val br: Int,
    private val onGlobalAdapterClickListener: OnGlobalAdapterClickListener?,
    private val brs: HashMap<Int, OnGlobalAdapterClickListener>
) : ListAdapter<GlobalVO, GlobalAdapter.GlobalViewHolder>(diffUtil) {

    private val HEADER: Int = 0
    private val BODY: Int = 1
    private val FOOTER: Int = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GlobalViewHolder {
        val layoutID = when (viewType) {
            HEADER -> headID
            BODY -> bodyID
            else -> footID
        }
        val binding: ViewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutID, parent, false)
        return GlobalViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        val item = currentList[position]
        return when (item.viewType) {
            MyViewType.HEADER -> HEADER
            MyViewType.BODY -> BODY
            else -> FOOTER
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun onBindViewHolder(holder: GlobalViewHolder, position: Int) {
        val item = currentList[position]

        // 바인딩 변수 설정
        holder.viewDataBinding.setVariable(br, item)

        // 추가 리스너 설정
        for ((key, value) in brs) {
            holder.viewDataBinding.setVariable(key, value)
        }

        // 클릭 리스너 설정
        holder.viewDataBinding.setVariable(BR.click, View.OnClickListener {
            onGlobalAdapterClickListener?.onGlobalAdapterItemClick(it, item, holder.adapterPosition)
        })

        holder.viewDataBinding.executePendingBindings()
    }

    inner class GlobalViewHolder(val viewDataBinding: ViewDataBinding) : RecyclerView.ViewHolder(viewDataBinding.root)

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<GlobalVO>() {
            override fun areItemsTheSame(oldItem: GlobalVO, newItem: GlobalVO): Boolean {
                return oldItem.uid == newItem.uid
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: GlobalVO, newItem: GlobalVO): Boolean {
                return oldItem == newItem
            }
        }
    }
}
