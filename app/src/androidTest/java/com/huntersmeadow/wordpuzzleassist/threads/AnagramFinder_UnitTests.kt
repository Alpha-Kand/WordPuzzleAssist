@file:Suppress("ClassName", "SpellCheckingInspection")

package com.huntersmeadow.wordpuzzleassist.threads

import com.huntersmeadow.wordpuzzleassist.MutableInteger
import com.huntersmeadow.wordpuzzleassist.removeLetter
import com.huntersmeadow.wordpuzzleassist.workclasses.AnagramFinder
import com.huntersmeadow.wordpuzzleassist.workclasses.WordDictionary
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TreeSet
import kotlin.collections.ArrayList

class AnagramFinder_UnitTests : Parent_UnitTests() {

    @Test
    fun computeAnagram_NoModifier() {
        val mProgress = MutableInteger(0)
        val af = AnagramFinder(mProgress)
        af.enableTesting()
        val against = ArrayList<String>()

        // No Letters.
        assertEquals(0, af.computeAnagram("").size)
        checkProgressValue(mProgress)

        // 1 letter.
        against.add("a")
        assertEquals(against, af.computeAnagram("a"))
        checkProgressValue(mProgress)

        // 2 letters.
        against.clear()
        against.add("ab")
        against.add("ba")
        testArrayList(against, af.computeAnagram("ab"))
        testArrayList(against, af.computeAnagram("Ab"))
        testArrayList(against, af.computeAnagram("aB"))
        checkProgressValue(mProgress)

        // 3 letters.
        against.clear()
        against.add("abc")
        against.add("acb")
        against.add("bac")
        against.add("bca")
        against.add("cab")
        against.add("cba")
        testArrayList(against, af.computeAnagram("abc"))
        testArrayList(against, af.computeAnagram("ABC"))
        testArrayList(against, af.computeAnagram("AbC"))
        testArrayList(against, af.computeAnagram("aBc"))
        checkProgressValue(mProgress)

        // 4 letters.
        against.clear()
        against.add("abcd")
        against.add("abdc")
        against.add("acbd")
        against.add("acdb")
        against.add("adbc")
        against.add("adcb")
        against.add("bacd")
        against.add("badc")
        against.add("bcad")
        against.add("bcda")
        against.add("bdac")
        against.add("bdca")
        against.add("cabd")
        against.add("cadb")
        against.add("cbad")
        against.add("cbda")
        against.add("cdab")
        against.add("cdba")
        against.add("dabc")
        against.add("dacb")
        against.add("dbac")
        against.add("dbca")
        against.add("dcab")
        against.add("dcba")
        testArrayList(against, af.computeAnagram("abcd"))
        checkProgressValue(mProgress)

        // Test that it finds proper words.
        af.disableTesting()
        against.clear()
        against.add("slime")
        against.add("miles")
        against.add("melis")
        against.add("limes")
        testArrayList(against, af.computeAnagram("smile"))
        testArrayList(against, af.computeAnagram("sMiLe"))
        testArrayList(against, af.computeAnagram("SMILe"))
        checkProgressValue(mProgress)
    }

    @Test
    fun computeAnagram_AddLetterModifier() {
        val mProgress = MutableInteger(0)
        val af = AnagramFinder(mProgress)
        af.enableTesting()
        val against = TreeSet<String>()

        // Zero letters to zero.
        assertEquals(
            0,
            af.computeAnagram("", AnagramFinder.Modifier.ADD_LETTER).size,
        )
        checkProgressValue(mProgress)

        // 1 letter up to 2.
        for (c in 'a'..'z') {
            against.add("a$c")
            against.add(c + "a")
        }
        var results = af.computeAnagram("a", AnagramFinder.Modifier.ADD_LETTER)
        testResults(results, 51, against)
        checkProgressValue(mProgress)

        // 2 letters up to 3.
        for (c in 'a'..'z') {
            against.addAll(af.computeAnagram("ab$c"))
        }
        results = af.computeAnagram("ab", AnagramFinder.Modifier.ADD_LETTER)
        testResults(results, 150, against)
        checkProgressValue(mProgress)

        // Test that it finds proper words.
        val input = "smile"
        for (c in 'a'..'z') {
            against.addAll(af.computeAnagram(input + c))
        }
        af.disableTesting()
        results = af.computeAnagram(input, AnagramFinder.Modifier.ADD_LETTER)
        testResults(results, 40, against)
        checkProgressValue(mProgress)
    }

