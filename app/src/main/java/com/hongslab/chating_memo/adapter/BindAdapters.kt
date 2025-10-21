package com.hongslab.chating_memo.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.material.card.MaterialCardView
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.BR
import com.hongslab.chating_memo.MyApplication
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.adapter.OnGlobalAdapterClickListener
import com.hongslab.chating_memo.dialog.EmojiMapper
import com.hongslab.chating_memo.models.ChatRoomVO
import com.hongslab.chating_memo.models.GlobalVO
import com.hongslab.chating_memo.models.ImageVO
import com.hongslab.chating_memo.models.NoticeTextState
import com.hongslab.chating_memo.utils.AdUtils
import com.hongslab.chating_memo.utils.IndentLeadingMarginSpan
import com.hongslab.chating_memo.utils.MyUtils
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre

object BindAdapters {
    @JvmStatic
    @BindingAdapter("A_head", "B_body", "C_foot", "list", "click")
    fun <T> setRecyclerView(
        view: RecyclerView,
        head: Int?,
        body: Int?,
        foot: Int?,
        list: ArrayList<T>?,
        click: OnGlobalAdapterClickListener?
    ) {
        list?.let {
            Dlog.d("******************* ${it.size}")
            val brs = HashMap<Int, OnGlobalAdapterClickListener>()
            if (click != null) {
                brs[BR.itemClick] = click
            }
            val existingAdapter = view.getTag(R.id.recycler_view_adapter) as? GlobalAdapter
            val adapter = existingAdapter ?: GlobalAdapter(head!!, body!!, foot!!, BR.model, click, brs).also { it2 ->
                Dlog.d("create")
                view.adapter = it2
                view.setTag(R.id.recycler_view_adapter, it2)  // 어댑터를 RecyclerView 태그에 저장
            }
            adapter.submitList(list as ArrayList<GlobalVO>)
        }
    }

    @JvmStatic
    @BindingAdapter("A_head", "B_body", "C_foot", "list", "click")
    fun <T> setViewPager(
        view: ViewPager2,
        head: Int?,
        body: Int?,
        foot: Int?,
        list: ArrayList<T>?,
        click: OnGlobalAdapterClickListener?
    ) {
        list?.let {
            Dlog.d("******************* ViewPager ${it.size}")
            val brs = HashMap<Int, OnGlobalAdapterClickListener>()
            if (click != null) {
                brs[BR.itemClick] = click
            }

            val existingAdapter = view.getTag(R.id.recycler_view_adapter) as? GlobalAdapter
            val adapter = existingAdapter ?: GlobalAdapter(head!!, body!!, foot!!, BR.model, click, brs).also { it2 ->
                Dlog.d("create ViewPager adapter")
                view.adapter = it2
                view.setTag(R.id.recycler_view_adapter, it2)  // 어댑터를 ViewPager2 태그에 저장
            }
            adapter.submitList(list as ArrayList<GlobalVO>)
        }
    }


    @JvmStatic
    @BindingAdapter("setCircleImage")
    fun setCircleImage(view: ImageView, url: String?) {
        if (!url.isNullOrEmpty()) { // url이 null 또는 빈 문자열인지 확인
            Glide.with(MyApplication.INSTANCE!!)
                .load(url)
                .error(R.drawable.progress_animation)
                .placeholder(R.drawable.progress_animation)
                .apply(RequestOptions().circleCrop())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    @JvmStatic
    @BindingAdapter("setRoundImage")
    fun setRoundImage(view: ImageView, url: String?) {
        if (!url.isNullOrEmpty()) { // url이 null 또는 빈 문자열인지 확인
            Glide.with(MyApplication.INSTANCE!!)
                .load(url)
                .error(R.drawable.progress_animation)
                .placeholder(R.drawable.progress_animation)
                .transform(CenterCrop(), RoundedCorners(MyUtils.dpToPx(10)))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    @JvmStatic
    @BindingAdapter("setImage")
    fun setImage(view: ImageView, url: String?) {
        if (!url.isNullOrEmpty()) { // url이 null 또는 빈 문자열인지 확인
            Glide.with(MyApplication.INSTANCE!!)
                .load(url)
                .fitCenter()
                .error(R.drawable.progress_animation)
                .placeholder(R.drawable.progress_animation)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    @JvmStatic
    @BindingAdapter("setImageRes")
    fun setImageRes(imageView: ImageView, drawable: Drawable?) {
        drawable?.let {
            imageView.setImageDrawable(it)
        }
    }

    @JvmStatic
    @BindingAdapter("setNoticeText")
    fun setNoticeText(textView: TextView, item: NoticeTextState?) {
        if (item == null) {
            return
        }

        val emojiDecode = EmojiMapper.decodeEmojis(item.message)
        textView.text = emojiDecode

        if (item.isExpand) {
            textView.maxLines = Int.MAX_VALUE
            textView.ellipsize = null
        } else {
            textView.maxLines = 1
            textView.ellipsize = TextUtils.TruncateAt.END
        }
    }

    @JvmStatic
    @BindingAdapter("setCardBackgroundColor")
    fun setCardBackgroundColor(view: MaterialCardView, colorString: String?) {
        colorString?.let {
            try {
                view.setCardBackgroundColor(Color.parseColor(it))
            } catch (e: Exception) {
                // 기본 색상으로 설정
                view.setCardBackgroundColor(Color.parseColor("#FFEB33"))
            }
        }
    }


    @JvmStatic
    @BindingAdapter("setSubsamplingScaleImageView")
    fun setSubsamplingScaleImageView(view: SubsamplingScaleImageView, url: String?) {

        Glide.with(MyApplication.INSTANCE!!)
            .asBitmap()
            .load(url)
            .placeholder(R.drawable.progress_animation)
            .error(R.drawable.expired_placeholder_dark_square)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    Dlog.d("setSubsamplingScaleImageView: $resource")
                    view.setImage(ImageSource.cachedBitmap(resource))
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Dlog.e("setSubsamplingScaleImageView: Load failed")

                    // 기본 에러 이미지 설정
                    val errorBitmap = BitmapFactory.decodeResource(
                        MyApplication.INSTANCE!!.resources,
                        R.drawable.expired_placeholder_dark_square
                    )
                    view.setImage(ImageSource.cachedBitmap(errorBitmap))
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // 필요 시, 리소스가 해제될 때 처리할 작업
                    
                }
            })
    }

    @JvmStatic
    @BindingAdapter("isCompletedText")
    fun isCompletedText(textView: TextView, item: ChatRoomVO) {
        if (item.isCompleted) {
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        textView.text = item.message
    }


    @JvmStatic
    @BindingAdapter("setLeadingMarginText")
    fun setLeadingMarginText(textView: TextView, text: String) {
        textView.text = SpannableStringBuilder(text).apply {
            setSpan(IndentLeadingMarginSpan(), 0, length, 0)
        }
    }

    @JvmStatic
    @BindingAdapter("loadAdBanner")
    fun loadAdBanner(view: FrameLayout, item: GlobalVO) {
        try {
            view.removeAllViews()

            val adBottomView = AdUtils(MyApplication.INSTANCE!!).getBanner()
            if (adBottomView == null) {
                view.setPadding(0, MyUtils.dpToPx(10), 0, 0)
            } else {
                view.addView(adBottomView)
                view.setPadding(0, MyUtils.dpToPx(10), 0, MyUtils.dpToPx(10))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}