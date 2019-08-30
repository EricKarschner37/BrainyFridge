package com.eric.karschner.brainyfridge

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_recipes.*

class RecipesFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        try {
            listener = context as OnRecipesSwipeRefresh
        } catch (e: Exception) {
            throw java.lang.ClassCastException(("$context must implement OnRecipeClickListener"))
        }

        return inflater.inflate(R.layout.fragment_recipes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        recipes_srl.setOnRefreshListener {
            listener.onSwipeRefresh()
        }

        recipes_srl.isRefreshing = true

        recipes_rv.layoutManager = GridLayoutManager(context, 2)
    }

    fun updateRecipes(recipes: ArrayList<Recipe>){
        recipes_rv.adapter = RecipesAdapter(recipes)
    }

    fun startRefreshingView(){
        recipes_srl.isRefreshing = true
    }

    fun endRefreshingView(){
        recipes_srl.isRefreshing = false
    }


}

data class Recipe(val name:String, val urlString: String, val imgURL: String)

private lateinit var listener : OnRecipesSwipeRefresh

interface OnRecipesSwipeRefresh{
    fun onSwipeRefresh()
}