    @Test
    fun computeAnagram_RemoveLetterModifier() {
        val mProgress = MutableInteger(0)
        val af = AnagramFinder(mProgress)
        af.enableTesting()
        val against = TreeSet<String>()

        // No letters.
        assertEquals(
            0,
            af.computeAnagram("", AnagramFinder.Modifier.REMOVE_LETTER).size,
        )
        checkProgressValue(mProgress)

        // 2 Letters down to 1.
        against.clear()
        assertEquals(
            2,
            af.computeAnagram("ab", AnagramFinder.Modifier.REMOVE_LETTER).size,
        )
        checkProgressValue(mProgress)

        // 3 letters down to 2.
        against.clear()
        against.add("ab")
        against.add("ba")
        against.add("ac")
        against.add("ca")
        against.add("bc")
        against.add("cb")
        testArrayList(
            ArrayList(against),
            af.computeAnagram("abc", AnagramFinder.Modifier.REMOVE_LETTER),
        )
        checkProgressValue(mProgress)

        // 4 letters down to 3.
        against.clear()
        val input = "abcd"
        for (i in input.indices) {
            against.addAll(af.computeAnagram(removeLetter(input, i)))
        }
        testArrayList(
            ArrayList(against),
            af.computeAnagram("abcd", AnagramFinder.Modifier.REMOVE_LETTER),
        )
        checkProgressValue(mProgress)

        // Test that it finds proper words.
        af.disableTesting()
        against.clear()
        // Make sure these inputs aren't actually words themselves.
        against.addAll(af.computeAnagram("smil"))
        against.addAll(af.computeAnagram("smie"))
        against.addAll(af.computeAnagram("smle"))
        against.addAll(af.computeAnagram("elis"))
        against.addAll(af.computeAnagram("imle"))
        testResults(
            af.computeAnagram("smile", AnagramFinder.Modifier.REMOVE_LETTER),
            15,
            against,
            false,
        )
        testResults(
            af.computeAnagram("sMIle", AnagramFinder.Modifier.REMOVE_LETTER),
            15,
            against,
            false,
        )
        testResults(
            af.computeAnagram("SmiLE", AnagramFinder.Modifier.REMOVE_LETTER),
            15,
            against,
            false,
        )
        checkProgressValue(mProgress)
    }

