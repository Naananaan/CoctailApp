@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.coctail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.coctail.network.RetrofitInstance
import com.example.coctail.ui.theme.CoctailTheme
import com.example.coctail.viewmodel.CocktailViewModel
import com.example.coctail.viewmodel.CocktailViewModelFactory
import com.example.coctail.ui.theme.introText
import java.net.URLEncoder

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
        // Back to Info Button
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(text = "Back to Info", color = MaterialTheme.colorScheme.onPrimary)
        }

        // Search TextField
        TextField(
            value = searchQueryState.value,
            onValueChange = { query -> searchQueryState.value = query },
            label = {
                Text(text = stringResource(id = R.string.search_label), color = MaterialTheme.colorScheme.onBackground)
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
        )

        // Search Button
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

        // Loading Indicator
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(
                text = stringResource(id = R.string.loading_message),
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            // Cocktail List
            LazyColumn {
                items(cocktailList) { cocktail ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                navController.navigate("recipe/${cocktail.strDrink}/${cocktail.strInstructions}")
                            },
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

            // Error Message
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
        composable("recipe/{cocktailName}/{cocktailRecipe}") { backStackEntry ->
            val cocktailName = backStackEntry.arguments?.getString("cocktailName")
            val cocktailRecipe = backStackEntry.arguments?.getString("cocktailRecipe")
            RecipeScreen(cocktailName ?: "", cocktailRecipe ?: "", navController)  // Pass navController here
        }
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

@Composable
fun RecipeScreen(cocktailName: String, cocktailRecipe: String, navController: NavHostController) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { navController.navigate("info") }) {
                Text("Back to Info")
            }
            Button(onClick = { navController.navigate("search") }) {
                Text("Back to Search")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = cocktailName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = cocktailRecipe, fontSize = 16.sp)
    }
}
