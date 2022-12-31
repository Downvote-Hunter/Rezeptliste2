package com.example.rezeptliste2.database.controller

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.rezeptliste2.MapUtil
import com.example.rezeptliste2.database.Database
import com.example.rezeptliste2.database.dao.RecipeDao
import com.example.rezeptliste2.database.dao.RezeptZutatDao
import com.example.rezeptliste2.database.dao.ZutatDao
import com.example.rezeptliste2.database.dto.Ingredient
import com.example.rezeptliste2.database.dto.Recipe

class RecipeController(context: Context) {
    fun getAllRecipes(): List<Recipe> {
        return rezeptDao.getAll()
    }

    fun getRecipeIngredientsDB(recipe: Recipe): List<Ingredient> {

        return rezeptZutatDao.getRecipeIngredients(recipe.r_id)
    }

    fun getRecipeIngredientsAvailableDB(
        recipe: Recipe, available: Boolean = true
    ): List<Ingredient> {

        return rezeptZutatDao.getRecipeIngredientsAvailable(recipe.r_id, available)
    }


    fun getRecipeIngredientAmountsDB(recipe: Recipe, ingredients: List<Ingredient>): List<String> {

        val amounts: MutableList<String> = mutableListOf()

        for (ingredient in ingredients) {
            amounts += rezeptZutatDao.getRecipeIngredientAmount(recipe.r_id, ingredient.z_id)
                ?: "not defined"
        }

        return amounts
    }

    private val recipeIngredientController: RecipeIngredientController =
        RecipeIngredientController(context)

    private val ingredientController: IngredientController = IngredientController(context)

    private fun deleteIngredientFromRecipeDB(recipeID: Int, ingredientID: Int) {
        val recipeIngredient = recipeIngredientController.getByID(ingredientID, recipeID)
        recipeIngredientController.delete(recipeIngredient)
    }

    private fun updateIngredientDB(recipeID: Int, ingredientID: Int, amount: String) {
        val recipeIngredient = recipeIngredientController.getByID(ingredientID, recipeID)
        recipeIngredient.menge = amount
        recipeIngredientController.update(recipeIngredient)
    }

    fun updateRecipeIngredientsDB(recipe: Recipe, recipeIngredientsAmount: MapUtil) {

        rezeptDao.update(recipe)

        removeDeletedIngredientsFromRecipe(recipe, recipeIngredientsAmount)

        addMissingIngredientsToDB(recipeIngredientsAmount)

        addMissingIngredientsToRecipe(recipe, recipeIngredientsAmount)

        updateExistingIngredients(recipe, recipeIngredientsAmount)

    }

    private fun updateExistingIngredients(recipe: Recipe, recipeIngredientsAmount: MapUtil) {

        getRecipeIngredientsDB(recipe).forEach {
            if (isIngredientInRecipe(
                    recipe, it
                ) && recipeIngredientsAmount.getValue(it) != "not defined"
            ) {
                updateIngredientDB(recipe.r_id, it.z_id, recipeIngredientsAmount.getValue(it))
            }
        }
    }

    private fun addMissingIngredientsToRecipe(recipe: Recipe, recipeIngredientsAmount: MapUtil) {

        recipeIngredientsAmount.getKeys().forEach {

            val newIngredient = ingredientController.getByName(it.name)

            if ((newIngredient != null) && !isIngredientInRecipe(recipe, newIngredient)) {

                recipeIngredientController.insert(
                    newIngredient.z_id, recipe.r_id, recipeIngredientsAmount.getValue(it)
                )

                Log.i(
                    "RecipeController",
                    "inserting recipeIngredient: ${newIngredient.z_id}, ${recipe.r_id}, ${
                        recipeIngredientsAmount.getValue(
                            it
                        )
                    }"
                )

            }
        }
    }

    private fun isIngredientInRecipe(recipe: Recipe, newIngredient: Ingredient): Boolean {
        return getRecipeIngredientsDB(recipe).contains(newIngredient)
    }

    private fun addMissingIngredientsToDB(recipeIngredientsAmount: MapUtil) {
        recipeIngredientsAmount.getKeys().forEach {
            if (!isIngredientInDB(it)) {
                Log.i("RecipeController", "inserting ingredient $it")
                ingredientController.insert(it)
            }
        }
    }

    private fun isIngredientInDB(ingredient: Ingredient): Boolean {
        return ingredientController.getAllIngredients().contains(ingredient)
    }

    private fun removeDeletedIngredientsFromRecipe(
        recipe: Recipe, recipeIngredientsAmount: MapUtil
    ) {
        getRecipeIngredientsDB(recipe).forEach {
            if (!isIngredientInList(
                    recipeIngredientsAmount, it,
                )
            ) {
                deleteIngredientFromRecipeDB(recipe.r_id, it.z_id)
            }
        }
    }

    private fun isIngredientInList(
        recipeIngredientsAmount: MapUtil, ingredient: Ingredient
    ) = recipeIngredientsAmount.containsKey(ingredient)


    private var zutatDao: ZutatDao
    private var rezeptZutatDao: RezeptZutatDao
    private var rezeptDao: RecipeDao
    private var db: Database

    init {
        this.db = Room.databaseBuilder(context, Database::class.java, "Rezeptliste.db")
            .createFromAsset("Database/Rezeptliste.db").allowMainThreadQueries().build()
        rezeptDao = db.rezeptDao()
        zutatDao = db.zutatDao()
        rezeptZutatDao = db.rezeptZutatDao()
    }


}