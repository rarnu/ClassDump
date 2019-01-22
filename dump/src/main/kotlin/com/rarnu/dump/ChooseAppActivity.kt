package com.rarnu.dump

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import com.rarnu.kt.android.BaseAdapter
import com.rarnu.kt.android.resStr
import com.rarnu.kt.android.showActionBack
import kotlinx.android.synthetic.main.activity_chooseapp.*
import kotlinx.android.synthetic.main.item_app.view.*
import kotlin.jvm.internal.Ref

class ChooseAppActivity: Activity(), AdapterView.OnItemClickListener {

    private lateinit var list: MutableList<ApplicationInfo>
    private lateinit var adapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooseapp)
        actionBar.title = resStr(R.string.title_choose_app)
        showActionBack()

        list = packageManager.getInstalledApplications(0)
        adapter = AppAdapter(this, list)
        lvApp.adapter = adapter

        lvApp.onItemClickListener = this
        edtFilter.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter.filter(edtFilter.text.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val inRet = Intent()
        inRet.putExtra("item", list[position])
        setResult(RESULT_OK, inRet)
        finish()
    }

    inner class AppAdapter(ctx: Context, list: MutableList<ApplicationInfo>) : BaseAdapter<ApplicationInfo, AppAdapter.AppHolder>(ctx, list) {

        override fun fillHolder(baseVew: View, holder: AppHolder, item: ApplicationInfo, position: Int) {
            holder.tvAppName.text = item.loadLabel(packageManager)
            holder.tvAppPackage.text = item.packageName
            holder.ivIcon.setImageDrawable(item.loadIcon(packageManager))
        }

        override fun getAdapterLayout() = R.layout.item_app

        override fun getValueText(item: ApplicationInfo) = item.packageName + item.loadLabel(packageManager)

        override fun newHolder(baseView: View) = AppHolder(baseView)

        inner class AppHolder(v: View) {
            internal val tvAppName = v.tvAppName
            internal val tvAppPackage = v.tvAppPackage
            internal val ivIcon = v.ivIcon
        }

    }

}