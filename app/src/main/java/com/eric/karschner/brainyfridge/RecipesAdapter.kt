package com.eric.karschner.brainyfridge

import android.support.v7.widget.RecyclerView
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList




class RecipesAdapter(private val recipesList: ArrayList<Recipe>) :
    RecyclerView.Adapter<RecipesAdapter.ViewHolder>(){

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recipe_iv = view.findViewById<ImageView>(R.id.recipe_iv)
        val name_tv = view.findViewById<TextView>(R.id.name_tv)
        val root = view.findViewById<View>(R.id.recipe_root)
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {

        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.recipe_list_item, parent, false)

        try {
            listener = context as OnRecipeClickListener
        } catch (e:ClassCastException) {
            throw java.lang.ClassCastException(("$context must implement OnRecipeClickListener"))
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val recipe = recipesList[position]

        Picasso.get().load(recipe.imgURL).into(holder.recipe_iv)

        holder.name_tv.text = recipe.name
        holder.root.setOnClickListener { listener.onClick(recipe.urlString) }
    }

    private lateinit var listener : OnRecipeClickListener

    interface OnRecipeClickListener{
        fun onClick(url:String)
    }

    override fun getItemCount(): Int = recipesList.size

}