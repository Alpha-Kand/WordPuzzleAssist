@file:Suppress("ClassName")

package com.huntersmeadow.wordpuzzleassist.threads

import com.huntersmeadow.wordpuzzleassist.R
import com.huntersmeadow.wordpuzzleassist.activities.AnagramsActivity
import com.huntersmeadow.wordpuzzleassist.activities.CryptoActivity
import com.huntersmeadow.wordpuzzleassist.activities.DictionaryActivity
import com.huntersmeadow.wordpuzzleassist.activities.FITBActivity
import com.huntersmeadow.wordpuzzleassist.workclasses.ActivityOrders
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityOrders_UnitTests {

    @Test
    fun puzzleAssistCount() {
        assertEquals(4, ActivityOrders.getActivityCount())
    }

    @Test
    fun anagramOrder() {
        val index = 0

        assertEquals(AnagramsActivity::class.java, ActivityOrders.getActivityClass(index))
        assertEquals(
            R.string.assist_card_title_anagram,
            ActivityOrders.getAssistCardTitle(ActivityOrders.ActivityOrder.ANAGRAM),
        )
        assertEquals(ActivityOrders.ActivityOrder.ANAGRAM, ActivityOrders.getAOEnum(index))
        assertEquals(R.string.directory_selection_anagram_title, ActivityOrders.getDirectorySelectionStrings(index * 2))
        assertEquals(
            R.string.directory_selection_anagram_desc,
            ActivityOrders.getDirectorySelectionStrings((index * 2) + 1),
        )
    }

    @Test
    fun fitbOrder() {
        val index = 1
        assertEquals(FITBActivity::class.java, ActivityOrders.getActivityClass(index))
        assertEquals(
            R.string.assist_card_title_fitb,
            ActivityOrders.getAssistCardTitle(ActivityOrders.ActivityOrder.FITB),
        )
        assertEquals(ActivityOrders.ActivityOrder.FITB, ActivityOrders.getAOEnum(index))
        assertEquals(R.string.directory_selection_fitb_title, ActivityOrders.getDirectorySelectionStrings(index * 2))
        assertEquals(
            R.string.directory_selection_fitb_desc,
            ActivityOrders.getDirectorySelectionStrings((index * 2) + 1),
        )
    }

    @Test
    fun cryptoOrder() {
        val index = 2
        assertEquals(CryptoActivity::class.java, ActivityOrders.getActivityClass(index))
        assertEquals(
            R.string.assist_card_title_crypto,
            ActivityOrders.getAssistCardTitle(ActivityOrders.ActivityOrder.CRYPTO),
        )
        assertEquals(ActivityOrders.ActivityOrder.CRYPTO, ActivityOrders.getAOEnum(index))
        assertEquals(R.string.directory_selection_crypto_title, ActivityOrders.getDirectorySelectionStrings(index * 2))
        assertEquals(
            R.string.directory_selection_crypto_desc,
            ActivityOrders.getDirectorySelectionStrings((index * 2) + 1),
        )
    }

    @Test
    fun dictionaryOrder() {
        val index = 3
        assertEquals(DictionaryActivity::class.java, ActivityOrders.getActivityClass(index))
        assertEquals(
            R.string.assist_card_title_dictionary,
            ActivityOrders.getAssistCardTitle(ActivityOrders.ActivityOrder.DICTIONARY),
        )
        assertEquals(ActivityOrders.ActivityOrder.DICTIONARY, ActivityOrders.getAOEnum(index))
        assertEquals(
            R.string.directory_selection_dictionary_title,
            ActivityOrders.getDirectorySelectionStrings(index * 2),
        )
        assertEquals(
            R.string.directory_selection_dictionary_desc,
            ActivityOrders.getDirectorySelectionStrings((index * 2) + 1),
        )
    }
}
