package com.ust.mytask.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.ust.mytask.R
import com.ust.mytask.model.database.AppDatabase
import com.ust.mytask.model.repository.MdnsRepository
import com.ust.mytask.viewmodel.MdnsViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var viewModel: MdnsViewModel
    private lateinit var adapter: MdnsAdapter
    private lateinit var scanner: MdnsScanner

    private var recyclerView : RecyclerView? = null

    private lateinit var view: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.home_screen, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = MdnsAdapter()

        recyclerView = view.findViewById(R.id.recyclerviewMdns)
        recyclerView?.adapter = adapter

        val dao = AppDatabase.get(requireContext()).mdnsDao()
        val repo = MdnsRepository(dao)
        viewModel = MdnsViewModel(repo)

        scanner = MdnsScanner(requireContext()) { device ->
            viewModel.addDevice(device)  // Save to DB
        }

        lifecycleScope.launch {
            viewModel.devices.collect { list ->
                adapter.submitList(list)
            }
        }

        scanner.startScanning()
    }

    override fun onDestroy() {
        scanner.stopScanning()
        super.onDestroy()
    }
}
