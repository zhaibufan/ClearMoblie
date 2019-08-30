package efficient.com.clearmobile.utils

import android.app.Activity
import java.util.*

class ActivityManagers{

    companion object {
        var stack = Stack<Activity>()

        fun push(activity: Activity) {
            stack.push(activity)
        }

        fun remove(activity: Activity) {
            if (!activity.isFinishing) {
                activity.finish()
            }
            stack.remove(activity)
        }
    }
}