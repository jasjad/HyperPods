package moe.chenxy.hyperpods.hook

import android.annotation.SuppressLint
import android.app.Service
import android.app.StatusBarManager
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Intent
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.IntentClass
import de.robv.android.xposed.XposedHelpers
import moe.chenxy.hyperpods.utils.SystemApisUtils.setIconVisibility


object HeadsetStateDispatcher : YukiBaseHooker() {
    @SuppressLint("StaticFieldLeak")
    override fun onHook() {
        "com.android.bluetooth.hfp.HeadsetA2dpSync".toClass().apply {
            var mHeadsetService : Service? = null
            method {
                name = "updateA2DPConnectionState"
                param(IntentClass)
            }.hook {
                after {
                    val intent = this.args[0] as Intent
                    val currState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0)
                    val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?
                    device?.let {
                        if (currState == BluetoothHeadset.STATE_CONNECTED) {
                            // Show Wireless Pods icon
                            if (mHeadsetService == null)
                                mHeadsetService = XposedHelpers.getObjectField(
                                    this.instance,
                                    "mHeadsetService"
                                ) as Service
                            val statusBarManager = mHeadsetService!!.getSystemService("statusbar") as StatusBarManager
                            statusBarManager.setIconVisibility("wireless_headset", true)
                        } else if (currState == BluetoothHeadset.STATE_DISCONNECTING || currState == BluetoothHeadset.STATE_DISCONNECTED) {
                            if (mHeadsetService == null)
                                mHeadsetService = XposedHelpers.getObjectField(
                                    this.instance,
                                    "mHeadsetService"
                                ) as Service
                            val statusBarManager = mHeadsetService!!.getSystemService("statusbar") as StatusBarManager
                            statusBarManager.setIconVisibility("wireless_headset", false)
                        }
                    }
                }
            }
        }
    }
}