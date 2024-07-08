package com.huntersmeadow.wordpuzzleassist.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.huntersmeadow.wordpuzzleassist.R

@SuppressLint("ViewConstructor")
class DeleteAssistCard internal constructor(context: Context, parent: AssistCard) : RelativeLayout(context) {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // VARIABLES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Reference to the AssistCard parent'. */
    private var mParent: AssistCard = parent

    /** Reference to the delete drawable. */
    private var mDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.deleteassist)!!

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // OVERRIDES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    init {
        setBackgroundColor(PrivateConstants.BACKGROUND_COLOUR)
        elevation = PrivateConstants.ELEVATION
    }

    override fun performClick(): Boolean {
        super.performClick()
        mParent.requestDeletion()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mDrawable.setBounds(0, 0, width, height)
        mDrawable.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_UP -> performClick()
        }
        return true
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PUBLIC
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Read only access to the AssistCard parent.
     *
     *  @return The AssistCard Parent.
     */
    fun getAssistCardParent(): AssistCard {
        return mParent
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // SUBCLASSES, ETC.
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    object PrivateConstants {
        const val BACKGROUND_COLOUR = 0x00000000
        const val ELEVATION = 30f
    }
}
