package com.nickmitrokhin.dialer.ui.contacts

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nickmitrokhin.dialer.ui.common.PermissionHelper
import com.nickmitrokhin.dialer.R
import com.nickmitrokhin.dialer.ui.common.bindWithLifecycle
import com.nickmitrokhin.dialer.databinding.FragmentContactsBinding
import com.nickmitrokhin.dialer.ui.common.ViewModelFactory
import kotlinx.coroutines.flow.collect


class ContactsFragment : Fragment() {
    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ContactsViewModel
    private lateinit var navController: NavController
    private val permissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CALL_LOG
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            owner = this,
            factory = ViewModelFactory(this, requireActivity().applicationContext)
        )[ContactsViewModel::class.java]

        setHasOptionsMenu(true)
        initBinding(inflater, container)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        checkContactListAccess()
    }

    private fun checkContactListAccess() {
        val context = requireContext()
        val requirePermissions = arrayListOf<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requirePermissions.add(permission)
            }
        }
        if (requirePermissions.isEmpty()) {
            bindModelState()
        } else {
            PermissionHelper.requestPermissions(
                this,
                requirePermissions.toTypedArray()
            ) { granted ->
                if (granted) {
                    bindModelState()
                }
            }
        }
    }

    private fun bindModelState() {
        bindWithLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiState.collect { state ->
                with(binding) {
                    searchLayout.visibility = if (state.searchEnabled) View.VISIBLE else View.GONE

                    if (state.searchQuery != contactSearch.text.toString()) {
                        contactSearch.setText(state.searchQuery)
                        contactSearch.setSelection(state.searchQuery.length)
                    }

                    (binding.contactList.adapter as ContactsAdapter).dataItems = state.contacts

                    if (state.scrollPosition != (contactList.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()) {
                        contactList.scrollToPosition(state.scrollPosition)
                    }
                }
            }
        }
    }

    private fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        initContactList()
        initContactSearch()
    }

    private fun initContactList() {
        with(binding) {
            contactList.adapter = ContactsAdapter { contact ->
                val bundle = bundleOf(
                    "contactID" to contact.id,
                    "contactName" to contact.name
                )
                navController.navigate(R.id.action_nav_contacts_to_nav_phones, bundle)
            }
            contactList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val itemPosition =
                        (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    viewModel.action(UIAction.Scroll(position = itemPosition))
                }
            })
        }
    }

    private fun initContactSearch() {
        binding.contactSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.action(UIAction.Search(query = p0.toString()))
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun resetBinding() {
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val item = menu.findItem(R.id.search_contacts)
        item.isChecked = viewModel.uiState.value.searchEnabled
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search_contacts -> {
                item.isChecked = !item.isChecked
                viewModel.action(UIAction.SearchEnabled(item.isChecked))
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetBinding()
    }
}