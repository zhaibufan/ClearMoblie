package efficient.com.clearmobile.utils

import android.util.Log

class LogUtils {

    companion object {
        private var logSwitch = false
        private const val NORMAL_TAG = "LogUtils"
        fun d(msg: String) {
            if (logSwitch) {
                Log.d(NORMAL_TAG, msg)
            }
        }

        fun i(msg: String) {
            if (logSwitch) {
                Log.i(NORMAL_TAG, msg)
            }
        }

        fun w(msg: String) {
            if (logSwitch) {
                Log.w(NORMAL_TAG, msg)
            }
        }

        fun e(msg: String) {
            if (logSwitch) {
                Log.e(NORMAL_TAG, msg)
            }
        }


        fun d(tag: String, msg: String) {
            Log.d(tag, msg)
        }

        fun i(tag: String, msg: String) {
            Log.i(tag, msg)
        }

        fun w(tag: String, msg: String) {
            Log.w(tag, msg)
        }

        fun e(tag: String, msg: String) {
            Log.e(tag, msg)
        }
    }

}