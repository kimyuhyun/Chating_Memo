package com.hongslab.chating_memo.views.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.Auth
import com.google.android.material.tabs.TabLayout
import com.hongslab.chating_memo.MyApplication
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.databinding.FragmentDevBinding
import com.hongslab.chating_memo.databinding.FragmentMoreBinding
import com.hongslab.chating_memo.dialog.IosConfirmDialog
import com.hongslab.chating_memo.manager.CloudinaryDeleter
import com.hongslab.chating_memo.repository.AuthRepository
import com.hongslab.chating_memo.repository.ImageRepository
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.MyUtils
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre
import com.hongslab.chating_memo.utils.startActivityForResult2
import com.hongslab.chating_memo.views.ChatMessageAC
import com.hongslab.chating_memo.views.FontSizeSettingAC
import com.hongslab.chating_memo.views.MainAC
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale


class More : Fragment(), View.OnClickListener {
    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!
    private var currentTabIndex = 0

    private val authRepository = AuthRepository()
    private val imageRepository = ImageRepository()

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_font_size_setting -> {
                lifecycleScope.launch {
                    val intent = Intent(requireActivity(), FontSizeSettingAC::class.java)
                    startActivityForResult2(intent)
                }
            }

            R.id.btn_share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.putExtra(Intent.EXTRA_SUBJECT, "'" + getString(R.string.app_name) + "'")
                intent.putExtra(Intent.EXTRA_TEXT, "${getString(R.string.app_name)} https://play.google.com/store/apps/details?id=${requireActivity().packageName}")
                intent.type = "text/plain"
                startActivity(Intent.createChooser(intent, "공유하기"))
            }

            R.id.btn_review -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + requireActivity().packageName))
                startActivity(intent)
            }

            R.id.btn_policy -> {
                val intent = if (Locale.getDefault().language == "ko") {
                    Intent(Intent.ACTION_VIEW, Uri.parse(MyApplication.POLICY_KR))
                } else {
                    Intent(Intent.ACTION_VIEW, Uri.parse(MyApplication.POLICY))
                }
                startActivity(intent)
            }

            R.id.btn_use_terms -> {
                val intent = if (Locale.getDefault().language == "ko") {
                    Intent(Intent.ACTION_VIEW, Uri.parse(MyApplication.USE_TERMS_KR))
                } else {
                    Intent(Intent.ACTION_VIEW, Uri.parse(MyApplication.USE_TERMS))
                }
                startActivity(intent)
            }

            R.id.btn_logout -> {
                IosConfirmDialog(requireActivity(), "로그아웃 하시겠습니까?", true) {
                    if (it) {
                        binding.llLoading.visibility = View.VISIBLE
                        val googleClient = MyApplication.GOOGLE_SIGN_IN_CLIENT
                        googleClient?.signOut()?.addOnCompleteListener { // 구글 세션 종료
                            // 1) 로컬 저장 토큰/유저 정보 삭제
                            SPre.set(SCol.ACCESS_TOKEN.name, "")
                            SPre.set(SCol.REFRESH_TOKEN.name, "")
                            SPre.set(SCol.CURRENT_TAB.name, "")

                            // 2) 로그인 화면으로 이동
                            restartApp(requireActivity())

                        }?.addOnFailureListener {
                            MyUtils.myToast("로그아웃에 실패했어요. 잠시 후 다시 시도해주세요.")
                            binding.llLoading.visibility = View.GONE
                        }
                    }
                }.show()
            }

            R.id.btn_leave -> {
                IosConfirmDialog(requireActivity(), "데이터는 모두 삭제 됩니다.\n계정을 삭제 하시겠습니까?", false) {
                    if (it) {
                        binding.llLoading.visibility = View.VISIBLE

                        val googleClient = MyApplication.GOOGLE_SIGN_IN_CLIENT
                        googleClient?.signOut()?.addOnCompleteListener {
                            SPre.set(SCol.ACCESS_TOKEN.name, "")
                            SPre.set(SCol.REFRESH_TOKEN.name, "")
                        }

                        GlobalScope.launch {
                            Dlog.d("1")
                            val urls = imageRepository.getExpiredImages(1)
                            if (urls.isNotEmpty()) {
                                val deletedCount = CloudinaryDeleter.getInstance().deleteImages(urls)
                                Dlog.d("Deleted $deletedCount out of ${urls.size} images")
                            }
                            Dlog.d("2")
                            authRepository.memberLeave()
                            Dlog.d("3")

                            SPre.set(SCol.ACCESS_TOKEN.name, "")
                            SPre.set(SCol.REFRESH_TOKEN.name, "")
                            SPre.set(SCol.CURRENT_TAB.name, "")

                            restartApp(requireActivity())
                        }
                    }
                }.show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.click = this
        binding.lifecycleOwner = this

        SPre.set(SCol.CURRENT_TAB.name, "2")

        setupVersionInfo()
        setupTabLayout()

        // UI 모드 판단!
        if (SPre.get(SCol.THEME.name) == "c_light") {
            currentTabIndex = 0
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))
        } else if (SPre.get(SCol.THEME.name) == "c_dark") {
            currentTabIndex = 1
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(1))
        } else {
            currentTabIndex = 2
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(2))
        }
    }

    private fun setupVersionInfo() {
        val packageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
        binding.tvVer.text = "Application v${packageInfo.versionName}"
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> SPre.set(SCol.THEME.name, "c_light")
                    1 -> SPre.set(SCol.THEME.name, "c_dark")
                    else -> {
                        SPre.set(SCol.THEME.name, MyUtils.getCurrentSystemTheme(requireActivity()))
                        if (MyUtils.getCurrentSystemTheme(requireActivity()) == "dark") {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        }
                    }
                }

                if (currentTabIndex != tab?.position) {
                    MyUtils.myToast("테마를 적용하기 위해 앱을 재시작 합니다.")
                    Handler(Looper.getMainLooper()).postDelayed({
                        restartApp(requireActivity())
                    }, 500)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }


    private fun restartApp(activity: Activity) {
        val launchIntent = Intent(activity, MainAC::class.java)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(launchIntent)
        activity.finishAffinity()
        Runtime.getRuntime().exit(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}