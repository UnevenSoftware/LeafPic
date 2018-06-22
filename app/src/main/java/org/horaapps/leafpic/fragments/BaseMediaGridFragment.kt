package org.horaapps.leafpic.fragments

import android.content.Context
import android.view.View

import org.horaapps.leafpic.items.ActionsListener

/**
 * Base class for fragments showing any kind of Media in a Grid fashion.
 *
 * Allows selection, multiple select Context Menus, etc.
 */
abstract class BaseMediaGridFragment : BaseFragment(), IFragment, ActionsListener {

    lateinit var editModeListener: EditModeListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is EditModeListener) editModeListener = context
        else throw RuntimeException("Parent must implement Edit Mode Listener!")
    }

    fun onBackPressed() = when (editMode()) {
        true -> {
            exitContextMenu()
            true
        }
        false -> false
    }

    /**
     * Exit the Context Menu.
     */
    protected fun exitContextMenu() {
        clearSelected()
        updateToolbar()
    }

    /**
     * Update the Toolbar for switching between Edit Mode.
     */
    protected fun updateToolbar() {
        editModeListener.changedEditMode(
                editMode(),
                getSelectedCount(),
                getTotalCount(),
                getToolbarButtonListener(editMode()),
                getToolbarTitle())

        // Refresh the Toolbar menu
        activity?.invalidateOptionsMenu()
    }

    /**
     * The total selected item count.
     */
    abstract fun getSelectedCount(): Int

    /**
     * The total number of items displayed.
     */
    abstract fun getTotalCount(): Int

    /**
     * A listener to be invoked when user taps on the Toolbar icon.
     */
    abstract fun getToolbarButtonListener(editMode: Boolean): View.OnClickListener?

    /**
     * Text to display on the toolbar.
     */
    abstract fun getToolbarTitle(): String?
}
