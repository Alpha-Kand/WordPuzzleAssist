@file:Suppress("ClassName", "SpellCheckingInspection")

package com.huntersmeadow.wordpuzzleassist.threads

import com.huntersmeadow.wordpuzzleassist.workclasses.FITBFinder
import org.junit.Assert.assertEquals
import org.junit.Test

class FITBFinder_UnitTests : Parent_UnitTests() {

    /** Checks the results array for newline characters that somehow have slipped through.
     *
     *  @param results The arraylist of results.
     */
    private fun checkForNewlines(results: ArrayList<String>) {
        for (s in results) {
            assertEquals(false, s.contains('\n'))
        }
    }

    @Test
    fun computeFITB() {
        val fitb = FITBFinder()
        val against = ArrayList<String>()

        // End single replace.
        var results = fitb.computeFITB("hell?")
        for (c in 'a'..'z') {
            val word = "hell$c"
            if (checkIfWordDictionary(word)) {
                against.add(word)
            }
        }
        checkForNewlines(results)
        testArrayList(against, results)
        results.clear()
        against.clear()

        // End double replace.
        results = fitb.computeFITB("hel??")
        for (c in 'a'..'z') {
            for (d in 'a'..'z') {
                val word = "hel$c$d"
                if (checkIfWordDictionary(word)) {
                    against.add(word)
                }
            }
        }
        checkForNewlines(results)
        testArrayList(against, results)
        results.clear()
        against.clear()

        // Start single replace.
        results = fitb.computeFITB("?ello")
        for (c in 'a'..'z') {
            val word = c + "ello"
            if (checkIfWordDictionary(word)) {
                against.add(word)
            }
        }
        checkForNewlines(results)
        testArrayList(against, results)
        results.clear()
        against.clear()

        // Start double replace.
        results = fitb.computeFITB("??llo")
        for (c in 'a'..'z') {
            for (d in 'a'..'z') {
                val word = c + "" + d + "llo"
                if (checkIfWordDictionary(word)) {
                    against.add(word)
                }
            }
        }
        checkForNewlines(results)
        testArrayList(against, results)
        results.clear()
        against.clear()

        // Middle single replace.
        results = fitb.computeFITB("he?Lo")
        for (c in 'a'..'z') {
            val word = "he" + c + "lo"
            if (checkIfWordDictionary(word)) {
                against.add(word)
            }
        }
        checkForNewlines(results)
        testArrayList(against, results)
        results.clear()
        against.clear()

        // Middle double replace.
        results = fitb.computeFITB("hE??o")
        for (c in 'a'..'z') {
            for (d in 'a'..'z') {
                val word = "he" + c + d + "o"
                if (checkIfWordDictionary(word)) {
                    against.add(word)
                }
            }
        }
        checkForNewlines(results)
        testArrayList(against, results)
        results.clear()
        against.clear()

        // End wild replace
        results = fitb.computeFITB("HELL*")
        assertEquals(92, results.count())
        checkForNewlines(results)
        results.clear()

        // Start wild replace
        results = fitb.computeFITB("*eLlo")
        assertEquals(37, results.count())
        checkForNewlines(results)
        results.clear()

        // Middle wild replace
        results = fitb.computeFITB("He*lO")
        assertEquals(2, results.count())
        checkForNewlines(results)
        results.clear()

        // Mixed ?s and *s.
        results = fitb.computeFITB("Amb?gu??*s")
        assertEquals(3, results.count())
        checkForNewlines(results)
        results.clear()
    }
}
