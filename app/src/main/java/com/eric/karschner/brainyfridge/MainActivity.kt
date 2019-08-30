package com.eric.karschner.brainyfridge

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View
import android.widget.EditText
import khttp.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.content.Intent
import android.net.Uri


private val NUM_PAGES = 2

class MainActivity : AppCompatActivity(),
    AddFoodDialog.AddFoodDialogListener,
    FoodAdapter.OnDeleteClickListener,
    RecipesAdapter.OnRecipeClickListener,
    OnRecipesSwipeRefresh {

    lateinit var mPager: ViewPager
    lateinit var foodFragment: FoodFragment
    lateinit var recipesFragment: RecipesFragment
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mPager = pager
        foodFragment = FoodFragment()
        recipesFragment = RecipesFragment()
        sharedPrefs = getSharedPreferences("shared", Context.MODE_PRIVATE)

        val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        mPager.adapter = pagerAdapter

        refreshRecipes()
    }




    fun onLeftArrowClick(v: View){
        mPager.currentItem = mPager.currentItem - 1
    }

    fun onRightArrowClick(v: View){
        mPager.currentItem = mPager.currentItem + 1
    }

    fun onRefreshClick(v: View){
        recipesFragment.startRefreshingView()
        refreshRecipes()
    }

    fun onAddButtonClick(v: View){
        val dialog = AddFoodDialog()
        dialog.show(supportFragmentManager, "AddFoodDialog")
    }

    override fun deleteFood(foodName: String, dateString: String) {
        Log.i("MainActivity", "Delete $foodName, $dateString")

        var foodsString = sharedPrefs.getString("foods", "")
        foodsString = foodsString.replace("$foodName-$dateString|", "")

        sharedPrefs.edit().run {
            putString("foods", foodsString)
            apply()
        }

        foodFragment.readFoods()
    }

    override fun onAddFoodPositiveClick(dialog: DialogFragment, editText: EditText) {
        val foodName = dialog.dialog.findViewById<EditText>(R.id.food_name_et).text.toString()
        showDatePickerDialog(foodName)
    }

    override fun onAddFoodNegativeClick(dialog: DialogFragment) {
        Log.i("MainActivity", "Cancel")
    }

    private fun showDatePickerDialog(foodName: String){
        val currentDate = Calendar.getInstance()
        val dialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
               writeFoodToStorage(foodName, "$month/$dayOfMonth/$year")},
            currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH))

        dialog.show()
    }

    private fun writeFoodToStorage(foodName:String, dateString:String){
        Log.i("MainActivity", "$foodName, $dateString")
        val prevFoods = sharedPrefs.getString("foods", "")
        with (sharedPrefs.edit()){
            putString("foods", "$foodName-$dateString|$prevFoods")
            apply()
        }

        foodFragment.readFoods()
        refreshRecipes()
    }

    override fun onBackPressed() {
        if (mPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            mPager.currentItem = mPager.currentItem - 1
        }
    }

    override fun onClick(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    override fun onSwipeRefresh() {
        refreshRecipes()
    }

    private fun refreshRecipes(){

        CoroutineScope(Dispatchers.IO).launch {

            delay(750)

            val foods = foodFragment.foodsList.clone() as ArrayList<Pair<String, String>>

            val ingredients = when(foods.size){
                0 -> "chicken"
                1 -> foods[0].first
                else -> "${foods[0].first}+${foods[1].first}"
            }

            val httpAddress = "https://api.edamam.com/search?q=" +
                    ingredients +
                    "&app_id=cb6dbe6f&app_key=cb1f7c127959cb82fd2eb727db3d0be0"

            Log.i("MainActivity", httpAddress)
            val response = get(url = httpAddress)

            Log.i("MainActivity", response.text)

            val recipesJSONArray = response.jsonObject.getJSONArray("hits")
            val recipes = ArrayList<Recipe>()

            for (i in 0 until recipesJSONArray.length()){
                val recipeJSONObject = recipesJSONArray.getJSONObject(i).getJSONObject("recipe")

                val recipe = Recipe(
                    name = recipeJSONObject.getString("label"),
                    urlString = recipeJSONObject.getString("url"),
                    imgURL = recipeJSONObject.getString("image")
                )

                recipes.add(recipe)

                Log.i("RecipesFragment", "recipe: $recipe")
            }

            val job = Job()

            CoroutineScope(Dispatchers.Main + job).launch {
                recipesFragment.endRefreshingView()
                recipesFragment.updateRecipes(recipes)
            }
        }
    }


    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = NUM_PAGES

        override fun getItem(position: Int): Fragment = when(position){
            0 -> foodFragment
            1 -> recipesFragment
            else -> foodFragment
        }
    }
}