package com.example.coctail.model

import com.google.gson.annotations.SerializedName

data class CocktailResponse(
    val drinks: List<Cocktail>?
)

data class Cocktail(
    val idDrink: String,
    val strDrink: String,
    val strDrinkThumb: String,
    val strInstructions: String

)