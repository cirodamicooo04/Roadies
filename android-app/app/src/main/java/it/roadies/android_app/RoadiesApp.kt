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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import it.roadies.android_app.ui.bookingFlow.BookingDocumentsScreen
import it.roadies.android_app.ui.bookingFlow.BookingPaymentScreen
import it.roadies.android_app.ui.bookingFlow.BookingStepMembersScreen
import it.roadies.android_app.ui.bookingFlow.BookingStepPeopleScreen
import it.roadies.android_app.viewmodel.bookingFlow.BookingFlowViewModel
import it.roadies.android_app.ui.bookingHome.BookingDetailScreen
import it.roadies.android_app.ui.bookingHome.BookingHomeScreen
import it.roadies.android_app.ui.chat.ChatListScreen
import it.roadies.android_app.ui.chat.ChatScreen
import it.roadies.android_app.ui.user.EditProfileScreen
import it.roadies.android_app.ui.user.FriendScreen
import it.roadies.android_app.ui.user.OrganizerTravelsScreen
import it.roadies.android_app.ui.user.UserProfileScreen
import it.roadies.android_app.ui.user.UserDocumentScreen
import it.roadies.android_app.viewmodel.AuthViewModel
import java.math.BigDecimal
import java.time.LocalDateTime


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

    val bottomRoutes = listOf("home", "travel", "bookings", "chat", "handle_users", "statistics", "handle_travels")
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
                                    navHostController.navigate("profile_graph") {
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
                                selected = currentDestination?.hierarchy?.any { it.route == "organizer_graph" } == true,
                                onClick = {
                                    navHostController.navigate("organizer_graph") {
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
                isAdmin = "ADMIN" in authState.roles,
                onLoginClick = onLoginClick
            )
        }
    }
}

