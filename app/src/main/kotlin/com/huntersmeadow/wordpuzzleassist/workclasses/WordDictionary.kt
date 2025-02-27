package com.huntersmeadow.wordpuzzleassist.workclasses

import androidx.appcompat.app.AlertDialog
import com.huntersmeadow.wordpuzzleassist.R
import com.huntersmeadow.wordpuzzleassist.WORDPUZZLEASSISTApp
import com.huntersmeadow.wordpuzzleassist.activities.BaseActivity
import com.huntersmeadow.wordpuzzleassist.halve
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.min

/** Encapsulates a list of words as a dictionary/lexicon that supports searching for string words.
 */
class WordDictionary private constructor() {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // VARIABLES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** A list of giant strings with one word per line. Each string contains words of the same size
     *  with the word length equalling the element index. e.g. 3 letter words in the third element
     *  string. */
    private var mWords = ArrayList<String>()

    /** The entire word list loading into a string. */
    private lateinit var mMonolithicString: String

    /** A list of all the words in the dictionary sorted alphabetically. */
    private var mDictionary = ArrayList<String>()

    /** Numerical counter keeping track of successfully loaded interpretations of the word file. */
    private val mLoadedSuccessfully: AtomicInteger = AtomicInteger(0)

    /** Boolean flag to ensure only one error dialog is shown. */
    private val mOneErrorDialog: AtomicBoolean = AtomicBoolean(false)

    /** Lock that pauses execution to allow the dictionary loading to finish before continuing. */
    private val mLock = ReentrantLock()

    /** Part of the above lock.*/
    private val mCondition = mLock.newCondition()

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // OVERRIDES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    init {
        // Load into dictionary.
        object : Thread() {
            override fun run() {
                try {
                    loadIntoArrayList()
                    mLoadedSuccessfully.incrementAndGet()
                    synchronizedNotifyAll()
                } catch (e: IOException) {
                    BaseActivity.getCurrentActivity()?.runOnUiThread { showLoadingWordListError() }
                }
            }
        }.start()

        // Load into sorted, giant strings.
        object : Thread() {
            override fun run() {
                try {
                    loadIntoStrings()
                    mLoadedSuccessfully.incrementAndGet()
                    synchronizedNotifyAll()
                } catch (e: IOException) {
                    BaseActivity.getCurrentActivity()?.runOnUiThread { showLoadingWordListError() }
                }
            }
        }.start()
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // REGULAR
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Returns whether or not every dictionary loading thread has finished.
     *
     *  @return Whether or not every dictionary loading thread has finished.
     */
    fun isLoaded(): Boolean {
        return PrivateConstants.WORD_LOADING_SUCCESS_TARGET == mLoadedSuccessfully.get()
    }

    /** Return whether or not any error dialogs were shown and errors occurred.
     *
     *  @return Whether or not any error dialogs were shown and errors occurred.
     */
    fun okay(): Boolean {
        return !mOneErrorDialog.get()
    }

    /** Returns one of the sorted strings depending on the given word size.
     *
     *  @param wordSize Specifies which word list to access.
     *  @return A word list.
     */
    fun getWords(wordSize: Int): String {
        if (!isLoaded()) {
            waitForThreads()
        }

        return mWords[wordSize]
    }

