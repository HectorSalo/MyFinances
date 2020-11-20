package com.skysam.hchirinos.myfinances.listaGastosModule.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.card.MaterialCardView
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.model.constructores.ImagenesListasConstructor

class ImagenesListasAdapter(private var imagenes: ArrayList<ImagenesListasConstructor>):
        RecyclerView.Adapter<ImagenesListasAdapter.ViewHolder>() {

    private lateinit var mContext: Context
    private val onClickListener: View.OnClickListener
    private lateinit var cardView: MaterialCardView

    init {
        onClickListener = View.OnClickListener { v ->

        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagen: ImageView = view.findViewById(R.id.iv_listas)
        val cardview: MaterialCardView = view.findViewById(R.id.cardview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cardview_imagenes_listas, parent, false)
        mContext = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = imagenes[position]

        val options = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(android.R.drawable.ic_menu_gallery)
                .placeholder(android.R.drawable.ic_menu_gallery)

        Glide.with(mContext).load(item.photoUrl)
                .apply(options).into(holder.imagen)

        with(holder.itemView) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount(): Int = imagenes.size
}