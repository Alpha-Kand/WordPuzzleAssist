package com.huntersmeadow.wordpuzzleassist.workclasses

import com.huntersmeadow.wordpuzzleassist.MutableInteger
import org.junit.Assert.assertEquals
import java.util.Stack

class CryptoFinder(
    callback: MutableInteger = MutableInteger(-1),
    reattempts: MutableInteger = MutableInteger(-1),
    maxAttempts: MutableInteger = MutableInteger(0),
) : AssistParent() {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // VARIABLES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** The collected results that perfectly match the given crypto cypher. */
    private val mResults = ArrayList<ArrayList<StringBuilder>>()

    /** The 'close matches' of the computation. Collected to hopefully return something useful. */
    private val mCloseMatches = ArrayList<Pair<Int, ArrayList<StringBuilder>>>()

    /** Running collection of words that complete the current solving attempt. */
    private var mSavedWords = ArrayList<StringBuilder>()

    /** A stack of currently used character arrays. Updated each new word. */
    private var mUsedCharacterStack = Stack<BooleanArray>()

    /** List of special symbols and capital letters applicable to the raw input.*/
    private val mRemovedSymbols = java.util.ArrayList<Pair<Int, Char>>()

    /** Record of gaps after each word in the raw input. */
    private var mInputGaps = ArrayList<Int>()

    /** Whether or not the latest computation was completely successful or not. */
    private var mHasCompleteResults = false

    /** The Dissector object that is built around the current input. */
    private lateinit var mDissector: Dissector

    /** Progress integer that this [CryptoFinder] updates as it progresses. */
    private var mProgress: MutableInteger = callback

    private var mAttemptNum: MutableInteger = reattempts
    private var mMaxAttempts: MutableInteger = maxAttempts

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PUBLIC
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Computes a substitution cypher given an input of words.
     *
     *  @param rawInput A string of various words.
     *  @return Return.first = Whether or not the results are complete or not.
     *          Return.second = A list of lists containing the strings in each possible result.
     */
    fun computeCrypto(rawInput: String): Pair<Boolean, ArrayList<String>> {
        // Reset algorithm collections for this next search.
        mResults.clear()
        mRemovedSymbols.clear()
        mInputGaps.clear()
        mSavedWords.clear()
        mCloseMatches.clear()
        mUsedCharacterStack.clear()
        mUsedCharacterStack.push(BooleanArray(26))
        mHasCompleteResults = true
        // Tokenize the input and sort the words. The word order is remembered for later.
        val sortPairList = getSortPairList(removeSymbols(rawInput))
        // Figure out which letters are repeated in the entire list.
        mDissector = Dissector(sortPairList.first)
        // 'ratchet' will be used to indicate which word we are currently replacing.
        val ratchet = 0
        mCloseMatches.add(Pair(-1, ArrayList()))
        // Keep track of how many times we can retry solving the cryptogram.
        var retries = 0
        mMaxAttempts.mValue = sortPairList.first.size
        // Run the main solving algorithm.
        while (mCloseMatches.size == 1) {
            computeCryptoWrapped(getBlankList(sortPairList.first), ratchet)
            if (mCancelled) {
                mResults.clear()
                return Pair(false, ArrayList())
            }

            // If there aren't even any close attempts, perhaps the first word in the list provides
            // zero results. Therefore, cycle through the words in order of length until we loop
            // through all the words or a close match is found.
            if (mCloseMatches.size == 1) {
                mAttemptNum.mValue++
                retries++
                if (retries == sortPairList.first.size) {
                    mHasCompleteResults = false
                    return Pair(mHasCompleteResults, ArrayList())
                }
                // First we reorder the sorting pairs so the words are correctly sorted later if a
                // close match is found.
                // Record the first sort pair.
                val firstOrder = sortPairList.second[0].second
                // Cycle the ordering pairs through the sort list except the last value.
                // e.g. "3 1 2 0" -> "1 2 0 0"
                val orderingPairs = sortPairList.second
                for (i in 0 until orderingPairs.size - 1) {
                    orderingPairs[i] = Pair(orderingPairs[i].first, orderingPairs[i + 1].second)
                }
                // Give the last ordering pair the sort order of the first, finishing the cycle
                orderingPairs[orderingPairs.size - 1] = Pair(orderingPairs.last().first, firstOrder)

                // Move the text sort pair to the back, mimicking the sort pairs.
                sortPairList.first.add(sortPairList.first.first())
                sortPairList.first.removeAt(0)
                // Reset the Dissector to reflect the new word ordering.
                mDissector = Dissector(sortPairList.first)
            }
        }
        // Tidy up the results.
        var stringResults = ArrayList<String>()
        mHasCompleteResults = true
        stringResults = analyzeCompleteResults(stringResults, sortPairList)
        stringResults = analyzeIncompleteResults(stringResults, sortPairList)
        return Pair(mHasCompleteResults, stringResults)
    }

    /** To be used to unit test internal functions. I know it's not perfect methodology, but it's
     *  quite valuable to have these checks. Call this function in a unit tested function. It calls
     *  all of this class' unit tests that test private functions and methods.
     */
    fun unitTestInternals() {
        UNIT_TEST_compareStringBuilderToDictWord()
        UNIT_TEST_getNextIncompleteWord()
        UNIT_TEST_cloneArrayListOfStringBuilders()
        UNIT_TEST_getSlimmedWord()
        UNIT_TEST_getSortPairList()
        UNIT_TEST_getBlankList()
        UNIT_TEST_fillInLetters()
        UNIT_TEST_applySlimmedWord()
        UNIT_TEST_removeSymbols()
        UNIT_TEST_insertSymbols()
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PRIVATE
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Compares a StringBuilder's characters to a [DictWord]'s characters in order. Similar to
     *  comparing strings.
     *
     *  @param first StringBuilder to compare.
     *  @param second DictWord to compare.
     *  @return True if the inputs match, false otherwise.
     */
    private fun compareStringBuilderToDictWord(first: StringBuilder, second: DictWord): Boolean {
        if (first.length == second.getLength()) {
            for (a in first.indices) {
                if (second.char(a) != first[a]) {
                    return false
                }
            }
            return true
        }
        return false
    }

    /** Finds the next incomplete word to attempt to solve. Does not assume the StringBuilder at the
     *  given ratchet index is already complete.
     *
     *  @param blankDupREF Reference to the current 'blankDup'.
     *  @param ratchet The 'blankDupREF' index to begin looking from.
     *  @return The index of the next incomplete word in 'blankDupREF'. Returns -1 if there are no
     *          more incomplete words.
     */
    private fun getNextIncompleteWord(blankDupREF: ArrayList<StringBuilder>, ratchet: Int): Int {
        for (i in ratchet until blankDupREF.size) {
            var incomplete = 0
            for (character in blankDupREF[i]) {
                if (character == '-') {
                    incomplete++
                }
            }
            if (incomplete > 0) {
                return i
            }
        }
        return -1 // All the words are complete.
    }

    /** Clones an ArrayList of StringBuilders. The new list contains completely new StringBuilder
     *  objects. This is fairly slow so use sparingly.
     *
     *  @param from ArrayList to copy.
     *  @return A new ArrayList of StringBuilders that have the same contents of the originals,
     *          except with new objects.
     */
    private fun cloneArrayListOfStringBuilders(from: ArrayList<StringBuilder>): ArrayList<StringBuilder> {
        val to = ArrayList<StringBuilder>()
        for (element in from) {
            to.add(StringBuilder(element))
        }
        return to
    }

    @Suppress("SpellCheckingInspection")
    /** Returns a list of characters that make up the referenced word. Each unique character only
     *  shows up once, and in the order they are first discovered.
     *  Examples "hello -> helo", "hippopotamus -> hipotamus" "success -> suces"
     *
     *  @param dWordREF [DictWord] reference for the desired word
     *  @return StringBuilder containing the new list of characters.
     */
    private fun getSlimmedWord(dWordREF: DictWord): StringBuilder {
        val booleans = BooleanArray(26)
        val sb = StringBuilder("")
        for (i in 0 until dWordREF.getLength()) {
            val character = dWordREF.char(i)
            val charInt = character.code - 'a'.code
            if (!booleans[charInt]) {
                sb.append(character)
                booleans[charInt] = true
            }
        }
        return sb
    }

    /** Converts the input into a list of [SortPair]s, one for each token and sorts them by length,
     *  largest to smallest.
     *
     * @param input Input string.
     * @return return.first = The list of [SortPair]s, return.second = List of Pairs, matching each
     *         SortPairs current index to it's original unsorted index.
     */
    private fun getSortPairList(input: String): Pair<ArrayList<SortPair>, ArrayList<Pair<Int, Int>>> {
        val sortPairList = ArrayList<SortPair>()
        var working = ""
        var index = 0
        var gapSize = 0
        // Tokenize the input.
        for (i in input.indices) {
            if (input[i] == ' ') {
                if (gapSize == 0) {
                    sortPairList.add(SortPair(working, index++))
                    working = ""
                }
                gapSize += 1
            } else {
                if (gapSize > 0) {
                    mInputGaps.add(gapSize)
                    gapSize = 0
                }
                working += input[i]
            }
        }
        if (working != "" || input == "") {
            sortPairList.add(SortPair(working, index)) // Add the last SortPair.
        }
        mInputGaps.add(gapSize)
        sortPairList.sortWith { a, b -> b.mString.length.compareTo(a.mString.length) }
        // Construct the list to un-sort the sorted list later.
        val sortList = ArrayList<Pair<Int, Int>>()
        for (sp in sortPairList.indices) {
            sortList.add(Pair(sp, sortPairList[sp].mNumber))
        }
        return Pair(sortPairList, sortList)
    }

    /** Creates a list of StringBuilders each containing a series of dashes. The amount of dashes
     *  for each StringBuilder mirrors the length of the words represented in the given input list
     *  of [SortPair]s. The StringBuilders aren't really 'blank' or empty of course.
     *
     *  @param arrayListREF ArrayList containing the [SortPair]s to build the new 'blank' ArrayList
     *                      from.
     *  @return A list of 'blanked' StringBuilders.
     */
    private fun getBlankList(arrayListREF: ArrayList<SortPair>): ArrayList<StringBuilder> {
        val zeroedList = ArrayList<StringBuilder>()
        for (sp in arrayListREF) {
            val element = StringBuilder("")
            while (element.length < sp.mString.length) {
                element.append("-")
            }
            zeroedList.add(element)
        }
        return zeroedList
    }

    /** Accomplishes the dirty work of applying a letter character to all of the points in the
     *  cryptogram it shows up in.
     *
     *  @param charList A list of character positions to apply the letter to.
     *  @param blankDupREF Reference to the current 'blankDup'.
     *  @param character The character to apply to the cryptogram.
     */
    private fun fillInLetters(
        charList: ListOfSameChars,
        blankDupREF: ArrayList<StringBuilder>,
        character: Char,
    ) {
        // Fill in blanks for all other letters that were the same in the original.
        for (m in charList) {
            blankDupREF[m.first].setCharAt(m.second, character)
        }
    }

    /** Applies a slimmed word to the current 'blankDupREF' at the given ratchet index. When filling
     *  in the word, it also applies the letters to other relevant words.
     *
     *  @param slimmed StringBuilder containing the letters for the word to try and apply.
     *  @param blankDupREF Reference to the current 'blankDup'.
     *  @param ratchetREF Next word to apply index.
     *  @param dWordREF Reference to the normal, un-slimmed word we are applying.
     *  @return True if successful, false otherwise.
     */
    private fun applySlimmedWord(
        slimmed: StringBuilder,
        blankDupREF: ArrayList<StringBuilder>,
        ratchetREF: Int,
        dWordREF: DictWord,
    ): Boolean {
        val dissectorWord = mDissector.getWord(ratchetREF)
        val blankWord = blankDupREF[ratchetREF]
        var k = -1
        val wordLen = blankWord.length
        parentForLoop@ for (i in slimmed.indices) {
            val cc = slimmed[i]
            k = i
            // Find the next blank letter to fill in.
            while (k < wordLen) {
                // Blank letter found, fill in the letters and continue to the next one.
                if (blankWord[k] == '-') {
                    val useCharacterArray = mUsedCharacterStack.peek()
                    val desiredLetter = cc.code - 'a'.code
                    if (!useCharacterArray[desiredLetter]) {
                        useCharacterArray[desiredLetter] = true
                        fillInLetters(dissectorWord[k], blankDupREF, cc)
                    }
                    continue@parentForLoop
                }
                // We found a letter that matches our current one we are looking to fill in so lets
                // skip to the next slimmed letter.
                if (cc == blankWord[k]) {
                    continue@parentForLoop
                }
                // Bail early if the word isn't matching the normal un-slimmed version.
                if (dWordREF.char(k) != blankWord[k]) {
                    return false
                }
                // Look at the next letter.
                k++
            }
            // We have a slimmed letter, but no more space for it so this word fails.
            return false
        }
        // We are out of slimmed letters, but is the blank word fill out?
        while (k < wordLen) {
            if (blankWord[k] == '-') {
                return false
            }
            k++
        }
        // Finally, the word was correctly filled out with the slimmed letters in order.
        return true
    }

    /** Copies the saved words to the passed ArrayList.
     *
     *  @param alREF A reference to the current ArrayList with word gaps.
     *  @param isToBeCloned Whether or not the list should be cloned when returned or not.
     *  @return An ArrayList with the blanks filled in with the saved words. If 'isToBeCloned' is
     *          true, then the ArrayList is a clone of the passed one.
     */
    private fun applySavedWordsToArrayList(
        alREF: ArrayList<StringBuilder>,
        isToBeCloned: Boolean,
    ): ArrayList<StringBuilder> {
        // Before we add the new result, we fill in the blanks with our saved words.
        var savedWordsIterator = 0
        for (word in alREF.indices) {
            if (alREF[word].toString() == "") {
                alREF[word] = StringBuilder(mSavedWords[savedWordsIterator++])
            }
        }
        // Add the complete result to the list.
        return if (isToBeCloned) cloneArrayListOfStringBuilders(alREF) else alREF
    }

    /** Analyzes the list of complete solutions, if they exist, and converts them to strings and
     *  sorts them according to their original ordering.
     *
     *  @param stringResultsREF Reference to the running list of string converted results.
     *  @param sortPairListREF Reference to the original 'sortPairList'.
     *  @return The passed stringResults object. I think this is cleaner than just editing the
     *          passed reference argument.
     */
    private fun analyzeCompleteResults(
        stringResultsREF: ArrayList<String>,
        sortPairListREF: Pair<ArrayList<SortPair>, ArrayList<Pair<Int, Int>>>,
    ): ArrayList<String> {
        val sb = StringBuilder("")
        // Convert the results to an ArrayList of strings.
        resultList@ for (result in mResults) {
            // Create a new result list and initialize it with empty elements.
            val singleResult = ArrayList<String>(sortPairListREF.first.size)
            repeat(sortPairListREF.first.size) {
                singleResult.add("")
            }
            // Add the correct values to the list.
            for (pair in sortPairListREF.second) {
                val word = result[pair.first].toString()
                // Check if the current result has an illegal word and skip it if it does.
                if (!WordDictionary.instance().findInDictionary(word)) {
                    continue@resultList
                }
                singleResult[pair.second] = word
            }
            sb.clear()
            for ((gapsIndex, word) in singleResult.withIndex()) {
                sb.append(word)
                repeat(mInputGaps[gapsIndex]) {
                    sb.append(' ')
                }
            }
            // Record the correct sorted list.
            stringResultsREF.add(insertSymbols(sb.toString()))
        }
        return stringResultsREF
    }

    /** Analyzes the list of incomplete solutions, if they exist, and converts them to strings and
     *  sorts them according to their original ordering.
     *
     *  @param stringResultsREF Reference to the running list of string converted results.
     *  @param sortPairListREF Reference to the original 'sortPairList'.
     *  @return The passed stringResults object. I think this is cleaner than just editing the
     *          passed reference argument.
     */
    private fun analyzeIncompleteResults(
        stringResultsREF: ArrayList<String>,
        sortPairListREF: Pair<ArrayList<SortPair>, ArrayList<Pair<Int, Int>>>,
    ): ArrayList<String> {
        val sb = StringBuilder("")
        // If we don't have any valid results then we collect close matches.
        if (stringResultsREF.isEmpty()) {
            mHasCompleteResults = false
            val completionLimit = mCloseMatches[mCloseMatches.size - 1].first
            var realStart = 0
            // First search for the first result of the largest completion rate. We only return
            // the closest matches of the same calibre.
            for (i in mCloseMatches.size - 1 downTo 0) {
                if (mCloseMatches[i].first < completionLimit) {
                    realStart = i + 1
                    break
                }
            }
            // Collect the proper results.
            for (j in realStart until mCloseMatches.size) {
                val result = mCloseMatches[j]
                // Create a new result list and initialize it with empty elements.
                val singleResult = ArrayList<String>(sortPairListREF.first.size)
                repeat(sortPairListREF.first.size) {
                    singleResult.add("")
                }
                // Add the correct values to the list.
                for (pair in sortPairListREF.second) {
                    val word = result.second[pair.first].toString()
                    singleResult[pair.second] = word
                }
                sb.clear()
                for ((gapsIndex, word) in singleResult.withIndex()) {
                    sb.append(word)
                    repeat(mInputGaps[gapsIndex]) {
                        sb.append(' ')
                    }
                }
                // Record the correct sorted list.
                stringResultsREF.add(insertSymbols(sb.toString()))
            }
        }
        return stringResultsREF
    }

    /** The main engine of the crypto solving algorithm.
     *
     *  @param blank An ArrayList containing the current working progress.
     *  @param ratchet The next index to start looking for a word to work on from.
     */
    private fun computeCryptoWrapped(blank: ArrayList<StringBuilder>, ratchet: Int) {
        // Find the next word to fill in.
        val newRatchet = getNextIncompleteWord(blank, ratchet)
        // If no word is found, add the current setup to the results list.
        if (newRatchet == -1) {
            mResults.add(applySavedWordsToArrayList(blank, true))
            return
        }
        // Get the length of the next word to complete.
        val wordLen = blank[newRatchet].length
        // Get a reference to the word list whose words sizes match our next word.
        val stringDict = WordDictionary.instance().getWords(wordLen)
        // Create our working DictWord object.
        val dWord = DictWord(0, wordLen, stringDict)
        // Make a new working set for this iteration.
        val blankDUP = cloneArrayListOfStringBuilders(blank)
        for (i in stringDict.indices step wordLen) {
            if (mCancelled) {
                return
            }
            // Update 'dWord' to point to the next word.
            dWord.setStart(i)
            val slimmed = getSlimmedWord(dWord)
            // Just reset the current word, instead of the entire thing.
            blankDUP[newRatchet] = StringBuilder(blank[newRatchet])
            // Copy the last used character array and add it to the stack.
            mUsedCharacterStack.push(mUsedCharacterStack.peek().copyOf())
            // Attempt to apply the slimmed word letters to the current word.
            val success = applySlimmedWord(slimmed, blankDUP, newRatchet, dWord)
            // The slimmed word might work, but does the result actually match the original word.
            val success2 = compareStringBuilderToDictWord(blankDUP[newRatchet], dWord)
            if (success && success2) {
                // If this progress is better or equal to the most recent progress, add it to the
                // close matches.
                if (newRatchet >= mCloseMatches[mCloseMatches.size - 1].first) {
                    val clonedArray = cloneArrayListOfStringBuilders(blankDUP)
                    val appliedArray = applySavedWordsToArrayList(clonedArray, false)
                    mCloseMatches.add(Pair(newRatchet, appliedArray))
                }
                // Record all of the next complete words and blank them.
                var j = newRatchet
                val limit = getNextIncompleteWord(blankDUP, j)
                var nope = false
                while (j < limit) {
                    val foo = blankDUP[j].toString()
                    if (!WordDictionary.instance().findInDictionary(foo)) {
                        nope = true
                    }
                    mSavedWords.add(StringBuilder(foo))
                    blankDUP[j++] = StringBuilder("")
                }
                // Finally move on the the next word.
                if (!nope) {
                    computeCryptoWrapped(blankDUP, newRatchet + 1)
                }
                // Reset 'blankDUP' and remove old saved words before trying out new words.
                j = newRatchet
                while (j < limit) {
                    mSavedWords.removeAt(mSavedWords.size - 1)
                    blankDUP[j] = StringBuilder(blank[j])
                    j++
                }
            }
            // Remove the last used character array, to make room for the next one.
            mUsedCharacterStack.pop()

            // Update our progress through the first word loop iteration.
            if (newRatchet == 0) {
                mProgress.mValue = ((i.toDouble() / stringDict.length.toDouble()) * 100.0).toInt()
            }
        }
    }

    /** Removes and records the capital letters and symbols from the input text.
     *
     *  @param rawInput Raw input to record special characters from.
     *  @return A sanitized string to compute.
     */
    private fun removeSymbols(rawInput: String): String {
        val sb = StringBuilder("")
        for (i in rawInput.indices) {
            val char = rawInput[i]
            // Add normal lower case letter.
            if (char in 'a'..'z') {
                sb.append(char)
            } else {
                // Add a capital letter pair.
                if (char in 'A'..'Z') {
                    mRemovedSymbols.add(Pair(i, 'A'))
                    sb.append(char.lowercase())
                } else {
                    // Add a normal symbol pair.
                    sb.append(' ')
                    if (char != ' ') {
                        mRemovedSymbols.add(Pair(i, char))
                    }
                }
            }
        }
        return sb.toString()
    }

    /** Modifies the passed input string to include the special characters removed earlier.
     *
     *  @param input Input string to modify.
     *  @return Input string with all the matching letter cases and special characters as the
     *          original raw input.
     */
    private fun insertSymbols(input: String): String {
        val sb = StringBuilder(input)
        for (pair in mRemovedSymbols) {
            // Set the letter to upper case.
            if (pair.second == 'A') {
                sb[pair.first] = sb[pair.first].uppercaseChar()
            } else {
                // Inserts a symbol.
                sb[pair.first] = pair.second
            }
        }
        return sb.toString()
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // UNIT TEST
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Internal unit test function for testing "compareStringBuilderToDictWord".
     */
    @Suppress("FunctionName", "SpellCheckingInspection")
    private fun UNIT_TEST_compareStringBuilderToDictWord() {
        val cf = CryptoFinder()
        val dictionary = "hellothere"
        val dWord = DictWord(0, 5, dictionary)

        assertEquals(
            true,
            cf.compareStringBuilderToDictWord(StringBuilder("hello"), dWord),
        )
        assertEquals(
            false,
            cf.compareStringBuilderToDictWord(StringBuilder("helloo"), dWord),
        )
        assertEquals(
            false,
            cf.compareStringBuilderToDictWord(StringBuilder("h"), dWord),
        )
        assertEquals(
            false,
            cf.compareStringBuilderToDictWord(StringBuilder(""), dWord),
        )
        dWord.setStart(2)
        assertEquals(
            true,
            cf.compareStringBuilderToDictWord(StringBuilder("lloth"), dWord),
        )
        dWord.setStart(5)
        assertEquals(
            true,
            cf.compareStringBuilderToDictWord(StringBuilder("there"), dWord),
        )
    }

    /** Internal unit test function for testing "getNextIncompleteWord".
     */
    @Suppress("FunctionName")
    private fun UNIT_TEST_getNextIncompleteWord() {
        val cf = CryptoFinder()
        val blanks = ArrayList<StringBuilder>()

        blanks.add(StringBuilder("full"))
        blanks.add(StringBuilder("-a-"))
        blanks.add(StringBuilder("-b-"))
        assertEquals(1, cf.getNextIncompleteWord(blanks, 0))
        assertEquals(1, cf.getNextIncompleteWord(blanks, 1))
        assertEquals(2, cf.getNextIncompleteWord(blanks, 2))

        blanks.clear()
        blanks.add(StringBuilder("full"))
        blanks.add(StringBuilder("---"))
        blanks.add(StringBuilder("---"))
        assertEquals(1, cf.getNextIncompleteWord(blanks, 0))

        blanks.clear()
        blanks.add(StringBuilder("full"))
        blanks.add(StringBuilder("full"))
        assertEquals(-1, cf.getNextIncompleteWord(blanks, 0))
        assertEquals(-1, cf.getNextIncompleteWord(blanks, 1))
        assertEquals(-1, cf.getNextIncompleteWord(blanks, 2))

        blanks.clear()
        blanks.add(StringBuilder("---"))
        blanks.add(StringBuilder("---"))
        assertEquals(0, cf.getNextIncompleteWord(blanks, 0))
        assertEquals(1, cf.getNextIncompleteWord(blanks, 1))
    }

    /** Internal unit test function for testing "cloneArrayListOfStringBuilders".
     */
    @Suppress("FunctionName")
    private fun UNIT_TEST_cloneArrayListOfStringBuilders() {
        val cf = CryptoFinder()

        // Test one element.
        val arrOne = ArrayList<StringBuilder>()
        arrOne.add(StringBuilder("a"))
        var result = cf.cloneArrayListOfStringBuilders(arrOne)
        assertEquals(result[0].toString(), arrOne[0].toString())

        // Test two elements.
        val arrTwo = ArrayList<StringBuilder>()
        arrTwo.add(StringBuilder("a"))
        arrTwo.add(StringBuilder("b"))
        result = cf.cloneArrayListOfStringBuilders(arrOne)
        assertEquals(result[0].toString(), arrTwo[0].toString())
        assertEquals(result[0].toString(), arrTwo[0].toString())

        // Test no elements.
        val arrThree = ArrayList<StringBuilder>()
        result = cf.cloneArrayListOfStringBuilders(arrThree)
        assertEquals(result.size, arrThree.size)
    }

    /** Internal unit test function for testing "getSlimmedWord".
     */
    @Suppress("FunctionName", "SpellCheckingInspection")
    private fun UNIT_TEST_getSlimmedWord() {
        val cf = CryptoFinder()
        val dictionary = "hellosmile"
        var dWord = DictWord(0, 5, dictionary)

        assertEquals("helo", cf.getSlimmedWord(dWord).toString())
        dWord.setStart(5)
        assertEquals("smile", cf.getSlimmedWord(dWord).toString())
        dWord = DictWord(0, 10, dictionary)
        assertEquals("helosmi", cf.getSlimmedWord(dWord).toString())
        assertEquals(
            "",
            cf.getSlimmedWord(DictWord(0, 0, "")).toString(),
        )
    }

    /** Internal unit test function for testing "getSortPairList".
     */
    @Suppress("FunctionName")
    private fun UNIT_TEST_getSortPairList() {
        val cf = CryptoFinder()
        var results = cf.getSortPairList("hello")
        assertEquals(SortPair("hello", 0), results.first[0])
        assertEquals(Pair(0, 0), results.second[0])

        results = cf.getSortPairList("hello there")
        assertEquals(SortPair("hello", 0), results.first[0])
        assertEquals(SortPair("there", 1), results.first[1])
        assertEquals(Pair(0, 0), results.second[0])
        assertEquals(Pair(1, 1), results.second[1])

        results = cf.getSortPairList("hello there world")
        assertEquals(SortPair("hello", 0), results.first[0])
        assertEquals(SortPair("there", 1), results.first[1])
        assertEquals(SortPair("world", 2), results.first[2])
        assertEquals(Pair(0, 0), results.second[0])
        assertEquals(Pair(1, 1), results.second[1])

        results = cf.getSortPairList("")
        assertEquals(SortPair("", 0), results.first[0])
        assertEquals(Pair(0, 0), results.second[0])
    }

    /** Internal unit test function for testing "getBlankList".
     */
    @Suppress("FunctionName")
    private fun UNIT_TEST_getBlankList() {
        val cf = CryptoFinder()
        var results = cf.getBlankList(cf.getSortPairList("hello").first)
        assertEquals("-----", results[0].toString())

        results = cf.getBlankList(cf.getSortPairList("smiley faces").first)
        assertEquals("------", results[0].toString())
        assertEquals("-----", results[1].toString())

        results = cf.getBlankList(cf.getSortPairList("a bc def").first)
        assertEquals("---", results[0].toString())
        assertEquals("--", results[1].toString())
        assertEquals("-", results[2].toString())

        results = cf.getBlankList(cf.getSortPairList("").first)
        assertEquals("", results[0].toString())
    }

    /** Internal unit test function for testing "fillInLetters".
     */
    @Suppress("FunctionName")
    private fun UNIT_TEST_fillInLetters() {
        val cf = CryptoFinder()
        var input = "hello"
        var sortPair = cf.getSortPairList(input).first
        var blank = cf.getBlankList(sortPair)
        cf.fillInLetters(Dissector(sortPair).dissection[0][0], blank, 'a')
        assertEquals(StringBuilder("a----").toString(), blank[0].toString())

        input = "hello there"
        sortPair = cf.getSortPairList(input).first
        blank = cf.getBlankList(sortPair)
        cf.fillInLetters(Dissector(sortPair).dissection[0][1], blank, 'a')
        assertEquals(StringBuilder("-a---").toString(), blank[0].toString())
        assertEquals(StringBuilder("--a-a").toString(), blank[1].toString())
    }

    /** Internal unit test function for testing "applySlimmedWord".
     */
    @Suppress("FunctionName", "SpellCheckingInspection")
    private fun UNIT_TEST_applySlimmedWord() {
        // Single word correct application.
        val cf = CryptoFinder()
        cf.mUsedCharacterStack.push(BooleanArray(26))
        var input = "hello"
        val dWord = DictWord(0, 5, input)
        var sortPair = cf.getSortPairList(input).first
        var blank = cf.getBlankList(sortPair)
        cf.mDissector = Dissector(sortPair)
        var result = cf.applySlimmedWord(
            cf.getSlimmedWord(
                DictWord(
                    0,
                    5,
                    input,
                ),
            ),
            blank,
            0,
            dWord,
        )
        assertEquals(true, result)
        assertEquals("hello", blank[0].toString())

        // Multiple word correct application.
        cf.mUsedCharacterStack.push(BooleanArray(26))
        input = "hello there"
        sortPair = cf.getSortPairList(input).first
        blank = cf.getBlankList(sortPair)
        cf.mDissector = Dissector(sortPair)
        result = cf.applySlimmedWord(
            cf.getSlimmedWord(
                DictWord(
                    0,
                    5,
                    input,
                ),
            ),
            blank,
            0,
            dWord,
        )
        assertEquals(true, result)
        assertEquals("hello", blank[0].toString())
        assertEquals("-he-e", blank[1].toString())

        // Multiple word incorrect application.
        cf.mUsedCharacterStack.push(BooleanArray(26))
        input = "hello there"
        sortPair = cf.getSortPairList(input).first
        blank = cf.getBlankList(sortPair)
        cf.mDissector = Dissector(sortPair)
        result = cf.applySlimmedWord(
            cf.getSlimmedWord(
                DictWord(
                    0,
                    5,
                    "smile",
                ),
            ),
            blank,
            0,
            DictWord(0, 5, "smile"),
        )
        assertEquals(false, result)
        assertEquals("smii-", blank[0].toString())
        assertEquals("-sm-m", blank[1].toString())

        // Multiple word correct application with preset letter.
        cf.mUsedCharacterStack.push(BooleanArray(26))
        input = "hello there"
        sortPair = cf.getSortPairList(input).first
        blank = cf.getBlankList(sortPair)
        blank[0].setCharAt(1, 'e')
        blank[1].setCharAt(2, 'e')
        blank[1].setCharAt(4, 'e')
        cf.mDissector = Dissector(sortPair)
        result = cf.applySlimmedWord(
            cf.getSlimmedWord(
                DictWord(
                    0,
                    5,
                    "hello",
                ),
            ),
            blank,
            0,
            dWord,
        )
        assertEquals(true, result)
        assertEquals("hello", blank[0].toString())
        assertEquals("-he-e", blank[1].toString())

        // Multiple word incorrect application with preset letter.
        cf.mUsedCharacterStack.push(BooleanArray(26))
        input = "hello there"
        sortPair = cf.getSortPairList(input).first
        blank = cf.getBlankList(sortPair)
        blank[0].setCharAt(1, 'f')
        cf.mDissector = Dissector(sortPair)
        result = cf.applySlimmedWord(
            cf.getSlimmedWord(
                DictWord(
                    0,
                    5,
                    "hello",
                ),
            ),
            blank,
            0,
            dWord,
        )
        assertEquals(false, result)
        assertEquals("hf---", blank[0].toString())
        assertEquals("-h---", blank[1].toString())
    }

    /** Internal unit test function for testing "removeSymbols".
     */
    @Suppress("FunctionName", "SpellCheckingInspection")
    private fun UNIT_TEST_removeSymbols() {
        // Standard example.
        mRemovedSymbols.clear()
        var result = removeSymbols("Hello?There!")
        assertEquals(result, "hello there ")
        assertEquals(mRemovedSymbols[0], Pair(0, 'A'))
        assertEquals(mRemovedSymbols[1], Pair(5, '?'))
        assertEquals(mRemovedSymbols[2], Pair(6, 'A'))
        assertEquals(mRemovedSymbols[3], Pair(11, '!'))

        // All caps.
        mRemovedSymbols.clear()
        result = removeSymbols("HELLO")
        assertEquals(result, "hello")
        assertEquals(mRemovedSymbols[0], Pair(0, 'A'))
        assertEquals(mRemovedSymbols[1], Pair(1, 'A'))
        assertEquals(mRemovedSymbols[2], Pair(2, 'A'))
        assertEquals(mRemovedSymbols[3], Pair(3, 'A'))
        assertEquals(mRemovedSymbols[4], Pair(4, 'A'))

        // All symbols.
        mRemovedSymbols.clear()
        result = removeSymbols("!@#")
        assertEquals(result, "   ")
        assertEquals(mRemovedSymbols[0], Pair(0, '!'))
        assertEquals(mRemovedSymbols[1], Pair(1, '@'))
        assertEquals(mRemovedSymbols[2], Pair(2, '#'))

        // Do nothing.
        mRemovedSymbols.clear()
        result = removeSymbols("hello")
        assertEquals(result, "hello")
        assertEquals(mRemovedSymbols.size, 0)
    }

    /** Internal unit test function for testing "insertSymbols".
     */
    @Suppress("FunctionName", "SpellCheckingInspection")
    private fun UNIT_TEST_insertSymbols() {
        // Standard example.
        mRemovedSymbols.clear()
        mRemovedSymbols.add(Pair(1, 'A'))
        mRemovedSymbols.add(Pair(3, '^'))
        mRemovedSymbols.add(Pair(5, 'A'))
        assertEquals("hEl^lO", insertSymbols("hel lo"))

        // All caps.
        mRemovedSymbols.clear()
        mRemovedSymbols.add(Pair(0, 'A'))
        mRemovedSymbols.add(Pair(1, 'A'))
        mRemovedSymbols.add(Pair(2, 'A'))
        assertEquals("AID", insertSymbols("aid"))

        // All symbols.
        mRemovedSymbols.clear()
        mRemovedSymbols.add(Pair(0, '('))
        mRemovedSymbols.add(Pair(1, '&'))
        mRemovedSymbols.add(Pair(2, ')'))
        assertEquals("(&)", insertSymbols("   "))

        // Do nothing.
        mRemovedSymbols.clear()
        assertEquals("why", insertSymbols("why"))
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // SUBCLASSES, ETC.
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Defines a word around a substring section of a string (section encompassing the entire
     *  string is allowed). Useful for defining "substrings" out of a larger string without making
     *  new expensive objects.
     *
     *  @param newStart The new starting index of the word.
     *  @param newLength The absolute length of the new word by counting letters.
     *  @param newDict The larger string to form a substring focus on.
     */
    class DictWord(newStart: Int, newLength: Int, newDict: String) {

        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
        // VARIABLES
        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

        /** The starting index for the defined word. */
        private var mStart: Int = newStart

        /** The length of the defined word. */
        private var mLength: Int = newLength

        /** The string containing the defined word. */
        private var mDict: String = newDict

        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
        // OVERRIDES
        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

        init {
            // Initialize the word proper.
            setStart(mStart)
        }

        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
        // PUBLIC
        // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

        /** Retrieves the character at the given index.
         *
         *  @param i The character index to retrieve.
         *  @return The desired character at the desired index.
         */
        fun char(i: Int): Char {
            return mDict[mStart + i]
        }

        /** Sets the new start of a word index.
         *
         *  @param i The new starting index.
         */
        fun setStart(i: Int) {
            mStart = i
        }

        /** Returns the substring's length.
         *
         *  @return The substring's length.
         */
        fun getLength(): Int {
            return mLength
        }
    }

    /** Collects quick references to each letter in the input, so adding a single letter to the
     *  results is linear, equal to the number of instances the letter occurs. Initializing the
     *  [Dissector] object is the only time matching characters are searched for. The links to
     *  characters points to their positions in the sorted input, not the original.
     *
     *  @param input List of SortPairs to analyze character positions in.
     */
    class Dissector(input: ArrayList<SortPair>) {

        /** Holds the index addresses of all the letters in the input. */
        var dissection = DissectedInput()

        /** Returns all of the character references in a word at the given index [i].
         *
         *  @param i Index of word to retrieve.
         *  @return All of the character references in a word at the given index [i].
         */
        fun getWord(i: Int): DissectedWord {
            return dissection[i]
        }

        init {
            // Initialize containers for each word in the input.
            for (sp in input) {
                val ar = DissectedWord()
                dissection.add(ar)
                repeat(sp.mString.length) {
                    ar.add(ListOfSameChars())
                }
            }

            // For each word in input...
            for (i in 0 until input.size) {
                val sp = input[i]
                // For each letter in the word...
                for (a in sp.mString.indices) {
                    // Look for other letters...
                    for (b in i until input.size) {
                        val sub = input[b]
                        // For each letter in the current word...
                        for (c in sub.mString.indices) {
                            // If the letters match...
                            if (sub.mString[c] == sp.mString[a]) {
                                // Add reference to the letter.
                                dissection[i][a].add(Pair(b, c))
                            }
                        }
                    }
                }
            }
        }
    }
}

/** POD class that matches a word from the original input to its original position in the input. */
data class SortPair(var mString: String, var mNumber: Int)

// Nice names for Dissector terms.
typealias CharacterPosition = Pair<Int, Int>
typealias ListOfSameChars = ArrayList<CharacterPosition>
typealias DissectedWord = ArrayList<ListOfSameChars>
typealias DissectedInput = ArrayList<DissectedWord>
