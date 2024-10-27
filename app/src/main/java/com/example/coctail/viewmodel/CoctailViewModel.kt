package com.example.coctail.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coctail.model.Cocktail
import com.example.coctail.model.CocktailResponse
import com.example.coctail.network.CocktailApiService
import kotlinx.coroutines.launch
import retrofit2.Response

class CocktailViewModel(private val api: CocktailApiService) : ViewModel() {

    private val _cocktailList = MutableLiveData<List<Cocktail>>()
    val cocktailList: LiveData<List<Cocktail>> get() = _cocktailList

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun searchCocktails(query: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response: Response<CocktailResponse> = api.searchCocktails(query)
                if (response.isSuccessful && response.body()?.drinks != null) {
                    _cocktailList.value = response.body()?.drinks ?: emptyList()
                    _errorMessage.value = null
                } else {
                    _cocktailList.value = emptyList()
                    _errorMessage.value = "No cocktails found."
                }
            } catch (e: Exception) {
                _cocktailList.value = emptyList()
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}
