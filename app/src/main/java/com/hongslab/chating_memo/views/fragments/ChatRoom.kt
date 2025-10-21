package com.hongslab.chating_memo.views.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.adapter.OnGlobalAdapterClickListener
import com.hongslab.chating_memo.databinding.FragmentChatRoomBinding
import com.hongslab.chating_memo.models.CateVO
import com.hongslab.chating_memo.models.ChatRoomVO
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.IosStyleAddLineDecoration
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre
import com.hongslab.chating_memo.utils.startActivityForResult2
import com.hongslab.chating_memo.viewmodels.ChatRoomViewModel
import com.hongslab.chating_memo.views.ChatMessageAC
import com.hongslab.chating_memo.views.MainAC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChatRoom : Fragment(), View.OnClickListener {
    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    private val vm: ChatRoomViewModel by activityViewModels()

    private val onGlobalAdapterClickListener = object : OnGlobalAdapterClickListener {
        override fun onGlobalAdapterItemClick(v: View, item2: Any, pos: Int) {
            val item = item2 as ChatRoomVO

            if (v.id == R.id.ll_box) {
                lifecycleScope.launch {
                    val intent = Intent(requireActivity(), ChatMessageAC::class.java)
                    intent.putExtra("cate_idx", item.cateIdx)
                    intent.putExtra("cate_name", item.cateName)
                    intent.putExtra("cate_color", item.color)
                    val activityResult = startActivityForResult2(intent)
                    if (activityResult.resultCode == Activity.RESULT_OK) {
                        activityResult.data?.getStringExtra("last_message")?.let { lastMessage ->
                            if (lastMessage != "") {
                                val isCompleted = activityResult.data.getBooleanExtra("last_is_completed", false)
                                vm.updateLastMessage(pos, lastMessage, isCompleted)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onClick(v: View) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.click = this
        binding.lifecycleOwner = this
        binding.vm = vm
        binding.itemClick = onGlobalAdapterClickListener

        SPre.set(SCol.CURRENT_TAB.name, "1")

        val decoration = IosStyleAddLineDecoration(requireActivity(), 1f, 20f)
        binding.recyclerView.addItemDecoration(decoration)

        vm.fetch()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}