    @Test
    fun computePhrases() {
        val mProgress = MutableInteger(0)
        val af = AnagramFinder(mProgress)
        af.enableTesting()
        val against = TreeSet<String>()

        // Test 1 - No Modifier.
        var input = "abcdef"
        var min = 3
        var max = 3
        against.addAll(phraseFinder(af, input, min, max))
        var results = af.computePhrases(input, AnagramFinder.Modifier.NONE, min, max)
        testResults(results, 360, against)
        checkProgressValue(mProgress)

        // Test 2 - No Modifier.
        input = "abcdef"
        min = 3
        max = 4
        against.addAll(phraseFinder(af, input, min, max))
        results = af.computePhrases(input, AnagramFinder.Modifier.NONE, min, max)
        testResults(results, 360, against)
        checkProgressValue(mProgress)

        // Test 3 - No Modifier.
        input = "abcdefg"
        min = 3
        max = 4
        against.addAll(phraseFinder(af, input, min, max))
        results = af.computePhrases(input, AnagramFinder.Modifier.NONE, min, max)
        testResults(results, 5040, against)
        checkProgressValue(mProgress)

        // Test 4 - No Modifier.
        input = "abcdef"
        min = 2
        max = 3
        against.addAll(phraseFinder(af, input, min, max))
        results = af.computePhrases(input, AnagramFinder.Modifier.NONE, min, max)
        testResults(results, 480, against)
        checkProgressValue(mProgress)

        // Test 5 - No Modifier.
        input = "abcdef"
        min = 2
        max = 3
        af.disableTesting()
        against.addAll(phraseFinder(af, input, min, max, true))
        results = af.computePhrases(input, AnagramFinder.Modifier.NONE, min, max)
        testResults(results, 116, against)
        checkProgressValue(mProgress)

        // Test 1 - Add Letter.
        af.enableTesting()
        input = "abcde"
        min = 3
        max = 3
        for (c in 'a'..'z') {
            against.addAll(phraseFinder(af, input + c, min, max))
        }
        results = af.computePhrases(input, AnagramFinder.Modifier.ADD_LETTER, min, max)
        testResults(results, 8460, against)
        checkProgressValue(mProgress)

        // Test 2 - Add Letter.
        input = "abcde"
        min = 2
        max = 3
        for (c in 'a'..'z') {
            against.addAll(phraseFinder(af, input + c, min, max))
        }
        results = af.computePhrases(input, AnagramFinder.Modifier.ADD_LETTER, min, max)
        testResults(results, 11280, against)
        checkProgressValue(mProgress)

        // Test 3 - Add Letter.
        input = "abcde"
        min = 3
        max = 3
        af.disableTesting()
        for (c in 'a'..'z') {
            against.addAll(phraseFinder(af, input + c, min, max, true))
        }
        results = af.computePhrases(input, AnagramFinder.Modifier.ADD_LETTER, min, max)
        testResults(results, 767, against)
        checkProgressValue(mProgress)

        // Test 1 - Remove Letter.
        af.enableTesting()
        input = "abcdefg"
        min = 3
        max = 3
        for (i in input.indices) {
            against.addAll(phraseFinder(af, removeLetter(input, i), min, max))
        }
        results = af.computePhrases(input, AnagramFinder.Modifier.REMOVE_LETTER, min, max)
        testResults(results, 2520, against)
        checkProgressValue(mProgress)

        // Test 2 - Remove Letter.
        input = "abcdefgh"
        min = 3
        max = 4
        for (i in input.indices) {
            against.addAll(phraseFinder(af, removeLetter(input, i), min, max))
        }
        results = af.computePhrases(input, AnagramFinder.Modifier.REMOVE_LETTER, min, max)
        testResults(results, 40320, against)
        checkProgressValue(mProgress)

        // Test 3 - Remove Letter.
        input = "abcdefg"
        min = 3
        max = 3
        af.disableTesting()
        for (i in input.indices) {
            against.addAll(phraseFinder(af, removeLetter(input, i), min, max, true))
        }
        results = af.computePhrases(input, AnagramFinder.Modifier.REMOVE_LETTER, min, max)
        testResults(results, 130, against)
        checkProgressValue(mProgress)

        // Test 1 - Boundary.
        af.enableTesting()
        input = "abcd"
        min = 0
        max = 0
        against.addAll(phraseFinder(af, input, min, max))
        results = af.computePhrases(input, AnagramFinder.Modifier.NONE, min, max)
        testResults(results, 0, against)
        checkProgressValue(mProgress)

        // Test 2 - Boundary.
        input = ""
        min = 0
        max = 0
        against.addAll(phraseFinder(af, input, min, max))
        results = af.computePhrases(input, AnagramFinder.Modifier.NONE, min, max)
        testResults(results, 0, against)
        checkProgressValue(mProgress)

        // Test 3 - Boundary.
        input = ""
        min = 3
        max = 4
        against.addAll(phraseFinder(af, input, min, max))
        results = af.computePhrases(input, AnagramFinder.Modifier.NONE, min, max)
        testResults(results, 0, against)
        checkProgressValue(mProgress)

        // Test 4 - Boundary.
        input = "abcd"
        min = 0
        max = 0
        for (c in 'a'..'z') {
            against.addAll(phraseFinder(af, input + c, min, max))
        }
        results = af.computePhrases(input, AnagramFinder.Modifier.ADD_LETTER, min, max)
        testResults(results, 0, against)
        checkProgressValue(mProgress)

        // Test 5 - Boundary.
        input = ""
        min = 0
        max = 0
        for (c in 'a'..'z') {
            against.addAll(phraseFinder(af, input + c, min, max))
        }
        results = af.computePhrases(input, AnagramFinder.Modifier.ADD_LETTER, min, max)
        testResults(results, 0, against)
        checkProgressValue(mProgress)

        // Test 6 - Boundary.
        input = ""
        min = 3
        max = 4
        for (c in 'a'..'z') {
            against.addAll(phraseFinder(af, input + c, min, max))
        }
        results = af.computePhrases(input, AnagramFinder.Modifier.ADD_LETTER, min, max)
        testResults(results, 0, against)
        checkProgressValue(mProgress)

        // Test 7 - Boundary.
        input = "abcd"
        min = 0
        max = 0
        for (i in input.indices) {
            against.addAll(phraseFinder(af, removeLetter(input, i), min, max))
        }
        results = af.computePhrases(input, AnagramFinder.Modifier.REMOVE_LETTER, min, max)
        testResults(results, 0, against)
        checkProgressValue(mProgress)

        // Test 8 - Boundary.
        input = ""
        min = 0
        max = 0
        for (i in input.indices) {
            against.addAll(phraseFinder(af, removeLetter(input, i), min, max))
        }
        results = af.computePhrases(input, AnagramFinder.Modifier.REMOVE_LETTER, min, max)
        testResults(results, 0, against)
        checkProgressValue(mProgress)

        // Test 9 - Boundary.
        input = ""
        min = 3
        max = 4
        for (i in input.indices) {
            against.addAll(phraseFinder(af, removeLetter(input, i), min, max))
        }
        results = af.computePhrases(input, AnagramFinder.Modifier.REMOVE_LETTER, min, max)
        testResults(results, 0, against)
        checkProgressValue(mProgress)
    }

