@file:Suppress("ClassName", "SpellCheckingInspection")

package com.huntersmeadow.wordpuzzleassist.threads

import com.huntersmeadow.wordpuzzleassist.workclasses.WordDictionary
import org.junit.Assert.assertEquals
import kotlin.collections.ArrayList

open class Parent_UnitTests {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // HELPER
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Checks if the given 'input' is in the dictionary.
     *
     *  @param input Input string to check.
     *  @return Boolean whether or not the input is in the dictionary.
     */
    protected fun checkIfWordDictionary(input: String): Boolean {
        return WordDictionary.instance().findInDictionary(input)
    }

    /** Compares the contents of two ArrayLists to ensure they are effectively equal. Fails a
     *  test if they differ.
     *
     *  @param against First ArrayList.
     *  @param results Second ArrayList.
     */
    protected fun testArrayList(against: ArrayList<String>, results: ArrayList<String>) {
        while (results.size > 0) {
            val index = results.size - 1
            assertEquals(true, against.contains(results[index]))
            results.removeAt(index)
        }
    }
}
