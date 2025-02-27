package com.huntersmeadow.wordpuzzleassist.workclasses

import android.content.DialogInterface

/** Generates an empty click listener.
 *
 *  @return A blank click listener.
 */
fun getEmptyClickListener(): DialogInterface.OnClickListener {
    return DialogInterface.OnClickListener { _, _ -> }
}

object PublicConstants {
    const val THREAD_PROGRESS_MAX = 100
    const val FPS60 = 1000 / 60L
}
