package io.github.johnondrej.backgroundlocation.presentation

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import io.github.johnondrej.backgroundlocation.R

/**
 * [AlertDialog] with custom message.
 */
class MessageDialog : DialogFragment() {

    private val messageRes: Int
        get() = requireArguments().getInt(KEY_MESSAGE_RES)

    private val requestCode: Int
        get() = requireArguments().getInt(KEY_REQUEST_CODE)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setMessage(messageRes)
            .setPositiveButton(R.string.general_ok) { _, _ ->
                (activity as? DialogConfirmationListener)?.onDialogConfirmed(requestCode)
            }
            .create()
    }

    companion object {

        private const val KEY_MESSAGE_RES = "message_res"
        private const val KEY_REQUEST_CODE = "request_code"

        fun newInstance(@StringRes messageRes: Int, requestCode: Int) = MessageDialog().apply {
            arguments = bundleOf(
                KEY_MESSAGE_RES to messageRes,
                KEY_REQUEST_CODE to requestCode
            )
        }
    }

    interface DialogConfirmationListener {

        fun onDialogConfirmed(requestCode: Int)
    }
}