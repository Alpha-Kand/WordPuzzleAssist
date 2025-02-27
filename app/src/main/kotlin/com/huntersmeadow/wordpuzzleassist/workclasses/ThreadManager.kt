package com.huntersmeadow.wordpuzzleassist.workclasses

import com.huntersmeadow.wordpuzzleassist.Mutable
import com.huntersmeadow.wordpuzzleassist.MutableInteger
import com.huntersmeadow.wordpuzzleassist.R
import com.huntersmeadow.wordpuzzleassist.WORDPUZZLEASSISTApp
import java.util.Collections
import kotlin.collections.ArrayList

class ThreadManager {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // VARIABLES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Contains all the threads and their related information. */
    var mPOD = ArrayList<AssistInfo>()

    /** The next index to assign to new thread jobs. */
    private var mCurrentIndex = 0

    /** The current thread in focus in whatever assist activity we're in. */
    var mFocusThread = -1

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PUBLIC
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Start the anagram assist thread.
     *
     *  @param input String input to pass to the assist.
     *  @return A pair containing the threads index and progress integer.
     */
    fun startAnagram(
        input: String,
        typeTM: ThreadManagerAnagramType,
        addRemove: AnagramFinder.Modifier,
        min: Int,
        max: Int,
    ): Pair<Int, MutableInteger> {
        // Add ANAGRAM specific data for this thread.
        val currentPOD = AssistInfo(input, mCurrentIndex, ActivityOrders.ActivityOrder.ANAGRAM)
        currentPOD.mAnagramAddRemove = addRemove
        currentPOD.mAnagramMax = max
        currentPOD.mAnagramMin = min
        currentPOD.mAnagramType = typeTM.ordinal
        mPOD.add(currentPOD)

        // Start the calculation coroutine.
        val updateJob = Thread {
            val af = AnagramFinder(currentPOD.mMutableInteger)
            currentPOD.mAssistParent = af
            val results = when (typeTM) {
                ThreadManagerAnagramType.PHRASES -> af.computePhrases(input, addRemove, min, max)
                ThreadManagerAnagramType.SUB_WORDS -> af.computeSubWords(input, addRemove, min, max)
                else -> af.computeAnagram(input, addRemove)
            }
            finishThread(currentPOD, results)
        }
        // Return the new coroutine's index and progress MutableInteger.
        return Pair(genericThreadGo(updateJob), currentPOD.mMutableInteger)
    }

    /** Start the FITB assist thread.
     *
     *  @param input String input to pass to the assist.
     *  @return A pair containing the threads index and progress integer.
     */
    fun startFITB(input: String, statuses: BooleanArray): Pair<Int, MutableInteger> {
        // Add FITB specific data for this thread.
        val currentPOD = AssistInfo(input, mCurrentIndex, ActivityOrders.ActivityOrder.FITB)
        currentPOD.mFITBSwitchStatuses = statuses
        mPOD.add(currentPOD)

        // Start the calculation coroutine.
        val updateJob = Thread {
            val fitb = FITBFinder()
            currentPOD.mAssistParent = fitb
            finishThread(currentPOD, fitb.computeFITB(input, statuses))
        }

        // Return the new coroutine's index and progress MutableInteger.
        return Pair(genericThreadGo(updateJob), currentPOD.mMutableInteger)
    }

    /** Start the crypto assist thread.
     *
     *  @param input String input to pass to the assist.
     *  @return A pair containing the threads index and progress integer.
     */
    fun startCrypto(input: String): Pair<Int, MutableInteger> {
        // Add CRYPTO specific data for this thread.
        val currentPOD = AssistInfo(input, mCurrentIndex, ActivityOrders.ActivityOrder.CRYPTO)
        mPOD.add(currentPOD)

        // Start the calculation coroutine.
        val updateJob = Thread {
            val crypto = CryptoFinder(
                currentPOD.mMutableInteger,
                currentPOD.mAttemptNum,
                currentPOD.mMaxAttempts,
            )
            currentPOD.mAssistParent = crypto
            finishThread(currentPOD, crypto.computeCrypto(input).second)
        }

        // Return the new coroutine's index and progress MutableInteger.
        return Pair(genericThreadGo(updateJob), currentPOD.mMutableInteger)
    }

    /** Start the crypto assist thread.
     *
     *  @param input String input to pass to the assist.
     *  @return A pair containing the threads index and progress integer.
     */
    fun startDictionary(input: String): Pair<Int, MutableInteger> {
        // Add DICTIONARY specific data for this thread.
        val currentPOD = AssistInfo(input, mCurrentIndex, ActivityOrders.ActivityOrder.DICTIONARY)
        mPOD.add(currentPOD)

        // Start the calculation coroutine.
        val updateJob = Thread {
            val results = ArrayList<String>()
            if (WordDictionary.instance().findInDictionary(input)) {
                results.add(WORDPUZZLEASSISTApp.appContext().getString(R.string.dictionary_successful_find, input))
            }
            finishThread(currentPOD, results)
        }

        // Return the new coroutine's index and progress MutableInteger.
        return Pair(genericThreadGo(updateJob), currentPOD.mMutableInteger)
    }

