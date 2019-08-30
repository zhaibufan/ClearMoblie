package efficient.com.clearmobile.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import efficient.com.clearmobile.utils.ActivityManagers

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutRes())
        ActivityManagers.push(this)
        initData()
        initView()
        doThings()
    }

    abstract fun getLayoutRes(): Int
    abstract fun initData()
    abstract fun initView()
    abstract fun doThings()

    override fun onDestroy() {
        super.onDestroy()
        ActivityManagers.remove(this)
    }
}