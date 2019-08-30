package efficient.com.clearmobile

import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.IPackageDataObserver
import android.content.pm.IPackageStatsObserver
import android.content.pm.PackageManager
import android.content.pm.PackageStats
import android.graphics.Color
import android.os.Build
import android.os.RemoteException
import android.os.SystemClock
import android.os.storage.StorageManager
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.design.widget.AppBarLayout
import android.text.format.Formatter
import android.util.Log
import android.view.View
import efficient.com.clearmobile.base.BaseActivity
import efficient.com.clearmobile.utils.LogUtils
import efficient.com.clearmobile.widget.AppBarStateChangeListener
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"

class MainActivity : BaseActivity() {

    private lateinit var pm : PackageManager

    override fun getLayoutRes(): Int {
        return R.layout.activity_main
    }

    override fun initData() {
        if (!checkAppUsagePermission(this)) {
            requestAppUsagePermission(this)
        }
    }

    override fun initView() {
        setSupportActionBar(toolbar)
        collapsing_toolbar_layout.run {
            title = "Clear Mobile"
            setExpandedTitleColor(Color.BLACK)
            setCollapsedTitleTextColor(Color.WHITE)
        }
        appbar_layout.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
                when (state) {
                    State.COLLAPSED -> {
                        LogUtils.d(TAG, "COLLAPSED")
                    }
                    State.EXPANDED -> {
                        LogUtils.d(TAG, "EXPANDED")
                    }
                    State.IDLE -> {
                        LogUtils.d(TAG, "IDLE")
                    }
                }
            }
        })
    }

    private fun checkAppUsagePermission(context: Context): Boolean {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val currentTime = System.currentTimeMillis()
        // try to get app usage state in last 1 min
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 60 * 1000, currentTime)
        return stats.size != 0
    }

    private fun requestAppUsagePermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun doThings() {
        scanner()
    }

    private fun scanner() {
        pm = packageManager
        object : Thread() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            override fun run() {
                SystemClock.sleep(100)
                val installedPackages = pm.getInstalledPackages(0)
                //设置进度条最大进度
                var count = 0
                for (packageInfo in installedPackages) {
                    SystemClock.sleep(100)
                    //设置进度条最大进度和当前进度
                    count++

                    //设置扫描显示的应用的名称
                    val name = packageInfo.applicationInfo.loadLabel(pm).toString()


                    //获取缓存大小
                    //反射获取缓存
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        try {
                            val loadClass = this@MainActivity.javaClass.getClassLoader()!!.loadClass("android.content.pm.PackageManager")
                            val method = loadClass.getDeclaredMethod("getPackageSizeInfo", String::class.java, IPackageStatsObserver::class.java)
                            //receiver : 类的实例,隐藏参数,方法不是静态的必须指定
                            method.invoke(pm, packageInfo.packageName, mStatsObserver)
                        } catch (e: Exception) {
                            // TODO Auto-generated catch block
                            e.printStackTrace()
                            Log.e(TAG, "error = " + e.message)
                        }

                    } else {
                        try {
                            val storageStatsManager = getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                            val storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
                            val info = packageManager.getApplicationInfo(packageInfo.packageName, 0)
                            val storageStats = storageStatsManager.queryStatsForUid(info.storageUuid, info.uid)
                            val appBytes = storageStats.appBytes
                            val cacheBytes = storageStats.cacheBytes
                            val dataBytes = storageStats.dataBytes
                            LogUtils.e(TAG, " cacheBytes = $cacheBytes  name = $name")
                            if (cacheBytes > 0) {
                                val cache = Formatter.formatFileSize(this@MainActivity, cacheBytes)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }
                //扫描完成
                if (this@MainActivity != null) {
                    runOnUiThread {
                        try {
                            val loadClass = this@MainActivity.javaClass.getClassLoader()!!.loadClass("android.content.pm.PackageManager")
                            //Long.class  Long     TYPE  long
                            val method = loadClass.getDeclaredMethod("freeStorageAndNotify", java.lang.Long.TYPE, IPackageDataObserver::class.java)
                            method.invoke(pm, java.lang.Long.MAX_VALUE, MyIPackageDataObserver())
                        } catch (e: Exception) {
                            // TODO Auto-generated catch block
                            e.printStackTrace()
                        }
                    }
                }
            }
        }.start()
    }

    //获取缓存大小
    internal var mStatsObserver: IPackageStatsObserver.Stub = object : IPackageStatsObserver.Stub() {
        override fun onGetStatsCompleted(stats: PackageStats, succeeded: Boolean) {
            val cachesize = stats.cacheSize//缓存大小
            LogUtils.d(TAG, "cachesize = $cachesize")
            /*long codesize = stats.codeSize;//应用程序的大小
        	long datasize = stats.dataSize;//数据大小
*/            if (cachesize > 0) {
                val cache = Formatter.formatFileSize(this@MainActivity, cachesize)
            }
            /*String code = Formatter.formatFileSize(getActivity(), codesize);
        	String data = Formatter.formatFileSize(getActivity(), datasize);*/
            //        	System.out.println(stats.packageName+"cachesize:"+cache +" codesize:"+code+" datasize:"+data);
        }
    }

    private inner class MyIPackageDataObserver : IPackageDataObserver.Stub() {
        //当缓存清理完成之后调用
        @Throws(RemoteException::class)
        override fun onRemoveCompleted(packageName: String, succeeded: Boolean) {
            LogUtils.d(TAG, "succeeded = $succeeded  packageName = $packageName")
        }
    }
}
