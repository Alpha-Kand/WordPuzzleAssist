package com.huntersmeadow.wordpuzzleassist.activities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.huntersmeadow.wordpuzzleassist.MutableInteger
import com.huntersmeadow.wordpuzzleassist.R
import com.huntersmeadow.wordpuzzleassist.customviews.FinishAssistIcon
import com.huntersmeadow.wordpuzzleassist.hideKeyboardMine
import com.huntersmeadow.wordpuzzleassist.workclasses.ThreadManager
import com.huntersmeadow.wordpuzzleassist.workclasses.WordDictionary
import java.util.ArrayList

/** The activity base for the puzzle solving activities.
 */
abstract class AssistBaseActivity : BaseActivity() {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // VARIABLES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    // / VIEW IDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /** Resource id's for items every assist must supply. */
    private var mStartButton: Int = 0
    private var mStopButton: Int = 0
    private var mInput: Int = 0
    private var mResultList: Int = 0
    private var mCancelledText: Int = 0

    /** Start function for the specific puzzle assist activity. */
    private var mStartFunction: ((String) -> Unit)? = null

    /** Local reference to the assist thread progress integer. */
    private var mProgressInteger = MutableInteger(0)

    /** Index of the assist thread we are following. */
    private var mIndex = -1

    /** Only checks for assist thread updates when this variable is true. */
    private var mLoop = false

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // ABSTRACT
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Resets assist activity specific values.
     */
    abstract fun resetDefaults()

    /** Updates the activity specific views according to the internal variables.
     */
    open fun updateViews() {
        findViewById<LinearLayout>(R.id.progress_layout)
            .findViewById<LinearLayout>(R.id.progress_layout)
            .post {
                val layout = getProgressLayout()
                val progressBarHeight = layout.findViewById<ProgressBar>(R.id.generic_pb).height
                val fai = layout.findViewById<FinishAssistIcon>(R.id.finished_computation)
                val layoutParams = fai.layoutParams
                layoutParams.width = progressBarHeight
                layoutParams.height = progressBarHeight
                fai.layoutParams = layoutParams
            }
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // OVERRIDES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetDefaults()
    }

