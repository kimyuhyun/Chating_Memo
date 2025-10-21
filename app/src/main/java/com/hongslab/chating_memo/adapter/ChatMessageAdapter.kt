package com.hongslab.chating_memo.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.util.Linkify
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.util.LinkifyCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hongslab.chating_memo.MyApplication
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.databinding.ChatDummyItemBinding
import com.hongslab.chating_memo.databinding.MeMessageItemBinding
import com.hongslab.chating_memo.databinding.MePhotoItemBinding
import com.hongslab.chating_memo.databinding.SystemMessageItemBinding
import com.hongslab.chating_memo.dialog.EmojiMapper
import com.hongslab.chating_memo.manager.CloudinaryUtils
import com.hongslab.chating_memo.models.ChatMessageVO
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.MyUtils
import com.hongslab.chating_memo.utils.MyUtils.Companion.highlightSearchText
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre

class ChatMessageAdapter(
    private val onGlobalAdapterClickListener: OnGlobalAdapterClickListener,
    private val cateColor: String,
) : ListAdapter<ChatMessageVO, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
    private var searchText = ""
    private var isCheckBoxShow = false

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChatMessageVO>() {
            override fun areItemsTheSame(oldItem: ChatMessageVO, newItem: ChatMessageVO): Boolean {
                // uid로 아이템 동일성 판단 (더 정확함)
                return oldItem.uid == newItem.uid
            }

            override fun areContentsTheSame(oldItem: ChatMessageVO, newItem: ChatMessageVO): Boolean {
                return oldItem.uid == newItem.uid &&
                        oldItem.idx == newItem.idx &&
                        oldItem.message == newItem.message &&
                        oldItem.msgType == newItem.msgType &&
                        oldItem.link == newItem.link &&
                        oldItem.created == newItem.created
//                        compareChildListsByUid(oldItem.child, newItem.child) // 추가
            }

            override fun getChangePayload(oldItem: ChatMessageVO, newItem: ChatMessageVO): Any? {
                val changes = mutableListOf<String>()

                if (oldItem.message != newItem.message) changes.add(PAYLOAD_MESSAGE)
                if (oldItem.link != newItem.link) changes.add(PAYLOAD_IMAGES)

                Dlog.d("DiffUtil getChangePayload - uid: ${oldItem.uid}, changes: $changes")

                return changes.ifEmpty { null }
            }
        }

        const val VIEW_TYPE_SYSTEM = 9
        const val VIEW_TYPE_TEXT = 1
        const val VIEW_TYPE_IMAGE = 2
        const val VIEW_TYPE_DUMMY = 3

        // Payload constants
        private const val PAYLOAD_MESSAGE = "message"
        private const val PAYLOAD_CHECKBOX = "checkbox"
        private const val PAYLOAD_CHECK_STATE = "check_state"
        private const val PAYLOAD_IMAGES = "images"
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).msgType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_TEXT -> MeViewHolder(MeMessageItemBinding.inflate(inflater, parent, false))
            VIEW_TYPE_IMAGE -> MePhotoViewHolder(MePhotoItemBinding.inflate(inflater, parent, false))
            VIEW_TYPE_SYSTEM -> SystemViewHolder(SystemMessageItemBinding.inflate(inflater, parent, false))
            else -> ChatDummyViewHolder(ChatDummyItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = getItem(position)

        when (holder) {
            is MeViewHolder -> {
                if (payloads.isEmpty()) {
                    holder.bind(item)
                } else {
                    // payloads가 List<String> 형태로 올 수 있음
                    val stringPayloads = mutableListOf<String>()
                    payloads.forEach { payload ->
                        when (payload) {
                            is String -> stringPayloads.add(payload)
                            is List<*> -> {
                                payload.filterIsInstance<String>().forEach { stringPayloads.add(it) }
                            }
                        }
                    }

                    if (PAYLOAD_CHECKBOX in stringPayloads) {
                        holder.updateCheckBoxVisibility(isCheckBoxShow)
                    } else {
                        holder.bindPartial(item, stringPayloads)
                    }
                }
            }

            is MePhotoViewHolder -> {
                if (payloads.isEmpty()) {
                    holder.bind(item)
                } else {
                    val stringPayloads = payloads.filterIsInstance<String>()
                    holder.bindPartial(item, stringPayloads)
                }
            }

            is SystemViewHolder -> holder.bind(item)
            is ChatDummyViewHolder -> holder.bind(item)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is MeViewHolder -> {
                holder.cleanup()
            }

            is MePhotoViewHolder -> holder.cleanup()
        }
    }

    // System Message ViewHolder
    inner class SystemViewHolder(private val binding: SystemMessageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatMessageVO) {
            binding.tvMessage.text = MyUtils.formatToYMDWithDayOfWeek(item.created)
        }
    }

    // Text Message ViewHolder
    inner class MeViewHolder(private val binding: MeMessageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private var currentItem: ChatMessageVO? = null
        fun bind(item: ChatMessageVO) {
            cleanup()
            currentItem = item

            bindMessage(item)
            bindTimeText(item)
            bindClickListeners(item)
        }

        fun bindPartial(item: ChatMessageVO, changes: List<String>) {
            currentItem = item

            changes.forEach { change ->
                Dlog.d("Processing change: $change")
                when (change) {
                    PAYLOAD_MESSAGE -> bindMessage(item)
                    PAYLOAD_CHECK_STATE -> updateCheckState(item.isChecked)
                }
            }
        }

        private fun bindMessage(item: ChatMessageVO) {
            if (cateColor != "") {
                binding.cvMessageBox.backgroundTintList = ColorStateList.valueOf(Color.parseColor(cateColor))
                binding.ivMessageTail.imageTintList = ColorStateList.valueOf(Color.parseColor(cateColor))
            }

            val decodeMessage = EmojiMapper.decodeEmojis(item.message)

            // 검색어 하이라이트 적용
            val highlightedText = highlightSearchText(decodeMessage, searchText)
            binding.tvMessage.text = highlightedText

            // 폰트사이즈 지정!
            val messageFontSize = SPre.get(SCol.MESSAGE_FONT_SIZE.name)?.toFloatOrNull() ?: 15f
            binding.tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, messageFontSize)

            // 화면 2/3 비율로 maxWidth 적용
            val displayMetrics = binding.root.context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            binding.tvMessage.maxWidth = (screenWidth * 60 / 100)  // 화면의 60%

            // 스트라이크 처리 (예: isCompleted가 true일 때)
            if (item.isCompleted) {
                binding.tvMessage.paintFlags = binding.tvMessage.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvMessage.paintFlags = binding.tvMessage.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            // 링크 설정
            LinkifyCompat.addLinks(binding.tvMessage, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
            binding.tvMessage.setLinkTextColor(
                ContextCompat.getColor(binding.root.context, R.color.systemBlue)
            )

            // 체크박스 쇼/히든
            if (isCheckBoxShow) {
                binding.btnCheckBox.visibility = View.VISIBLE
                if (item.isChecked) {
                    binding.btnCheckBox.setImageResource(R.drawable.ic_check)
                } else {
                    binding.btnCheckBox.setImageResource(R.drawable.ic_uncheck)
                }
            } else {
                binding.btnCheckBox.visibility = View.INVISIBLE
            }
        }

        private fun bindTimeText(item: ChatMessageVO) {
            binding.tvTimeText.text = MyUtils.formatTimeWithAmPm(item.created)
        }


        private fun bindClickListeners(item: ChatMessageVO) {
            binding.cvMessageBox.setOnLongClickListener { view ->
                onGlobalAdapterClickListener.onGlobalAdapterItemClick(view, item, bindingAdapterPosition)
                true
            }

            binding.btnCheckBox.setOnClickListener { view ->
                item.isChecked = !item.isChecked

                // 체크 상태만 업데이트
                updateCheckState(item.isChecked)
            }
        }

        fun updateCheckBoxVisibility(show: Boolean) {
            binding.btnCheckBox.visibility = if (show) View.VISIBLE else View.INVISIBLE
        }

        fun updateCheckState(isChecked: Boolean) {
            if (isCheckBoxShow) {
                binding.btnCheckBox.setImageResource(
                    if (isChecked) R.drawable.ic_check else R.drawable.ic_uncheck
                )
            }
        }

        fun cleanup() {
            currentItem = null
        }


    }

    // Photo Message ViewHolder
    inner class MePhotoViewHolder(private val binding: MePhotoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private var currentItem: ChatMessageVO? = null
        private val imageViewPool = mutableListOf<ImageView>()

        fun bind(item: ChatMessageVO) {
            cleanup()
            currentItem = item

            bindTimeText(item)
            bindImages(item)
            bindClickListeners(item)
        }

        fun bindPartial(item: ChatMessageVO, changes: List<String>) {
            currentItem = item

            changes.forEach { change ->
                when (change) {
                    PAYLOAD_IMAGES -> bindImages(item)
                }
            }
        }

        private fun bindTimeText(item: ChatMessageVO) {
            binding.tvTimeText.text = MyUtils.formatTimeWithAmPm(item.created)
        }

        private fun bindImages(item: ChatMessageVO) {
            val urls = item.link.split(",")
                .filter { it.isNotBlank() }
                .map { it.trim() } // Remove whitespace

            when (urls.size) {
                0 -> binding.imageContainer.visibility = View.GONE
                1 -> setupSingleImage(urls)
                2 -> setupTwoImages(urls)
                3 -> setupThreeImages(urls)
                4 -> setupFourImages(urls)
                else -> setupMultipleImages(urls)
            }
        }

        private fun bindClickListeners(item: ChatMessageVO) {
            binding.imageContainer.setOnClickListener { view ->
                onGlobalAdapterClickListener?.onGlobalAdapterItemClick(view, item, bindingAdapterPosition)
            }

            binding.btnImageMore.setOnClickListener { view ->
                onGlobalAdapterClickListener?.onGlobalAdapterItemClick(view, item, bindingAdapterPosition)
            }
        }

        // 이미지 레이아웃 메서드들
        private fun setupSingleImage(urls: List<String>) {
            try {
                binding.imageContainer.removeAllViews()
                binding.imageContainer.visibility = View.VISIBLE

                val imageView = getOrCreateImageView(200, 200)
                loadImage(urls[0], imageView)
                binding.imageContainer.addView(imageView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun setupTwoImages(urls: List<String>) {
            binding.imageContainer.removeAllViews()
            binding.imageContainer.visibility = View.VISIBLE

            val container = createHorizontalContainer()

            urls.take(2).forEach { url ->
                val imageView = getOrCreateImageView(140, 140)
                loadImage(url, imageView)
                container.addView(imageView)
            }

            binding.imageContainer.addView(container)
        }

        private fun setupThreeImages(urls: List<String>) {
            binding.imageContainer.removeAllViews()
            binding.imageContainer.visibility = View.VISIBLE

            val mainContainer = createVerticalContainer()

            // 첫 번째 이미지
            val firstImage = getOrCreateImageView(294, 120)
            loadImage(urls[0], firstImage)
            mainContainer.addView(firstImage)

            // 두 번째, 세 번째 이미지
            val bottomRow = createHorizontalContainer()
            urls.drop(1).take(2).forEach { url ->
                val imageView = getOrCreateImageView(145, 120)
                loadImage(url, imageView)
                bottomRow.addView(imageView)
            }

            mainContainer.addView(bottomRow)
            binding.imageContainer.addView(mainContainer)
        }

        private fun setupFourImages(urls: List<String>) {
            binding.imageContainer.removeAllViews()
            binding.imageContainer.visibility = View.VISIBLE

            val mainContainer = createVerticalContainer()
            val firstRow = createHorizontalContainer()
            val secondRow = createHorizontalContainer()

            urls.take(4).forEachIndexed { index, url ->
                val imageView = getOrCreateImageView(145, 120)
                loadImage(url, imageView)

                if (index < 2) {
                    firstRow.addView(imageView)
                } else {
                    secondRow.addView(imageView)
                }
            }

            mainContainer.addView(firstRow)
            mainContainer.addView(secondRow)
            binding.imageContainer.addView(mainContainer)
        }

        private fun setupMultipleImages(urls: List<String>) {
            // 4장까지만 보여주고 나머지는 +N 표시
            binding.imageContainer.removeAllViews()
            binding.imageContainer.visibility = View.VISIBLE

            val mainContainer = createVerticalContainer()
            val firstRow = createHorizontalContainer()
            val secondRow = createHorizontalContainer()

            urls.take(4).forEachIndexed { index, url ->
                val imageView = getOrCreateImageView(145, 120)
                loadImage(url, imageView)

                if (index < 2) {
                    firstRow.addView(imageView)
                } else {
                    secondRow.addView(imageView)
                }
            }

            mainContainer.addView(firstRow)
            mainContainer.addView(secondRow)
            binding.imageContainer.addView(mainContainer)

            // +N 표시 (이 부분이 실행되는지 확인)
            if (urls.size > 4) {
                Dlog.d("URLs size: ${urls.size}, adding overlay")
                // View가 완전히 레이아웃된 후 실행
                binding.imageContainer.post {
                    addMoreImagesOverlay(urls.size - 4)
                }
            } else {
                Dlog.d("URLs size: ${urls.size}, no overlay needed")
            }
        }

        // 헬퍼 메서드들
        private fun getOrCreateImageView(width: Int, height: Int): ImageView {
            val imageView = if (imageViewPool.isNotEmpty()) {
                imageViewPool.removeAt(0)
            } else {
                createImageView()
            }

            val params = imageView.layoutParams as? LinearLayout.LayoutParams
                ?: LinearLayout.LayoutParams(0, 0)

            params.width = dpToPx(width)
            params.height = dpToPx(height)
            params.setMargins(dpToPx(1), dpToPx(1), dpToPx(1), dpToPx(1))
            imageView.layoutParams = params

            return imageView
        }

        private fun createImageView(): ImageView {
            return ImageView(binding.root.context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundColor(Color.LTGRAY)
            }
        }

        private fun createHorizontalContainer(): LinearLayout {
            return LinearLayout(binding.root.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        private fun createVerticalContainer(): LinearLayout {
            return LinearLayout(binding.root.context).apply {
                orientation = LinearLayout.VERTICAL
            }
        }

        private fun loadImage(url: String, imageView: ImageView) {
            val thumbnailUrl = CloudinaryUtils.getSmallImageUrl(url)

            Glide.with(MyApplication.INSTANCE!!)
                .load(thumbnailUrl)
                .centerCrop()
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.expired_placeholder_dark_square)
                .into(imageView)
        }

        private fun dpToPx(dp: Int): Int {
            return (dp * binding.root.context.resources.displayMetrics.density).toInt()
        }

        fun cleanup() {
            // 이미지뷰들을 풀로 회수
            recycleImageViews(binding.imageContainer)
            binding.imageContainer.removeAllViews()
            binding.imageContainer.setOnClickListener(null)
            currentItem = null
        }

        private fun recycleImageViews(viewGroup: ViewGroup) {
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                when (child) {
                    is ImageView -> {
                        // Clear Glide request before recycling
                        Glide.with(binding.root.context).clear(child)
                        child.setImageDrawable(null) // Clear drawable reference
                        imageViewPool.add(child)
                    }

                    is ViewGroup -> recycleImageViews(child)
                }
            }
        }

        private fun addMoreImagesOverlay(extraCount: Int) {
            try {
                Dlog.d("addMoreImagesOverlay called with extraCount: $extraCount")

                if (binding.imageContainer.childCount == 0) {
                    Dlog.d("No children in imageContainer")
                    return
                }

                // imageContainer -> mainContainer -> secondRow -> lastImageView 순으로 찾기
                val mainContainer = binding.imageContainer.getChildAt(0) as? LinearLayout
                if (mainContainer == null) {
                    Dlog.d("Main container is not LinearLayout")
                    return
                }

                if (mainContainer.childCount < 2) {
                    Dlog.d("Main container doesn't have second row")
                    return
                }

                // 두 번째 행(secondRow) 가져오기
                val secondRow = mainContainer.getChildAt(1) as? LinearLayout
                if (secondRow == null) {
                    Dlog.d("Second row is not LinearLayout")
                    return
                }

                if (secondRow.childCount == 0) {
                    Dlog.d("Second row has no children")
                    return
                }

                // 두 번째 행의 마지막 이미지뷰 가져오기
                val lastImageView = secondRow.getChildAt(secondRow.childCount - 1) as? ImageView
                if (lastImageView == null) {
                    Dlog.d("Last child in second row is not ImageView")
                    return
                }

                Dlog.d("Creating overlay for last image")

                // 원본 LayoutParams 저장
                val originalParams = lastImageView.layoutParams

                // FrameLayout으로 감싸기 (이미지 + 오버레이)
                val frameLayout = FrameLayout(binding.root.context).apply {
                    layoutParams = originalParams
                }

                // 기존 이미지 뷰를 FrameLayout에 추가하기 전에 LayoutParams 조정
                secondRow.removeView(lastImageView)
                lastImageView.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                frameLayout.addView(lastImageView)

                // 반투명 검정 오버레이
                val overlayView = View(binding.root.context).apply {
                    setBackgroundColor(Color.parseColor("#80000000"))
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }

                // +N 텍스트
                val textView = TextView(binding.root.context).apply {
                    text = "+$extraCount"
                    setTextColor(Color.WHITE)
                    textSize = 20f
                    setTypeface(typeface, Typeface.BOLD)
                    gravity = Gravity.CENTER
                    setShadowLayer(4f, 0f, 0f, Color.BLACK)
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }

                // 오버레이와 텍스트 추가
                frameLayout.addView(overlayView)
                frameLayout.addView(textView)

                // FrameLayout을 두 번째 행의 마지막 위치에 삽입
                secondRow.addView(frameLayout)

                Dlog.d("Overlay added successfully")

            } catch (e: Exception) {
                Dlog.e("Error in addMoreImagesOverlay: ${e.message}")
                e.printStackTrace()
            }
        }

    }

    // Dummy ViewHolder
    inner class ChatDummyViewHolder(
        private val binding: ChatDummyItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatMessageVO) {
            // Dummy implementation
        }
    }


    fun setSearchText(query: String) {
        searchText = query
    }

    fun toggleCheckBoxVisibility(isShow: Boolean) {
        isCheckBoxShow = isShow
        notifyItemRangeChanged(0, itemCount, PAYLOAD_CHECKBOX)
    }

}