@Composable
fun NavigationView(navHostController: NavHostController, modifier: Modifier = Modifier, isAdmin: Boolean, onLoginClick: () -> Unit) {
    val startDestination: String = if (isAdmin) "handle_users" else "home_graph"


    NavHost(
        navController = navHostController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        navigation(route = "home_graph", startDestination = "home") {
            composable(route = "home") {
                HomeScreen(navHostController)
            }
            composable(
                route = "search_screen?continent={continent}&country={country}&destination={destination}&minPrice={minPrice}&maxPrice={maxPrice}&minDurationDays={minDurationDays}&maxDurationDays={maxDurationDays}&type={type}",
                arguments = listOf(
                    navArgument("continent") { type = NavType.StringType; nullable = true },
                    navArgument("country") { type = NavType.StringType; nullable = true },
                    navArgument("destination") { type = NavType.StringType; nullable = true },
                    navArgument("minPrice") { type = NavType.StringType; nullable = true },
                    navArgument("maxPrice") { type = NavType.StringType; nullable = true },
                    navArgument("minDurationDays") { type = NavType.StringType; nullable = true },
                    navArgument("maxDurationDays") { type = NavType.StringType; nullable = true },
                    navArgument("type") { type = NavType.StringType; nullable = true }
                )
            ) {
                SearchScreen(navHostController = navHostController)
            }

            composable(route = "travel_detail/{id}") {
                TravelDetailScreen(
                    navHostController = navHostController,
                    onLoginRequest = onLoginClick
                )
            }

            composable(route = "activity_detail/{id}") {
                ActivityDetailScreen(
                    navHostController = navHostController,
                    onLoginRequest = onLoginClick
                )
            }

            composable(
                route = "booking_people/{bookingId}?travelId={travelId}&activityId={activityId}",
                arguments = listOf(
                    navArgument("bookingId") { type = NavType.StringType },
                    navArgument("travelId") {
                        type = NavType.StringType; nullable = true; defaultValue = null
                    },
                    navArgument("activityId") {
                        type = NavType.StringType; nullable = true; defaultValue = null
                    },
                )
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId").orEmpty()
                val travelId = backStackEntry.arguments?.getString("travelId")
                val activityId = backStackEntry.arguments?.getString("activityId")

                BookingStepPeopleScreen(
                    onBack = { navHostController.popBackStack() },
                    onConfirmed = { peopleCount ->
                        navHostController.navigate("booking_members/$bookingId?peopleCount=$peopleCount")
                    }
                )
            }

            composable(
                route = "booking_members/{bookingId}?peopleCount={peopleCount}",
                arguments = listOf(
                    navArgument("bookingId") { type = NavType.StringType },
                    navArgument("peopleCount") { type = NavType.IntType; defaultValue = 1 },
                )
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId").orEmpty()
                val parentEntry = remember(backStackEntry) {
                    navHostController.getBackStackEntry("home_graph")
                }
                val flowViewModel: BookingFlowViewModel = hiltViewModel(parentEntry)

                BookingStepMembersScreen(
                    onBack = { navHostController.popBackStack() },
                    onMembersInserted = { uploadItems ->
                        flowViewModel.setItems(uploadItems)
                        navHostController.navigate("booking_documents/$bookingId")
                    },
                    onExpired = {
                        navHostController.navigate("bookings") {
                            popUpTo("home_graph") { inclusive = true }
                        }
                    },
                    flowViewModel = flowViewModel
                )
            }

            composable(
                route = "booking_documents/{bookingId}",
                arguments = listOf(
                    navArgument("bookingId") { type = NavType.StringType },
                )
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId").orEmpty()
                val parentEntry = remember(backStackEntry) {
                    navHostController.getBackStackEntry("home_graph")
                }
                val flowViewModel: BookingFlowViewModel = hiltViewModel(parentEntry)

                BookingDocumentsScreen(
                    flowViewModel = flowViewModel,
                    onCompleted = {
                        navHostController.navigate("booking_payment/$bookingId")
                    },
                    onExpired = {
                        navHostController.navigate("home") {
                            popUpTo("home_graph") { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "booking_payment/{bookingId}",
                arguments = listOf(
                    navArgument("bookingId") { type = NavType.StringType },
                )
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navHostController.getBackStackEntry("home_graph")
                }
                val flowViewModel: BookingFlowViewModel = hiltViewModel(parentEntry)

                BookingPaymentScreen(
                    onCompleted = {
                        navHostController.navigate("home") {
                            popUpTo("home_graph")
                        }
                    },
                    onExpired = {
                        navHostController.navigate("home") {
                            popUpTo("home_graph") { inclusive = true }
                        }
                    },
                    flowViewModel = flowViewModel
                )
            }


        }


        composable(route = "bookings") {
            BookingHomeScreen(navHostController = navHostController)
        }
        composable(
            route = "booking_detail/{principalId}/{travelName}/{peopleCount}/{totalPrice}/{startDate}/{endDate}/{departureType}",
            arguments = listOf(
                navArgument("principalId") { type = NavType.StringType },
                navArgument("travelName") { type = NavType.StringType },
                navArgument("peopleCount") { type = NavType.IntType },
                navArgument("totalPrice") { type = NavType.StringType },
                navArgument("startDate") { type = NavType.StringType },
                navArgument("endDate") { type = NavType.StringType },
                navArgument("departureType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val principalId = backStackEntry.arguments?.getString("principalId").orEmpty()
            val travelName = backStackEntry.arguments?.getString("travelName").orEmpty()
            val peopleCount = backStackEntry.arguments?.getInt("peopleCount") ?: 0
            val totalPrice = BigDecimal(backStackEntry.arguments?.getString("totalPrice") ?: "0")

            val startDateStr = backStackEntry.arguments?.getString("startDate")
            val endDateStr = backStackEntry.arguments?.getString("endDate")
            val startDate =
                startDateStr?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
                    ?: LocalDateTime.now()
            val endDate = endDateStr?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
                ?: LocalDateTime.now()
            val departureType = backStackEntry.arguments?.getString("departureType").orEmpty()

            BookingDetailScreen(
                travelName = travelName,
                peopleCount = peopleCount,
                startDate = startDate,
                endDate = endDate,
                totalPrice = totalPrice,
                onNavigateToTravel = {
                    if (departureType == "ACTIVITY") {
                        navHostController.navigate("activity_detail/$principalId")
                    } else {
                        navHostController.navigate("travel_detail/$principalId")
                    }
                }
            )
        }
        composable(route = "handle_users") {

        }
        composable(route = "statistics") {

        }

        composable(route = "chat") {
            ChatListScreen(
                onNavigateToChat = { conversationId ->
                    navHostController.navigate("chat/$conversationId")
                }
            )
        }

        composable(
            route = "chat/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId").orEmpty()

            ChatScreen(conversationId = conversationId, onBack = { navHostController.popBackStack() })
        }

        navigation(route = "organizer_graph", startDestination = "handle_travels") {
            composable(route = "handle_travels") {
                OrganizerDashboard(navHostController = navHostController)
            }
            composable(route = "create_travel") {
                TravelCreationScreen(
                    navHostController = navHostController,
                    onNavigateBack = { navHostController.popBackStack() })
            }
            composable(route = "create_activity"){
                ActivityCreationScreen(navHostController = navHostController)
            }
            composable(route = "update_travel/{id}", arguments = listOf(
                navArgument("id") {type = NavType.StringType}
            )
            ){
                TravelUpdateScreen(navHostController = navHostController)
            }
            composable(route="update_activity/{id}", arguments = listOf(
                navArgument("id") {type = NavType.StringType}
            )){
                ActivityUpdateScreen(navHostController = navHostController)
            }
        }

        navigation(route = "profile_graph", startDestination = "profile_main") {
            composable(route = "profile_main") {
                ProfileScreen(
                    onNavigateTo = { rotta ->
                        navHostController.navigate(rotta)
                    }
                )
            }
            composable(route = "edit_profile") {
                EditProfileScreen(onBack = { navHostController.popBackStack() })
            }
            composable(route = "friend") {
                FriendScreen(
                    navController = navHostController,
                    onBack = { navHostController.popBackStack() }
                )
            }

            composable(
                route = "user_profile/{username}",
                arguments = listOf(navArgument("username") { type = NavType.StringType })
            ) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username").orEmpty()
                UserProfileScreen(
                    username = username,
                    onBack = { navHostController.popBackStack() },
                    onNavigateToChat = { conversationId ->
                        navHostController.navigate("chat/$conversationId")
                    },
                    onNavigateToOrganizedTrips = { organizerUsername ->
                        if (!organizerUsername.isNullOrEmpty()) {
                            navHostController.navigate("organizer_travels/$organizerUsername")
                        }
                    }
                )
            }

            composable(
                route="user_documents" ){
                UserDocumentScreen(
                    onBack = { navHostController.popBackStack() }
                )
            }

            composable(route="organizer_travels/{username}",
                arguments = listOf(navArgument("username") {type = NavType.StringType})
            ){
                OrganizerTravelsScreen(navHostController = navHostController)
            }
        }
    }
}