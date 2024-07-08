package com.huntersmeadow.wordpuzzleassist.activities

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.huntersmeadow.wordpuzzleassist.Mutable
import com.huntersmeadow.wordpuzzleassist.MutableInteger
import com.huntersmeadow.wordpuzzleassist.R
import com.huntersmeadow.wordpuzzleassist.workclasses.AnagramFinder
import com.huntersmeadow.wordpuzzleassist.workclasses.ThreadManager
import com.huntersmeadow.wordpuzzleassist.workclasses.getEmptyClickListener

/** Anagram solving activity. */
class AnagramsActivity : AssistBaseActivity() {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // VARIABLES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Maximum word length. */
    private var mMaxLength = MutableInteger(0)

    /** Minimum word length. */
    private var mMinLength = MutableInteger(0)

    /** Specifies whether the algorithm should find words with +1/-1 letters. */
    private var mAddLetter = Mutable(AnagramFinder.Modifier.NONE)

    /** Specifies the anagram type: 0 = normal, 1 = phrases, 2 = sub words. */
    private var mAnagramType = MutableInteger(0)

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // OVERRIDES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anagram)
        setActionBarTitle(getString(R.string.activity_title_anagram))
        setIDs(
            R.id.anagram_start,
            R.id.anagram_stop,
            R.id.anagram_input,
            R.id.anagram_results,
            R.string.anagram_cancelled_text,
            ::startAnagram,
        )
        updateViews()

