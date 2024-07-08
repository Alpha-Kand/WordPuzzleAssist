@file:Suppress("ClassName")

package com.huntersmeadow.wordpuzzleassist.threads

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.huntersmeadow.wordpuzzleassist.workclasses.WordDictionary
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WordDictionary_UnitTests {

    @Test
    fun findInDictionary() {
        val wd = WordDictionary.instance()
        assertEquals(true, wd.findInDictionary("hello"))
        assertEquals(true, wd.findInDictionary("bat"))
        assertEquals(true, wd.findInDictionary("batch"))
        assertEquals(true, wd.findInDictionary("batches"))
        assertEquals(true, wd.findInDictionary("a"))
        assertEquals(false, wd.findInDictionary("not a word"))
        assertEquals(false, wd.findInDictionary(""))
        assertEquals(false, wd.findInDictionary(" "))
    }

    @Test
    fun findPre() {
        val wd = WordDictionary.instance()
        assertEquals(true, wd.findPre("uncharacteristically"))
        assertEquals(true, wd.findPre("hello"))
        assertEquals(true, wd.findPre("bat"))
        assertEquals(true, wd.findPre("batch"))
        assertEquals(true, wd.findPre("batches"))
        assertEquals(true, wd.findPre("mirage"))
        assertEquals(true, wd.findPre("mirages"))
        assertEquals(false, wd.findPre("not a word"))

        assertEquals(true, wd.findPre(""))
        assertEquals(false, wd.findPre(" "))

        @Suppress("SpellCheckingInspection")
        assertEquals(true, wd.findPre("batc"))
        assertEquals(true, wd.findPre("a"))
    }
}
