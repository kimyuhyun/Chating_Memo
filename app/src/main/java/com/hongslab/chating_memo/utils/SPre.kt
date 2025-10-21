package com.hongslab.chating_memo.utils

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.hongslab.chating_memo.MyApplication



class SPre {
    companion object {
        fun get(column: String?): String? {
            val pref: SharedPreferences = MyApplication.INSTANCE!!.getSharedPreferences("USER_INFO", AppCompatActivity.MODE_PRIVATE)
            return pref.getString(column, "")
        }

        fun set(column: String?, value: String?) {
            val pref: SharedPreferences = MyApplication.INSTANCE!!.getSharedPreferences("USER_INFO", AppCompatActivity.MODE_PRIVATE)
            val userInfo = pref.edit()
            userInfo.putString(column, value)
            userInfo.commit()
        }

        fun isFirst(): Boolean {
            val pref: SharedPreferences = MyApplication.INSTANCE!!.getSharedPreferences("USER_INFO", AppCompatActivity.MODE_PRIVATE)
            return pref.getBoolean("IS_FIRST", true)
        }

        fun setFirst(flag: Boolean) {
            val pref: SharedPreferences = MyApplication.INSTANCE!!.getSharedPreferences("USER_INFO", AppCompatActivity.MODE_PRIVATE)
            val userInfo = pref.edit()
            userInfo.putBoolean("IS_FIRST", flag)
            userInfo.commit()
        }
    }
}