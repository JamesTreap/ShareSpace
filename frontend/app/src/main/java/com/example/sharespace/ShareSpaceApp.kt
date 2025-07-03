package com.example.sharespace


import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sharespace.ui.screens.bills.AddBillScreen
import com.example.sharespace.ui.screens.bills.BillsListScreen
import com.example.sharespace.ui.screens.bills.EditBillScreen
import com.example.sharespace.ui.screens.profile.EditProfileScreen
import com.example.sharespace.ui.screens.room.AddRoommateScreen
import com.example.sharespace.ui.screens.room.CreateRoomScreen
import com.example.sharespace.ui.screens.room.EditRoomScreen
import com.example.sharespace.ui.screens.room.EditRoommateScreen
import com.example.sharespace.ui.screens.room.HomeOverviewScreen
import com.example.sharespace.ui.screens.room.RoomSummaryScreen
import com.example.sharespace.ui.screens.tasks.AddTaskScreen
import com.example.sharespace.ui.screens.tasks.EditTaskScreen
import com.example.sharespace.ui.screens.tasks.TasksListScreen

enum class ShareSpaceScreens(@StringRes val title: Int) {
    Login(title = R.string.login_screen),
    HomeOverview(title = R.string.home_overview_screen),
    EditProfile(title = R.string.edit_profile_screen),
    RoomSummary(title = R.string.room_summary_screen),
    AddRoommate(title = R.string.add_roommate_screen),
    EditRoommate(title = R.string.edit_roommate_screen),
    CreateRoom(title = R.string.create_room_screen),
    EditRoom(title = R.string.edit_room_screen),
    BillsList(title = R.string.bills_list_screen),
    AddBill(title = R.string.add_bill_screen),
    EditBill(title = R.string.edit_bill_screen),
    TasksList(title = R.string.tasks_list_screen),
    AddTask(title = R.string.add_task_screen),
    EditTask(title = R.string.edit_task_screen),
}

@Composable
fun ShareSpaceApp(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = ShareSpaceScreens.HomeOverview.name,
        modifier = Modifier,
    ) {
        composable(route = ShareSpaceScreens.HomeOverview.name) {
            HomeOverviewScreen(
                onUserProfileClick = { navController.navigate(ShareSpaceScreens.EditProfile.name) },
                onCreateRoomClick = { navController.navigate(ShareSpaceScreens.CreateRoom.name) },
                onRoomClick = { navController.navigate(ShareSpaceScreens.RoomSummary.name) })
        }
        composable(route = ShareSpaceScreens.EditProfile.name) {
            EditProfileScreen(onCreateRoomClick = { navController.navigate(ShareSpaceScreens.CreateRoom.name)})
        }
        composable(route = ShareSpaceScreens.RoomSummary.name) {
            RoomSummaryScreen(
                onViewBillsClick = { navController.navigate(ShareSpaceScreens.BillsList.name) },
                onAddRoommateClick = { navController.navigate(ShareSpaceScreens.AddRoommate.name) },
                onAddTaskClick = { navController.navigate(ShareSpaceScreens.AddTask.name) },
                onViewTasksClick = { navController.navigate(ShareSpaceScreens.TasksList.name) },
            )
        }
        composable(route = ShareSpaceScreens.AddRoommate.name) {
            AddRoommateScreen()
        }
        composable(route = ShareSpaceScreens.EditRoommate.name) {
            EditRoommateScreen()
        }
        composable(route = ShareSpaceScreens.CreateRoom.name) {
            CreateRoomScreen()
        }
        composable(route = ShareSpaceScreens.EditRoom.name) {
            EditRoomScreen()
        }
        composable(route = ShareSpaceScreens.BillsList.name) {
            BillsListScreen(onAddBillClick = { navController.navigate(ShareSpaceScreens.AddBill.name) })
        }
        composable(route = ShareSpaceScreens.AddBill.name) {
            AddBillScreen()
        }
        composable(route = ShareSpaceScreens.EditBill.name) {
            EditBillScreen()
        }
        composable(route = ShareSpaceScreens.TasksList.name) {
            TasksListScreen(onAddTaskClick = { navController.navigate(ShareSpaceScreens.AddTask.name) })
        }
        composable(route = ShareSpaceScreens.AddTask.name) {
            AddTaskScreen()
        }
        composable(route = ShareSpaceScreens.EditTask.name) {
            EditTaskScreen()
        }
    }


}