        // Hard wire in the button callbacks.
        this.findViewById<Button>(R.id.anagram_start).setOnClickListener {
            startAssistCALLBACK()
        }
        this.findViewById<Button>(R.id.anagram_stop).setOnClickListener {
            stopAssistCALLBACK()
        }
        this.findViewById<Button>(R.id.anagram_ce).setOnClickListener {
            clearInputCALLBACK()
        }
        this.findViewById<Button>(R.id.anagram_add_letter).setOnClickListener {
            onSetAddLetterCALLBACK()
        }
        this.findViewById<Button>(R.id.anagram_change_max).setOnClickListener {
            onSetMaxWordLengthCALLBACK()
        }
        this.findViewById<Button>(R.id.anagram_change_min).setOnClickListener {
            onSetMinWordLengthCALLBACK()
        }
        val anagramTypeRadios = this.findViewById<RadioGroup>(R.id.anagram_type_radios)
        for (aaa in 0 until anagramTypeRadios.childCount) {
            anagramTypeRadios.getChildAt(aaa).setOnClickListener { onSelectAnagramTypeCALLBACK() }
        }
    }

    override fun onResume() {
        super.onResume()
        if (getIndex() != -1) {
            ThreadManager.instance().getAnagramInfo(
                getIndex(),
                mAddLetter,
                mMaxLength,
                mMinLength,
                mAnagramType,
            )
        }
        updateViews()
    }

    override fun helpMenuCALLBACK() {
        super.helpMenuCALLBACK()
        val thisthis = this
        val builder = AlertDialog.Builder(this)
            .setMessage(getString(R.string.anagram_help_text))
            .setTitle(getString(R.string.help_title))
            .setPositiveButton(getString(R.string.help_example_button)) { _, _ ->
                (thisthis.findViewById(R.id.anagram_input) as EditText).setText(getString(R.string.anagram_help_example_text))
                thisthis.mAddLetter.mValue = AnagramFinder.Modifier.NONE
                thisthis.updateViews()
                startAssistCALLBACK() // unusedView)
            }
        builder.create().show()
    }

    override fun resetDefaults() {
        mAddLetter.mValue = AnagramFinder.Modifier.NONE
        mMaxLength.mValue = PrivateConstants.DEFAULT_MAX_WORD_LENGTH
        mMinLength.mValue = PrivateConstants.DEFAULT_MIN_WORD_LENGTH
    }

    /** Updates the add/remove letter button text, min/max button text, and modifier radio button
     *  selection from internal variables. */
    override fun updateViews() {
        super.updateViews()
        // Update add/remove letter button text.
        findViewById<Button>(R.id.anagram_add_letter).text =
            getString(R.string.anagram_add_remove_letter_button_text, mAddLetter.mValue.amount)
        // Update max button text.
        findViewById<Button>(R.id.anagram_change_max).text =
            getString(R.string.anagram_max_word_size_text, mMaxLength.mValue)
        // Update min button text.
        findViewById<Button>(R.id.anagram_change_min).text =
            getString(R.string.anagram_min_word_size_text, mMinLength.mValue)
        // Update modifier radios.
        (
            findViewById<RadioGroup>(R.id.anagram_type_radios)
                .getChildAt(mAnagramType.mValue) as RadioButton
            ).isChecked = true
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // CALLBACKS
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    private fun onSelectAnagramTypeCALLBACK() {
        val radios = this.findViewById<RadioGroup>(R.id.anagram_type_radios)
        mAnagramType.mValue = radios.indexOfChild(findViewById(radios.checkedRadioButtonId))
    }

    private fun onSetAddLetterCALLBACK() {
        // Casting to int gets the inflater to shut up.
        val id = R.layout.dialog_anagram_numberpicker
        // Create dialog.
        val dialog = AlertDialog.Builder(this)
            .setMessage(getString(R.string.anagram_add_remove_dialog_message))
            .setView(layoutInflater.inflate(id, null))
            .setPositiveButton(
                getString(R.string.dialog_ok),
                AddRemoveLetterClickListener(
                    this,
                    findViewById(R.id.anagram_add_letter),
                    R.string.anagram_add_remove_letter_button_text,
                    mAddLetter,
                ),
            )
            .setNegativeButton(getString(R.string.dialog_cancel), getEmptyClickListener())
            .create()
        dialog.show()
        // Set dialog's number-picker values.
        val np = dialog.findViewById<NumberPicker>(R.id.numPicker)
        np?.minValue = 0
        np?.maxValue = 2
        np?.value = mAddLetter.mValue.amount + 1
        np?.displayedValues = arrayOf("-1", "0", "1")
    }

    private fun onSetMaxWordLengthCALLBACK() {
        setWordLength(
            R.id.anagram_change_max,
            R.string.anagram_max_word_size_text,
            mMaxLength,
            getString(R.string.anagram_maximum_length),
        )
    }

    private fun onSetMinWordLengthCALLBACK() {
        setWordLength(
            R.id.anagram_change_min,
            R.string.anagram_min_word_size_text,
            mMinLength,
            getString(R.string.anagram_minimum_length),
        )
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PRIVATE
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Method unique to this activity that is passed to the parent as a generic 'start assist
     *  thread' method.
     *
     *  @param input String input to start the assist thread with.
     */
    private fun startAnagram(input: String) {
        registerThreadWithSelf(
            ThreadManager.instance().startAnagram(
                input,
                getAnagramType(),
                mAddLetter.mValue,
                mMinLength.mValue,
                mMaxLength.mValue,
            ),
        )
    }

    /** Gets the anagram type specified from the radio group.
     *
     *  @return The anagram type specified by the anagram activity radios.
     */
    private fun getAnagramType(): ThreadManager.ThreadManagerAnagramType {
        return when (findViewById<RadioGroup>(R.id.anagram_type_radios).checkedRadioButtonId) {
            R.id.radio_phrases -> ThreadManager.ThreadManagerAnagramType.PHRASES
            R.id.radio_sub_words -> ThreadManager.ThreadManagerAnagramType.SUB_WORDS
            else -> ThreadManager.ThreadManagerAnagramType.NORMAL
        }
    }

    /** Given a value, returns the [AnagramFinder.Modifier] enum value.
     *
     *  @param value Value amount to compare to the [AnagramFinder.Modifier]s
     */
    private fun getAnagramFinderEnum(value: Int): AnagramFinder.Modifier {
        return when (value) {
            1 -> AnagramFinder.Modifier.ADD_LETTER
            -1 -> AnagramFinder.Modifier.REMOVE_LETTER
            else -> AnagramFinder.Modifier.NONE
        }
    }

    /** Generic function that opens a word length picker dialog.
     *
     *  @param buttonViewID ID of the button clicked.
     *  @param stringID ID of the button text string.
     *  @param mutInt Mutable integer containing the current value of the min/max word length.
     *  @param title String title for the dialog.
     */
    private fun setWordLength(
        buttonViewID: Int,
        stringID: Int,
        mutInt: MutableInteger,
        title: String,
    ) {
        val dialogID = R.layout.dialog_anagram_numberpicker
        val b = AlertDialog.Builder(this)
            .setMessage(getString(R.string.anagram_change_word_length_message, title))
            .setView(layoutInflater.inflate(dialogID, null))
            .setPositiveButton(
                getString(R.string.dialog_ok),
                MinMaxWordLengthClickListener(
                    this,
                    findViewById<View>(buttonViewID) as Button,
                    stringID,
                    mutInt,
                ),
            )
            .setNegativeButton(getString(R.string.dialog_cancel), getEmptyClickListener())
            .create()
        b.show()
        val np = b.findViewById<NumberPicker>(R.id.numPicker)
        np?.minValue = PrivateConstants.MINIMUM_WORD_LENGTH
        np?.maxValue = PrivateConstants.MAXIMUM_WORD_LENGTH
        np?.value = mutInt.mValue
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // SUBCLASSES, ETC.
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Parent for the add/remove letter and min/max word length dialog click listeners.
     *
     *  @param mButtonViewID Reference to the Button that will be updated.
     *  @param mStringID Reference to the string the TextView will be updated with.
     */
    private abstract class ParentClickListener(
        var thisthis: AnagramsActivity,
        /** Reference to the Button that will be updated. */
        var mButtonViewID: Button,
        /** Reference to the string the TextView will be updated with. */
        var mStringID: Int,
    ) : DialogInterface.OnClickListener

    /** Add/remove letter dialog click listener.
     *
     *  @param buttonID Reference to the Button that will be updated.
     *  @param stringID Reference to the string the TextView will be updated with.
     *  @param mMutableRef Reference to the add/remove MutableInteger.
     */
    private inner class AddRemoveLetterClickListener(
        newThisThis: AnagramsActivity,
        buttonID: Button,
        stringID: Int,
        /** Reference to a MutableInteger related to one of the AnagramFinder parameters. */
        private var mMutableRef: Mutable<AnagramFinder.Modifier>,
    ) : ParentClickListener(newThisThis, buttonID, stringID) {
        /** On click callback. */
        override fun onClick(dialog: DialogInterface, which: Int) {
            // Update the passed mutable integer and update the relevant button text.
            mMutableRef.mValue = getAnagramFinderEnum(
                (dialog as Dialog).findViewById<NumberPicker>(R.id.numPicker).value - 1,
            )
            mButtonViewID.text = getString(mStringID, mMutableRef.mValue.amount)
            thisthis.updateViews()
        }
    }

    /** Min/max word length dialog click listener.
     *
     *  @param buttonID Reference to the Button that will be updated.
     *  @param stringID Reference to the string the TextView will be updated with.
     *  @param mMutableRef Reference to the add/remove MutableInteger.
     */
    private inner class MinMaxWordLengthClickListener(
        newThisThis: AnagramsActivity,
        buttonID: Button,
        stringID: Int,
        /** Reference to a MutableInteger related to one of the AnagramFinder parameters. */
        private var mMutableRef: MutableInteger,
    ) : ParentClickListener(newThisThis, buttonID, stringID) {
        /** On click callback. */
        override fun onClick(dialog: DialogInterface, which: Int) {
            // Update the passed mutable integer and update the relevant button text.
            mMutableRef.mValue = (dialog as Dialog).findViewById<NumberPicker>(R.id.numPicker).value
            mButtonViewID.text = getString(mStringID, mMutableRef.mValue)
            thisthis.updateViews()
        }
    }

    object PrivateConstants {
        const val DEFAULT_MAX_WORD_LENGTH = 4
        const val DEFAULT_MIN_WORD_LENGTH = 4
        const val MINIMUM_WORD_LENGTH = 3
        const val MAXIMUM_WORD_LENGTH = 20
    }
}
