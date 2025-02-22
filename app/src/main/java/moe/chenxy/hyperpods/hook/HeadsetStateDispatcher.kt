package moe.chenxy.hyperpods.hook

import android.annotation.SuppressLint
import android.app.Service
import android.app.StatusBarManager
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.ContextWrapper
import android.content.Intent
import android.os.Handler
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.IntentClass
import de.robv.android.xposed.XposedHelpers
import moe.chenxy.hyperpods.utils.SystemApisUtils.setIconVisibility


object HeadsetStateDispatcher : YukiBaseHooker() {
    @SuppressLint("StaticFieldLeak")
    override fun onHook() {
        "com.android.bluetooth.a2dp.A2dpService".toClass().apply {
            method {
                name = "handleConnectionStateChanged"
                paramCount = 3
            }.hook {
                after {
                    val currState = this.args[2] as Int
                    val fromState = this.args[1] as Int
                    val device = this.args[0] as BluetoothDevice?
                    val handler = XposedHelpers.getObjectField(this.instance, "mHandler") as Handler
                    if (device == null || currState == fromState) {
                        return@after
                    }
                    handler.post {
			val context = this.instance as ContextWrapper
                        if (currState == BluetoothHeadset.STATE_CONNECTED) {
                            // Show Wireless Pods icon
                            val statusBarManager = context.getSystemService("statusbar") as StatusBarManager
                            statusBarManager.setIconVisibility("wireless_headset", true)
                        } else if (currState == BluetoothHeadset.STATE_DISCONNECTING || currState == BluetoothHeadset.STATE_DISCONNECTED) {
                            val statusBarManager = context.getSystemService("statusbar") as StatusBarManager
                            statusBarManager.setIconVisibility("wireless_headset", false)
                        }
                    }
                }
            }
        }
    }
}