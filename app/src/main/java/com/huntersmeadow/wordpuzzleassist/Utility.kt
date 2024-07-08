package com.huntersmeadow.wordpuzzleassist

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager

/** Generic Mutable class.
 *
 *  @param mValue Variable the [Mutable] object is to contain.
 */
open class Mutable<T>(var mValue: T)

/** A class for when you need to pass an integer like an object and be able to change it.
 *
 *  @param value Integer value to hold on to.*/
class MutableInteger(value: Int) : Mutable<Int>(value)

/**
 * Hides the keyboard without needing to know which view currently holds focus.
 *
 * @param activity Current active Activity.
 */
fun hideKeyboardMine(activity: Activity?) {
    val view = activity?.currentFocus ?: View(activity)
    (activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(view.windowToken, 0)
}

/** Removes a character from a string at the given index.
 *
 *  @param input String to remove a character from.
 *  @param index Index of character to remove.
 *  @return A new string with the removed character.
 */
fun removeLetter(input: String, index: Int): String {
    return input.substring(0, index) + input.substring(index + 1)
}


/**
 * Doubles the value passed to it.
 *
 * @param [num] Value to double.
 * @return Doubled value of the passed value.
 */
fun double(num: Number): Double {
    return num.toDouble() * 2.0
}

/**
 * Halves the numeric value passed to it.
 *
 * @param [num] Value to halve.
 * @return Half of the value passed to this function.
 */
fun halve(num: Number): Double {
    return num.toDouble() / 2.0
}

/**
 * Computes a 'factorial' starting at the maximum number of '[num]' and multiplies the '[n]' smaller
 * numbers. For example 'reverseSubFactorial(5,3)' = '5 * 4 * 3' and stops before '* 2 * 1'.
 *
 * @param num The largest starting number.
 * @param n The number of times to multiply the running factorial before stopping.
 * @return The factorial up to the given stopping point.
 */
fun reverseSubFactorial(num: Int, n: Int): Int {
    if (n == 0) {
        return 0
    }

    var innerNum = num
    var result = innerNum

    repeat(n - 1){ // "result = innerI" is the first step.
        result *= --innerNum
    }

    return result
}
