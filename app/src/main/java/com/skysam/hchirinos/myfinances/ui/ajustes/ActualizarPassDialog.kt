package com.skysam.hchirinos.myfinances.ui.ajustes

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.databinding.DialogModificarPassBinding

class ActualizarPassDialog : DialogFragment() {

    private var _dialogModificarPassBinding : DialogModificarPassBinding? = null
    private val dialogModificarPassBinding get() = _dialogModificarPassBinding!!
    private val user = FirebaseAuth.getInstance().currentUser
    private var button : Button? = null
    private var passNuevo : Boolean = false
    private var dialog : AlertDialog? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _dialogModificarPassBinding = DialogModificarPassBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.text_pass_viejo))
                .setView(dialogModificarPassBinding.root)
                .setPositiveButton(getString(R.string.text_validar), null)
                .setNegativeButton(getString(R.string.btn_cancelar), null)

        dialog = builder.create()
        dialog?.show()
        button = dialog?.getButton(DialogInterface.BUTTON_POSITIVE)
        button?.setOnClickListener {
            dialogModificarPassBinding.inputPass.error = null
            dialogModificarPassBinding.inputRepetirPass.error = null
            if (passNuevo) {
                validarPassNuevo()
            } else {
                validarPassOld()
            }
        }
        return dialog as AlertDialog
    }

    private fun validarPassOld() {
        var email = ""
        val pass = dialogModificarPassBinding.etPass.text.toString()
        user?.let {
            for (profile in it.providerData) {
                email = profile.email.toString()
            }
        }
        if (pass.isNullOrEmpty()) {
            dialogModificarPassBinding.inputPass.error = getString(R.string.error_campo_vacio)
            return
        }

        dialogModificarPassBinding.progressBar.visibility = View.VISIBLE
        button?.hideKeyboard()
        val credential = EmailAuthProvider.getCredential(email, pass)
        user?.reauthenticate(credential)?.addOnSuccessListener {
            dialogModificarPassBinding.progressBar.visibility = View.GONE
            dialogModificarPassBinding.inputRepetirPass.visibility = View.VISIBLE
            dialogModificarPassBinding.etPass.setText("")
            button?.text = getString(R.string.btn_actualizar)
            dialog?.setTitle(getString(R.string.text_pass_nuevo))
            passNuevo = true
        }?.addOnFailureListener {
            dialogModificarPassBinding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), getString(R.string.error_pass_code), Toast.LENGTH_LONG).show()
        }
    }

    private fun validarPassNuevo() {
        var passNuevo = dialogModificarPassBinding.etPass.text.toString()
        var passNuevoRepetir = dialogModificarPassBinding.etRepetirPass.text.toString()
        if (passNuevo.isNullOrEmpty()) {
            dialogModificarPassBinding.inputPass.error = getString(R.string.error_campo_vacio)
            return
        }
        if (passNuevoRepetir.isNullOrEmpty()) {
            dialogModificarPassBinding.inputRepetirPass.error = getString(R.string.error_campo_vacio)
            return
        }
        if (passNuevo != passNuevoRepetir) {
            dialogModificarPassBinding.inputRepetirPass.error = getString(R.string.error_pass_match)
            return
        }

        dialogModificarPassBinding.progressBar.visibility = View.VISIBLE
        button?.hideKeyboard()

        user?.updatePassword(passNuevo)?.addOnSuccessListener {
            dialogModificarPassBinding.progressBar.visibility = View.VISIBLE
            Toast.makeText(requireContext(), getString(R.string.actualizar_pass_ok), Toast.LENGTH_LONG).show()
            dialog?.dismiss()
        }?.addOnFailureListener {
            dialogModificarPassBinding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), getString(R.string.error_actualizar_pass), Toast.LENGTH_LONG).show()
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}