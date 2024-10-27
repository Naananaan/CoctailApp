@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.coctail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.coctail.network.RetrofitInstance
import com.example.coctail.ui.theme.CoctailTheme
import com.example.coctail.viewmodel.CocktailViewModel
import com.example.coctail.viewmodel.CocktailViewModelFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import coil.compose.rememberImagePainter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import coil.compose.rememberAsyncImagePainter
import com.example.coctail.ui.theme.introText


class MainActivity : ComponentActivity() {
    private lateinit var cocktailViewModel: CocktailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cocktailViewModel = ViewModelProvider(
            this,
            CocktailViewModelFactory(RetrofitInstance.api)
        ).get(CocktailViewModel::class.java)

        setContent {
            CoctailTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        viewModel = cocktailViewModel
                    )
                }
            }
        }
    }
}


@Composable
fun CocktailSearchScreen(navController: NavHostController, viewModel: CocktailViewModel) {
    val searchQueryState = remember { mutableStateOf("") }
    val cocktailList by viewModel.cocktailList.observeAsState(emptyList())
    val errorMessage by viewModel.errorMessage.observeAsState()
    val loading by viewModel.loading.observeAsState(false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(text = "Back to Info", color = MaterialTheme.colorScheme.onPrimary)
        }

        TextField(
            value = searchQueryState.value,
            onValueChange = { query -> searchQueryState.value = query },
            label = { Text(text = stringResource(id = R.string.search_label), color = MaterialTheme.colorScheme.onBackground) },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        Button(
            onClick = {
                if (searchQueryState.value.isNotBlank()) {
                    viewModel.searchCocktails(searchQueryState.value)
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(text = stringResource(id = R.string.search_button), color = MaterialTheme.colorScheme.onPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(
                text = stringResource(id = R.string.loading_message),
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            LazyColumn {
                items(cocktailList) { cocktail ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(cocktail.strDrinkThumb),
                            contentDescription = cocktail.strDrink,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = cocktail.strDrink, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }

            errorMessage?.let {
                Text(
                    text = stringResource(id = R.string.error_message, it),
                    color = Color.Red
                )
            }
        }
    }
}


@Composable
fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier,
    viewModel: CocktailViewModel
) {
    NavHost(navController = navController, startDestination = "info") {
        composable("search") { CocktailSearchScreen(navController, viewModel) }
        composable("info") { InfoScreen(navController) }
    }
}

@Composable
fun InfoScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = introText,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Button
        Button(onClick = { navController.navigate("search") }) {
            Text("Go to Search Page", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}