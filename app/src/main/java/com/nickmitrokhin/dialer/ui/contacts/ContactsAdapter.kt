package com.nickmitrokhin.dialer.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.nickmitrokhin.dialer.R
import com.nickmitrokhin.dialer.ui.common.RecyclerViewBaseAdapter
import com.nickmitrokhin.dialer.databinding.ContactItemBinding
import com.nickmitrokhin.dialer.domain.models.Contact

class ContactsAdapter(
    private val itemClickHandler: (Contact) -> Unit
) : RecyclerViewBaseAdapter<ContactItemBinding, Contact>() {
    class ContactViewHolder(
        binding: ContactItemBinding,
        clickHandler: (Int) -> Unit
    ) : ViewHolder<ContactItemBinding, Contact>(binding, clickHandler) {
        override fun bind(item: Contact) {
            with(viewBinding) {
                txtContactName.text = item.name

                if (item.photo.isNotBlank()) {
                    Glide.with(ivContactPhoto.context)
                        .load(item.photo)
                        .circleCrop()
                        .placeholder(R.drawable.ic_default_contact)
                        .error(R.drawable.ic_default_contact)
                        .into(ivContactPhoto)
                } else {
                    ivContactPhoto.setImageResource(R.drawable.ic_default_contact)
                }
            }
        }
    }

    override fun createViewHolderCore(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ContactItemBinding.inflate(inflater, parent, false)
        return ContactViewHolder(binding) { position ->
            itemClickHandler(dataItems[position])
        }
    }
}