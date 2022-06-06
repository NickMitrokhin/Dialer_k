package com.nickmitrokhin.dialer.ui.phones

import android.view.LayoutInflater
import android.view.ViewGroup
import com.nickmitrokhin.dialer.R
import com.nickmitrokhin.dialer.ui.common.RecyclerViewBaseAdapter
import com.nickmitrokhin.dialer.databinding.PhoneItemBinding

class PhonesAdapter(
    private val itemClickHandler: (String) -> Unit
) : RecyclerViewBaseAdapter<PhoneItemBinding, String>() {
    class PhoneViewHolder(
        binding: PhoneItemBinding,
        clickHandler: (Int) -> Unit
    ) : ViewHolder<PhoneItemBinding, String>(binding, clickHandler) {
        override fun bind(item: String) {
            with(viewBinding) {
                txtPhone.text = item
                ivPhone.setImageResource(R.drawable.ic_phone)
            }
        }
    }

    override fun createViewHolderCore(parent: ViewGroup, viewType: Int): PhoneViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PhoneItemBinding.inflate(inflater, parent, false)
        return PhoneViewHolder(binding) { position ->
            itemClickHandler(dataItems[position])
        }
    }
}