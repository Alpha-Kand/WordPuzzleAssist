package com.huntersmeadow.wordpuzzleassist.activities

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.huntersmeadow.wordpuzzleassist.R
import com.huntersmeadow.wordpuzzleassist.workclasses.ThreadManager

/** Dictionary activity. */
class DictionaryActivity : AssistBaseActivity() {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // OVERRIDES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary)
        setActionBarTitle(getString(R.string.activity_title_dictionary))
        setIDs(
            startButton = R.id.dictionary_search,
            stopButton = R.id.dummyStopButton,
            input = R.id.dictionary_input,
            resultList = R.id.dictionary_results,
            cancelledText = -1,
            startFunction = ::startDictionary,
        )

        // Hard wire in the button callbacks.
        this.findViewById<Button>(R.id.dictionary_search).setOnClickListener {
            startAssistCALLBACK()
        }
        this.findViewById<Button>(R.id.dictionary_ce).setOnClickListener {
            clearInputCALLBACK()
        }
    }

    override fun helpMenuCALLBACK() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.dictionary_help_text))
            .setTitle(getString(R.string.help_title))
            .create()
            .show()
    }

    override fun resetDefaults() {
        // Empty.
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PRIVATE
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Method unique to this activity that is passed to the parent as a generic 'start assist
     *  thread' method.
     *
     *  @param input String input to start the assist thread with.
     */
    private fun startDictionary(input: String) {
        registerThreadWithSelf(ThreadManager.instance().startDictionary(input))
    }
}
