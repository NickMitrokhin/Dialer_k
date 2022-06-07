package com.nickmitrokhin.dialer.ui.phones


import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.nickmitrokhin.dialer.R
import com.nickmitrokhin.dialer.databinding.FragmentPhonesBinding
import com.nickmitrokhin.dialer.ui.common.ViewModelFactory
import com.nickmitrokhin.dialer.ui.common.bindWithLifecycle
import kotlinx.coroutines.flow.collect

class PhonesFragment : Fragment() {
    private var _binding: FragmentPhonesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PhonesViewModel
    private var contactID: String? = null
    private var contactName: String? = null
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {
            contactID = it.getString("contactID")
            contactName = it.getString("contactName")
        }
        viewModel = ViewModelProvider(
            owner = this,
            factory = ViewModelFactory(this, requireActivity())
        )[PhonesViewModel::class.java]

        initBinding(inflater, container)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        bindPhoneList()
        if(contactID != null) {
            viewModel.action(UIAction.Contact(id = contactID!!))
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.send_sms) {
            val adapter = binding.phoneList.adapter as PhonesAdapter
            val position = adapter.position

            if (position >= 0) {
                val phoneNumber = adapter.dataItems[position]
                val bodySMS = "${contactName}: $phoneNumber"

                viewModel.action(UIAction.CreateSms(bodySMS))
            }
        }

        return super.onContextItemSelected(item)
    }

    private fun initViewTitle() {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = contactName
    }

    override fun onResume() {
        super.onResume()
        initViewTitle()
    }

    private fun bindPhoneList() {
        registerForContextMenu(binding.phoneList)
        bindWithLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiState.collect { value ->
                (binding.phoneList.adapter as PhonesAdapter).dataItems = value.phones
            }
        }
    }

    private fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) {
        _binding = FragmentPhonesBinding.inflate(inflater, container, false)
        initPhoneList()
    }

    private fun initPhoneList() {
        binding.phoneList.adapter = PhonesAdapter { phone ->
            navController.navigate(R.id.action_nav_phones_to_nav_dialer, bundleOf(
                "phone" to phone
            ))
        }
    }

    private fun resetBinding() {
        _binding = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetBinding()
    }
}