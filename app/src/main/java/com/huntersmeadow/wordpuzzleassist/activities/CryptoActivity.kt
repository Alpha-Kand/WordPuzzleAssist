package com.huntersmeadow.wordpuzzleassist.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.huntersmeadow.wordpuzzleassist.R
import com.huntersmeadow.wordpuzzleassist.hideKeyboardMine
import com.huntersmeadow.wordpuzzleassist.workclasses.ThreadManager

/** Cryptogram solving activity. */
class CryptoActivity : AssistBaseActivity() {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // VARIABLES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Keeps track of if the 'keyboard listener' is attached. */
    private var mKeyboardListenerAttached = false

    /** Root layout that automatically gets changes when the keyboard appears. */
    private var mRootLayout: ViewGroup? = null

    /** Records the last height difference to determine when the keyboard has shown up or not. Set
     *  to a huge number to start off 'hiding' the keyboard. */
    private var mLast = 1_000_000

    /** Layout listener that determines when the keyboard has shown up. */
    private val mKeyboardLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val heightDiff = mRootLayout!!.rootView.height - mRootLayout!!.height
        if (mLast != heightDiff) {
            if (mLast < heightDiff) {
                onShowKeyboard()
            } else {
                onHideKeyboard()
            }
            mLast = heightDiff
        }
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // OVERRIDES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crypto)
        setActionBarTitle(getString(R.string.activity_title_crypto))
        setIDs(
            R.id.crypto_start,
            R.id.crypto_stop,
            R.id.crypto_input,
            R.id.crypto_results,
            R.string.crypto_cancelled_text,
            ::startCrypto,
        )
        updateViews()

        // Hard wire in the button callbacks.
        this.findViewById<Button>(R.id.crypto_start).setOnClickListener {
            startAssistCALLBACK()
        }
        this.findViewById<Button>(R.id.crypto_stop).setOnClickListener {
            stopAssistCALLBACK()
        }
        this.findViewById<Button>(R.id.crypto_ce).setOnClickListener {
            clearInputCALLBACK()
        }
        this.findViewById<Button>(R.id.crypto_DoneInput).setOnClickListener {
            onDoneInputCALLBACK()
        }
    }

    override fun onResume() {
        super.onResume()
        attachKeyboardListeners()
        hideKeyboardMine(this)
    }

    override fun onPause() {
        super.onPause()
        if (mKeyboardListenerAttached) {
            mRootLayout
                ?.viewTreeObserver
                ?.removeOnGlobalLayoutListener(mKeyboardLayoutListener)
        }
    }

    override fun helpMenuCALLBACK() {
        val thisthis = this
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.crypto_help_text))
            .setTitle(getString(R.string.help_title))
            .setNegativeButton(getString(R.string.crypto_help_example_one_line_button)) { _, _ ->
                (thisthis.findViewById(R.id.crypto_input) as EditText)
                    .setText(getString(R.string.crypto_help_example_one_line))
                startAssistCALLBACK()
            }
            .setPositiveButton(getString(R.string.crypto_help_example_multiline_button)) { _, _ ->
                (thisthis.findViewById(R.id.crypto_input) as EditText)
                    .setText(getString(R.string.crypto_help_example_multi))
                startAssistCALLBACK()
            }
            .create()
            .show()
    }

    override fun resetDefaults() {
        // Empty.
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // CALLBACKS
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    private fun onDoneInputCALLBACK() {
        hideKeyboardMine(this)
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PRIVATE
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Method unique to this activity that is passed to the parent as a generic 'start assist
     *  thread' method.
     *
     *  @param input String input to start the assist thread with.
     */
    private fun startCrypto(input: String) {
        registerThreadWithSelf(ThreadManager.instance().startCrypto(input))
    }

    /** Hide start, stop, etc. buttons when the keyboard is shown. */
    private fun onShowKeyboard() {
        findViewById<View>(R.id.crypto_start).visibility = View.GONE
        findViewById<View>(R.id.crypto_stop).visibility = View.GONE
        findViewById<View>(R.id.crypto_DoneInput).visibility = View.VISIBLE
        findViewById<EditText>(R.id.crypto_input).maxLines = 100
    }

    /** Show start, stop, etc. buttons when the keyboard is hidden. */
    private fun onHideKeyboard() {
        findViewById<EditText>(R.id.crypto_input).setLines(1)
        findViewById<View>(R.id.crypto_DoneInput).visibility = View.GONE
        findViewById<View>(R.id.crypto_start).visibility = View.VISIBLE
        findViewById<View>(R.id.crypto_stop).visibility = View.VISIBLE
    }

    /** Attach the 'keyboard listener'. */
    private fun attachKeyboardListeners() {
        if (mKeyboardListenerAttached) {
            return
        }
        mRootLayout = findViewById(R.id.crypto_layout)
        mRootLayout!!.viewTreeObserver.addOnGlobalLayoutListener(mKeyboardLayoutListener)
        mKeyboardListenerAttached = true
    }
}
