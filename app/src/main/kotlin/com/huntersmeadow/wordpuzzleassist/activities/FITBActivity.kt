package com.huntersmeadow.wordpuzzleassist.activities

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import com.huntersmeadow.wordpuzzleassist.Mutable
import com.huntersmeadow.wordpuzzleassist.R
import com.huntersmeadow.wordpuzzleassist.workclasses.ThreadManager

/** FITB solving activity. */
class FITBActivity : AssistBaseActivity() {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // VARIABLES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** A list of the 'ignore letters' switch views. */
    private var mSwitches: ArrayList<SwitchCompat> = ArrayList()

    /** Keeps track of the activation status of all the letter the user enables/disables. */
    private var mSwitchStatuses = Mutable(BooleanArray(26))

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // OVERRIDES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_in_the_blanks)
        setActionBarTitle(getString(R.string.activity_title_fitb))
        setIDs(
            startButton = R.id.fillintheblanks_start,
            stopButton = R.id.fillintheblanks_stop,
            input = R.id.fillintheblanks_input,
            resultList = R.id.fillintheblanks_results,
            cancelledText = R.string.fillintheblanks_cancelled_text,
            startFunction = ::startFITB,
        )
        updateViews()

        // Hard wire in the button callbacks.
        this.findViewById<Button>(R.id.fillintheblanks_start).setOnClickListener {
            startAssistCALLBACK()
        }
        this.findViewById<Button>(R.id.fillintheblanks_stop).setOnClickListener {
            stopAssistCALLBACK()
        }
        this.findViewById<Button>(R.id.fillintheblanks_ce).setOnClickListener {
            clearInputCALLBACK()
        }
        this.findViewById<Button>(R.id.fillintheblanks_asterisk).setOnClickListener {
            addQuestionMarkCALLBACK()
        }
        this.findViewById<Button>(R.id.fillintheblanks_QuestionMark).setOnClickListener {
            addAsteriskCALLBACK()
        }
        this.findViewById<Button>(R.id.fillintheblanks_IgnoreLetters).setOnClickListener {
            ignoreLettersCALLBACK()
        }
    }

    public override fun onResume() {
        super.onResume()
        if (getIndex() != -1) {
            ThreadManager.instance().getFITBInfo(getIndex(), mSwitchStatuses)
        }
        updateViews()
    }

    override fun helpMenuCALLBACK() {
        super.helpMenuCALLBACK()
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.fillintheblanks_help_text))
            .setTitle(getString(R.string.help_title))
            .setNegativeButton(getString(R.string.fillintheblanks_help_example_single_button)) { _, _ ->
                findViewById<EditText>(R.id.fillintheblanks_input)
                    ?.setText(getString(R.string.fillintheblanks_help_example_single))
                startAssistCALLBACK()
            }.setPositiveButton(getString(R.string.fillintheblanks_help_example_multi_button)) { _, _ ->
                findViewById<EditText>(R.id.fillintheblanks_input)
                    ?.setText(getString(R.string.fillintheblanks_help_example_multi))
                startAssistCALLBACK()
            }
            .create()
            .show()
    }

    override fun resetDefaults() {
        // Set all the switch statuses to 'allowed'
        for (i in 0..25) {
            mSwitchStatuses.mValue[i] = true
        }
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // CALLBACKS
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    private fun addQuestionMarkCALLBACK() {
        val et: EditText = findViewById(R.id.fillintheblanks_input)
        et.setText(getString(R.string.fillintheblanks_add_question_mark, et.text))
        et.setSelection(et.text.length)
    }

    private fun addAsteriskCALLBACK() {
        val et: EditText = findViewById(R.id.fillintheblanks_input)
        et.setText(getString(R.string.fillintheblanks_add_asterisk, et.text))
        et.setSelection(et.text.length)
    }

    /** Show ignore letters dialog button. */
    private fun ignoreLettersCALLBACK() {
        mSwitches.clear()
        val descriptionID = View.generateViewId()
        val gridLayoutId = View.generateViewId()
        val relativeLayout = RelativeLayout(this)
        relativeLayout.gravity = Gravity.CENTER

        // Add description text.
        run {
            val description = TextView(this)
            description.text = getString(R.string.fillintheblanks_ignore_description)
            description.id = descriptionID
            relativeLayout.addView(description)
        }

        // Add all the switches and their labels.
        run {
            val gridLayout = GridLayout(this)
            gridLayout.id = gridLayoutId
            gridLayout.columnCount = 4 * 3
            generateIgnoreLetterDialogLayoutParams(descriptionID, gridLayout)
            relativeLayout.addView(gridLayout)

            var a = 'A'
            while (a <= 'Z') {
                // Switch label.
                val textView = TextView(this)
                textView.text = a.toString()
                // Letter switch.
                val switchView = SwitchCompat(this)
                switchView.isChecked = mSwitchStatuses.mValue[a - 'A']
                mSwitches.add(switchView)
                // Invisible spacer text view.
                val spacer = TextView(this)
                spacer.text = a.toString()
                spacer.visibility = View.INVISIBLE
                gridLayout.addView(textView)
                gridLayout.addView(switchView)
                gridLayout.addView(spacer)
                ++a
            }
        }

        // Add the "all on"/"all off" buttons.
        val linearLayout = LinearLayout(this)
        run {
            generateIgnoreLetterDialogLayoutParams(gridLayoutId, linearLayout)
            // Add "all on" button.
            run {
                val button = Button(this)
                button.text = getString(R.string.fillintheblanks_choose_letters_all_on)
                button.setOnClickListener {
                    for (i in mSwitches.indices) {
                        mSwitches[i].isChecked = true
                    }
                }
                linearLayout.addView(button)
            }
            // Add "all off" button.
            run {
                val button = Button(this)
                button.text = getString(R.string.fillintheblanks_choose_letters_all_off)
                button.setOnClickListener {
                    for (i in mSwitches.indices) {
                        mSwitches[i].isChecked = false
                    }
                }
                linearLayout.addView(button)
            }
        }
        relativeLayout.addView(linearLayout)
        // Create and show dialog.
        AlertDialog.Builder(this)
            .setView(relativeLayout)
            .setPositiveButton(getString(R.string.dialog_ok)) { _, _ ->
                for (i in mSwitches.indices) {
                    mSwitchStatuses.mValue[i] = mSwitches[i].isChecked
                }
            }
            .setNegativeButton(getString(R.string.dialog_cancel)) { _, _ -> }
            .create()
            .show()
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PRIVATE
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Method unique to this activity that is passed to the parent as a generic 'start assist
     *  thread' method.
     *
     *  @param input String input to start the assist thread with.
     */
    private fun startFITB(input: String) {
        registerThreadWithSelf(ThreadManager.instance().startFITB(input, mSwitchStatuses.mValue))
    }

    /** Generates a set of [RelativeLayout] layout params and gives it to the passed view. Only for
     *  use with the 'ignore letters' dialog generation.
     *
     *  @param otherID ID of another view to position the [view] under
     *  @param view View to give the layout params to.
     */
    private fun generateIgnoreLetterDialogLayoutParams(otherID: Int, view: View) {
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
        )
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, otherID)
        layoutParams.addRule(RelativeLayout.BELOW, otherID)
        view.layoutParams = layoutParams
    }
}
