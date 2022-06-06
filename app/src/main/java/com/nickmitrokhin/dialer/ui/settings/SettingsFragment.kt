package com.nickmitrokhin.dialer.ui.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.nickmitrokhin.dialer.ui.common.InputFilterMinMax
import com.nickmitrokhin.dialer.ui.common.bindWithLifecycle
import com.nickmitrokhin.dialer.databinding.FragmentSettingsBinding
import com.nickmitrokhin.dialer.ui.common.ViewModelFactory
import kotlinx.coroutines.flow.collect

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(
                owner = this,
                factory = ViewModelFactory(this, requireActivity().applicationContext)
            )[SettingsViewModel::class.java]

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
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        initEditors()
        initSaveButton()
    }

    private fun initEditors() {
        with(binding) {
            with(dialCount) {
                filters = arrayOf(InputFilterMinMax(1, 20))
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        viewModel.action(
                            UIAction.DialCountChange(
                                value = getUShortEditTextValue(p0)
                            )
                        )
                    }

                    override fun afterTextChanged(p0: Editable?) {}
                })
            }

            with(timeout) {
                filters = arrayOf(InputFilterMinMax(1, 30))
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        viewModel.action(
                            UIAction.TimeoutChange(
                                value = getUShortEditTextValue(p0)
                            )
                        )
                    }

                    override fun afterTextChanged(p0: Editable?) {}
                })
            }
        }
    }

    private fun initSaveButton() {
        binding.save.setOnClickListener {
            viewModel.saveState()
        }
    }

    private fun bindModelState() {
        bindWithLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiState.collect { state ->
                bindEditText(binding.dialCount, state.dialCount)
                bindEditText(binding.timeout, state.timeout)
            }
        }
    }

    private fun resetBinding() {
        _binding = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetBinding()
    }

    private companion object {
        private const val DEFAULT_MIN_VALUE: UShort = 1u

        private fun bindEditText(textView: EditText, value: UShort) {
            val textViewValue = getUShortEditTextValue(textView.text)

            if (textViewValue != value) {
                val textValue = value.toString()

                textView.setText(textValue)
                textView.setSelection(textValue.length)
            }
        }

        private fun getUShortEditTextValue(txt: CharSequence?): UShort {
            val text = txt.toString()
            return if (text.isEmpty()) DEFAULT_MIN_VALUE else text.toUShort()
        }
    }
}