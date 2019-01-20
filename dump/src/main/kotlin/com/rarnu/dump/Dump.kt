@file:Suppress("DEPRECATION")
package com.rarnu.dump

import android.content.Context
import android.os.Environment
import com.rarnu.kt.android.fileWriteText
import com.rarnu.kt.android.runOnMainThread
import dalvik.system.DexClassLoader
import dalvik.system.DexFile
import java.io.File
import kotlin.concurrent.thread

object Dump {

    /**
     * 当前是否正在进行 dump，若正在进行 dump，则调用 @see dump 方法会失败
     */
    var isRunning = false

    /**
     * dump apk内的类定义，以子线程方式运行
     * @param ctx
     * @param apkPath 要dump的apk文件路径
     * @param prefix 要 dump 的类前缀(与 packagename 不同，如被 dump 的 packagename 是 com.tencent.mm，但是前缀是 com.tencent，就可以 dump 到如 com.tencent.wcdb 等包内的内容)
     * @param outputPath sd卡上的输出路径(如: output，则具体输出路径为 /sdcard/output)
     * @param progress dump 过程回调，className 参数标识了当前 dump 的类
     * @param complete dump 结束回调, succ 参数标识了 dump 是否成功
     */
    fun dump(ctx: Context, apkPath: String, prefix: String, outputPath: String, progress:(className: String?) -> Unit, complete:(succ: Boolean) -> Unit) = thread {
        if (isRunning) {
            runOnMainThread { complete(false) }
            return@thread
        }
        isRunning = true
        val oat = ctx.getDir(outputPath, 0)
        if (!oat.exists()) {
            oat.mkdirs()
        }
        val loader = DexClassLoader(apkPath, oat.absolutePath, null, ClassLoader.getSystemClassLoader())
        val list = mutableListOf<String>()
        val dex = DexFile(apkPath)
        val en = dex.entries()
        while (en.hasMoreElements()) {
            val cn = en.nextElement()
            if (cn.startsWith(prefix)) {
                list.add(cn)
            }
        }
        val basePath = File(Environment.getExternalStorageDirectory(), outputPath)
        if (!basePath.exists()) {
            basePath.mkdirs()
        }
        list.forEach {
            if (isRunning) {
                val fn = File(basePath, "$it.dump")
                if (!fn.exists()) {
                    val clz = try { loader.loadClass(it) } catch (t: Throwable) { null }
                    if (clz != null) {
                        var str = ""
                        try { clz.declaredFields } catch (t: Throwable) { null }?.forEach { f -> str += "${f.name}:${f.type.name}\n" }
                        try { clz.declaredMethods } catch (t: Throwable) {null}?.forEach { m ->
                            str += "${m.name}("
                            m.parameterTypes?.forEach { p -> str += "${p.name}," }
                            str = str.trimEnd(',')
                            str += "):${m.returnType.name}\n"
                        }
                        try { clz.declaredConstructors } catch (t: Throwable) { null }?.forEach { c ->
                            str += "<init>("
                            try { c.parameterTypes } catch (e: Throwable) { null }?.forEach { p -> str += "${p.name}," }
                            str = str.trimEnd(',')
                            str += ")\n"
                        }
                        fileWriteText(fn, str)
                        runOnMainThread { progress(it) }
                    }
                }
            }
        }
        isRunning = false
        runOnMainThread { complete(true) }

    }


}