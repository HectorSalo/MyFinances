package com.skysam.hchirinos.myfinances.listaGastosModule.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.card.MaterialCardView
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.model.constructores.ImagenesListasConstructor

class ImagenesListasAdapter(private var imagenes: ArrayList<ImagenesListasConstructor>, private var context: Context,
private var crearEditarListaClick: CrearEditarListaClick):
        RecyclerView.Adapter<ImagenesListasAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagen: ImageView = view.findViewById(R.id.iv_listas)
        val cardview: MaterialCardView = view.findViewById(R.id.cardview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.cardview_imagenes_listas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = imagenes[position]

        val options = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(android.R.drawable.ic_menu_gallery)
                .placeholder(android.R.drawable.ic_menu_gallery)

        val options2 = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.drawable.ic_image_not_selected_96)
                .placeholder(android.R.drawable.ic_menu_gallery)

        if (item.photoUrl != null) {
            Glide.with(context).load(item.photoUrl)
                    .apply(options).into(holder.imagen)
        } else {
            Glide.with(context).load(R.drawable.ic_image_not_selected_96)
                    .apply(options2).into(holder.imagen)
        }

        if (item.imageSelected!!) {
            holder.cardview.strokeColor = ContextCompat.getColor(context, R.color.design_default_color_secondary_variant)
        } else {
            holder.cardview.strokeColor = ContextCompat.getColor(context, android.R.color.darker_gray)
        }

        with(holder.itemView) {
            tag = item
            holder.itemView.setOnClickListener {
                update(position)
                crearEditarListaClick.onImageClick(position)
            }
        }
    }

    override fun getItemCount(): Int = imagenes.size

    fun update(position: Int) {
        for (j in 0 until imagenes.size) {
            imagenes[j].imageSelected = false
        }
        imagenes[position].imageSelected = true
        notifyDataSetChanged()
    }
}