package com.hongslab.chating_memo.adapter

import android.R.attr.visibility
import android.app.ProgressDialog.show
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
                binding.llCheckBoxArea.visibility = View.VISIBLE
                if (item.isChecked) {
                    binding.btnCheckBox.setImageResource(R.drawable.ic_check)
                } else {
                    binding.btnCheckBox.setImageResource(R.drawable.ic_uncheck)
                }
            } else {
                binding.llCheckBoxArea.visibility = View.INVISIBLE
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

            binding.llCheckBoxArea.setOnClickListener { view ->
                item.isChecked = !item.isChecked

                // 체크 상태만 업데이트
                updateCheckState(item.isChecked)
            }
        }

        fun updateCheckBoxVisibility(isShow: Boolean) {
            Dlog.d("updateCheckBoxVisibility: $isShow")
            binding.llCheckBoxArea.visibility = if (isShow) View.VISIBLE else View.INVISIBLE
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
        fun bind(item: ChatMessageVO) {
            binding.tvTimeText.text = MyUtils.formatTimeWithAmPm(item.created)
            setupImages(item)

            // 더보기 클릭
            binding.btnImageMore.setOnClickListener { view ->
                onGlobalAdapterClickListener.onGlobalAdapterItemClick(view, item, bindingAdapterPosition)
            }

            // 이미지 그리드 클릭
            binding.imageContainer.setOnClickListener { view ->
                onGlobalAdapterClickListener.onGlobalAdapterItemClick(view, item, bindingAdapterPosition)
            }
        }

        fun bindPartial(item: ChatMessageVO, payloads: List<String>) {
            payloads.forEach { payload ->
                when (payload) {
                    PAYLOAD_IMAGES -> setupImages(item)
                }
            }
        }

        private fun setupImages(item: ChatMessageVO) {
            // 모든 레이아웃 숨기기
            binding.layoutSingle.visibility = View.GONE
            binding.layoutTwo.visibility = View.GONE
            binding.layoutThree.visibility = View.GONE
            binding.layoutFour.visibility = View.GONE
            binding.layoutFive.visibility = View.GONE
            binding.layoutSix.visibility = View.GONE
            binding.layoutMany.visibility = View.GONE

            val imageUrls: List<String> = item.link.split(",")
                .filter { it.isNotBlank() }
                .map { it.trim() } // Remove whitespace

            when (imageUrls.size) {
                1 -> setupSingleImage(imageUrls)
                2 -> setupTwoImages(imageUrls)
                3 -> setupThreeImages(imageUrls)
                4 -> setupFourImages(imageUrls)
                5 -> setupFiveImages(imageUrls)
                6 -> setupSixImages(imageUrls)
                in 7..10 -> setupManyImages(imageUrls)
            }
        }

        private fun setupSingleImage(urls: List<String>) {
            binding.layoutSingle.visibility = View.VISIBLE
            loadImage(binding.layoutSingle, urls[0], 0, urls)
        }

        private fun setupTwoImages(urls: List<String>) {
            binding.layoutTwo.visibility = View.VISIBLE
            loadImage(binding.imageTwo1, urls[0], 0, urls)
            loadImage(binding.imageTwo2, urls[1], 1, urls)
        }

        private fun setupThreeImages(urls: List<String>) {
            binding.layoutThree.visibility = View.VISIBLE
            loadImage(binding.imageThree1, urls[0], 0, urls)
            loadImage(binding.imageThree2, urls[1], 1, urls)
            loadImage(binding.imageThree3, urls[2], 2, urls)
        }

        private fun setupFourImages(urls: List<String>) {
            binding.layoutFour.visibility = View.VISIBLE
            loadImage(binding.imageFour1, urls[0], 0, urls)
            loadImage(binding.imageFour2, urls[1], 1, urls)
            loadImage(binding.imageFour3, urls[2], 2, urls)
            loadImage(binding.imageFour4, urls[3], 3, urls)
        }

        private fun setupFiveImages(urls: List<String>) {
            binding.layoutFive.visibility = View.VISIBLE
            loadImage(binding.imageFive1, urls[0], 0, urls)
            loadImage(binding.imageFive2, urls[1], 1, urls)
            loadImage(binding.imageFive3, urls[2], 2, urls)
            loadImage(binding.imageFive4, urls[3], 3, urls)
            loadImage(binding.imageFive5, urls[4], 4, urls)
        }

        private fun setupSixImages(urls: List<String>) {
            binding.layoutSix.visibility = View.VISIBLE
            loadImage(binding.imageSix1, urls[0], 0, urls)
            loadImage(binding.imageSix2, urls[1], 1, urls)
            loadImage(binding.imageSix3, urls[2], 2, urls)
            loadImage(binding.imageSix4, urls[3], 3, urls)
            loadImage(binding.imageSix5, urls[4], 4, urls)
            loadImage(binding.imageSix6, urls[5], 5, urls)
        }

        private fun setupManyImages(urls: List<String>) {
            binding.layoutMany.visibility = View.VISIBLE

            val imageViews = listOf(
                binding.imageMany1, binding.imageMany2, binding.imageMany3,
                binding.imageMany4, binding.imageMany5, binding.imageMany6,
                binding.imageMany7, binding.imageMany8, binding.imageMany9,
                binding.imageMany10
            )

            imageViews.forEach { it.visibility = View.GONE }

            urls.take(10).forEachIndexed { index, url ->
                imageViews[index].visibility = View.VISIBLE
                loadImage(imageViews[index], url, index, urls)
            }
        }

        private fun loadImage(imageView: ImageView, url: String, position: Int, allUrls: List<String>) {
            Glide.with(imageView.context)
                .load(url)
                .placeholder(R.drawable.expired_placeholder_dark_square)
                .error(R.drawable.expired_placeholder_dark_square)
                .centerCrop()
                .into(imageView)
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