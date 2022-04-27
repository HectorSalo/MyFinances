package com.skysam.hchirinos.myfinances.homeModule.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.skysam.hchirinos.myfinances.common.model.constructores.ItemCronologiaConstructor
import com.skysam.hchirinos.myfinances.databinding.FragmentCronologiaBinding
import com.skysam.hchirinos.myfinances.homeModule.viewmodel.MainViewModel


class CronologiaFragment : Fragment() {

    private var _binding: FragmentCronologiaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private val listIngresos: MutableList<ItemCronologiaConstructor> = mutableListOf()
    private val listGastos: MutableList<ItemCronologiaConstructor> = mutableListOf()
    private val lista: MutableList<ItemCronologiaConstructor> = mutableListOf()
    private lateinit var adapter: CronologiaListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentCronologiaBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                CronologiaFragment()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CronologiaListAdapter(lista)
        binding.rvCronologia.setHasFixedSize(true)
        binding.rvCronologia.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(requireContext(),
                LinearLayoutManager.HORIZONTAL)
        binding.rvCronologia.addItemDecoration(dividerItemDecoration)

        loadViewModels()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadViewModels() {
        viewModel.listIngresos.observe(viewLifecycleOwner) {
            listIngresos.clear()
            listIngresos.addAll(it)
            sortList()
        }
        viewModel.listGastos.observe(viewLifecycleOwner) {
            listGastos.clear()
            listGastos.addAll(it)
            sortList()
        }
    }

    private fun sortList() {
        if (_binding != null) {
            lista.clear()
            lista.addAll(listIngresos)
            lista.addAll(listGastos)
            if (lista.isEmpty()) {
                binding.tvSinCronologia.visibility = View.VISIBLE
                binding.rvCronologia.visibility = View.GONE
            } else {
                lista.sortWith { t, t2 -> t.fecha!!.compareTo(t2.fecha) }
                adapter = CronologiaListAdapter(lista)
                binding.rvCronologia.adapter = adapter
                binding.tvSinCronologia.visibility = View.GONE
                binding.rvCronologia.visibility = View.VISIBLE
            }
        }
    }
}