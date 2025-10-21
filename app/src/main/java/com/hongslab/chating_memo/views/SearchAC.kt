package com.hongslab.chating_memo.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.adapter.ChatMessageAdapter
import com.hongslab.chating_memo.adapter.OnGlobalAdapterClickListener
import com.hongslab.chating_memo.databinding.ActivitySearchBinding
import com.hongslab.chating_memo.dialog.EmojiMapper
import com.hongslab.chating_memo.dialog.IosConfirmDialog
import com.hongslab.chating_memo.dialog.TextActionDialog
import com.hongslab.chating_memo.manager.CloudinaryDeleter
import com.hongslab.chating_memo.models.ChatMessageVO
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.MyUtils
import com.hongslab.chating_memo.utils.startActivityForResult2
import com.hongslab.chating_memo.viewmodels.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.core.view.isVisible

class SearchAC : BaseAC(TransitionMode.HORIZON), View.OnClickListener {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var chatMessageAdapter: ChatMessageAdapter
    private val vm: SearchViewModel by viewModels()

    private var cateIdx = ""
    private var cateColor = ""
    private var isParentRefresh = false


    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_back -> onBackPress()
            R.id.btn_clear -> {
                binding.etQuery.setText("")
                MyUtils.setKeyboard(true, binding.etQuery)
            }

            R.id.btn_merge_memo -> {
                vm.mergeAndSendMessages(cateIdx)
                chatMessageAdapter.toggleCheckBoxVisibility(isShow = false)
                binding.llMergeBtnBox.visibility = View.GONE
            }

            R.id.btn_merge_memo_cancel -> {
                chatMessageAdapter.toggleCheckBoxVisibility(isShow = false)
                binding.llMergeBtnBox.visibility = View.GONE
            }
        }


    }

    private val onGlobalAdapterClickListener = object : OnGlobalAdapterClickListener {
        override fun onGlobalAdapterItemClick(v: View, item2: Any, pos: Int) {
            val item = item2 as ChatMessageVO

            when (v.id) {
                R.id.cv_message_box -> {
                    if (binding.llMergeBtnBox.isVisible) {
                        return
                    }


                    TextActionDialog(this@SearchAC, item) { selected ->
                        when (selected) {
                            "copy" -> {
                                val tmp = EmojiMapper.decodeEmojis(item.message)
                                MyUtils.copyText(this@SearchAC, tmp)
                            }

                            "select_copy" -> {
                                lifecycleScope.launch {
                                    val intent = Intent(this@SearchAC, SelectCopyAC::class.java)
                                    intent.putExtra("item", item)
                                    startActivityForResult2(intent)
                                }
                            }

                            "goto_bottom" -> {
                                vm.setGotoBottom(item.idx)
                                isParentRefresh = true
                            }

                            "merge_memo" -> {
                                chatMessageAdapter.toggleCheckBoxVisibility(isShow = true)
                                binding.llMergeBtnBox.visibility = View.VISIBLE
                            }

                            "delete" -> {
                                IosConfirmDialog(this@SearchAC, "삭제 하시겠습니까?", true) {
                                    if (it) {
                                        vm.delete(item.idx)
                                        isParentRefresh = true
                                    }
                                }.show()
                            }
                        }

                    }.show()
                }

                R.id.btn_check_box -> {

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.click = this
        binding.vm = vm
        binding.lifecycleOwner = this
        binding.itemClick = onGlobalAdapterClickListener

        cateIdx = intent.getStringExtra("cate_idx") ?: ""
        cateColor = intent.getStringExtra("cate_color") ?: ""

        chatMessageAdapter = ChatMessageAdapter(onGlobalAdapterClickListener, cateColor)
        binding.recyclerView.adapter = chatMessageAdapter


        // 뒤로가기 버튼 처리!
        onBackPressedDispatcher.addCallback(this) {
            onBackPress()
        }


        setupView()
        setupListener()
        setupObserver()


        MyUtils.setKeyboard(true, binding.etQuery)

    }

    private fun setupView() {
        window.apply {
            if (MyUtils.isDarkModeApply(this@SearchAC)) {
                // 네비바 black
                navigationBarColor = ContextCompat.getColor(context, R.color.black)
                // 네비바 아이콘(true: 검정 / false: 흰색)
                WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightNavigationBars = false
            }
        }
    }

    private fun setupListener() {
        binding.etQuery.addTextChangedListener {
            val query = it.toString()
            if (query.isNotEmpty()) {
                binding.btnClear.visibility = View.VISIBLE
            } else {
                binding.btnClear.visibility = View.INVISIBLE
            }
        }

        binding.etQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etQuery.text.toString().trim()
                if (query.isEmpty()) {
                    MyUtils.myToast("검색어를 입력해 주세요.")
                    return@setOnEditorActionListener false
                }
                chatMessageAdapter.setSearchText(query)
                vm.fetch(cateIdx, query)
                MyUtils.setKeyboard(false, binding.etQuery)
                true
            }
            false
        }
    }

    private fun setupObserver() {
        vm.items.observe(this) { items ->
            Dlog.d("Items updated - size: ${items.size}")

            // 리스트가 동일한 경우 갱신하지 않음
            val currentList = chatMessageAdapter.currentList
            if (currentList.size == items.size && currentList == items) {
                return@observe
            }

            chatMessageAdapter.submitList(ArrayList(items)) {
                Dlog.d("chatMessageAdapter.submitList completed")
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        if (vm.uiScrollPosition >= 0 && vm.uiScrollPosition < items.size) {
                            binding.recyclerView.smoothScrollToPosition(vm.uiScrollPosition)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, 50)
            }
        }
    }

    private fun onBackPress() {
        Dlog.d("isParentRefresh: $isParentRefresh")
        if (isParentRefresh) {
            setResult(RESULT_OK)
        }
        finish()
    }
}