package com.hongslab.chating_memo.utils
import android.util.Log
import com.hongslab.chating_memo.MyApplication



class Dlog {
    companion object {
        private const val TAG = "####"

        @JvmStatic
        fun e(message: String) {
            if (MyApplication.DEBUG) Log.e(TAG, buildLogMsg(message))
        }

        @JvmStatic
        fun w(message: String) {
            if (MyApplication.DEBUG) Log.w(TAG, buildLogMsg(message))
        }

        @JvmStatic
        fun i(message: String) {
            if (MyApplication.DEBUG) Log.i(TAG, buildLogMsg(message))
        }

        @JvmStatic
        fun d(message: String) {
            if (MyApplication.DEBUG) Log.d(TAG, buildLogMsg(message))
        }

        @JvmStatic
        fun v(message: String) {
            if (MyApplication.DEBUG) Log.v(TAG, buildLogMsg(message))
        }

        private fun buildLogMsg(message: String): String {
            val ste = Thread.currentThread().stackTrace[4]
            val sb = StringBuilder()

            sb.append("[")
            sb.append(ste.fileName.replace(".java", ""))
            sb.append("::")
            sb.append(ste.methodName)
            sb.append("]")
            sb.append(message)
            return sb.toString()
        }

    }
}