    /** Cancels the thread at the given index. Does not delete it and it's related information can
     *  still be accessed.
     */
    fun cancelThread(index: Int) {
        if (index > -1 && index < mPOD.size) {
            mPOD[index].mCancelled = true
            mPOD[index].mAssistParent.cancelComputation()
        }
    }

    /** Deletes the thread at the given index.
     *
     *  @param index Index to delete thread at.
     */
    fun deleteThread(index: Int) {
        if (index > -1 && index < mPOD.size) {
            mPOD.removeAt(index)
            for (i in index until mPOD.size) {
                mPOD[i].mIndex--
            }
            mCurrentIndex--
        }
    }

    /** Swaps the positions of two threads at the given indexes.
     *
     *  @param firstIndex First thread to swap with second.
     *  @param secondIndex Second thread to swap with first.
     */
    fun swapThreads(firstIndex: Int, secondIndex: Int) {
        // Swap the indexes held in the [POD]s.
        val index = mPOD[firstIndex].mIndex
        mPOD[firstIndex].mIndex = mPOD[secondIndex].mIndex
        mPOD[secondIndex].mIndex = index
        Collections.swap(mPOD, firstIndex, secondIndex)
    }

    /** Retrieves anagram related information.
     *
     *  @param index Index of thread to get information from.
     *  @param add Add/remove letter or neither modifier.
     *  @param max Maximum word size.
     *  @param min Minimum word size.
     *  @param type Anagram/phrases/sub words specifier.
     */
    fun getAnagramInfo(
        index: Int,
        add: Mutable<AnagramFinder.Modifier>,
        max: MutableInteger,
        min: MutableInteger,
        type: MutableInteger,
    ) {
        add.mValue = mPOD[index].mAnagramAddRemove
        max.mValue = mPOD[index].mAnagramMax
        min.mValue = mPOD[index].mAnagramMin
        type.mValue = mPOD[index].mAnagramType
    }

    /** Retrieves FITB related information.
     *
     *  @param index Index of thread to get information from.
     *  @param statuses Ignore letters information for FITB.
     */
    fun getFITBInfo(index: Int, statuses: Mutable<BooleanArray>) {
        statuses.mValue = mPOD[index].mFITBSwitchStatuses
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PRIVATE
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Starts the next thread and updates the next index.
     *
     *  @param updateJob Job to start and save.
     *  @return The index of the current job.
     */
    private fun genericThreadGo(updateJob: Thread): Int {
        updateJob.start()
        mPOD[mCurrentIndex].mJob = updateJob
        mFocusThread = mCurrentIndex
        return mCurrentIndex++
    }

    /** Finishes up the thread results and progress values.
     *
     *  @param currentPOD Reference to the POD containing the relevant data.
     *  @param results The results of the the assist computation.
     */
    private fun finishThread(
        currentPOD: AssistInfo,
        results: ArrayList<String>,
    ) {
        if (results.isEmpty()) {
            results.add(
                WORDPUZZLEASSISTApp.appContext().resources.getString(R.string.base_thread_no_results),
            )
        }
        currentPOD.mResults = results
        currentPOD.mMutableInteger.mValue =
            if (currentPOD.mCancelled) {
                0
            } else {
                PublicConstants.THREAD_PROGRESS_MAX
            }
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // SUBCLASSES, ETC.
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Anagram type enum. Owned by [ThreadManager] as [AnagramFinder] itself has no need of it.
     */
    enum class ThreadManagerAnagramType {
        NORMAL,
        PHRASES,
        SUB_WORDS,
    }

    /** POD class for thread information.
     *
     *  @param input Input string for this thread job.
     *  @param index New index for this thread job.
     *  @param assistType Assist type specifying the thread job type.
     */
    class AssistInfo(input: String, index: Int, assistType: ActivityOrders.ActivityOrder) {

        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
        // VARIABLES
        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

        // / GENERIC VARIABLES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        var mInput: String = input
        var mIndex: Int = index
        var mAssistType = assistType
        var mCancelled: Boolean = false
        var mMutableInteger = MutableInteger(0)
        var mJob = Thread()
        var mResults = makeNewResultList()
        var mAssistParent = AssistParent()

        // / ANAGRAM VARIABLES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        var mAnagramAddRemove: AnagramFinder.Modifier = AnagramFinder.Modifier.NONE
        var mAnagramMin: Int = 0
        var mAnagramMax: Int = 0
        var mAnagramType: Int = 0

        // / FITB VARIABLES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        var mFITBSwitchStatuses = BooleanArray(26)

        // / CRYPTO VARIABLES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        var mAttemptNum = MutableInteger(1)
        var mMaxAttempts = MutableInteger(0)

        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
        // PRIVATE
        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

        /** Returns a new ArrayList with the 'no results yet' string in it.
         *
         *  @return Default ArrayList.
         */
        private fun makeNewResultList(): ArrayList<String> {
            val al = ArrayList<String>()
            al.addAll(
                WORDPUZZLEASSISTApp.appContext().resources.getStringArray(R.array.generic_no_results_yet_text),
            )
            return al
        }
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // STATIC
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    companion object {

        private var mInstance: ThreadManager = ThreadManager()

        fun instance(): ThreadManager {
            return mInstance
        }
    }
}
