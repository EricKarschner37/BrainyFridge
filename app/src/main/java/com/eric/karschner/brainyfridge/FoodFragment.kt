package com.eric.karschner.brainyfridge

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import kotlinx.android.synthetic.main.add_food_dialog.*
import kotlinx.android.synthetic.main.fragment_food.*
import java.lang.IllegalStateException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FoodFragment : Fragment(){

    private var sharedPref: SharedPreferences? = null
    var foodsList: ArrayList<Pair<String, String>> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_food, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = activity?.getSharedPreferences("shared", Context.MODE_PRIVATE)

        foods_rv.layoutManager = LinearLayoutManager(context)
        readFoods()
    }

    fun readFoods(){
        val foodsString = sharedPref?.getString("foods", " - ") ?: " - "
        Log.i("FoodFragment", "$foodsString")
        val foodsSplit = foodsString.split("|")

        foodsList.removeAll(foodsList)

        for (pair in foodsSplit){
            val split = pair.split("-")

            if (split.size == 2) {
                val food = split[0]
                val date = split[1]

                if (food.trim() != "" && date.trim() != "") foodsList.add(Pair(food, date))
            }
        }

        Log.i("foodsList", "$foodsList")

        val format = SimpleDateFormat("mm/dd/yyyy")
        foodsList.sortBy { format.parse(it.second) }
        updateFoodListView()
    }

    private fun updateFoodListView(){
        foods_rv.adapter = FoodAdapter(foodsList)
    }
}

class AddFoodDialog: DialogFragment(){

    private lateinit var listener: AddFoodDialogListener

    interface AddFoodDialogListener {
        fun onAddFoodPositiveClick(dialog: DialogFragment, editText: EditText)
        fun onAddFoodNegativeClick(dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            val view = inflater.inflate(R.layout.add_food_dialog, null)
            val editText: EditText = view.findViewById(R.id.food_name_et)

            builder.setView(inflater.inflate(R.layout.add_food_dialog, null))
                .setPositiveButton("Continue") { _, _ ->
                    listener.onAddFoodPositiveClick(this, editText)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    listener.onAddFoodNegativeClick(this)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as AddFoodDialogListener
        } catch (e:ClassCastException) {
            throw java.lang.ClassCastException(("$context must implement AddFoodDialogListener"))
        }
    }
}
