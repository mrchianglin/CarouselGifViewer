package com.bernaferrari.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.airbnb.epoxy.EpoxyRecyclerView
import com.bernaferrari.base.misc.normalizeString
import com.bernaferrari.base.misc.onTextChanged
import com.bernaferrari.base.misc.showKeyboardOnView
import com.bernaferrari.base.view.onScroll
import com.bernaferrari.ui.R
import com.bernaferrari.ui.base.SharedBaseFrag
import com.bernaferrari.ui.extensions.hideKeyboardWhenNecessary
import kotlinx.android.synthetic.main.frag_search.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

/**
 * Simple fragment with a search box, a toolbar and a recyclerview.
 */
abstract class BaseSearchFragment : SharedBaseFrag(), CoroutineScope {

    override val recyclerView: EpoxyRecyclerView by lazy { recycler }

    val container: ConstraintLayout by lazy { baseContainer }

    open val showKeyboardWhenLoaded = true

    open val sidePadding = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.frag_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // app might crash if user is scrolling fast and quickly switching screens,
        // so the nullable seems necessary.
        recycler?.onScroll { _, dy ->
            // this will take care of titleElevation
            // recycler might be null when back is pressed
            val raiseTitleBar = dy > 0 || recycler.computeVerticalScrollOffset() != 0
            title_bar?.isActivated = raiseTitleBar // animated via a StateListAnimator
        }

        recycler?.updatePadding(left = sidePadding, right = sidePadding)

        toolbarMenu.isVisible = showMenu

        if (showMenu) {
            (activity as? AppCompatActivity)?.setSupportActionBar(toolbarMenu)
            toolbarMenu.title = null
        }

        queryInput.onTextChanged { search ->
            queryClear.isInvisible = search.isEmpty()
            recycler.smoothScrollToPosition(0)
            onTextChanged(search.toString().normalizeString())
        }

        searchIcon.setOnClickListener {
            queryInput.showKeyboardOnView()
        }

        if (showKeyboardWhenLoaded) {
            queryInput.showKeyboardOnView()
        }

        hideKeyboardWhenNecessary(recycler, queryInput)

        queryClear.setOnClickListener { queryInput.setText("") }

        if (closeIconRes == null) {
            close.visibility = View.GONE
        } else {
            val closeIcon = closeIconRes ?: 0
            close.setImageResource(closeIcon)
            close.setOnClickListener { dismiss() }
        }
    }

    abstract fun onTextChanged(searchText: String)

    fun setInputHint(hint: String) {
        queryInput?.hint = hint
    }

    fun getInputText(): String = queryInput.text.toString()

    fun scrollToPosition(pos: Int) = recycler.scrollToPosition(pos)

    override fun onDestroy() {
        coroutineContext.cancel()
        disposableManager.clear()
        super.onDestroy()
    }
}