    override fun onResume() {
        super.onResume()
        loadInfoFromThreadManager()
        resetDefaults()
        setStartStopButtonActive(true)
        // Start the update loop.
        mLoop = true
        Thread {
            while (mLoop) {
                Thread.sleep((1000 / 60).toLong())
                val progressLayout = getProgressLayout()
                // Update progress bar.
                runOnUiThread {
                    if (mProgressInteger.mValue > -1) {
                        val view = progressLayout.findViewById<ProgressBar>(R.id.generic_pb)
                        view.progress = mProgressInteger.mValue
                    }

                    // Set the finish computation icon visibility.
                    if (mProgressInteger.mValue >= 100) {
                        val view = findViewById<FinishAssistIcon>(R.id.finished_computation)
                        view.visibility = View.VISIBLE
                    }
                }

                // Update the extra text section. Currently is only used with the crypto assist.
                if (mIndex != -1) {
                    val pod = ThreadManager.instance().mPOD[mIndex]
                    // Never go above the maximum attempt number, even if the variable says
                    // otherwise.
                    val attemptNumber = pod.mAttemptNum.mValue.coerceAtMost(pod.mMaxAttempts.mValue)
                    // Only show the attempt number if there is more than one attempt.
                    if (attemptNumber > MutableInteger(1).mValue) {
                        runOnUiThread {
                            val view = progressLayout.findViewById<TextView>(R.id.generic_textview)
                            view.text = getString(
                                R.string.crypto_attempt_number,
                                attemptNumber,
                                pod.mMaxAttempts.mValue,
                            )
                        }
                    } else {
                        runOnUiThread {
                            val view = progressLayout.findViewById<TextView>(R.id.generic_textview)
                            view.text = ""
                        }
                    }
                }

                // Check if the thread is done, and if so reflect that state change.
                if (mProgressInteger.mValue == 100) {
                    if (mIndex != -1) {
                        runOnUiThread {
                            setStartStopButtonActive(true)
                            setResults(ThreadManager.instance().mPOD[mIndex].mResults)
                        }
                    }
                    mProgressInteger.mValue++
                }
            }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        mLoop = false
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // CALLBACKS
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    open fun startAssistCALLBACK() {
        val input = getInput()
        if (checkIfCanStart(input)) {
            resetProgressBar()
            setStartStopButtonActive(false)
            ThreadManager.instance().deleteThread(mIndex)
            mStartFunction?.invoke(input)
        }
    }

    open fun stopAssistCALLBACK() {
        setStartStopButtonActive(true)
        val tm = ThreadManager.instance()
        tm.cancelThread(mIndex)
        // Set the results to show the assist cancelled text.
        val cancelled = ArrayList<String>()
        cancelled.add(getString(mCancelledText))
        setResults(cancelled)
    }

    open fun clearInputCALLBACK() {
        findViewById<EditText>(mInput).setText("")
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // INTERNALS
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Sets the internal index and progress integer to the result from starting a new assist from
     *  [ThreadManager].
     *
     *  @param result The result from one of the [ThreadManager]'s create assist thread methods.
     */
    internal fun registerThreadWithSelf(result: Pair<Int, MutableInteger>) {
        mIndex = result.first
        mProgressInteger = result.second
        loadInfoFromThreadManager()
    }

    /** Sets the internal references to various common puzzle solving activity buttons and views
     *  from the actual activity.
     */
    internal fun setIDs(
        startButton: Int,
        stopButton: Int,
        input: Int,
        resultList: Int,
        cancelledText: Int,
        startFunction: (String) -> Unit,
    ) {
        mStartButton = startButton
        mStopButton = stopButton
        mInput = input
        mResultList = resultList
        mCancelledText = cancelledText
        mStartFunction = startFunction
    }

    /** Returns the index for the current thread, if available. */
    internal fun getIndex(): Int {
        return mIndex
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PRIVATE
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Hides the keyboard, if it's showing and returns the input from the input field.
     *
     *  @return The input for the next assist thread.
     */
    private fun getInput(): String {
        // Hide keyboard.
        val input = findViewById<EditText>(mInput)
        hideKeyboardMine(this)
        input.clearFocus()
        return input.text.toString()
    }

    /** Resets the progress bar's percentage. */
    private fun resetProgressBar() {
        getProgressLayout().findViewById<ProgressBar>(R.id.generic_pb).progress = 0
    }

    /** Sets the start/stop buttons active statuses.
     *
     *  @param start 'True' for set the start button active and stop button inactive, false for
     *               opposite.
     */
    private fun setStartStopButtonActive(start: Boolean) {
        findViewById<Button>(mStartButton).isEnabled = start
        findViewById<Button>(mStopButton).isEnabled = !start
    }

    /** Sets the result list to the given ArrayList.
     *
     *  @param results ArrayList of strings containing the results. Each string is one line in the
     *                 results.
     */
    private fun setResults(results: ArrayList<String>) {
        val ad = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            results,
        )
        findViewById<ListView>(mResultList).adapter = ad
    }

    /** Checks if certain conditions are acceptable before starting the next thread.
     *
     *  @return Whether or not the assist can continue.
     */
    private fun checkIfCanStart(input: String): Boolean {
        // Check for empty input.
        if (input == "") {
            return false
        }

        // Ensure the word dictionary has been loaded successfully.
        val wd = WordDictionary.instance()
        if (!wd.okay()) {
            Toast.makeText(
                getCurrentActivity(),
                getString(R.string.assist_base_word_list_failed),
                Toast.LENGTH_SHORT,
            ).show()
            return false
        }
        if (!wd.isLoaded()) {
            Toast.makeText(
                getCurrentActivity(),
                getString(R.string.assist_base_word_list_loading),
                Toast.LENGTH_SHORT,
            ).show()
            return false
        }
        return true
    }

    /** Updates this activity with information from the [ThreadManager], if applicable.
     */
    private fun loadInfoFromThreadManager() {
        val tm = ThreadManager.instance()
        // Load information from the thread manager, if needed.
        mIndex = tm.mFocusThread
        if (mIndex >= 0) {
            val pod = tm.mPOD[mIndex]
            // Hook up to the target's progress integer.
            mProgressInteger = pod.mMutableInteger
            // Set the input text and current results text.
            findViewById<EditText>(mInput).setText(pod.mInput)
            setResults(pod.mResults)
            if (mProgressInteger.mValue < 100 && !pod.mCancelled) {
                setStartStopButtonActive(false)
            } else {
                setStartStopButtonActive(true)
            }
        }
    }

    /** Shortcut to get the progress layout's base LinearLayout to search for sub views.
     *
     *  @return The parent LinearLayout of the progress layout.
     */
    private fun getProgressLayout(): LinearLayout {
        return findViewById<LinearLayout>(R.id.progress_layout)
            .findViewById(R.id.progress_layout)
    }
}
