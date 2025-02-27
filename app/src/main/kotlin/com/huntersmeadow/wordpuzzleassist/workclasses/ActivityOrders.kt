package com.huntersmeadow.wordpuzzleassist.workclasses

import com.huntersmeadow.wordpuzzleassist.R
import com.huntersmeadow.wordpuzzleassist.activities.AnagramsActivity
import com.huntersmeadow.wordpuzzleassist.activities.BaseActivity
import com.huntersmeadow.wordpuzzleassist.activities.CryptoActivity
import com.huntersmeadow.wordpuzzleassist.activities.DictionaryActivity
import com.huntersmeadow.wordpuzzleassist.activities.FITBActivity
import com.huntersmeadow.wordpuzzleassist.customviews.AssistCard
import java.util.ArrayList

/** Activity Orders defines containers and the order the puzzle assist threads, activities, etc.
 *  should be in. With the exception of the thread pools in 'Thread Manager', all of the containers
 *  and ordering are in this class.
 */
class ActivityOrders private constructor() {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // VARIABLES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** ArrayList of activity class types. */
    private val mActivityClass: ArrayList<Class<out BaseActivity>> = ArrayList()

    /** ArrayList of Assist Card Titles. */
    private val mAssistCardTitles: ArrayList<Int> = ArrayList()

    /** ArrayList of the ActivityOrder enum values. */
    private val mAOEnum: ArrayList<ActivityOrder> = ArrayList()

    /** ArrayList of the directory selection description strings. */
    private val mDirectorySelectionStrings: ArrayList<Int> = ArrayList()

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // OVERRIDES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    init {
        mActivityClass.add(AnagramsActivity::class.java)
        mActivityClass.add(FITBActivity::class.java)
        mActivityClass.add(CryptoActivity::class.java)
        mActivityClass.add(DictionaryActivity::class.java)

        mAssistCardTitles.add(R.string.assist_card_title_anagram)
        mAssistCardTitles.add(R.string.assist_card_title_fitb)
        mAssistCardTitles.add(R.string.assist_card_title_crypto)
        mAssistCardTitles.add(R.string.assist_card_title_dictionary)

        mAOEnum.add(ActivityOrder.ANAGRAM)
        mAOEnum.add(ActivityOrder.FITB)
        mAOEnum.add(ActivityOrder.CRYPTO)
        mAOEnum.add(ActivityOrder.DICTIONARY)

        mDirectorySelectionStrings.add(R.string.directory_selection_anagram_title)
        mDirectorySelectionStrings.add(R.string.directory_selection_anagram_desc)
        mDirectorySelectionStrings.add(R.string.directory_selection_fitb_title)
        mDirectorySelectionStrings.add(R.string.directory_selection_fitb_desc)
        mDirectorySelectionStrings.add(R.string.directory_selection_crypto_title)
        mDirectorySelectionStrings.add(R.string.directory_selection_crypto_desc)
        mDirectorySelectionStrings.add(R.string.directory_selection_dictionary_title)
        mDirectorySelectionStrings.add(R.string.directory_selection_dictionary_desc)
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // SUBCLASSES, ETC.
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Master ordering enum.
     */
    enum class ActivityOrder {
        /** Anagram Enum. */
        ANAGRAM,

        /** Fill In The Blanks Enum.*/
        FITB,

        /** Crypto Solve Enum. */
        CRYPTO,

        /** Dictionary/lexicon look-up Enum.*/
        DICTIONARY,
    }

    companion object {

        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
        // STATIC
        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

        /** Static variable of the global ActivityOrders object. */
        private var mInstance: ActivityOrders = ActivityOrders()

        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
        // REGULAR
        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

        /** Returns the number of activities and assists are in this app.
         *
         *  @return The number of activities and assists are in this app.
         */
        fun getActivityCount(): Int {
            return mInstance.mActivityClass.size
        }

        /** Returns the Activity class according to the given index.
         *
         *  @param index The numerical enum index to get from.
         *  @return The Activity class according to the given index.
         */
        fun getActivityClass(index: Int): Class<out BaseActivity> {
            return mInstance.mActivityClass[index]
        }

        /** Returns the appropriate [AssistCard] title according to the given index.
         *
         *  @param assistType The enum to get from.
         *  @return The appropriate [AssistCard] title according to the given index.
         */
        fun getAssistCardTitle(assistType: ActivityOrder): Int {
            return mInstance.mAssistCardTitles[assistType.ordinal]
        }

        /** Returns the appropriate [ActivityOrder] according to the given Int index.
         *
         *  @param index The numerical enum index to get from.
         *  @return The appropriate [ActivityOrder] according to the given Int index.
         */
        fun getAOEnum(index: Int): ActivityOrder {
            return mInstance.mAOEnum[index]
        }

        /** Returns the directory selection string id Int according to the given index.
         *
         *  @param index The numerical enum index to get from.
         *  @return The directory selection string id Int according to the given index.
         */
        fun getDirectorySelectionStrings(index: Int): Int {
            return mInstance.mDirectorySelectionStrings[index]
        }
    }
}
