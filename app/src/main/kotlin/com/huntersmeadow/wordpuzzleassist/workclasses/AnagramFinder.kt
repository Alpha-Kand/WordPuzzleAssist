package com.huntersmeadow.wordpuzzleassist.workclasses

import com.huntersmeadow.wordpuzzleassist.MutableInteger
import com.huntersmeadow.wordpuzzleassist.removeLetter
import com.huntersmeadow.wordpuzzleassist.reverseSubFactorial
import java.util.Locale
import java.util.Stack
import java.util.TreeSet

/** A pseudo library class. Can compute various anagram and anagram-like operations on string input.
 */
class AnagramFinder(callback: MutableInteger = MutableInteger(-1)) : AssistParent() {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // VARIABLES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Contains the results of the last/in-progress operation. */
    private var mResults = TreeSet<String>()

    /** Shortcut reference to the WordDictionary object. */
    private var mWD = WordDictionary.instance()

    /** Function reference that checks if a word is in the dictionary when not testing and always
     *  returns true when testing. */
    private var mCheckIfWord = ::checkIfWordDictionary

    /** Function reference that checks if the input starts or is a word is in the dictionary when
     *  not testing and always returns true when testing. */
    private var mCheckIfPre = ::checkIfPreDictionary

    /** Whether or not this AnagramFinder object is in testing mode or not. */
    private var mTesting = false

    /** Progress integer that this [AnagramFinder] updates as it progresses. */
    private var mProgress: MutableInteger = callback

    /** Variable containing the true progress according to various input values. Converted to an
     *  Integer and given to [mProgress] regularly. */
    private var mInternal: Double = 0.0

    /** Information regarding how to handle the increase in progress. */
    private var mUpdatePOD = UpdatePOD(0, 0.0)

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PUBLIC
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Computes the anagrams of a given input.
     *
     *  @param input The input string.
     *  @param modifier See [Modifier].
     *  @return An ArrayList containing the results of the computation.
     */
    fun computeAnagram(input: String, modifier: Modifier = Modifier.NONE): ArrayList<String> {
        val lambda = { value: String, _: Int, _: Int -> computeAnagramWrapped(value) }
        return compute(lambda, input, -1, -1, modifier)
    }

    /** Attempts to find phrases that can be made out of the given input. A phrase consists of
     *  several words anagram-ed from the whole. Every letter will be used once. Note that the given
     *  minimum and maximum amount of letters for the sub-words must combine in some way to exactly
     *  equal the amount of letters in the input string.
     *
     *  @param input The input string.
     *  @param modifier See [Modifier].
     *  @param min The minimum word size.
     *  @param max The maximum word size.
     *  @return An ArrayList containing the results of the computation.
     */
    fun computePhrases(
        input: String,
        modifier: Modifier = Modifier.NONE,
        min: Int = 1,
        max: Int = 50,
    ): ArrayList<String> {
        val lambda = {
                value: String,
                mini: Int,
                maxi: Int,
            ->
            computePhrasesWrapped(value, mini, maxi)
        }
        return compute(lambda, input, min, max, modifier)
    }

    /** Attempts to find sub-words in the given input. Each sub-word is at least 1 letter smaller
     *  than the given input.
     *
     *  @param input The input string.
     *  @param modifier See [Modifier].
     *  @param min The minimum word size.
     *  @param max The maximum word size.
     *  @return An ArrayList containing the results of the computation.
     */
    fun computeSubWords(
        input: String,
        modifier: Modifier = Modifier.NONE,
        min: Int = 1,
        max: Int = 50,
    ): ArrayList<String> {
        val lambda = {
                value: String,
                mini: Int,
                maxi: Int,
            ->
            computeSubWordsWrapped(value, mini, maxi)
        }
        return compute(lambda, input, min, max, modifier)
    }

    /** Enables testing mode for the AnagramFinder object. This means that non-word combinations are
     *  returned in future computations results.
     */
    fun enableTesting() {
        mCheckIfWord = ::acceptAllIterations
        mCheckIfPre = ::acceptAllIterations
        mTesting = true
    }

