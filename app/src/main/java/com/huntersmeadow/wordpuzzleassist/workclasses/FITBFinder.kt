package com.huntersmeadow.wordpuzzleassist.workclasses

import java.util.Locale
import java.util.regex.Pattern

/** A pseudo library class. Only has one feature which is a "fill in the blanks" operation.
 */
class FITBFinder : AssistParent() {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // REGULAR
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Computes a "fill in the blanks" operation. Replaces '?' with single characters, replaces '*'
     *  with variable replacement.
     *
     *  @param rawInput The input string.
     *  @return An ArrayList containing the results of the computation.
     */
    fun computeFITB(rawInput: String, allowedLetters: BooleanArray? = null): ArrayList<String> {
        val input = rawInput.lowercase(Locale.getDefault())
        val results = ArrayList<String>()
        val originalLetters = countLetters(input)
        val mono = WordDictionary.instance().getMonolithicString()
        val search = Pattern
            .compile("\n" + substituteRegexSymbols(input))
            .matcher(mono)
        val forbiddenLetters = ArrayList<Char>()
        // Find out which letters are forbidden for this search.
        if (allowedLetters != null) {
            for (i in 0..25) {
                if (!allowedLetters[i]) {
                    forbiddenLetters.add(('a'.code + i).toChar())
                }
            }
        }
        // Search for matches.
        mainSearch@ while (search.find()) {
            // Bail if somehow cancelled.
            if (mCancelled) {
                return ArrayList()
            }
            // Skip this result if it's not the entire word.
            if (mono[search.end()] != '\n') {
                continue@mainSearch
            }
            val word = mono.substring(search.start() + 1, search.end())
            // Check for new forbidden letters.
            if (allowedLetters != null) {
                val newLetters = countLetters(word)
                for (c in forbiddenLetters) {
                    // If there has been an increase in forbidden letters, then it was 'added' to
                    // the blank.
                    if (newLetters[c - 'a'] > originalLetters[c - 'a']) {
                        continue@mainSearch
                    }
                }
            }
            results.add(word)
        }
        return results
    }

    /** Tallies the letters in the given word and returns a int array with the results.
     *
     *  @param word String word to count the letters of.
     *  @return Int array containing the amounts of each letter (including non used letters as 0).
     */
    private fun countLetters(word: String): IntArray {
        val letters = IntArray(26)
        for (element in word) {
            if (element in 'a'..'z') {
                letters[element - 'a']++
            }
        }
        return letters
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PRIVATE
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Swaps user friendly symbols into regex symbols.
     *  '?' -> Single replacement.
     *  '*' -> Multiple replacement.
     *
     *  @param word Input string.
     *  @return The input string with the substitutions.
     */
    private fun substituteRegexSymbols(word: String): String {
        val build = StringBuilder()
        for (i in word.indices) {
            when {
                word[i] == '*' -> build.append("[a-zA-Z]*")
                word[i] == '?' -> build.append("[a-zA-Z]")
                else -> build.append(word[i])
            }
        }
        return build.toString()
    }
}
