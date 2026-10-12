package vn.comtamcam.hungtb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        GuardService.batDau(context)
        GuiWorker.day(context)
    }
}
