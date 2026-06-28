package it.roadies.android_app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AssignmentTurnedIn
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import it.roadies.android_app.viewmodel.AuthViewModel
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadiesApp(
    authViewModel: AuthViewModel,
    onLoginClick: () -> Unit
){
    val navHostController = rememberNavController()

    //Per il parametro selected dei NavigationBarItem
    val navBackStackEntry by navHostController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val bottomRoutes = listOf("home", "home_graph", "travel", "bookings", "chat", "handle_users", "statistics", "handle_travels")
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
                    if (!authState.isLoading) {
                        if (!authState.isLogged) {
                            Button(
                                onClick = onLoginClick
                            ) {
                                Text(stringResource(R.string.login))
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    navHostController.navigate("profile") {
                                        popUpTo(navHostController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = stringResource(R.string.profile)
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!authState.isLoading) {
                NavigationBar() {
                    if ("ADMIN" in authState.roles) {
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == "handle_users" } == true,
                            onClick = {
                                navHostController.navigate("handle_users") {
                                    popUpTo(navHostController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
                            selected = currentDestination?.hierarchy?.any { it.route == "statistics" } == true,
                            onClick = {
                                navHostController.navigate("statistics") {
                                    popUpTo(navHostController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
                            selected = currentDestination?.hierarchy?.any { it.route == "home_graph" } == true,
                            onClick = {
                                navHostController.navigate("home_graph") {
                                    popUpTo(navHostController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
//                    NavigationBarItem(
//                        selected = currentRoute == "travel",
//                        onClick = {
//                            navHostController.navigate("travel")
//                        },
//                        icon = {
//                            Icon(
//                                imageVector = Icons.Default.FlightTakeoff,
//                                contentDescription = stringResource(R.string.travel)
//                            )
//                        },
//                        label = {
//                            Text(stringResource(R.string.travel))
//                        }
//                    )
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == "bookings" } == true,
                            onClick = {
                                navHostController.navigate("bookings") {
                                    popUpTo(navHostController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
                                selected = currentDestination?.hierarchy?.any { it.route == "chat" } == true,
                                onClick = {
                                    navHostController.navigate("chat") {
                                        popUpTo(navHostController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
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
                        if ("ORGANIZER" in authState.roles) {
                            NavigationBarItem(
                                selected = currentDestination?.hierarchy?.any { it.route == "handle_travels" } == true,
                                onClick = {
                                    navHostController.navigate("handle_travels") {
                                        popUpTo(navHostController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
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
        }

    ) {
        paddingValues ->
        if (!authState.isLoading) {
            NavigationView(
                navHostController = navHostController,
                modifier = Modifier.padding(paddingValues),
                isAdmin = "ADMIN" in authState.roles
            )
        }
    }
}

@Composable
fun NavigationView(navHostController: NavHostController, modifier: Modifier = Modifier, isAdmin: Boolean) {
    val startDestination: String = if (isAdmin) "handle_users" else "home_graph"


    NavHost(navController = navHostController, startDestination = startDestination, modifier = modifier) {
        navigation(route="home_graph", startDestination="home"){
            composable(route = "home") {
                HomeScreen(navHostController)
            }
            composable(route="search_screen?continent={continent}&country={country}&destination={destination}&minPrice={minPrice}&maxPrice={maxPrice}&minDurationDays={minDurationDays}&maxDurationDays={maxDurationDays}&type={type}",
                arguments = listOf(
                    navArgument("continent") {type = NavType.StringType; nullable=true},
                    navArgument("country") {type = NavType.StringType; nullable=true},
                    navArgument("destination") {type = NavType.StringType; nullable=true},
                    navArgument("minPrice") {type = NavType.StringType; nullable=true},
                    navArgument("maxPrice") {type = NavType.StringType; nullable=true},
                    navArgument("minDurationDays") {type = NavType.StringType; nullable=true},
                    navArgument("maxDurationDays") {type = NavType.StringType; nullable=true},
                    navArgument("type") {type = NavType.StringType; nullable=true}
                )
            ) {
               SearchScreen(navHostController = navHostController)
            }

            composable(route="travel_detail/{id}"){
                backStackEntry -> val id = UUID.fromString(backStackEntry.arguments?.getString("id").orEmpty())
                TravelDetailScreen(navHostController=navHostController ,id = id)
            }

            composable(route="activity_detail/{id}"){
                backStackEntry -> val id = UUID.fromString(backStackEntry.arguments?.getString("id").orEmpty())
                ActivityDetailScreen(navHostController=navHostController ,id = id)
            }



        }


        composable(route ="bookings"){
            //Creare un activity con all'interno la composable desiderata che accetta come parametro un NavHostController
        }
        composable(route = "handle_users"){

        }
        composable(route = "statistics"){

        }
        composable(route = "chat"){

        }
        composable(route = "handle_travels"){

        }
        composable(route = "profile") {
            ProfileScreen()
        }
    }
}
