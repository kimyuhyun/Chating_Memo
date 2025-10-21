package com.hongslab.chating_memo.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hongslab.chating_memo.databinding.FragmentDevBinding
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre


class Dev : Fragment(), View.OnClickListener {
    private var _binding: FragmentDevBinding? = null
    private val binding get() = _binding!!

    override fun onClick(v: View) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDevBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.click = this
        binding.lifecycleOwner = this

        SPre.set(SCol.CURRENT_TAB.name, "0")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}