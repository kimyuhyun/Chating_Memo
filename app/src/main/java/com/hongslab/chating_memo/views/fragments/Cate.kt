package com.hongslab.chating_memo.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.adapter.OnGlobalAdapterClickListener
import com.hongslab.chating_memo.databinding.FragmentCateBinding
import com.hongslab.chating_memo.databinding.FragmentDevBinding
import com.hongslab.chating_memo.dialog.ColorSelectDialog
import com.hongslab.chating_memo.dialog.IosConfirmDialog
import com.hongslab.chating_memo.dialog.IosInputDialog
import com.hongslab.chating_memo.dialog.IosMenuDialog
import com.hongslab.chating_memo.models.CateVO
import com.hongslab.chating_memo.models.GlobalVO
import com.hongslab.chating_memo.utils.IosStyleAddLineDecoration
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre
import com.hongslab.chating_memo.utils.startActivityForResult2
import com.hongslab.chating_memo.viewmodels.CateViewModel
import com.hongslab.chating_memo.views.ChatMessageAC
import com.hongslab.chating_memo.views.MainAC
import kotlinx.coroutines.launch


class Cate : Fragment(), View.OnClickListener {
    private var _binding: FragmentCateBinding? = null
    private val binding get() = _binding!!

    private val vm: CateViewModel by activityViewModels()

    private val onGlobalAdapterClickListener = object : OnGlobalAdapterClickListener {
        override fun onGlobalAdapterItemClick(v: View, item2: Any, pos: Int) {
            val item = item2 as CateVO

            when (v.id) {
                R.id.btn_more_action -> {
                    val location = IntArray(2)
                    v.getLocationOnScreen(location)
                    val x = 20
                    val y = location[1] - 80
                    val g = Gravity.TOP or Gravity.RIGHT
                    val list = arrayListOf<String>()
                    list.add("최상위로 올리기")
                    list.add("색상변경")
                    list.add("수정")
                    list.add("삭제")
                    IosMenuDialog(requireActivity(), x, y, g, list) { menuPos ->
                        when (menuPos) {
                            0 -> vm.updateToTop(item.idx)
                            1 -> {
                                ColorSelectDialog(requireActivity()) { colorCode ->
                                    vm.updateColor(item.idx, colorCode)
                                }.show()
                            }

                            2 -> {
                                IosInputDialog(requireActivity(), "분류명을 입력 해 주세요.", false, item.name1) {
                                    if (it != "") {
                                        vm.update(item.idx, it)
                                    }
                                }.show()
                            }

                            3 -> {
                                IosConfirmDialog(requireActivity(), "삭제 하시겠습니까?", true) {
                                    if (it) {
                                        vm.delete(item.idx)
                                    }
                                }.show()
                            }
                        }
                    }.show()
                }

                R.id.ll_box -> {
                    lifecycleScope.launch {
                        val intent = Intent(requireActivity(), ChatMessageAC::class.java)
                        intent.putExtra("cate_idx", item.idx)
                        intent.putExtra("cate_name", item.name1)
                        intent.putExtra("cate_color", item.color)
                        startActivityForResult2(intent)
                    }
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_add_cate -> {
                IosInputDialog(requireActivity(), "분류명을 입력 해 주세요.", false, "") {
                    if (it != "") {
                        vm.add(it)
                    }
                }.show()

            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.click = this
        binding.lifecycleOwner = this
        binding.vm = vm
        binding.itemClick = onGlobalAdapterClickListener

        SPre.set(SCol.CURRENT_TAB.name, "0")

        val decoration = IosStyleAddLineDecoration(requireActivity(), 1f, 20f)
        binding.recyclerView.addItemDecoration(decoration)

        val ac = (requireActivity() as MainAC)
        if (!ac.isCateLoaded) {
            vm.fetch()
            ac.isCateLoaded = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}