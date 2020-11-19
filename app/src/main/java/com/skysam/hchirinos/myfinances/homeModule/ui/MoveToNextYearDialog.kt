package com.skysam.hchirinos.myfinances.homeModule.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.databinding.DialogMoveNextYearBinding
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenter

class MoveToNextYearDialog(private val year: Int, private val homePresenter: HomePresenter): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialogMoveNextYearBinding = DialogMoveNextYearBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(R.string.dialog_title))
                .setView(dialogMoveNextYearBinding.root)
                .setPositiveButton(R.string.dialog_btn_mover, null)
                .setNegativeButton(R.string.btn_cancelar, null)

        val dialog = builder.create()
        dialog.show()

        val buttonNegative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        val buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        buttonPositive.setOnClickListener {
            dialogMoveNextYearBinding.lottieAnimationView.visibility = View.VISIBLE
            buttonNegative.visibility = View.INVISIBLE
            buttonPositive.visibility = View.INVISIBLE
            homePresenter.moveDataNextYear(year)
        }

        return dialog
    }
}