package com.huntersmeadow.wordpuzzleassist.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AlertDialog
import com.huntersmeadow.wordpuzzleassist.R
import com.huntersmeadow.wordpuzzleassist.customviews.AssistCard
import com.huntersmeadow.wordpuzzleassist.customviews.DeleteAssistCard
import com.huntersmeadow.wordpuzzleassist.customviews.FreeLayoutView
import com.huntersmeadow.wordpuzzleassist.workclasses.ActivityOrders
import com.huntersmeadow.wordpuzzleassist.workclasses.ThreadManager
import com.huntersmeadow.wordpuzzleassist.workclasses.getEmptyClickListener

/** Activity that holds the [AssistCard]s and acts as a portal to the various assists in the app.
 */
class DirectoryActivity : BaseActivity() {

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // VARIABLES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** A reference to the main FreeLayoutView. */
    private val mFLV: FreeLayoutView by lazy { findViewById(R.id.flv) }

    /** A list of all the AssistCards in the FreeLayoutView. */
    private val mACards = ArrayList<AssistCard>()

    /** A reference to the assist select dialog. */
    private lateinit var mAssistSelectDialog: Dialog

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // OVERRIDES
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_directory)
        setActionBarTitle(getString(R.string.activity_title_directory))
        mAssistSelectDialog = createAssistListDialog(setupAssistList())
    }

    override fun onResume() {
        super.onResume()
        ThreadManager.instance().mFocusThread = -1
        // Add enough room to 'mFLV.mAssistOrder' to ensure the AssistCards can
        // be put in their correct position orders.
        mFLV.mAssistOrder.clear()
        /*while (mFLV.mAssistOrder.size < ThreadManager.instance().mPOD.size)
        {
            mFLV.mAssistOrder.add(createDummyAssistCard())
        }*/
        addAssistCards()
    }

    override fun onPause() {
        super.onPause()
        mFLV.removeAllViews()
        mACards.clear()
    }

    override fun onStop() {
        super.onStop()
        mFLV.mAssistOrder.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.directory_actionbar, menu)
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.choose_new_assist_menu -> {
                mAssistSelectDialog.show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // CALLBACKS
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    override fun helpMenuCALLBACK() {
        // Insert 'add_assist' drawable to string.
        val spannableString = SpannableString(getString(R.string.directory_help_text))
        spannableString.setSpan(
            ImageSpan(this, R.drawable.ic_add_assist),
            9,
            10,
            0,
        )
        AlertDialog.Builder(this)
            .setMessage(spannableString)
            .setTitle(getString(R.string.help_title))
            .create()
            .show()
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PUBLIC
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Deletes the given [AssistCard].
     *
     *  @param assistCard The [AssistCard] to delete.
     */
    fun deleteAssistCard(assistCard: AssistCard) {
        mFLV.deleteAssistCard(assistCard)
    }

    /** Causes all the [AssistCard]s to show their [DeleteAssistCard]s. */
    fun showDACs() {
        val count = mFLV.mAssistOrder.size
        for (i in 0 until count) {
            mFLV.mAssistOrder[i].showDAC()
        }
    }

    /** Flags the [AssistCard]s to stop their update coroutines. */
    fun stopCoroutines() {
        for (i in 0 until mFLV.mAssistOrder.size) {
            mFLV.mAssistOrder[i].stopLooping()
        }
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // PRIVATE
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    /** Add an assist card for every thread. */
    private fun addAssistCards() {
        for (i in ThreadManager.instance().mPOD.indices) {
            val pod = ThreadManager.instance().mPOD[i]
            val assistCard = addAssistCard(pod)
            mACards.add(assistCard)
            mFLV.addView(assistCard)
            // mFLV.mAssistOrder[pod.mIndex] = assistCard
            mFLV.mAssistOrder.add(assistCard)
        }
    }

    /** Sets up the ListView that goes in the choose assist dialog.
     *
     *  @return The ListView of the choose assist dialog.
     */
    private fun setupAssistList(): ListView {
        val assistList = ListView(this)
        assistList.adapter = SimpleAdapter(
            this,
            getChooseAssistDialogText(),
            R.layout.directory_list_item,
            arrayOf(
                PrivateConstants.DIRECTORY_SELECTION_NAME,
                PrivateConstants.DIRECTORY_SELECTION_DESC,
            ),
            intArrayOf(
                R.id.directory_title,
                R.id.directory_description,
            ),
        )
        assistList.onItemClickListener = AdapterView.OnItemClickListener { _, view, position, _ ->
            mAssistSelectDialog.cancel()
            startActivity(Intent(view.context, ActivityOrders.getActivityClass(position)))
        }
        return assistList
    }

    /** Creates the choose assist dialog.
     *
     *  @param assistList A ListView full of assist choices.
     *  @return The assist choices dialog.
     */
    private fun createAssistListDialog(assistList: ListView): Dialog {
        return AlertDialog.Builder(this)
            .setView(assistList)
            .setNegativeButton(getString(R.string.dialog_cancel), getEmptyClickListener())
            .create()
    }

    /** Creates a new [AssistCard].
     *
     *  @param pod The information object to customize the new [AssistCard] with.
     *  @return The new [AssistCard].
     */
    private fun addAssistCard(pod: ThreadManager.AssistInfo): AssistCard {
        return AssistCard(this, pod)
    }

    /** Creates the text to populate the choose assist dialog.
     *
     *  @return The text for the choose assist dialog.
     */
    private fun getChooseAssistDialogText(): List<Map<String, String>> {
        val mainList = ArrayList<Map<String, String>>()
        var hashMap: MutableMap<String, String>
        val name = PrivateConstants.DIRECTORY_SELECTION_NAME
        val desc = PrivateConstants.DIRECTORY_SELECTION_DESC
        var i = 0
        while (i < ActivityOrders.getActivityCount() * 2) {
            hashMap = HashMap(2)
            hashMap[name] = getString(ActivityOrders.getDirectorySelectionStrings(i))
            hashMap[desc] = getString(ActivityOrders.getDirectorySelectionStrings(i + 1))
            mainList.add(hashMap)
            i += 2
        }
        return mainList
    }

    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
    // SUBCLASSES, ETC.
    // -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~

    object PrivateConstants {
        const val DIRECTORY_SELECTION_NAME = "name"
        const val DIRECTORY_SELECTION_DESC = "desc"
    }
}
