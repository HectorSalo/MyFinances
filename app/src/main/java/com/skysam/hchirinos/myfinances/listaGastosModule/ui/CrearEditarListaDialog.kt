package com.skysam.hchirinos.myfinances.listaGastosModule.ui

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Constraints
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.model.constructores.ImagenesListasConstructor
import com.skysam.hchirinos.myfinances.common.model.constructores.ListasConstructor
import com.skysam.hchirinos.myfinances.common.utils.ClassesCommon
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.databinding.DialogCrearListaBinding
import com.skysam.hchirinos.myfinances.listaGastosModule.presenter.CrearEditarListaPresenter
import com.skysam.hchirinos.myfinances.listaGastosModule.presenter.CrearEditarListaPresenterClass
import java.util.*
import kotlin.collections.ArrayList

class CrearEditarListaDialog(private val twoPane: Boolean, private val guardar: Boolean, private val lista: ArrayList<ListasConstructor>, private val position: Int?,
                             private val adapter: ListasPendientesAdapter):
        DialogFragment(), CrearEditarListaView, CrearEditarListaClick {
    private var _binding : DialogCrearListaBinding? = null
    private val binding get() = _binding!!
    private val user = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private var dialog : AlertDialog? = null
    private var imagenesListas: MutableList<ImagenesListasConstructor> = mutableListOf()
    private lateinit var imagenesListasAdapter: ImagenesListasAdapter
    private var crearEditarListaPresenter: CrearEditarListaPresenter = CrearEditarListaPresenterClass(this)
    private var imagen: String? = null
    private var imagenVieja: String? = null
    private var uriLocal: String? = null


    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            goGallery()
        } else {
            Toast.makeText(context, getString(R.string.txt_error_permiso_lectura), Toast.LENGTH_SHORT).show()
        }
    }

    private val requestIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
        if (result.resultCode == Activity.RESULT_OK) {
            showImage(result.data!!)
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCrearListaBinding.inflate(layoutInflater)

        crearEditarListaPresenter.getImages()

        var title = getString(R.string.btn_nueva_lista)
        if (!guardar) {
            binding.etNombre.setText(lista[position!!].nombreLista)
            title = getString(R.string.text_editar_lista)
        }

        val builder = AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(binding.root)
                .setPositiveButton(getString(R.string.btn_guardar), null)
                .setNegativeButton(getString(R.string.btn_cancelar), null)

        dialog = builder.create()
        dialog?.show()
        dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
            binding.inputNombre.error = null
            validarLista()
        }

        binding.rgImagenes.setOnCheckedChangeListener { _, i ->
            when(i) {
                R.id.rb_imagenes_predifinidas -> {
                    binding.rvImagenesListas.visibility = View.VISIBLE
                    binding.ibGaleria.visibility = View.GONE
                }
                R.id.rb_galeria -> {
                    binding.rvImagenesListas.visibility = View.GONE
                    binding.ibGaleria.visibility = View.VISIBLE
                }
            }
        }

        binding.ibGaleria.setOnClickListener { requestPermission() }

        return dialog as AlertDialog
    }

    private fun validarLista() {
        val nombre = binding.etNombre.text.toString()
        if (nombre.isEmpty()) {
            binding.inputNombre.error = getString(R.string.error_campo_vacio)
            return
        }

        if (binding.rbGaleria.isChecked) {
            if (uriLocal == null) {
                Toast.makeText(context, getString(R.string.error_sin_imagen_galeria), Toast.LENGTH_SHORT).show()
            } else {
                crearEditarListaPresenter.uploadImage(Uri.parse(uriLocal))
                binding.etNombre.isEnabled = false
                binding.tvSubirImagen.visibility = View.VISIBLE
                binding.pbSubirImagen.visibility = View.VISIBLE
                binding.ibGaleria.isClickable = false
                dialog!!.setCancelable(false)
            }
        } else {
            if (guardar) guardarLista(nombre) else editarLista(nombre)
        }
    }

    private fun guardarLista(nombre: String) {
        Toast.makeText(context, "Guardando...", Toast.LENGTH_SHORT).show()
        val calendar = Calendar.getInstance()
        val fechaIngreso = calendar.time

        val docData: MutableMap<String, Any?> = HashMap()
        docData[Constants.BD_NOMBRE] = nombre
        docData[Constants.BD_CANTIDAD_ITEMS] = 0
        docData[Constants.BD_FECHA_INGRESO] = fechaIngreso
        docData[Constants.BD_IMAGEN] = imagen

        db.collection(Constants.BD_LISTA_GASTOS).document(user!!.uid).collection(Constants.BD_TODAS_LISTAS)
                .add(docData)
                .addOnSuccessListener { document ->
                    val docId = document.id
                    Log.d(Constraints.TAG, "DocumentSnapshot written succesfully")

                    if (twoPane) {
                        val itemNuevo: ListasConstructor?
                        val fragment = ListaPendientesDetailFragment().apply {
                            arguments = Bundle().apply {
                                putString(ListaPendientesDetailFragment.ARG_ITEM_ID, docId)
                                putString(ListaPendientesDetailFragment.ARG_ITEM_NOMBRE, nombre)
                            }
                        }
                        activity?.supportFragmentManager
                                ?.beginTransaction()
                                ?.replace(R.id.listapendientes_detail_container, fragment)
                                ?.commit()
                        itemNuevo = ListasConstructor()
                        itemNuevo.nombreLista = nombre
                        itemNuevo.idLista = docId
                        itemNuevo.cantidadItems = 0
                        itemNuevo.imagen = imagen
                        lista.add(itemNuevo)
                        adapter.updateList(lista)
                    } else {
                        val intent = Intent(context, ListaPendientesDetailActivity::class.java).apply {
                            putExtra(ListaPendientesDetailFragment.ARG_ITEM_ID, docId)
                            putExtra(ListaPendientesDetailFragment.ARG_ITEM_NOMBRE, nombre)
                            putExtra(ListaPendientesDetailFragment.ARG_ITEM_IMAGEN, imagen)
                        }
                        context?.startActivity(intent)
                    }
                    dialog?.dismiss()
                }
                .addOnFailureListener { e ->
                    Log.w(Constraints.TAG, "Error adding document", e)
                    Toast.makeText(context, getString(R.string.error_guardar_data), Toast.LENGTH_SHORT).show()
                    dialog?.dismiss()
                }
    }

    private fun editarLista(nombre: String) {
        Toast.makeText(context, "Actualizando...", Toast.LENGTH_SHORT).show()

        db.collection(Constants.BD_LISTA_GASTOS).document(user!!.uid).collection(Constants.BD_TODAS_LISTAS).document(lista[position!!].idLista)
                .update(Constants.BD_NOMBRE, nombre, Constants.BD_IMAGEN, imagen)
                .addOnSuccessListener {
                    if (!imagenVieja.equals(imagen)) {
                        var x = 0
                        for (j in 0 until imagenesListas.size) {
                           if (imagenesListas[j].photoUrl.equals(imagenVieja)) {
                               x += 1
                           }
                        }
                        if (x == 0) deleteImageOld()
                    }
                    updateSuccess(nombre)
                }
                .addOnFailureListener {
                    Toast.makeText(context, getString(R.string.error_guardar_data), Toast.LENGTH_SHORT).show()
                }
    }

    private fun deleteImageOld() {
        crearEditarListaPresenter.deleteOldImage(imagenVieja)
    }

    private fun updateSuccess(nombre: String) {
        Toast.makeText(context, getString(R.string.process_succes), Toast.LENGTH_SHORT).show()
        dialog?.dismiss()
        lista[position!!].nombreLista = nombre
        lista[position].imagen = imagen
        adapter.updateList(lista)
    }

    override fun cargarImagenes(imagenes: MutableList<ImagenesListasConstructor>) {
        imagenesListas.clear()
        imagenesListas.addAll(imagenes)

        if (!guardar) {
            var isImgFromServer = false
            imagenVieja = lista[position!!].imagen
            imagen = lista[position].imagen
            if (lista[position].imagen != null) {
                for (j in 0 until imagenesListas.size) {
                    imagenesListas[j].isImageSelected = imagenesListas[j].photoUrl.equals(lista[position].imagen)
                    if (imagenesListas[j].photoUrl.equals(lista[position].imagen)) {
                        isImgFromServer = true
                    }
                }
                if (!isImgFromServer) {
                    Glide.with(requireContext()).load(imagen)
                            .error(R.drawable.ic_add_photo_96)
                            .into(binding.ibGaleria)
                    binding.rbGaleria.isChecked = true
                }
            }
        }

        imagenesListasAdapter = ImagenesListasAdapter(imagenesListas, requireContext(), this)
        binding.rvImagenesListas.adapter = imagenesListasAdapter
    }

    override fun progressUploadImage(progress: Double) {
        binding.pbSubirImagen.progress = progress.toInt()
    }

    override fun resultUploadImage(statusOk: Boolean, data: String) {
        if (statusOk) {
            imagen = data
            val nombre = binding.etNombre.text.toString()
            if (guardar) guardarLista(nombre) else editarLista(nombre)
        } else {
            binding.tvSubirImagen.visibility = View.GONE
            binding.pbSubirImagen.visibility = View.GONE
            binding.ibGaleria.isClickable = true
            binding.etNombre.isEnabled = true
            Toast.makeText(context, data, Toast.LENGTH_SHORT).show()
        }
    }


    override fun onImageClick(position: Int) {
        imagen = imagenesListas[position].photoUrl
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                goGallery()
                return
            }
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            requestPermissionOld()
        }
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionOld() {
        if (checkPermission()) {
            goGallery()
        } else {
            Toast.makeText(context, getString(R.string.txt_error_permiso_lectura), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImage(it: Intent) {
        uriLocal = it.dataString
        previewImage(uriLocal)
    }

    private fun previewImage(url: String?) {
        val sizeImagePreview = resources.getDimensionPixelSize(R.dimen.size_img_preview)
        val bitmap = ClassesCommon.reduceBitmap(url, sizeImagePreview, sizeImagePreview)

        if (bitmap != null) {
            binding.ibGaleria.setImageBitmap(bitmap)
        }
    }


    private fun goGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        requestIntentLauncher.launch(intent)
    }


}