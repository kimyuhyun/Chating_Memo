package com.hongslab.chating_memo.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.hongslab.chating_memo.BR
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.adapter.GlobalAdapter
import com.hongslab.chating_memo.adapter.OnGlobalAdapterClickListener
import com.hongslab.chating_memo.databinding.EmojiPopupBinding
import com.hongslab.chating_memo.models.EmojiVO
import com.hongslab.chating_memo.models.GlobalVO

class EmojiPopup(
    private val context: Context,
    private val anchorView: View, // EditText나 입력창
    private val callBack: (String?) -> Unit
) : PopupWindow(), View.OnClickListener {

    private lateinit var binding: EmojiPopupBinding
    private lateinit var adapter: GlobalAdapter

    val items = arrayListOf(
        EmojiVO(name1 = "❤️"),     // 빨간 하트
        EmojiVO(name1 = "\uD83D\uDC9D"), // 💝 리본 하트
        EmojiVO(name1 = "\uD83D\uDE04"), // 😄 웃는 얼굴
        EmojiVO(name1 = "\uD83D\uDE00"), // 😀 활짝 웃는 얼굴
        EmojiVO(name1 = "\uD83D\uDE0D"), // 😍 하트 눈 얼굴
        EmojiVO(name1 = "\uD83D\uDC4C"), // 👌 OK 손 기호
        EmojiVO(name1 = "\uD83D\uDC4F"), // 👏 박수치는 손
        EmojiVO(name1 = "\uD83D\uDE31"), // 😱 무서워 비명 지르는 얼굴
        EmojiVO(name1 = "\uD83E\uDD71"), // 🥱 하품하는 얼굴
        EmojiVO(name1 = "\uD83D\uDC4D"), // 👍 엄지 손가락
        EmojiVO(name1 = "\uD83D\uDC4E"), // 👎 엄지 아래
        EmojiVO(name1 = "\uD83E\uDD26"), // 🤦 얼굴을 가리는 사람
        EmojiVO(name1 = "\uD83D\uDCAF"), // 💯 100점
        EmojiVO(name1 = "\uD83D\uDE02"), // 😂 웃음 눈물
        EmojiVO(name1 = "\uD83E\uDD70"), // 🥰 하트 얼굴
        EmojiVO(name1 = "\uD83D\uDE18"), // 😘 키스
        EmojiVO(name1 = "\uD83E\uDD14"), // 🤔 생각하는 얼굴
        EmojiVO(name1 = "\uD83D\uDE0E"), // 😎 선글라스 얼굴
        EmojiVO(name1 = "\uD83D\uDE2D"), // 😭 큰 울음
        EmojiVO(name1 = "\uD83D\uDE44"), // 🙄 눈 굴리는 얼굴
        EmojiVO(name1 = "\uD83E\uDD23"), // 🤣 바닥에서 구르며 웃기
        EmojiVO(name1 = "\uD83D\uDC95"), // 💕 두 하트
        EmojiVO(name1 = "\uD83D\uDE0A"), // 😊 웃는 얼굴
        EmojiVO(name1 = "\uD83D\uDE34"), // 😴 자는 얼굴
        EmojiVO(name1 = "🙏"),          // 기도하는 손
        EmojiVO(name1 = "\uD83E\uDD17"), // 🤗 포옹하는 얼굴
        EmojiVO(name1 = "\uD83D\uDE33"), // 😳 놀란 얼굴
        EmojiVO(name1 = "\uD83D\uDE1C"), // 😜 윙크하고 혀 내밀기
        EmojiVO(name1 = "\uD83D\uDC4B"), // 👋 흔드는 손
        EmojiVO(name1 = "✨"),           // 반짝이는 별
        EmojiVO(name1 = "\uD83E\uDD79"), // 🥹 참는 얼굴
        EmojiVO(name1 = "\uD83D\uDE0B"), // 😋 맛있는 얼굴
        EmojiVO(name1 = "\uD83E\uDD7A"), // 🥺 애원하는 얼굴
        EmojiVO(name1 = "\uD83D\uDE1D"), // 😝 윙크하며 혀 내밀기
        EmojiVO(name1 = "\uD83D\uDE1E"), // 😞 실망한 얼굴
        EmojiVO(name1 = "\uD83D\uDE2E"), // 😮 놀란 얼굴
        EmojiVO(name1 = "\uD83D\uDC96"), // 💖 반짝이는 하트
        EmojiVO(name1 = "\uD83D\uDC97"), // 💗 성장하는 하트
        EmojiVO(name1 = "\uD83D\uDC93"), // 💓 beating 하트
        EmojiVO(name1 = "\uD83D\uDC9E"), // 💞 회전하는 하트
        EmojiVO(name1 = "\uD83D\uDC9A"), // 💚 초록 하트
        EmojiVO(name1 = "\uD83D\uDC99"), // 💙 파란 하트
        EmojiVO(name1 = "\uD83D\uDC9C"), // 💜 보라 하트
        EmojiVO(name1 = "\uD83E\uDD0D"), // 🤍 하얀 하트
        EmojiVO(name1 = "\uD83E\uDD0E"), // 🤎 갈색 하트
        EmojiVO(name1 = "\uD83D\uDDA4"), // 🖤 검정 하트
        EmojiVO(name1 = "\uD83E\uDD42"), // 🥂 건배
        EmojiVO(name1 = "\uD83C\uDF89"), // 🎉 파티 폭죽
        EmojiVO(name1 = "\uD83C\uDF8A"), // 🎊 색종이
        EmojiVO(name1 = "\uD83D\uDE48"), // 🙈 보기 싫은 원숭이
        EmojiVO(name1 = "\uD83D\uDC98"), // 💘 화살 맞은 하트
        EmojiVO(name1 = "\uD83D\uDE0F"), // 😏 씩 웃는 얼굴
        EmojiVO(name1 = "\uD83D\uDE01"), // 😁 히히 웃는 얼굴
        EmojiVO(name1 = "\uD83D\uDE2C"), // 😬 찡그린 얼굴
        EmojiVO(name1 = "\uD83E\uDD2D"), // 🤭 손으로 가린 웃음
        EmojiVO(name1 = "\uD83E\uDD2B"), // 🤫 쉿
        EmojiVO(name1 = "\uD83D\uDE12"), // 😒 썩소
        EmojiVO(name1 = "\uD83D\uDE29"), // 😩 지친 얼굴
        EmojiVO(name1 = "\uD83D\uDE35"), // 😵 현기증 얼굴
        EmojiVO(name1 = "\uD83E\uDD2F"), // 🤯 폭발하는 머리
        EmojiVO(name1 = "⭐"),          // ⭐ 별
        EmojiVO(name1 = "\uD83D\uDC8E"), // 💎 보석
        EmojiVO(name1 = "\uD83C\uDF1F"), // 🌟 빛나는 별
        EmojiVO(name1 = "\uD83D\uDC8B"), // 💋 키스마크
        EmojiVO(name1 = "\uD83D\uDC40")  // 👀 눈
    )

    private val onGlobalAdapterClickListener = object : OnGlobalAdapterClickListener {
        override fun onGlobalAdapterItemClick(v: View, item2: Any, pos: Int) {
            val item = item2 as EmojiVO
            callBack(item.name1)
            // dismiss() // 자동으로 닫지 않음
        }
    }

    init {
        setupView()
    }

    private fun setupView() {
        binding = EmojiPopupBinding.inflate(LayoutInflater.from(context))
        binding.click = this

        // PopupWindow 설정
        contentView = binding.root
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.WRAP_CONTENT

        // 배경 투명하게
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 터치 가능하게
        isTouchable = true
        isFocusable = false // 키보드 유지를 위해 false
        isOutsideTouchable = true

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val brs = HashMap<Int, OnGlobalAdapterClickListener>()
        brs[BR.itemClick] = onGlobalAdapterClickListener
        adapter = GlobalAdapter(0, R.layout.emoji_item, 0, BR.model, onGlobalAdapterClickListener, brs)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }

        adapter.submitList(items as ArrayList<GlobalVO>)
    }

    fun showAboveKeyboard() {
        // 키보드 높이 계산
        val rootView = (context as Activity).findViewById<View>(android.R.id.content)
        val screenHeight = rootView.height
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        val keyboardHeight = screenHeight - rect.bottom

        if (keyboardHeight > 200) { // 키보드가 올라와 있으면
            showAtLocation(anchorView, Gravity.BOTTOM, 0, keyboardHeight)
        } else { // 키보드가 없으면 하단에
            showAtLocation(anchorView, Gravity.BOTTOM, 0, 0)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_close -> {
                callBack(null)
                dismiss()
            }
        }
    }
}