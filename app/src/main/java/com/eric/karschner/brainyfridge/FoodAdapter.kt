package com.eric.karschner.brainyfridge

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList




class FoodAdapter(private val foodsList: ArrayList<Pair<String, String>>) :
    RecyclerView.Adapter<FoodAdapter.ViewHolder>(){

    private lateinit var red_circle : Drawable

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val days_tv: TextView = view.findViewById(R.id.days_tv)
        val food_name_tv: TextView = view.findViewById(R.id.food_name_tv)
        val delete_button: ImageButton = view.findViewById(R.id.delete_button)
        val days_color_view: View = view.findViewById(R.id.days_color_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {

        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.food_list_item, parent, false)

        red_circle = context.getDrawable(R.drawable.red_circle)

        try {
            listener = context as OnDeleteClickListener
        } catch (e:ClassCastException) {
            throw java.lang.ClassCastException(("$context must implement AddFoodDialogListener"))
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val food = foodsList[position]
        val format = SimpleDateFormat("mm/dd/yyyy")

        val dateString = food.second
        val date = format.parse(dateString)


        val now = Calendar.getInstance()

        val nowDate = format.parse("${now.get(Calendar.MONTH)}/${now.get(Calendar.DAY_OF_MONTH)}/${now.get(Calendar.YEAR)}")

        val days = daysBetween(nowDate, date)

        if (days < 0){
            holder.days_color_view.background = red_circle
        }


        holder.days_tv.text = "$days"


        Log.i("FoodAdapter", "$food")

        holder.food_name_tv.text = food.first
        holder.delete_button.setOnClickListener {
            listener.deleteFood(food.first, food.second)
        }
    }

    private lateinit var listener : OnDeleteClickListener

    interface OnDeleteClickListener{
        fun deleteFood(foodName:String, dateString:String)
    }

    override fun getItemCount(): Int = foodsList.size

    fun daysBetween(d1: Date, d2: Date): Int {
        return ((d2.time - d1.time) / (1000 * 60 * 60 * 24)).toInt()
    }
}