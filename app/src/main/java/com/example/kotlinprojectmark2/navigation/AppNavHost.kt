package com.example.kotlinprojectmark2.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kotlinprojectmark2.ui.EntryDetailScreen
import com.example.kotlinprojectmark2.ui.screens.MainListScreen
import com.example.kotlinprojectmark2.util.ARG_ID
import com.example.kotlinprojectmark2.util.ROUTE_DETAIL
import com.example.kotlinprojectmark2.util.ROUTE_LIST

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = ROUTE_LIST) {
        composable(ROUTE_LIST) {
            MainListScreen(onOpenDetail = { id -> nav.navigate("$ROUTE_DETAIL/$id") })
        }
        composable(
            route = "$ROUTE_DETAIL/{$ARG_ID}",
            arguments = listOf(navArgument(ARG_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt(ARG_ID) ?: -1
            EntryDetailScreen(entryId = id, onNavigateBack = { nav.popBackStack() })
        }
    }
}
