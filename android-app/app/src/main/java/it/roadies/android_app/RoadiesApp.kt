package it.roadies.android_app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadiesApp(){
    val navHostController = rememberNavController()

    //Per il parametro selected dei NavigationBarItem
    val navBackStackEntry by navHostController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomRoutes = listOf("home", "travel", "bookings")
    val showBackButton = currentRoute !in bottomRoutes

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Roadies")
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(
                            onClick = {
                                navHostController.popBackStack()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Indietro"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Decidere se aprire una nuova schermata o menu a tendina.
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.profile)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar() {
                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = {
                        navHostController.navigate("home")
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = stringResource(R.string.home)
                        )
                    },
                    label = {
                        Text(stringResource(R.string.home))
                    }
                )
                NavigationBarItem(
                    selected = currentRoute == "travel",
                    onClick = {
                        navHostController.navigate("travel")
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = stringResource(R.string.travel)
                        )
                    },
                    label = {
                        Text(stringResource(R.string.travel))
                    }
                )
                NavigationBarItem(
                    selected = currentRoute == "bookings",
                    onClick = {
                        navHostController.navigate("bookings")
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = stringResource(R.string.bookings)
                        )
                    },
                    label = {
                        Text(stringResource(R.string.bookings))
                    }
                )
                // Aggiungere con if eventuali altri pannelli in base al ruolo.
            }
        },

    ) {
        paddingValues -> NavigationView(navHostController = navHostController, modifier = Modifier.padding(paddingValues))
    }
}

@Composable
fun NavigationView(navHostController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navHostController, startDestination = "home", modifier = modifier) {
        composable(route = "home") {
            //Creare un activity con all'interno la composable desiderata che accetta come parametro un NavHostController
        }
        composable(route ="bookings"){
            //Creare un activity con all'interno la composable desiderata che accetta come parametro un NavHostController
        }
        composable(route = "travel" ){
            SearchTravelActivity( navHostController= navHostController)
        }
    }
}