    @Test
    fun computeSubWords() {
        val mProgress = MutableInteger(0)
        val af = AnagramFinder(mProgress)

        // Test 1 - No Modifier.
        // Finding sub words is a lot like anagram's remove a letter.
        var against = TreeSet<String>()
        var min = 3
        var max = 3
        against.addAll(af.computeAnagram("abc"))
        against.add("abc")
        against.add("abd")
        against.add("bcd")
        against.addAll(af.computeAnagram("abd"))
        against.addAll(af.computeAnagram("acd"))
        against.addAll(af.computeAnagram("bcd"))
        var results = af.computeSubWords("abcd", AnagramFinder.Modifier.NONE, min, max)
        testResults(results, 11, against)
        checkProgressValue(mProgress)

        // Test 2 - No Modifier.
        var input = "abcde"
        min = 3
        max = 4
        against = subWordFinderForTests(input, min, max)
        results = af.computeSubWords(input, AnagramFinder.Modifier.NONE, min, max)
        testResults(results, 33, against)
        checkProgressValue(mProgress)

        // Test 3 - No Modifier.
        input = "racecars"
        min = 3
        max = input.length
        against = subWordFinderForTests(input, min, max)
        results = af.computeSubWords(input, AnagramFinder.Modifier.NONE, min, max)
        testResults(results, 126, against)
        checkProgressValue(mProgress)

        // Test 4 - No Modifier.
        input = "racecars"
        min = 5
        max = input.length
        against = subWordFinderForTests(input, min, max)
        results = af.computeSubWords(input, AnagramFinder.Modifier.NONE, min, max)
        testResults(results, 49, against)
        checkProgressValue(mProgress)

        // Test 5 - No Modifier.
        against.clear()
        min = 3
        max = 3
        assertEquals(
            0,
            af.computeSubWords("", AnagramFinder.Modifier.NONE, min, max).size,
        )
        checkProgressValue(mProgress)

        // Test 6 - No Modifier.
        against.clear()
        min = 3
        max = 3
        assertEquals(
            0,
            af.computeSubWords("a", AnagramFinder.Modifier.NONE, min, max).size,
        )
        checkProgressValue(mProgress)

        // Test 1 - Add Letter.
        against.clear()
        input = "abc"
        min = 3
        max = 3
        for (c in 'a'..'z') {
            against.addAll(subWordFinderForTests(input + c, min, max))
        }
        results = af.computeSubWords(input, AnagramFinder.Modifier.ADD_LETTER, min, max)
        testResults(results, 105, against)
        checkProgressValue(mProgress)

        // Test 2 - Add Letter.
        input = "abcd"
        min = 3
        max = 4
        for (c in 'a'..'z') {
            against.addAll(subWordFinderForTests(input + c, min, max))
        }
        results = af.computeSubWords(input, AnagramFinder.Modifier.ADD_LETTER, min, max)
        testResults(results, 241, against)
        checkProgressValue(mProgress)

        // Test 3 - Add Letter.
        input = "raceca"
        min = 3
        max = input.length
        for (c in 'a'..'z') {
            against.addAll(subWordFinderForTests(input + c, min, max))
        }
        results = af.computeSubWords(input, AnagramFinder.Modifier.ADD_LETTER, min, max)
        testResults(results, 805, against)
        checkProgressValue(mProgress)

        // Test 4 - Add Letter.
        input = "raceca"
        min = 5
        max = input.length
        for (c in 'a'..'z') {
            against.addAll(subWordFinderForTests(input + c, min, max))
        }
        results = af.computeSubWords(input, AnagramFinder.Modifier.ADD_LETTER, min, max)
        testResults(results, 243, against)
        checkProgressValue(mProgress)

        // Test 5 - Add Letter.
        against.clear()
        min = 3
        max = 3
        assertEquals(
            0,
            af.computeSubWords("", AnagramFinder.Modifier.ADD_LETTER, min, max).size,
        )
        checkProgressValue(mProgress)

        // Test 6 - Add Letter.
        against.clear()
        min = 3
        max = 3
        assertEquals(
            0,
            af.computeSubWords(
                "a",
                AnagramFinder.Modifier.ADD_LETTER,
                min,
                max,
            ).size,
        )
        checkProgressValue(mProgress)

        // Test 1 - Remove Letter.
        against.clear()
        input = "abce"
        min = 3
        max = 3
        against.addAll(subWordFinderForTests(input, min, max))
        results = af.computeSubWords(input, AnagramFinder.Modifier.REMOVE_LETTER, min, max)
        testResults(results, 9, against)
        checkProgressValue(mProgress)

        // Test 2 - Remove Letter.
        input = "abcd"
        min = 3
        max = 4
        against.addAll(subWordFinderForTests(input, min, max))
        results = af.computeSubWords(input, AnagramFinder.Modifier.REMOVE_LETTER, min, max)
        testResults(results, 11, against)
        checkProgressValue(mProgress)

        // Test 3 - Remove Letter.
        input = "raceca"
        min = 3
        max = input.length
        against.addAll(subWordFinderForTests(input, min, max))
        results = af.computeSubWords(input, AnagramFinder.Modifier.REMOVE_LETTER, min, max)
        testResults(results, 33, against)
        checkProgressValue(mProgress)

        // Test 4 - Remove Letter.
        input = "raceca"
        min = 5
        max = input.length
        against.addAll(subWordFinderForTests(input, min, max))
        results = af.computeSubWords(input, AnagramFinder.Modifier.REMOVE_LETTER, min, max)
        testResults(results, 5, against)
        checkProgressValue(mProgress)

        // Test 5 - Remove Letter.
        against.clear()
        min = 3
        max = 3
        assertEquals(
            0,
            af.computeSubWords(
                "",
                AnagramFinder.Modifier.REMOVE_LETTER,
                min,
                max,
            ).size,
        )
        checkProgressValue(mProgress)

        // Test 6 - Remove Letter.
        against.clear()
        min = 3
        max = 3
        assertEquals(
            0,
            af.computeSubWords(
                "a",
                AnagramFinder.Modifier.REMOVE_LETTER,
                min,
                max,
            ).size,
        )
        checkProgressValue(mProgress)
    }

