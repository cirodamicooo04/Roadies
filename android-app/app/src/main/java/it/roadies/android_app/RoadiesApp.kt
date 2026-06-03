package it.roadies.android_app

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import it.roadies.android_app.viewmodel.AuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadiesApp(
    authViewModel: AuthViewModel,
    onLoginClick: () -> Unit
){
    val navHostController = rememberNavController()

    //Per il parametro selected dei NavigationBarItem
    val navBackStackEntry by navHostController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomRoutes = listOf("home", "travel", "bookings", "chat", "handle_users", "statistics")
    val showBackButton = currentRoute !in bottomRoutes

    val authState by authViewModel.authState.collectAsState()

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
                    if (!authState.isLogged){
                        Button(
                            onClick = onLoginClick
                        ) {
                            Text(stringResource(R.string.login))
                        }
                    }
                    else {
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
                }
            )
        },
        bottomBar = {
            NavigationBar() {
                if ("ADMIN" in authState.roles) {
                    NavigationBarItem(
                        selected = currentRoute == "handle_users",
                        onClick = {
                            navHostController.navigate("handle_users")
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.PeopleAlt,
                                contentDescription = stringResource(R.string.handle_users)
                            )
                        },
                        label = {
                            Text(stringResource(R.string.handle_users))
                        }
                    )

                    NavigationBarItem(
                        selected = currentRoute == "statistics",
                        onClick = {
                            navHostController.navigate("statistics")
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.QueryStats,
                                contentDescription = stringResource(R.string.statistics)
                            )
                        },
                        label = {
                            Text(stringResource(R.string.statistics))
                        }
                    )

                } else {

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
                    if (authState.isLogged) {
                        NavigationBarItem(
                            selected = currentRoute == "chat",
                            onClick = {
                                navHostController.navigate(("chat"))
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = stringResource(R.string.chat)
                                )
                            },
                            label = {
                                Text(stringResource(R.string.chat))
                            }
                        )
                    }
                    if ("ORGANIZER" in authState.roles){
                        NavigationBarItem(
                            selected = currentRoute == "handle_travels",
                            onClick = {
                                navHostController.navigate(("handle_travels"))
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Luggage,
                                    contentDescription = stringResource(R.string.handle_travels)
                                )
                            },
                            label = {
                                Text(stringResource(R.string.handle_travels))
                            }
                        )
                    }
                }
            }
        }

    ) {
        paddingValues -> NavigationView(navHostController = navHostController, modifier = Modifier.padding(paddingValues), isAdmin = "ADMIN" in authState.roles )
    }
}

@Composable
fun NavigationView(navHostController: NavHostController, modifier: Modifier = Modifier, isAdmin: Boolean) {
    val startDestination: String = if (isAdmin) "handle_users" else "home"

    NavHost(navController = navHostController, startDestination = startDestination, modifier = modifier) {
        composable(route = "home") {
            //Creare un activity con all'interno la composable desiderata che accetta come parametro un NavHostController
        }
        composable(route ="bookings"){
            //Creare un activity con all'interno la composable desiderata che accetta come parametro un NavHostController
        }
        composable(route = "travel" ){
            SearchTravelActivity( navHostController= navHostController)
        }
        composable(route = "handle_users"){

        }
        composable(route = "statistics"){

        }
        composable(route = "chat"){

        }
        composable(route = "handle_travels"){

        }
    }
}