    fun getMonolithicString(): String {
        if (!isLoaded()) {
            waitForThreads()
        }

        return mMonolithicString
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PRIVATE
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Uses a lock to wait until the dictionary loading thread have finished. Used to halt thread
     *  that intends to use the dictionary until the dictionary is ready.
     */
    private fun waitForThreads() {
        mLock.lock()
        try {
            while (okay() && !isLoaded()) {
                mCondition.await()
            }
        } finally {
            mLock.unlock()
        }
    }

    /** Wrapper for a synchronized notify all. */
    private fun synchronizedNotifyAll() {
        mLock.withLock {
            mCondition.signalAll()
        }
    }

    /** Shows a error dialog from within the word list loading threads. */
    private fun showLoadingWordListError() {
        if (mOneErrorDialog.compareAndSet(false, true)) {
            BaseActivity.getCurrentActivity()?.run {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.error_title))
                    .setMessage(getString(R.string.error_loading_words))
                    .setNeutralButton(
                        getString(R.string.dialog_ok),
                        getEmptyClickListener(),
                    )
                    .setIcon(android.R.drawable.ic_dialog_alert).show()
            }
        }
        synchronizedNotifyAll()
    }

    /** Loads the word list file into an ArrayList/Dictionary and the monolithic string.
     *
     *  @throws IOException if the file doesn't exist or reading it fails.
     */
    @Throws(java.io.IOException::class)
    private fun loadIntoArrayList() {
        val bf = BufferedReader(
            InputStreamReader(
                WORDPUZZLEASSISTApp
                    .appContext()
                    .assets
                    .open(PrivateConstants.WORD_FILE),
            ),
        )
        val sb = StringBuilder("\n")
        while (bf.ready()) {
            val line = bf.readLine()
            mDictionary.add(line)
            sb.append(line)
            sb.append("\n")
        }
        mMonolithicString = sb.toString()
        bf.close()
    }

    /** Loads the word list into strings sorted by word length.
     *
     *  @throws IOException if the file doesn't exist or reading it fails.
     */
    @Throws(java.io.IOException::class)
    private fun loadIntoStrings() {
        val mWordsBuilder = ArrayList<StringBuilder>()
        while (mWordsBuilder.size < PrivateConstants.WORD_MAX_WORD_SIZE) {
            mWordsBuilder.add(StringBuilder())
        }
        val bf = BufferedReader(
            InputStreamReader(
                WORDPUZZLEASSISTApp
                    .appContext()
                    .assets
                    .open(PrivateConstants.WORD_FILE),
            ),
        )
        while (bf.ready()) {
            val line = bf.readLine()
            mWordsBuilder[line.length].append(line)
        }
        for (sBuilder in mWordsBuilder) {
            mWords.add(sBuilder.toString())
        }
    }

    /** Returns whether or not the given word/prefix is in the dictionary. Used binary searching
     *  algorithm.
     *
     *  @param word The word/prefix to search for.
     *  @return True if the search [word]/prefix is found, false otherwise.
     */
    fun findPre(word: String): Boolean {
        if (!isLoaded()) {
            waitForThreads()
        }

        if (word == "") {
            return true
        }

        var left = 0
        var right = mDictionary.size - 1

        while (true) {
            val mid = halve(right + left).toInt()
            val dict = mDictionary[mid]

            if (dict.length >= word.length) {
                if (word == dict.substring(0, word.length)) {
                    return true
                }
            }

            var charCompare = false

            // Compare both Strings to determine alphabetic order.
            for (c in dict.indices) {
                if (word[c] > dict[c]) {
                    charCompare = true
                    left = mid
                    break
                } else if (word[c] < dict[c]) {
                    charCompare = true
                    right = mid
                    break
                }
            }

            // Either word or dict is a leading substring of the other.
            if (!charCompare) {
                left = mid
            }

            if (right - left <= 1)
                // No more words to search through.
                {
                    return false
                }
        }
    }

    /** Returns whether or not the given word is in the dictionary. Used binary searching algorithm.
     *
     *  @param word Word to search for in the dictionary.
     *  @return True if the search [word] is found, false otherwise.
     */
    fun findInDictionary(word: String): Boolean {
        if (!isLoaded()) {
            waitForThreads()
        }

        var leftmostIndex = 0
        var rightmostIndex = mDictionary.size - 1

        while (true) {
            val midIndex = halve(rightmostIndex + leftmostIndex).toInt()
            val midWord = mDictionary[midIndex]

            if (word == midWord) {
                return true
            }

            var charCompare = false

            // Compare the search word against the middle word to find which side (left or right)
            // to continue searching in. Does nothing if the search word or middle word is a leading
            // / substring of the other.
            for (c in 0 until min(word.length, midWord.length)) {
                if (word[c] > midWord[c]) {
                    charCompare = true
                    leftmostIndex = midIndex
                    break
                } else if (word[c] < midWord[c]) {
                    charCompare = true
                    rightmostIndex = midIndex
                    break
                }
            }

            // Either the search word or middle word is a leading substring of the other so decide
            // left or right depending on which one is bigger.
            if (!charCompare) {
                if (word.length < midWord.length) {
                    rightmostIndex = midIndex
                } else if (word.length > midWord.length) {
                    leftmostIndex = midIndex
                }
            }

            // No more words to search through, must not have found the search word.
            if (rightmostIndex - leftmostIndex <= 1) {
                return mDictionary[leftmostIndex] == word
            }
        }
    }

    companion object {

        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
        // STATIC
        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

        /** Reference to a static 'this'. */
        private var mInstance: WordDictionary = WordDictionary()

        /** Returns a reference to the [WordDictionary] instance.
         *
         *  @return The global static [WordDictionary] instance.
         */
        fun instance(): WordDictionary {
            return mInstance
        }
    }

    object PrivateConstants {
        const val WORD_LOADING_SUCCESS_TARGET = 2
        const val WORD_MAX_WORD_SIZE = 50
        const val WORD_FILE = "words.txt"
    }
}