    @Test
    fun funkySymbols() {
        val mProgress = MutableInteger(0)
        val af = AnagramFinder(mProgress)
        val against = ArrayList<String>()
        against.clear()
        against.add("slime")
        against.add("miles")
        against.add("melis")
        against.add("limes")
        testArrayList(against, af.computeAnagram("?smile?"))
        testArrayList(against, af.computeAnagram("s M i L e"))
        testArrayList(against, af.computeAnagram("S<>m!!iL    e//"))
        checkProgressValue(mProgress)
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // HELPER
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Bundled function that checks that the results match the correct amount and match the
     *  'against' list contents.
     *
     *  @param results The results of the [AnagramFinder] computation.
     *  @param expectedAmount The expected amount of results in [results].
     *  @param against The comparison list.
     */
    private fun testResults(
        results: ArrayList<String>,
        expectedAmount: Int,
        against: TreeSet<String>,
        resetAgainst: Boolean = true,
    ) {
        assertEquals(expectedAmount, results.size) // Check 'result's number of anagrams.
        testArrayList(ArrayList(against), results)
        if (resetAgainst) {
            against.clear()
        }
    }

    private fun checkProgressValue(mi: MutableInteger) {
        val delta = 2
        assertEquals(true, 100 - mi.mValue < delta)
    }

    /** An explicit but slow algorithm that's only job is to get results for testing. Although it
     *  uses a similar algorithm to [AnagramFinder], two algorithms producing the same result does
     *  increase confidence.
     *
     *  @param input Input string to search for sub strings.
     *  @param min   Minimum sub word size.
     *  @param max   Maximum sub word size.
     *  @return A TreeSet containing the sub words.
     */
    private fun subWordFinderForTests(input: String, min: Int, max: Int): TreeSet<String> {
        val against = TreeSet<String>()

        if (input.length < min) {
            return against
        }

        for (a in input.indices) {
            val sub = input.substring(0, a) + input.substring(a + 1)

            if (sub.length in min..max) {
                against.addAll(AnagramFinder().computeAnagram(sub))
                if (checkIfWordDictionary(sub)) {
                    against.add(sub)
                }
            }

            against.addAll(subWordFinderForTests(sub, min, max))
        }

        return against
    }

    /** Quick and dirty phrase finder.
     *
     *  @param af A reference to the anagram finder object.
     *  @param input The input string.
     *  @param min The minimum word size.
     *  @param max The maximum word size.
     *  @param onlyWords Whether or not we are testing real words or not.
     *  @return A TreeSet containing the phrases.
     */
    private fun phraseFinder(
        af: AnagramFinder,
        input: String,
        min: Int,
        max: Int,
        onlyWords: Boolean = false,
    ): TreeSet<String> {
        val against = TreeSet<String>()
        if (onlyWords) {
            af.enableTesting()
        }
        val set = af.computeAnagram(input)
        if (onlyWords) {
            af.disableTesting()
        }

        if (max == 0) {
            return against
        }

        for (a in set) {
            val phraseList = TreeSet<String>()
            phraseFinderWrapped(a, TreeSet(), phraseList, min, max, onlyWords)
            against.addAll(phraseList)
        }

        return against
    }

    /** Wrapped recursive function that finds phrases.
     *
     * @param input The input string.
     * @param workingPhrase The current phrase in progress.
     * @param phraseList The list of completed phrases.
     * @param min The minimum word size.
     * @param max The maximum word size.
     * @param onlyWords Whether or not we are testing real words or not.
     */
    private fun phraseFinderWrapped(
        input: String,
        workingPhrase: TreeSet<String>,
        phraseList: TreeSet<String>,
        min: Int,
        max: Int,
        onlyWords: Boolean,
    ) {
        if (input.length >= min) {
            for (c in min..max) {
                if (input.length < c) {
                    break
                }
                val foo = input.substring(0, c)
                val bar = input.substring(c)
                if (onlyWords) {
                    if (!WordDictionary.instance().findInDictionary(foo)) {
                        continue
                    }
                }
                workingPhrase.add(foo)
                phraseFinderWrapped(bar, workingPhrase, phraseList, min, max, onlyWords)
                workingPhrase.remove(foo)
            }
        } else if (input.isEmpty()) {
            val sb = StringBuilder()
            for (b in workingPhrase) {
                sb.append(b)
                sb.append(" ")
            }
            phraseList.add(sb.substring(0, sb.length - 1))
        }
    }
}
