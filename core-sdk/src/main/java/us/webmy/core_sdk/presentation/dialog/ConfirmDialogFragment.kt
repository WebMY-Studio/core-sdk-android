package us.webmy.core_sdk.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import us.webmy.core_sdk.databinding.FragmentDialogConfirmBinding

class ConfirmDialogFragment(
    private val title: String,
    private val description: String,
    private val ctaCancel: String,
    private val ctaConfirm: String,
    private val onConfirm: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentDialogConfirmBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDialogConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = title
        binding.tvDescription.text = description
        binding.btnCancel.text = ctaCancel
        binding.btnConfirm.text = ctaConfirm

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnConfirm.setOnClickListener {
            onConfirm()
            dismiss()
        }
    }
}