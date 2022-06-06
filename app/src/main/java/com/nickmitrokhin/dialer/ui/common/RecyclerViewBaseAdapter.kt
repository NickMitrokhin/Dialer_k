package com.nickmitrokhin.dialer.ui.common

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class RecyclerViewBaseAdapter<T : ViewBinding, I>: RecyclerView.Adapter<RecyclerViewBaseAdapter.ViewHolder<T, I>>() {
    abstract class ViewHolder<T : ViewBinding, I>(
        binding: T,
        clickHandler: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        protected val viewBinding = binding
        protected val itemClickHandler = clickHandler

        init {
            viewBinding.root.setOnClickListener(this)
        }

        abstract fun bind(item: I)

        override fun onClick(v: View) {
            itemClickHandler(adapterPosition)
        }
    }

    var dataItems: List<I> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T, I> {
        return createViewHolderCore(parent, viewType)
    }

    protected abstract fun createViewHolderCore(parent: ViewGroup, viewType: Int): ViewHolder<T, I>

    override fun onBindViewHolder(holder: ViewHolder<T, I>, position: Int) {
        val contact = dataItems[position]
        holder.bind(contact)
    }

    override fun getItemCount(): Int = dataItems.size
}