    /** Disables testing mode for the AnagramFinder object. This means that only words in the
     *  dictionary are returned in future computation results.
     */
    fun disableTesting() {
        mCheckIfWord = ::checkIfWordDictionary
        mCheckIfPre = ::checkIfPreDictionary
        mTesting = false
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PRIVATE
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Generalizes the anagram/phrase/sub-word method calls to the three types of modifiers.
     *
     *  @param lambda A lambda containing a anagram/phrase/sub-word method to be called.
     *  @param rawInput The input string.
     *  @param min The minimum word size if relevant.
     *  @param max The maximum word size if relevant.
     *  @return An ArrayList containing the search results.
     */
    private fun compute(
        lambda: (String, Int, Int) -> Unit,
        rawInput: String,
        min: Int,
        max: Int,
        modifier: Modifier = Modifier.NONE,
    ): ArrayList<String> {
        // Very quick values to check and abort early if possible.
        if (rawInput == "" || min == 0 || max == 0) {
            mProgress.mValue = 100
            return ArrayList()
        }

        mResults.clear()
        mProgress.mValue = 0
        mInternal = 0.0
        val input = prepareInput(rawInput)
        when (modifier) {
            Modifier.NONE -> {
                // Check if we are computing anagrams.
                if (min == -1 && max == -1) {
                    // Handle 1 character inputs.
                    if (input.length == 1) {
                        if (mCheckIfWord(input)) {
                            mResults.add(input)
                        }
                        mProgress.mValue = 100
                        return ArrayList(mResults)
                    }
                }

                mUpdatePOD = UpdatePOD(input.length, 1.0)

                lambda(input, min, max)

                if (!mTesting) {
                    mResults.remove(input)
                }
            }

            Modifier.ADD_LETTER -> {
                mUpdatePOD = UpdatePOD(input.length + 1, 26.0)
                val runningResults = TreeSet<String>()
                // Add each letter once to the anagram and collect all of the results.
                for (c in 'a'..'z') {
                    mResults.clear()
                    lambda(input + c, min, max)
                    runningResults.addAll(mResults)
                }
                mResults = runningResults
            }

            Modifier.REMOVE_LETTER -> {
                mUpdatePOD = UpdatePOD(input.length - 1, (input.length).toDouble())
                val runningResults = TreeSet<String>()
                // Remove each letter once and collect the results.
                for (i in input.indices) {
                    mResults.clear()
                    // Check if removing a letter leaves us with only one character and handle it
                    // immediately.
                    val smallerInput = removeLetter(input, i)
                    if (smallerInput.length == 1) {
                        if (mCheckIfWord(smallerInput)) {
                            mResults.add(smallerInput)
                        }
                        mInternal += (1.0 / smallerInput.length.toDouble()) * 100
                        mProgress.mValue = mInternal.toInt()
                    } else {
                        lambda(removeLetter(input, i), min, max)
                    }
                    runningResults.addAll(mResults)
                }
                mResults = runningResults

                if (!mTesting) {
                    mResults.remove(input)
                }
            }
        }

        if (mCancelled) {
            mResults.clear()
            mResults.add("Computation cancelled.")
        }
        if (!mTesting) {
            mProgress.mValue = 100
        }
        return ArrayList(mResults)
    }

    /** Computes finding standard anagrams with the given input. Fills [mResults] with the
     *  computation results.
     *
     *  @param input The input string to compute.
     */
    private fun computeAnagramWrapped(input: String) {
        val stack = Stack<Pair<String, String>>()
        stack.push(Pair("", input))

        while (!stack.empty()) {
            if (mCancelled) {
                return
            }
            val top = stack.pop()
            val pre = top.first
            val post = top.second

            if (post.length == 2) {
                handleTwoLengthAnagram(pre, post)
                increaseProgress(mUpdatePOD.mLastTwo)
                continue
            }

            for (a in post.indices) {
                if (!checkForDuplicateLetters(a, post)) {
                    val subPre = pre + post[a]
                    val subPost = removeLetter(post, a)

                    if (mCheckIfPre(subPre)) {
                        stack.push(Pair(subPre, subPost))
                    } else {
                        increaseProgress(subPre.length)
                    }
                } else {
                    increaseProgress(pre.length + 1)
                }
            }
        }
    }

    /** Computes finding phrases with the given input. Fills [mResults] with the computation
     *  results.
     *
     *  @param input The input string to compute.
     *  @param min The minimum word size to add to results.
     *  @param max The maximum word size to add to results.
     */
    private fun computePhrasesWrapped(
        input: String,
        min: Int,
        max: Int,
    ) {
        val stack = Stack<PhraseStackItem>()
        stack.push(PhraseStackItem("", input, TreeSet()))

        while (!stack.empty()) {
            if (mCancelled) {
                return
            }
            val top = stack.pop()
            val pre = top.mPre
            val post = top.mPost
            val tree = top.mTree

            if (post.length == 2 && post.length + pre.length in min..max) {
                handleTwoLengthPhrases(pre, post, tree)
                increaseProgress(mUpdatePOD.mLastTwo)
                continue
            }

            for (a in post.indices) {
                if (!checkForDuplicateLetters(a, post)) {
                    val subPre = pre + post[a]
                    val subPost = removeLetter(post, a)
                    val stackSize = stack.size

                    if (mCheckIfWord(subPre) && subPre.length in min..max) {
                        val newTree = TreeSet(tree)
                        newTree.add(subPre)
                        stack.push(PhraseStackItem("", subPost, newTree))
                    }

                    if (mCheckIfPre(subPre)) {
                        stack.push(PhraseStackItem(subPre, subPost, TreeSet(tree)))
                    }

                    if (stackSize == stack.size) {
                        increaseProgress(subPre.length)
                    }
                } else {
                    increaseProgress(pre.length + 1)
                }
            }
        }
    }

    /** Computes finding sub words with the given input. Fills [mResults] with the computation
     *  results.
     *
     *  @param input The input string to compute.
     *  @param min The minimum word size to add to results.
     *  @param max The maximum word size to add to results.
     */
    private fun computeSubWordsWrapped(
        input: String,
        min: Int,
        max: Int,
    ) {
        val stack = Stack<Pair<String, String>>()
        stack.push(Pair("", input))

        while (!stack.empty()) {
            if (mCancelled) {
                return
            }
            val top = stack.pop()
            val pre = top.first
            val post = top.second

            for (a in post.indices) {
                if (!checkForDuplicateLetters(a, post)) {
                    val subPre = pre + post[a]
                    val subPost = removeLetter(post, a)
                    val stackSize = stack.size

                    if (mCheckIfWord(subPre) && subPre.length in min..max) {
                        mResults.add(subPre)
                        stack.push(Pair("", subPost))
                    }

                    if (mCheckIfPre(subPre)) {
                        stack.push(Pair(subPre, subPost))
                    }

                    if (stackSize == stack.size) {
                        increaseProgress(subPre.length)
                    }
                } else {
                    increaseProgress(pre.length + 1)
                }
            }
        }
    }

    /** Checks if we are about to swap the same letter to keep from computing the same configuration
     *  again.
     *
     *  @param a Index of letter to check.
     *  @param post Remaining letters to check before being swapped.
     *  @return True if the letter is a duplicate, false otherwise.
     */
    private fun checkForDuplicateLetters(a: Int, post: String): Boolean {
        if (!mTesting) {
            for (b in 0 until a) {
                if (post[a] == post[b]) {
                    return true
                }
            }
        }
        return false
    }

    /** Quickly handles the last two letters to be rearranged instead of looping until the very end.
     *
     *  @param pre The prefix of the new potential word.
     *  @param post The last two letters to add and rearrange at the end.
     */
    private fun handleTwoLengthAnagram(pre: String, post: String) {
        val a = pre + post[0] + post[1]
        val b = pre + post[1] + post[0]

        if (mCheckIfWord(a)) {
            mResults.add(a)
        }

        if (mCheckIfWord(b)) {
            mResults.add(b)
        }
    }

    /** Quickly handles the last two letters to be rearranged instead of looping until the very end.
     *
     *  @param pre The prefix of the new potential word.
     *  @param post The last two letters to add and rearrange at the end.
     */
    private fun handleTwoLengthPhrases(pre: String, post: String, tree: TreeSet<String>) {
        (pre + post[0] + post[1]).let {
            if (mCheckIfWord(it)) {
                addPhrase(it, tree)
            }
        }

        (pre + post[1] + post[0]).let {
            if (mCheckIfWord(it)) {
                addPhrase(it, tree)
            }
        }
    }

    /** Turns the given input and tree into a string to be added to [mResults]. Arguably the biggest
     *  bottleneck in the computation.
     *
     *  @param input The last word to add to the phrase.
     *  @param tree The phrase tree in progress.
     */
    private fun addPhrase(input: String, tree: TreeSet<String>) {
        tree.add(input)
        val sb = StringBuilder()
        tree.forEach {
            sb.append(it)
            sb.append(" ")
        }

        mResults.add(sb.substring(0, sb.length - 1))
        tree.remove(input)
    }

    /** Checks if the given string is in the dictionary. Meant to be referenced via [mCheckIfWord]
     *  when not testing.
     *
     *  @param input The string to check.
     *  @return Whether or not the input is in the dictionary.
     */
    private fun checkIfWordDictionary(input: String): Boolean {
        return mWD.findInDictionary(input)
    }

    /** Checks if the given string prefix is in the dictionary. Meant to be referenced via
     *  [mCheckIfPre] when not testing.
     *
     *  @param input The string to check.
     *  @return Whether or not the prefix is in the dictionary.
     */
    private fun checkIfPreDictionary(input: String): Boolean {
        return mWD.findPre(input)
    }

    /** Always returns true. Meant to be referenced via [mCheckIfWord] and [mCheckIfPre] when
     *  testing.
     *
     *  @param input The string to check.
     *  @return Whether or not the input is in the dictionary.
     */
    private fun acceptAllIterations(@Suppress("UNUSED_PARAMETER") input: String): Boolean {
        return true
    }

    /** Increases the progress of the current computation.
     *
     *  @param preLetters The amount of letters being analyzed to figure out how much progress to
     *                    skip to. If we are skipping the last n letters of the input, this
     *                    number should be total_length - n.
     */
    private fun increaseProgress(preLetters: Int) {
        mInternal += (
                reverseSubFactorial(preLetters, preLetters).toDouble() / reverseSubFactorial(
                    mUpdatePOD.mLength,
                    mUpdatePOD.mLength,
                ).toDouble()
                ) * 100.0
        mProgress.mValue = (mInternal / mUpdatePOD.mMultiplier).toInt()
    }

    /** Sets the input to lowercase and removes all non-letter characters.
     *
     *  @param rawInput Unchanged original input.
     *  @return Input ready to compute.
     */
    private fun prepareInput(rawInput: String): String {
        val sb = StringBuilder("")
        for (char in rawInput.lowercase(Locale.getDefault())) {
            if (char in 'a'..'z') {
                sb.append(char)
            }
        }
        return sb.toString()
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // SUBCLASSES, ETC.
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Enum containing the possible modifications to the various computations.
     *
     *  @param amount The number of letters to take or remove when calculating the anagram results.
     */
    enum class Modifier(val amount: Int) {
        /** Default behaviour. */
        NONE(0),

        /** The computation is done 26 different times, with each time 1 different letter of the
         *  alphabet is added to the given input. */
        ADD_LETTER(1),

        /** The computation is done n times, where n is the amount of letters in the input. Each
         *  computation, 1 different letter is removed. */
        REMOVE_LETTER(-1),
    }

    /** Represents a single frame in the find-phrase computation.
     */
    private class PhraseStackItem(pre: String, post: String, tree: TreeSet<String>) {
        /** The front of a current iteration. */
        var mPre = pre

        /** The rest of the input to rearrange. */
        var mPost = post

        /** The tree containing the current phrase. */
        var mTree = tree
    }

    /** Contains information regarding the current input progress values. */
    class UpdatePOD(length: Int, multiplier: Double) {
        /** Length of the true input (must reflect +-1 letter modifier). */
        var mLength: Int = length

        /** Refers to skipping the last two letters with a quick swap-and-compare. */
        val mLastTwo: Int = 2

        /** The amount of 'passes' over the word the computation will take (must reflect +- letter
         *  modifier). */
        var mMultiplier: Double = multiplier
    }
}
