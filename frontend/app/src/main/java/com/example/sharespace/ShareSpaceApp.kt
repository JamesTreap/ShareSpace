package com.example.sharespace

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sharespace.auth.ui.SignupScreen
import com.example.sharespace.room.ui.AddRoommateScreen
import com.example.sharespace.room.ui.roomSummary.RoomSummaryScreen
import com.example.sharespace.ui.screens.auth.LoginScreen
import com.example.sharespace.ui.screens.finance.AddBillScreen
import com.example.sharespace.ui.screens.finance.BillsListScreen
import com.example.sharespace.ui.screens.finance.EditBillScreen
import com.example.sharespace.ui.screens.finance.FinanceManagerScreen
import com.example.sharespace.ui.screens.profile.MainProfileScreen
import com.example.sharespace.ui.screens.room.CreateRoomScreen
import com.example.sharespace.ui.screens.room.EditRoomScreen
import com.example.sharespace.ui.screens.room.EditRoommateScreen
import com.example.sharespace.ui.screens.tasks.AddTaskScreen
import com.example.sharespace.ui.screens.tasks.EditTaskScreen
import com.example.sharespace.ui.screens.tasks.TasksListScreen
import com.example.sharespace.user.ui.EditProfileScreen
import com.example.sharespace.user.ui.ViewProfileScreen

enum class ShareSpaceScreens(@StringRes val title: Int) {
    Login(title = R.string.login_screen),
    Signup(title = R.string.signup_screen),
    MainProfile(title = R.string.main_profile_screen),
    EditProfile(title = R.string.edit_profile_screen),
    ViewProfile(title = R.string.view_profile_screen),
    RoomSummary(title = R.string.room_summary_screen),
    AddRoommate(title = R.string.add_roommate_screen),
    EditRoommate(title = R.string.edit_roommate_screen),
    CreateRoom(title = R.string.create_room_screen),
    EditRoom(title = R.string.edit_room_screen),
    BillsList(title = R.string.bills_list_screen),
    AddBill(title = R.string.add_bill_screen),
    EditBill(title = R.string.edit_bill_screen),
    FinanceManager(title = R.string.finance_manager_screen),
    TasksList(title = R.string.tasks_list_screen),
    AddTask(title = R.string.add_task_screen),
    EditTask(title = R.string.edit_task_screen),
}

@Composable
fun ShareSpaceApp(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier,
    ) {
        composable(route = ShareSpaceScreens.Login.name) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(ShareSpaceScreens.MainProfile.name) },
                onSignupClick = { navController.navigate(ShareSpaceScreens.Signup.name) }
            )
        }
        composable(route = ShareSpaceScreens.Signup.name) {
            SignupScreen(
                onSignupSuccess = { navController.navigate(ShareSpaceScreens.MainProfile.name) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = ShareSpaceScreens.MainProfile.name) {
            MainProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onCreateRoomClick = { navController.navigate(ShareSpaceScreens.CreateRoom.name) },
                onNavigateToRoom = {
                    navController.navigate(ShareSpaceScreens.RoomSummary.name)
                },
                onLogOut = {
                    navController.navigate(ShareSpaceScreens.Login.name) {
                        popUpTo(ShareSpaceScreens.MainProfile.name) { inclusive = true }
                    }
                },
                onViewProfileClick = {
                    navController.navigate(ShareSpaceScreens.ViewProfile.name)
                },
                onFinanceManagerClick = { // Add this line
                    navController.navigate(ShareSpaceScreens.FinanceManager.name)
                }
            )
        }
        composable(route = ShareSpaceScreens.EditProfile.name) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = ShareSpaceScreens.ViewProfile.name) {
            ViewProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(ShareSpaceScreens.EditProfile.name) },
                onLogout = {
                    navController.navigate(ShareSpaceScreens.Login.name) {
                        popUpTo(ShareSpaceScreens.MainProfile.name) { inclusive = true }
                    }
                },
            )
        }
        composable(route = ShareSpaceScreens.RoomSummary.name) {
            RoomSummaryScreen(
                onAddRoommateClick = { navController.navigate(ShareSpaceScreens.AddRoommate.name) },
                onAddTaskClick = { navController.navigate(ShareSpaceScreens.AddTask.name) },
                onViewTasksClick = { navController.navigate(ShareSpaceScreens.TasksList.name) },
                onFinanceManagerClick = { navController.navigate(ShareSpaceScreens.FinanceManager.name) },
                onNavigateBack = { navController.popBackStack() },
                onAddBillClick = { navController.navigate(ShareSpaceScreens.AddBill.name) },
                onEditClick = { navController.navigate(ShareSpaceScreens.EditRoom.name) },
            )
        }
        composable(route = ShareSpaceScreens.AddRoommate.name) {
            AddRoommateScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = ShareSpaceScreens.EditRoommate.name) {
            EditRoommateScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = ShareSpaceScreens.CreateRoom.name) {
            CreateRoomScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = ShareSpaceScreens.EditRoom.name) {
            EditRoomScreen(
                onNavigateBack = { navController.popBackStack() },
                onUpdateSuccessAndNavigateBack = { navController.popBackStack() },
            )
        }
        composable(route = ShareSpaceScreens.BillsList.name) {
            BillsListScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddBillClick = { navController.navigate(ShareSpaceScreens.AddBill.name) },
                onFinanceManagerClick = { navController.navigate(ShareSpaceScreens.FinanceManager.name) }
            )
        }
        composable(route = ShareSpaceScreens.FinanceManager.name) {
            FinanceManagerScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddBillClick = { navController.navigate(ShareSpaceScreens.AddBill.name) }
            )
        }
        composable(route = ShareSpaceScreens.AddBill.name) {
            AddBillScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = ShareSpaceScreens.EditBill.name) {
            EditBillScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = ShareSpaceScreens.TasksList.name) {
            TasksListScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddTaskClick = { navController.navigate(ShareSpaceScreens.AddTask.name) },
                onEditTaskClick = { taskId ->
                    navController.navigate("${ShareSpaceScreens.EditTask.name}/$taskId")
                }
            )
        }
        composable(route = ShareSpaceScreens.AddTask.name) {
            AddTaskScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = ShareSpaceScreens.EditTask.name + "/{taskId}"
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: -1
            EditTaskScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}