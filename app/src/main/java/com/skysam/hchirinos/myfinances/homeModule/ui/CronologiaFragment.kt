package com.skysam.hchirinos.myfinances.homeModule.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.skysam.hchirinos.myfinances.common.model.constructores.ItemCronologiaConstructor
import com.skysam.hchirinos.myfinances.databinding.FragmentCronologiaBinding
import com.skysam.hchirinos.myfinances.homeModule.presenter.CronologiaPresenter
import com.skysam.hchirinos.myfinances.homeModule.presenter.CronologiaPresenterClass
import java.util.*
import kotlin.collections.ArrayList


class CronologiaFragment : Fragment(), CronologiaView {

    private var _binding: FragmentCronologiaBinding? = null
    private val binding get() = _binding!!
    private lateinit var cronologiaPresenter: CronologiaPresenter
    private lateinit var lista: ArrayList<ItemCronologiaConstructor>
    private lateinit var adapter: CronologiaListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentCronologiaBinding.inflate(inflater, container, false)
        cronologiaPresenter = CronologiaPresenterClass(this)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                CronologiaFragment()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lista = ArrayList()
        adapter = CronologiaListAdapter(lista)
        binding.rvCronologia.setHasFixedSize(true)
        binding.rvCronologia.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(requireContext(),
                LinearLayoutManager.HORIZONTAL)
        binding.rvCronologia.addItemDecoration(dividerItemDecoration)


        val calendar = Calendar.getInstance()
        val month = calendar[Calendar.MONTH]
        val year = calendar[Calendar.YEAR]

        cronologiaPresenter.getCronologia(month, year)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun listCronologia(list: ArrayList<ItemCronologiaConstructor>) {
        if (list.isEmpty()) {
            binding.tvSinCronologia.visibility = View.VISIBLE
            binding.rvCronologia.visibility = View.GONE
        } else {
            lista = list
            lista.sortWith { t, t2 -> t.fecha!!.compareTo(t2.fecha) }
            adapter = CronologiaListAdapter(lista)
            binding.rvCronologia.adapter = adapter
            binding.tvSinCronologia.visibility = View.GONE
            binding.rvCronologia.visibility = View.VISIBLE
        }
    }
}