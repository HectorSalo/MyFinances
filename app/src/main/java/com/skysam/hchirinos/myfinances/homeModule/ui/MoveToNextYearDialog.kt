package com.skysam.hchirinos.myfinances.homeModule.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenter

class MoveToNextYearDialog(private val year: Int, private val homePresenter: HomePresenter): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(R.string.dialog_title))
                .setMessage(getString(R.string.dialog_message))
                .setPositiveButton(R.string.dialog_btn_mover, DialogInterface.OnClickListener { _, _ ->
                    moverDatos()
                })
                .setNegativeButton(R.string.btn_cancelar, null)

        return builder.create()
    }

    private fun moverDatos() {
        //homePresenter.
    }
}