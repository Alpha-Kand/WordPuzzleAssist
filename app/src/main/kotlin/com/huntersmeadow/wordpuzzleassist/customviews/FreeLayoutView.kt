package com.huntersmeadow.wordpuzzleassist.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.huntersmeadow.wordpuzzleassist.R
import com.huntersmeadow.wordpuzzleassist.double
import com.huntersmeadow.wordpuzzleassist.halve

/** Container for and drawer of AssistCard views.
 *
 *  @param context Parent context.
 *  @param attrs AttributeSet.
 */
class FreeLayoutView(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // VARIABLES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Paint for painting the ghost help text. */
    private var mPaint = Paint()

    /** Generic container for holding rect bounds data. */
    private var mBounds = Rect()

    /** Just a dummy TextView for getting the default size of text. */
    private var mTextViewForSize = TextView(getContext())

    /** Drawable that mimics the 'add new assist button' for ghost help text. */
    private var mAddAssist = ContextCompat.getDrawable(getContext(), R.drawable.ic_add_assist)!!

    /** List of all the AssistCards. */
    var mAssistOrder = ArrayList<AssistCard>()

    /** Smaller reference for the 'scaledDensity' value. */
    private var mScaleDensity = resources.displayMetrics.scaledDensity

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // OVERRIDES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** For xml creation. */
    init {
        mPaint.textSize = PrivateConstants.GHOST_TEXT_SIZE * mScaleDensity
        mPaint.color = PrivateConstants.GHOST_TEXT_COLOR
        // Ensure the onDraw function is called.
        setWillNotDraw(false)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_UP -> performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        // If the user touches the background (this) then hide the DeleteAssistCard views.
        for (ac in mAssistOrder) {
            ac.hideDAC()
        }
        requestLayout()
        return true
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mAssistOrder.isEmpty()) {
            // Prepare ghost text and icon values.
            val ghostTextFront = context.getString(R.string.flv_first_ghost_text)
            val ghostTextBack = context.getString(R.string.flv_second_ghost_text)
            val ghostIconSize = (PrivateConstants.GHOST_TEXT_SIZE * mScaleDensity).toInt()
            val ghostIconSizeHalf = halve(ghostIconSize)
            // Get the ghost text sizes.
            mPaint.getTextBounds(ghostTextFront, 0, ghostTextFront.length, mBounds)
            val firstTextWidth = mBounds.right
            mPaint.getTextBounds(ghostTextBack, 0, ghostTextFront.length, mBounds)
            val secondTextWidth = mBounds.right
            // Figure out where to place the ghost text and icon.
            val totalHalfWidth = halve(
                firstTextWidth +
                    ghostIconSizeHalf +
                    ghostIconSize +
                    ghostIconSizeHalf +
                    secondTextWidth,
            )
            val halfWidth = halve(width)
            val ghostYPos = height / 3

            // Draw the front ghost text.
            canvas.drawText(
                ghostTextFront,
                (halfWidth - totalHalfWidth).toFloat(),
                ghostYPos.toFloat(),
                mPaint,
            )
            // Draw the ghost icon.
            mAddAssist.setBounds(
                (halfWidth - ghostIconSizeHalf).toInt(),
                ghostYPos - ghostIconSize,
                (halfWidth + ghostIconSizeHalf).toInt(),
                ghostYPos,
            )
            mAddAssist.draw(canvas)
            // Draw the ghost back text.
            canvas.drawText(
                ghostTextBack,
                (halfWidth + halve(ghostIconSize)).toFloat(),
                ghostYPos.toFloat(),
                mPaint,
            )
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        drawAssistCards()
        drawDeleteAssistCardViews()
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PUBLIC
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Removes a given AssistCard from this FLV view.
     *
     * @param ac The [AssistCard] to remove from this view.
     */
    fun deleteAssistCard(ac: AssistCard?) {
        mAssistOrder.remove(ac)
        removeView(ac)
        requestLayout()
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PRIVATE
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Reorders the [AssistCard] if they have been swapped or moved around.
     *
     *  @param assistWidth Width of any given [AssistCard].
     *  @param assistHeight Height of any given [AssistCard].
     */
    private fun determineAssistCardOrdering(assistWidth: Int, assistHeight: Int) {
        val cols = PrivateConstants.COLUMNS
        val margin = PrivateConstants.DRAW_MARGIN
        // Determine if a child AssistCard has been swapped with another and update positions
        // accordingly.
        for (i in mAssistOrder.indices) {
            val child = mAssistOrder[i]
            val coord = child.getCoordinate()

            // If an AssistCard has been dragged and needs to be reordered...
            if (coord != null) {
                // Find the index of the column the AssistCard was dragged into.
                val columnIndex = coord.x / (assistWidth + margin * halve((cols + 1)))
                // Find the index of the row the AssistCard was dragged into.
                val rowIndex = coord.y / (assistHeight + margin)

                // Find the index in the 'mAssistOrder' the dragged AssistCard
                // is going to swap with.
                val trueIndex = (double(rowIndex) + columnIndex).toInt()

                // If the AssistCard was dragged onto another AssistCard.
                if (trueIndex < mAssistOrder.size) {
                    // Swap the positions of the two AssistCards.
                    val other = mAssistOrder[trueIndex]
                    child.swapIndexes(other)
                    mAssistOrder[trueIndex] = child
                    mAssistOrder[i] = other
                } else {
                    // If the AssistCard was dragged off the end then add it to
                    // the end and remove it from wherever it was before.
                    mAssistOrder.add(child)
                    mAssistOrder.removeAt(i)
                }
                // Reset everything and begin again.
                child.clearCoordinate()
                // We assume only one child was moved to be swapped at a time, so abandon looking
                // for any more child coordinates.
                break
            }
        }
    }

    /** Draw the AssistCards and their children, if necessary.
     */
    private fun drawAssistCards() {
        val defaultTextSize = mTextViewForSize.textSize
        val margin = PrivateConstants.DRAW_MARGIN
        val cols = PrivateConstants.COLUMNS
        // Assist size values.
        val assistHeight = (defaultTextSize * PrivateConstants.ASSISTCARD_SIZE).toInt()
        val assistWidth = (width - margin * (cols + 1)) / cols

        // Reorder AssistCard ordering if necessary.
        determineAssistCardOrdering(assistWidth, assistHeight)

        // Y coordinates to draw the AssistCards.
        var yyy = margin
        // Loop through the children and draw them properly aligned.
        for (i in mAssistOrder.indices) {
            val child = mAssistOrder[i]
            // Determine the next child's X position.
            val columnIndex = i % cols
            // X coordinates to draw the AssistCards.
            val xxx = assistWidth * columnIndex + margin * (columnIndex + 1)

            // Determine the next child's Y position.
            if (i > 0 && columnIndex == 0) {
                yyy += assistHeight + margin
            }

            // Layout the child AssistCard.
            child.layout(xxx, yyy, xxx + assistWidth, yyy + assistHeight)
            child.x = xxx.toFloat()
            child.y = yyy.toFloat()
        }
    }

    /** Draws the [DeleteAssistCard] objects over the [AssistCard]s.
     */
    private fun drawDeleteAssistCardViews() {
        // Layout DeleteAssistCards over their AssistCards.
        for (m in 0 until childCount) {
            val child = getChildAt(m)
            if (child is DeleteAssistCard) {
                val assistCard = child.getAssistCardParent()
                val left = assistCard.x.toInt() + assistCard.width - halve(child.width)
                val top = assistCard.y.toInt() - halve(child.height)
                val right = left + child.layoutParams.width
                val bottom = top + child.layoutParams.height
                child.layout(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
            }
        }
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // SUBCLASSES, ETC.
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    object PrivateConstants {
        const val GHOST_TEXT_SIZE = 25
        const val GHOST_TEXT_COLOR = 0x33000000
        const val ASSISTCARD_SIZE = 4 // Four lines of text tall.
        const val DRAW_MARGIN = 30
        const val COLUMNS = 2
    }
}
