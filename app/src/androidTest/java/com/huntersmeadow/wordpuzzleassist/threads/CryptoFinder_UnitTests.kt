@file:Suppress("ClassName", "SpellCheckingInspection")

package com.huntersmeadow.wordpuzzleassist.threads

import com.huntersmeadow.wordpuzzleassist.workclasses.CryptoFinder
import org.junit.Assert.assertEquals
import org.junit.Test

class CryptoFinder_UnitTests : Parent_UnitTests() {

    @Test
    fun computeCryptoWorking() {
        // A handful of decent sized words.
        val cf = CryptoFinder()
        var input = "university highschool college kindergarten universite friendly chatter"
        var resultPair = cf.computeCrypto(input)
        var result = resultPair.second
        assertEquals(true, resultPair.first)
        assertEquals(1, result.size)
        assertEquals(input, result[0])

        // A standard crypto family puzzle.
        input = "cmgp uhwmoib ippvrphmr gvwvhqqv hrntvbphmr avnn" +
            " diqq ip vinv nvgsvirp aigbd iumop cibv"
        resultPair = cf.computeCrypto(input)
        result = resultPair.second
        assertEquals(true, resultPair.first)
        assertEquals(6, result.size)

        // The first word finishes the second and last word.
        input = "hippopotamus miss"
        resultPair = cf.computeCrypto(input)
        result = resultPair.second
        assertEquals(true, resultPair.first)
        assertEquals(1, result.size)
        assertEquals(input, result[0])

        // A bunch of decent words and a few words that can be 'swapped' or are 'interchangeable'.
        input = "university highschool college kindergarten hello there" +
            " friendly world jello freed paused objects copying"
        resultPair = cf.computeCrypto(input)
        result = resultPair.second
        assertEquals(true, resultPair.first)
        assertEquals(1, result.size)
        assertEquals(input, result[0])

        // Focus on interchangeable words.
        input = "paraheliotropic hello jello cello bello"
        resultPair = cf.computeCrypto(input)
        result = resultPair.second
        assertEquals(true, resultPair.first)
        assertEquals(2, result.size)
        assertEquals("paraheliotropic hello bello cello jello", result[0])
        assertEquals("paraheliotropic hello jello cello bello", result[1])

        // Mixed symbols and capital letters.
        input = "Superb, excellent, SYMBOLS!!!"
        resultPair = cf.computeCrypto(input)
        result = resultPair.second
        assertEquals(true, resultPair.first)
        assertEquals(14, result.size)
        assertEquals("Tucano, ambassade, THROWST!!!", result[0])
        assertEquals("Superb, excellent, SYMBOLS!!!", result[4])

        input = "It doesn't have to be literally mansion's. Whimsical and WILD. Totally CrAzY!"
        resultPair = cf.computeCrypto(input)
        result = resultPair.second
        assertEquals(true, resultPair.first)
        assertEquals(38, result.size)
        assertEquals(
            "It doesn't habe to fe literally mansion's. Whimsical and WILD." +
                " Totally CrApY!",
            result[0],
        )
        assertEquals(
            "It doesn't have to be literally mansion's. Whimsical and WILD." +
                " Totally CrAzY!",
            result[33],
        )
    }

    @Test
    fun computeCryptoNotWorking() {
        // Small complete but contains non-words.
        val cf = CryptoFinder()
        var input = "hello llhll eeeeh"
        var resultPair = cf.computeCrypto(input)
        var result = resultPair.second
        assertEquals(false, resultPair.first)
        assertEquals(1011, result.size)
        assertEquals("aboon ooaoo bbbba", result[0])
        assertEquals("acool ooaoo cccca", result[1])
        assertEquals("zudda ddzdd uuuuz", result[1010])

        // Larger selection that completes input.
        input = "kindergarten highschool college hello there eeeeh"
        resultPair = cf.computeCrypto(input)
        result = resultPair.second
        assertEquals(false, resultPair.first)
        assertEquals(1, result.size)
        assertEquals(input, result[0])

        // Large input that doen't complete.
        input = "universiby highschool college kindergarten universite friendly chatter"
        resultPair = cf.computeCrypto(input)
        result = resultPair.second
        assertEquals(false, resultPair.first)
        assertEquals(1, result.size)
        assertEquals(
            "-ni-er-i-- -ig------- ----ege kindergarten" +
                " -ni-er-ite -riend-- --atter",
            result[0],
        )

        // When the algorithm sorts the words, the first word up has ZERO matches. Must cycle
        // through words to get a match.
        input = "Tvachmbakz Lpddhnh Yanyblyppd Oavjhmnfmkhvasdf"
        resultPair = cf.computeCrypto(input)
        result = resultPair.second
        assertEquals(false, resultPair.first)
        assertEquals(2, result.size)
        assertEquals("Condiments Flyyiri Unruefully -no-imr-mtion-y-", result[0])
        assertEquals("University College Highschool -in-erg-rteni-l-", result[1])

        // Absolultely no results, even when cycling through all the words.
        input = "afafafa afafafafa afafafafafa afafafafafafa"
        resultPair = cf.computeCrypto(input)
        result = resultPair.second
        assertEquals(false, resultPair.first)
        assertEquals(0, result.size)
    }

    @Test
    fun cryptoFinderInternals() {
        val cf = CryptoFinder()
        cf.unitTestInternals()
    }
}
