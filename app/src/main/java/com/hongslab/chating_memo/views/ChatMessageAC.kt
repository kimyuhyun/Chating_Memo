package com.hongslab.chating_memo.views

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gun0912.tedpermission.coroutine.TedPermission
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.adapter.ChatMessageAdapter
import com.hongslab.chating_memo.adapter.ChatMessageAdapter.Companion.VIEW_TYPE_IMAGE
import com.hongslab.chating_memo.adapter.ChatMessageAdapter.Companion.VIEW_TYPE_TEXT
import com.hongslab.chating_memo.adapter.OnGlobalAdapterClickListener
import com.hongslab.chating_memo.databinding.ActivityChatMessageBinding
import com.hongslab.chating_memo.dialog.EmojiDialog
import com.hongslab.chating_memo.dialog.EmojiMapper
import com.hongslab.chating_memo.dialog.EmojiPopup
import com.hongslab.chating_memo.dialog.ImageActionDialog
import com.hongslab.chating_memo.dialog.IosConfirmDialog
import com.hongslab.chating_memo.dialog.IosInputDialog
import com.hongslab.chating_memo.dialog.TextActionDialog
import com.hongslab.chating_memo.manager.CloudinaryDeleter
import com.hongslab.chating_memo.manager.CloudinaryUploader
import com.hongslab.chating_memo.manager.CloudinaryUtils
import com.hongslab.chating_memo.manager.UploadResult
import com.hongslab.chating_memo.models.ChatMessageVO
import com.hongslab.chating_memo.models.ChatRoomVO
import com.hongslab.chating_memo.models.ImageVO
import com.hongslab.chating_memo.utils.AdUtils
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.MyUtils
import com.hongslab.chating_memo.utils.startActivityForResult2
import com.hongslab.chating_memo.viewmodels.ChatMessageViewModel
import com.hongslab.chating_memo.viewmodels.ChatRoomViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatMessageAC : BaseAC(TransitionMode.HORIZON), View.OnClickListener {
    private lateinit var binding: ActivityChatMessageBinding
    private lateinit var chatMessageAdapter: ChatMessageAdapter
    private val vm: ChatMessageViewModel by viewModels()

    private var emojiPopup: EmojiPopup? = null

    private var cateColor = ""
    private var cateIdx = ""
    private var lastMessage = ""
    private var lastIsCompleted = false
    private var isUserScrolling = false  // 사용자가 직접 스크롤 중인지
    private var shouldAutoScroll = true  // 자동 스크롤 해야 하는지

    private lateinit var cloudinaryUploader: CloudinaryUploader


    private val onGlobalAdapterClickListener = object : OnGlobalAdapterClickListener {
        override fun onGlobalAdapterItemClick(v: View, item2: Any, pos: Int) {
            val item = item2 as ChatMessageVO

            when (v.id) {
                R.id.cv_message_box -> {
                    if (binding.llMergeBtnBox.isVisible) {
                        return
                    }

                    showTextActionMenu(pos, item)
                }

                R.id.btn_check_box -> {

                }

                R.id.image_container -> {
                    lifecycleScope.launch {
                        val urls = item.link.split(",").filter { it.isNotBlank() }
                        val list = arrayListOf<ImageVO>()

                        urls.forEach { url ->
                            val vo = ImageVO(url = url)
                            list.add(vo)
                        }

                        val intent = Intent(this@ChatMessageAC, FullScreenImageViewerAC::class.java)
                        intent.putParcelableArrayListExtra("urls", list)
                        startActivityForResult2(intent)
                    }
                }

                R.id.btn_image_more -> showImageActionMenu(pos, item)

            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_back -> onBackPress()

            R.id.btn_search -> {
                lifecycleScope.launch {
                    val intent = Intent(this@ChatMessageAC, SearchAC::class.java)
                    intent.putExtra("cate_idx", cateIdx)
                    intent.putExtra("cate_color", cateColor)
                    val activityResult = startActivityForResult2(intent)
                    if (activityResult.resultCode == Activity.RESULT_OK) {
                        // 최하단으로 내리기 액션을 했다면 다시 로드 한다!
                        vm.refresh(cateIdx)
                    }
                }
            }

            R.id.btn_send -> {
                val message = binding.etMessage.text.toString().trim()
                if (message.isEmpty()) {
                    MyUtils.myToast("메세지를 입력해주세요.")
                    return
                }

                vm.sendMessage(cateIdx, message)
                lastMessage = message

                binding.etMessage.setText("")
            }

            R.id.btn_imoji -> {
                MyUtils.setKeyboard(true, binding.etMessage)
                emojiPopup = EmojiPopup(this, binding.etMessage) { emoji ->
                    emoji?.let {
                        // 현재 커서 위치에 이모지 삽입
                        val currentText = binding.etMessage.text.toString()
                        val cursorPosition = binding.etMessage.selectionStart
                        val newText = StringBuilder(currentText).insert(cursorPosition, it).toString()
                        binding.etMessage.setText(newText)
                        binding.etMessage.setSelection(cursorPosition + it.length)
                    }
                }
                emojiPopup?.showAboveKeyboard()
            }

            R.id.btn_expand, R.id.btn_expand2 -> {
                vm.setNoticeExpand()
            }

            R.id.btn_notice_delete -> {
                vm.delNotice(cateIdx)
            }

            R.id.btn_image_upload -> {
                cloudinaryUploader.setCurrentImageUrls(arrayListOf())

                cloudinaryUploader.selectAndUploadImages(
                    onResult = { result ->
                        when (result) {
                            is UploadResult.Success -> {
                                Toast.makeText(
                                    this,
                                    "${result.uploadedCount}장 업로드 완료",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // 업로드된 이미지들을 메시지로 전송
                                sendImageMessages(result.imageUrls.takeLast(result.uploadedCount))
                            }

                            is UploadResult.Error -> {
                                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onProgress = { isUploading ->
                        showUploadProgress(isUploading)
                    }
                )
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

    private fun sendImageMessages(newImageUrls: List<String>) {
        val urls = newImageUrls.joinToString(",")
        vm.sendImageMessage(cateIdx, urls)
    }

    private fun showUploadProgress(show: Boolean) {
        binding.llLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.click = this
        binding.includeNoticeMessage.vm = vm
        binding.includeNoticeMessage.click = this
        binding.includeNoticeMessage.lifecycleOwner = this

        // CloudinaryUploader 초기화
        cloudinaryUploader = CloudinaryUploader(this)

        setupView()
        setupObserver()
        setupListener()
        setupKeyboardListener()

        try {
            binding.adBottomContainer.removeAllViews()
            val adBannerView = AdUtils(this).getBanner()
            if (adBannerView != null) {
                binding.adBottomContainer.addView(adBannerView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        intent.getStringExtra("cate_name")?.let {
            if (it != null) {
                binding.tvTitle.text = it
            }
        }

        intent.getStringExtra("cate_idx")?.let {
            if (it != null) {
                cateIdx = it
                vm.fetch(it)
            }
        }

        intent.getStringExtra("cate_color")?.let {
            if (it != null) {
                cateColor = it
            }
        }

        chatMessageAdapter = ChatMessageAdapter(onGlobalAdapterClickListener, cateColor)
        binding.recyclerView.adapter = chatMessageAdapter

        // 뒤로가기 버튼 처리!
        onBackPressedDispatcher.addCallback(this) {
            onBackPress()
        }
    }

    private fun setupView() {
        window.apply {
            if (MyUtils.isDarkModeApply(this@ChatMessageAC)) {
                // 네비바 black
                navigationBarColor = ContextCompat.getColor(context, R.color.black)
                // 네비바 아이콘(true: 검정 / false: 흰색)
                WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightNavigationBars = false
            }
        }

        val layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // 중요: 아래부터 채우기
            // RecyclerView 최적화 설정
            isItemPrefetchEnabled = true
        }
        binding.recyclerView.apply {
            this.layoutManager = layoutManager
            // ViewHolder 재활용 풀 크기 증가
            setRecycledViewPool(RecyclerView.RecycledViewPool().apply {
                setMaxRecycledViews(VIEW_TYPE_TEXT, 20)
                setMaxRecycledViews(VIEW_TYPE_IMAGE, 10)
            })
            // 키보드로 인한 레이아웃 변경 시 깜빡임 방지
            itemAnimator = null
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

            // 마지막 데이터 넣어주기!
            if (items.isNotEmpty()) {
                lastMessage = items[items.size - 1].message
                lastIsCompleted = items[items.size - 1].isCompleted
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

        vm.isLoading.observe(this) {
            Dlog.d("$it")
            if (it) {
                binding.llLoading.visibility = View.VISIBLE
            } else {
                binding.llLoading.visibility = View.GONE
            }
        }


        vm.noticeTextStateItem.observe(this) {
            if (it.message == "") {
                binding.includeNoticeMessage.root.visibility = View.GONE
            } else {
                binding.includeNoticeMessage.root.visibility = View.VISIBLE
            }
        }
    }

    private fun setupListener() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        // 사용자가 직접 스크롤 시작
                        isUserScrolling = true
                    }

                    RecyclerView.SCROLL_STATE_IDLE -> {
                        // 스크롤이 멈춤
                        isUserScrolling = false
                        checkIfAtBottom()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // 상단에 도달했고
                if (firstVisibleItemPosition == 0) {
                    vm.fetch(cateIdx)
                }

                // 사용자가 직접 스크롤했을 때만 자동스크롤 상태 업데이트
                if (isUserScrolling) {
                    checkIfAtBottom()
                }
            }
        })
    }

    private fun checkIfAtBottom() {
        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        val totalItemCount = layoutManager.itemCount

        // 마지막 아이템이 완전히 보이는지 확인
        shouldAutoScroll = lastVisibleItemPosition >= totalItemCount - 1 && !binding.recyclerView.canScrollVertically(1)
    }

    private fun setupKeyboardListener() {
        val rootView = binding.root
        var isKeyboardVisible = false

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = rootView.rootView.height - rootView.height
            val keyboardThreshold = 200 * resources.displayMetrics.density
            val keyboardCurrentlyVisible = heightDiff > keyboardThreshold

            if (keyboardCurrentlyVisible != isKeyboardVisible) {
                isKeyboardVisible = keyboardCurrentlyVisible

                if (keyboardCurrentlyVisible) {
                    // 키보드가 올라옴
                    if (emojiPopup?.isShowing == true) {
                        emojiPopup?.dismiss()
                        emojiPopup?.showAboveKeyboard()
                    }
                } else {
                    // 키보드가 내려감
                    emojiPopup?.dismiss()

                    // 키보드가 내려간 후 RecyclerView 갱신 방지
                    Handler(Looper.getMainLooper()).postDelayed({
                        // 필요시에만 위치 조정
                        if (shouldAutoScroll && chatMessageAdapter.itemCount > 0) {
                            binding.recyclerView.scrollToPosition(chatMessageAdapter.itemCount - 1)
                        }
                    }, 300)
                }
            }
        }

        // 레이아웃 변경 리스너 수정
        binding.root.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom && isKeyboardVisible) {
                // 키보드가 올라올 때만 스크롤 조정
                if (shouldAutoScroll) {
                    binding.recyclerView.postDelayed({
                        try {
                            if (chatMessageAdapter.itemCount > 0) {
                                binding.recyclerView.scrollToPosition(chatMessageAdapter.itemCount - 1)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, 100) // 지연 시간 단축
                }
            }
        }
    }

    private fun showTextActionMenu(pos: Int, item: ChatMessageVO) {
        TextActionDialog(this@ChatMessageAC, item) { selected ->
            when (selected) {
                "notice" -> vm.setNotice(cateIdx, item.message)
                "copy" -> {
                    val tmp = EmojiMapper.decodeEmojis(item.message)
                    MyUtils.copyText(this@ChatMessageAC, tmp)
                }

                "select_copy" -> {
                    lifecycleScope.launch {
                        val intent = Intent(this@ChatMessageAC, SelectCopyAC::class.java)
                        intent.putExtra("item", item)
                        startActivityForResult2(intent)
                    }
                }

                "completed" -> {
                    vm.setCompleted(item.idx, true)
                    lastIsCompleted = true
                }

                "completed_cancel" -> {
                    vm.setCompleted(item.idx, false)
                    lastIsCompleted = false

                }

                "goto_bottom" -> vm.setGotoBottom(item.idx)

                "merge_memo" -> {
                    chatMessageAdapter.toggleCheckBoxVisibility(isShow = true)
                    binding.llMergeBtnBox.visibility = View.VISIBLE
                }

                "delete" -> {
                    IosConfirmDialog(this@ChatMessageAC, "삭제 하시겠습니까?", true) {
                        if (it) {
                            vm.delete(item.idx)
                        }
                    }.show()
                }
            }

        }.show()
    }

    private fun showImageActionMenu(pos: Int, item: ChatMessageVO) {
        ImageActionDialog(this@ChatMessageAC) { selected ->
            when (selected) {
                "delete" -> {
                    IosConfirmDialog(this@ChatMessageAC, "삭제 하시겠습니까?", true) {
                        if (it) {
                            vm.delete(item.idx)
                        }
                    }.show()
                }
            }
        }.show()
    }

    private fun onBackPress() {
        // 뒤로가기도 계속 살아있다.!
        GlobalScope.launch(Dispatchers.IO) {
            CloudinaryDeleter.getInstance().retryFailedDeletions()
        }

        val intent = Intent()
        intent.putExtra("last_is_completed", lastIsCompleted)
        intent.putExtra("last_message", lastMessage)
        setResult(RESULT_OK, intent)
        finish()
    }
}