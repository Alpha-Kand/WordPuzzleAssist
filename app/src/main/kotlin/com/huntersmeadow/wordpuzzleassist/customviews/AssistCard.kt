package com.huntersmeadow.wordpuzzleassist.customviews

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.huntersmeadow.wordpuzzleassist.activities.DirectoryActivity
import com.huntersmeadow.wordpuzzleassist.halve
import com.huntersmeadow.wordpuzzleassist.workclasses.ActivityOrders
import com.huntersmeadow.wordpuzzleassist.workclasses.PublicConstants
import com.huntersmeadow.wordpuzzleassist.workclasses.ThreadManager
import kotlin.math.abs

/** This class represents a running or completed assist that is accessible from the
 *  [DirectoryActivity]. Can be tapped to open the assist activity that it represents or can be
 *  dragged around and reorganized.
 *
 *  @param directoryActivity Reference to the parent directoryActivity.
 *  @param assistInfo        The current assist and thread information.
 */
@SuppressLint("ViewConstructor")
class AssistCard(
    directoryActivity: DirectoryActivity,
    assistInfo: ThreadManager.AssistInfo,
) : RelativeLayout(directoryActivity) {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // VARIABLES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    // / PERSONAL ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** Reference to the active DirectoryActivity. Also counts as a Context object. */
    private var mDirAct: DirectoryActivity = directoryActivity

    /** Flag that allows the progress thread to continue updating. */
    private var mLoop = false

    // / CHILD VIEWS AND STRUCTURE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /** Reference to child TextView. */
    private var mTV = TextView(context)

    /** Reference to child ProgressBar. */
    private var mPB = ProgressBar(
        context,
        null,
        android.R.attr.progressBarStyleHorizontal,
    )

    /** Reference to child [DeleteAssistCard]. */
    private var mDAC = DeleteAssistCard(context, this)

    /** Reference to child [FinishAssistIcon]. */
    private var mFI = FinishAssistIcon(context)

    /** Reference to whatever the user's default text size i */
    private var mTextSize: Float = TextView(context).textSize

    // / PRESS AND DRAG ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /** Handler for handling long touch presses. */
    private val mLongPressHandler = Handler(Looper.getMainLooper())

    /** Runnable that has all AssistCards show their DACs. */
    private var mLongPressedRunnable = Runnable { performLongClick() }

    /**
     * Object that contains the coordinates of this AssistCard when the user stops
     * dragging it.
     */
    private var mCoord: Coord? = null

    /** Whether or not the card has been moved. */
    private var mMoved = false

    /** Whether or not a long press has been achieved. */
    private var mLongPress = false

    /** X coordinate at the original down press. */
    private var mXPositionAtDownPress: Float = 0.toFloat()

    /** Y coordinate at the original down press. */
    private var mYPositionAtDownPress: Float = 0.toFloat()

    /** Offset from this AssistCard's origin X to where it was pressed. */
    private var mXPressOffset: Float = 0f

    /** Offset from this AssistCard's origin Y to where it was pressed. */
    private var mYPressOffset: Float = 0f

    // / REPRESENTATION INFORMATION ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /** Reference to whatever class this AssistCard is supposed to represent. */
    private var mClassType: ActivityOrders.ActivityOrder = assistInfo.mAssistType

    /** Progress integer. */
    private var mMutableInteger = assistInfo.mMutableInteger

    /** Index of the assist thread information. */
    private var mAssistInfo = assistInfo

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // OVERRIDES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    init {
        mTV.text = mDirAct.getString(
            ActivityOrders.getAssistCardTitle(assistInfo.mAssistType),
            assistInfo.mInput,
        )
        setupStructure()
        mLoop = true
        Thread {
            while (mLoop) {
                Thread.sleep(PublicConstants.FPS60)
                mDirAct.runOnUiThread {
                    mPB.progress = mMutableInteger.mValue

                    // Show completion icon visibility.
                    if (mMutableInteger.mValue >= 100) {
                        mFI.visibility = View.VISIBLE
                    }
                }
            }
        }.start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                // Record the current position.
                mXPositionAtDownPress = x
                mYPositionAtDownPress = y
                // Record the offset from this AssistCards position and the press event location.
                mXPressOffset = event.rawX - mXPositionAtDownPress
                mYPressOffset = event.rawY - mYPositionAtDownPress
                // Start the long press handler.
                mLongPressHandler.postDelayed(
                    mLongPressedRunnable,
                    PrivateConstants.ASSISTCARD_LONG_PRESS_MILLISECONDS,
                )
            }

            MotionEvent.ACTION_MOVE -> {
                // Position this AssistCard to the current press position, with the offset from
                // wherever it was originally pressed.
                x = event.rawX - mXPressOffset
                y = event.rawY - mYPressOffset
                // Cancel long press if the user has dragged this AssistCard any appreciable
                // distance.
                if (abs(mXPositionAtDownPress - x) > halve(width) ||
                    abs(mYPositionAtDownPress - y) > halve(height)
                ) {
                    mMoved = true
                    mLongPressHandler.removeCallbacks(mLongPressedRunnable)
                }
            }

            MotionEvent.ACTION_UP -> performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        // Cancel the long press if it's still running.
        mLongPressHandler.removeCallbacks(mLongPressedRunnable)
        // If the AssistCard has been moved enough and a long press hasn't occurred, open the
        // proper assist activity.
        if (!mMoved && !mLongPress) {
            ThreadManager.instance().mFocusThread = mAssistInfo.mIndex
            mDirAct.stopCoroutines()
            context.startActivity(
                Intent(
                    context,
                    ActivityOrders.getActivityClass(mClassType.ordinal),
                ),
            )
        }
        // Record the final position so FLV can reposition it correctly.
        mCoord = Coord((x + halve(width)).toInt(), (y + halve(height)).toInt())
        parent.requestLayout()
        // Reset values.
        mMoved = false
        mLongPress = false
        return true
    }

    override fun performLongClick(): Boolean {
        super.performLongClick()
        // Ask the parent DirectoryActivity to request all the AssistCards show their DACs.
        mDirAct.showDACs()
        mLongPress = true
        return true
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var offsetY = 0
        // Handle each child in turn.
        for (i in 0 until childCount) {
            val child = getChildAt(i)

            // Special treatment for DeleteAssistCard.
            if (child is DeleteAssistCard) {
                val lp = child.getLayoutParams() as MarginLayoutParams
                child.layout(
                    width - lp.width,
                    0,
                    width,
                    lp.height,
                )
                continue
            }

            // Special treatment for FinishAssistIcon.
            if (child is FinishAssistIcon) {
                val size = getDimension(PrivateConstants.FINISH_ICON_SIZE)
                val scootch = (size * 0.4).toInt()
                child.layout(width - size, height - size - scootch, width, height - scootch)
                continue
            }

            var childHeight: Int
            val horPadding: Int = getDimension(PrivateConstants.HOR_PADDING)
            val verPadding: Int

            // Get child height and padding.
            if (child is TextView) {
                childHeight = getDimension(PrivateConstants.TITLE_HEIGHT)
                verPadding = getDimension(PrivateConstants.TITLE_VER_PADDING)
            } else {
                childHeight = getDimension(PrivateConstants.PB_HEIGHT)
                verPadding = getDimension(PrivateConstants.PB_VER_PADDING)
            }

            // Set the child's width.
            var childWidth = (child.layoutParams as MarginLayoutParams).width
            if (childWidth == MarginLayoutParams.MATCH_PARENT) {
                childWidth = width - horPadding - horPadding
            }

            // Set the child's height.
            if (childHeight == MarginLayoutParams.MATCH_PARENT) {
                childHeight = height - verPadding - verPadding
            }

            // Figure out the final positioning.
            val top = offsetY + verPadding
            val right = horPadding + childWidth
            val bottom = top + childHeight
            child.layout(horPadding, top, right, bottom)
            offsetY = bottom + verPadding
        }
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // REGULAR
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Shows the DeleteAssistCard child for this AssistCard. */
    fun showDAC() {
        mDAC.visibility = View.VISIBLE
    }

    /** Hides this AssistCard's DeleteAssistCard (DAC) child. */
    fun hideDAC() {
        mDAC.visibility = View.INVISIBLE
    }

    /** Returns the release press coordinate.
     *
     *  @return The coordinate of where this AssistCard was dragged to.
     */
    fun getCoordinate(): Coord? {
        return mCoord
    }

    /** Clears this AssistCard's dragged-to coordinates. */
    fun clearCoordinate() {
        mCoord = null
    }

    /** Signals that this AssistCard's progress thread should stop and exit. */
    fun stopLooping() {
        mLoop = false
    }

    /** Asks the DirectoryActivity to delete this AssistCard. */
    fun requestDeletion() {
        mDirAct.deleteAssistCard(this)
        ThreadManager.instance().deleteThread(mAssistInfo.mIndex)
    }

    /** Swaps the indexes of the current AssistCard and another.
     *
     *  @param other Other AssistCard to swap indexes with.
     */
    fun swapIndexes(other: AssistCard) {
        ThreadManager.instance().swapThreads(mAssistInfo.mIndex, other.mAssistInfo.mIndex)
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PRIVATE
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Returns the default text size multiplied by the given 'factor'.
     *
     *  @param factor Multiplier to apply to this view's default text size.
     *  @return The scaled size.
     */
    private fun getDimension(factor: Double): Int {
        return (mTextSize * factor).toInt()
    }

    /** Initializes the AssistCard's child views, background colour and 'elevation' look.
     */
    private fun setupStructure() {
        setupTextView()
        setupProgressBar()
        setupDAC()
        setupFAI()

        setBackgroundColor(PrivateConstants.ASSISTCARD_BACKGROUND_COLOR)
        elevation = PrivateConstants.ASSISTCARD_ELEVATION
    }

    /** Sets up the child TextView's dimensions and values. */
    private fun setupTextView() {
        val layoutParams = MarginLayoutParams(
            MarginLayoutParams.MATCH_PARENT,
            getDimension(PrivateConstants.TITLE_HEIGHT),
        )
        layoutParams.leftMargin = getDimension(PrivateConstants.HOR_PADDING)
        layoutParams.topMargin = getDimension(PrivateConstants.TITLE_VER_PADDING)
        layoutParams.rightMargin = getDimension(PrivateConstants.HOR_PADDING)
        layoutParams.bottomMargin = getDimension(PrivateConstants.TITLE_VER_PADDING)
        mTV.layoutParams = layoutParams
        mTV.maxLines = PrivateConstants.TEXT_VIEW_MAX_LINES
        mTV.ellipsize = TextUtils.TruncateAt.END
        addView(mTV)
    }

    /** Sets up the child ProgressBar's dimensions and values. */
    private fun setupProgressBar() {
        val layoutParams = MarginLayoutParams(
            MarginLayoutParams.MATCH_PARENT,
            getDimension(PrivateConstants.PB_HEIGHT),
        )
        layoutParams.leftMargin = getDimension(PrivateConstants.HOR_PADDING)
        layoutParams.topMargin = getDimension(PrivateConstants.PB_VER_PADDING)
        layoutParams.rightMargin = getDimension(PrivateConstants.HOR_PADDING)
        layoutParams.bottomMargin = getDimension(PrivateConstants.PB_VER_PADDING)
        mPB.layoutParams = layoutParams
        mPB.max = PublicConstants.THREAD_PROGRESS_MAX
        mPB.progress = 0
        addView(mPB)
    }

    /** Sets up the child DeleteAssistCard (DAC) dimensions and values. */
    private fun setupDAC() {
        val layoutParams = MarginLayoutParams(
            getDimension(PrivateConstants.DELETE_ICON_SIZE),
            getDimension(PrivateConstants.DELETE_ICON_SIZE),
        )
        mDAC.layoutParams = layoutParams
        mDAC.visibility = View.INVISIBLE
        addView(mDAC)
    }

    /** Sets up the child [FinishAssistIcon] (FAI) values. */
    private fun setupFAI() {
        mFI.visibility = if (mMutableInteger.mValue < 100) View.INVISIBLE else View.VISIBLE
        addView(mFI)
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // SUBCLASSES, ETC.
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Simple class for keeping track of simple x & y coordinates. */
    class Coord(var x: Int, var y: Int)

    object PrivateConstants {
        // Look and feel.
        const val TITLE_HEIGHT = 1.3
        const val PB_HEIGHT = 1.0
        const val HOR_PADDING = 0.5
        const val TITLE_VER_PADDING = 0.5
        const val PB_VER_PADDING = 0.2
        const val DELETE_ICON_SIZE = 1.5
        const val FINISH_ICON_SIZE = 1.2
        const val TEXT_VIEW_MAX_LINES = 1

        // Touch related.
        const val ASSISTCARD_LONG_PRESS_MILLISECONDS = 1100L

        // Aesthetics.
        const val ASSISTCARD_ELEVATION = 20F
        const val ASSISTCARD_BACKGROUND_COLOR = 0xFFFFFFFF.toInt()
    }
}
