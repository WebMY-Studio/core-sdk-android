package us.webmy.core_sdk.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import us.webmy.core_sdk.databinding.FragmentDialogTextInputBinding

class TextInputDialogFragment(
    private val title: String,
    private val description: String,
    private val hint: String,
    private val suggestion: String = "",
    private val ctaConfirm: String,
    private val dismissOnOk: Boolean = true,
    private val onSave: (BottomSheetDialogFragment, String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentDialogTextInputBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDialogTextInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.apply {
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        binding.etInput.setText(suggestion)
        binding.etInput.hint = hint
        binding.tvTitle.text = title
        binding.tvDescription.text = description
        binding.btnConfirm.text = ctaConfirm

        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener {
            val name = binding.etInput.text.toString().trim()
            onSave(this, name.ifEmpty { suggestion })
            if (dismissOnOk) dismiss()
        }
    }
}