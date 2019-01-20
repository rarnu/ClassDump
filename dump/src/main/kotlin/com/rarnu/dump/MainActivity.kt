package com.rarnu.dump

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import com.rarnu.kt.android.alert
import com.rarnu.kt.android.resStr
import com.rarnu.kt.android.toEditable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_app.view.*

class MainActivity : Activity() {

    private val reqIdChooseApp = Menu.FIRST + 1
    private val menuIdInfo = Menu.FIRST + 2
    private var selectedApp: ApplicationInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }

        btnChooseApp.setOnClickListener { startActivityForResult(Intent(this, ChooseAppActivity::class.java), reqIdChooseApp) }
        btnDump.setOnClickListener { it ->
            if (Dump.isRunning) {
                Dump.isRunning = false
                btnDump.text = resStr(R.string.btn_start_dump)
            } else {
                if (selectedApp != null) {
                    tvLog.append("start dump ...\n")
                    Dump.dump(this, selectedApp!!.publicSourceDir, edtFilter.text.toString(), "${selectedApp!!.packageName}.dump", {
                        tvLog.append("$it\n")
                        svLog.fullScroll(ScrollView.FOCUS_DOWN)
                    }) {
                        tvLog.append("dump completed.\n")
                        btnDump.text = resStr(R.string.btn_start_dump)
                    }
                    btnDump.text = resStr(R.string.btn_stop_dump)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        val m = menu.add(0, menuIdInfo, 0, R.string.menu_info)
        m.setIcon(android.R.drawable.ic_menu_info_details)
        m.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            menuIdInfo -> alert(resStr(R.string.alert_title), resStr(R.string.alert_message), resStr(R.string.alert_ok)) { }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) return
        when(requestCode) {
            reqIdChooseApp -> {
                if (data != null) {
                    selectedApp = data.getParcelableExtra("item")
                    if (selectedApp != null) {
                        layAppItem.ivIcon.setImageDrawable(selectedApp?.loadIcon(packageManager))
                        layAppItem.tvAppName.text = selectedApp?.loadLabel(packageManager)
                        layAppItem.tvAppPackage.text = selectedApp?.packageName
                        edtFilter.text = selectedApp?.packageName?.toEditable()
                        laySelected.visibility = View.VISIBLE
                        layBottom.visibility = View.VISIBLE
                    }
                }
            }
        }

    }


    /*
    Dump.dump(this, wxPath, "com.tencent", "wxdump", {

            }, {

            })
     */


}


