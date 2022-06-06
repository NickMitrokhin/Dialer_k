package com.nickmitrokhin.dialer.ui.dialer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.nickmitrokhin.dialer.R
import com.nickmitrokhin.dialer.databinding.FragmentDialerBinding
import com.nickmitrokhin.dialer.ui.common.ViewModelFactory
import com.nickmitrokhin.dialer.ui.common.bindWithLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.job
import kotlin.coroutines.coroutineContext

class DialerFragment: Fragment() {
    private var _binding: FragmentDialerBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DialerViewModel
    private var phoneNumber: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {
            phoneNumber = it.getString("phone")
        }
        viewModel = ViewModelProvider(
            owner = this,
            factory = ViewModelFactory(this, requireActivity().applicationContext)
        )[DialerViewModel::class.java]
        viewModel.setActivityContextCallback {
            requireActivity()
        }
        viewModel.setPendingIntentCallback { requestCode, intent, flags ->
            requireActivity().createPendingResult(requestCode, intent, flags)
        }

        initBinding(inflater, container)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindModelState()
    }

    private fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) {
        _binding = FragmentDialerBinding.inflate(inflater, container, false)
    }

    private fun bindModelState() {
        bindWithLifecycle(Lifecycle.State.STARTED) {
            val currentJob = coroutineContext.job
            viewModel.uiState.collect { state ->
                with(binding) {
                    indicatorTimeout.beginUpdate()
                    if (state.timeout > 0.toUShort()
                        && state.currentTimeout > 0.toUShort()
                        && indicatorTimeout.visibility == View.GONE) {
                        indicatorTimeout.max = state.timeout.toInt()
                    }
                    indicatorTimeout.value = state.currentTimeout.toInt()
                    indicatorTimeout.visibility = if (state.dialCount == state.currentDialCount
                        && state.currentDialCount > 0.toUShort()) View.GONE else View.VISIBLE
                    indicatorTimeout.endUpdate()

                    txtAttemptCount.text = "${getString(R.string.text_attempts_prefix)} ${state.currentDialCount}"

                    if (state.finished) {
                        findNavController().popBackStack()
                        currentJob.cancel()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.action(UIAction.Start(phoneNumber!!))
    }

    override fun onResume() {
        super.onResume()
        viewModel.action(UIAction.Resume)
    }

    override fun onPause() {
        viewModel.action(UIAction.Pause)
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.action(UIAction.PendingResult)
    }
}