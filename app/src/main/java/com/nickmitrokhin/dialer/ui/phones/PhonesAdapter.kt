package com.nickmitrokhin.dialer.ui.phones

import android.view.*
import com.nickmitrokhin.dialer.R
import com.nickmitrokhin.dialer.ui.common.RecyclerViewBaseAdapter
import com.nickmitrokhin.dialer.databinding.PhoneItemBinding

class PhonesAdapter(
    private val itemClickHandler: (String) -> Unit,
) : RecyclerViewBaseAdapter<PhoneItemBinding, String>() {
    class PhoneViewHolder(
        binding: PhoneItemBinding,
        clickHandler: (Int) -> Unit
    ) : ViewHolder<PhoneItemBinding, String>(binding, clickHandler), View.OnCreateContextMenuListener {

        init {
            binding.root.setOnCreateContextMenuListener(this)
        }

        override fun bind(item: String) {
            with(viewBinding) {
                txtPhone.text = item
                ivPhone.setImageResource(R.drawable.ic_phone)
            }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            view: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.add(
                Menu.NONE,
                R.id.send_sms,
                Menu.NONE, R.string.send_sms
            )
        }
    }

    private var _position: Int = -1
    val position: Int
        get() = _position

    override fun createViewHolderCore(parent: ViewGroup, viewType: Int): PhoneViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PhoneItemBinding.inflate(inflater, parent, false)
        return PhoneViewHolder(binding) { position ->
            itemClickHandler(dataItems[position])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder<PhoneItemBinding, String>, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.setOnLongClickListener {
            _position = holder.adapterPosition
            false
        }
    }

    override fun onViewRecycled(holder: ViewHolder<PhoneItemBinding, String>) {
        holder.itemView.setOnLongClickListener(null)
        super.onViewRecycled(holder)
    }
}