package com.huntersmeadow.wordpuzzleassist.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.huntersmeadow.wordpuzzleassist.R

/** Base activity that all activities inherit from.
 *  Provides:
 *  > 'getCurrentActivity' provides access to whatever activity we are currently in. It
 *    automatically updates as each activity is loaded or unloaded.
 *  > Creates the help and about title bar buttons.
 *  > Provides access to the puzzle solving thread manager.
 */
open class BaseActivity : AppCompatActivity() {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // OVERRIDES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActiveActivity = this
    }

    override fun onResume() {
        super.onResume()
        mActiveActivity = this
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.standard_actionbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.help_menu_item -> {
                helpMenuCALLBACK()
                true
            }

            R.id.about_menu_item -> {
                aboutMenuCallback()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // CALLBACKS
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Abstract help menu callback. */
    open fun helpMenuCALLBACK() {
    }

    /** About menu callback. */
    private fun aboutMenuCallback() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.base_activity_about))
        builder.setTitle(getString(R.string.base_activity_about_title))
        builder.create().show()
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // REGULAR
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Set this activity's title to the given input string.
     *
     *  @param title The string to set the title to.
     */
    fun setActionBarTitle(title: String) {
        val ab = this.supportActionBar
        if (ab != null) {
            ab.title = title
        }
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-
    // SUBCLASSES, ETC.
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-

    companion object {
        /** The current active activity. */
        private var mActiveActivity: BaseActivity? = null

        /** Get a reference to the current activity.
         *
         *  @return The current active activity.
         */
        fun getCurrentActivity(): BaseActivity? {
            return mActiveActivity
        }
    }
}
