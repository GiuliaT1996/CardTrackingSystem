package com.angiuprojects.cardtrackingapp.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.angiuprojects.cardtrackingapp.R
import com.angiuprojects.cardtrackingapp.adapters.CardRecyclerAdapter
import com.angiuprojects.cardtrackingapp.entities.Card
import com.angiuprojects.cardtrackingapp.queries.Queries
import com.angiuprojects.cardtrackingapp.utilities.CardMarketUtils
import com.angiuprojects.cardtrackingapp.utilities.Constants
import com.angiuprojects.cardtrackingapp.utilities.Utils
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import java.lang.Exception


class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        Constants.getInstance().getInstanceCards()?.removeAll{it.name == ""}

        if(Constants.getInstance().getInstanceSettings()?.get(0)?.enabled == true)
            CardMarketUtils.callCardMarketCoroutine()

        Constants.getInstance().getInstanceCards()?.let { setRecyclerAdapter(it) }
    }

    private fun setRecyclerAdapter(cardList: MutableList<Card>) {

        val adapter = CardRecyclerAdapter(cardList, this)
        val recyclerView = findViewById<RecyclerView>(R.id.card_recycler_view)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        swipeToDelete(cardList, adapter)
    }


    private fun createFilterAdapter() {

        val filterSpinner = findViewById<AutoCompleteTextView>(R.id.filter_spinner)

        Utils.setSpinnerInfo(Constants.getInstance().fieldList, filterSpinner, this)

        filterSpinner?.onItemClickListener =
        OnItemClickListener { parent, view, position, id ->

            var fieldList = mutableListOf<String>()

            when(position) {
                0 -> fieldList = Constants.getInstance().getInstanceCards()?.map { it.archetype }?.distinct()
                    ?.toMutableList()!!
                1 -> fieldList = Constants.getInstance().getInstanceCards()?.map { it.duelist }?.distinct()
                    ?.toMutableList()!!
                2 -> fieldList = Constants.getInstance().getInstanceCards()?.map { it.set }?.distinct()
                    ?.toMutableList()!!
                3 -> fieldList = Constants.getInstance().priceRange
                else -> Log.e(Constants.getInstance().CARD_TRACKING_DEBUGGER, "Nessun campo selezionato")
            }

            createChildFilter(fieldList, position)
        }
    }

    private fun swipeToDelete(cardList: MutableList<Card>, cardRecyclerAdapter: CardRecyclerAdapter) {

        val cardRecyclerView = findViewById<RecyclerView>(R.id.card_recycler_view)
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedCard: Card = cardList[viewHolder.adapterPosition]

                val position = viewHolder.adapterPosition

                cardList.removeAt(viewHolder.adapterPosition)

                cardRecyclerAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                Queries.getInstance().deleteCard(deletedCard)

                Snackbar.make(cardRecyclerView, "Cancellata la carta " + deletedCard.name, Snackbar.LENGTH_LONG)
                    .setAction(
                        "Annulla"
                    ) {
                        cardList.add(position, deletedCard)
                        cardRecyclerAdapter.notifyItemInserted(position)
                        Queries.getInstance().addUpdateCard(deletedCard, updatePrice = false)
                    }.show()
            }
        }).attachToRecyclerView(cardRecyclerView)
    }

    private fun createChildFilter(filterItemList: List<String>, field: Int) {
        val childFilterSpinner = findViewById<AutoCompleteTextView>(R.id.child_filter_spinner)
        childFilterSpinner.setText("")

        Utils.setSpinnerInfo(filterItemList, childFilterSpinner, this)

        childFilterSpinner?.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                val filteredList = Utils.filter(field, parent.getItemAtPosition(position) as String)
                setRecyclerAdapter(filteredList)
            }

        findViewById<TextInputLayout>(R.id.child_filter_dropdown).visibility = View.VISIBLE
    }

    fun onClickFilter(view : View) {
        setButtonNonClickable()
        findViewById<ImageButton>(R.id.refresh_button).visibility = View.VISIBLE
        findViewById<TextInputLayout>(R.id.filter_dropdown).visibility = View.VISIBLE
        createFilterAdapter()
    }

    fun onClickSearch(view : View) {
        setButtonNonClickable()
        findViewById<ImageButton>(R.id.refresh_button).visibility = View.VISIBLE
        val searchInputText = findViewById<TextInputLayout>(R.id.search_input_text)
        searchInputText.visibility = View.VISIBLE
        searchInputText.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredList = Utils.filter(4, s.toString())
                setRecyclerAdapter(filteredList)
            }
        })
    }

    private fun setButtonNonClickable() {
        findViewById<ImageButton>(R.id.filter_button).isClickable = false
        findViewById<ImageButton>(R.id.search_button).isClickable = false
        findViewById<TextView>(R.id.title).visibility = View.INVISIBLE
        findViewById<ImageButton>(R.id.settings_button).visibility = View.INVISIBLE
    }

    fun onClickRefreshActivity(view : View) {
        setContentView(R.layout.activity_menu)
        Constants.getInstance().getInstanceCards()?.let { setRecyclerAdapter(it) }
    }

    override fun onBackPressed() {
        finishAffinity()
        finish()
    }

    fun onClickAdd(view: View) {
        finish()
        val i = Intent(this, AddActivity::class.java)
        startActivity(i)
    }

    fun onClickOpenSettings(view: View) {
        val i = Intent(this, SettingsActivity::class.java)
        startActivity(i)
    }
}