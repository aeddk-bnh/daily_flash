package com.dailyflash.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dailyflash.core.camera.ICameraService
import com.dailyflash.domain.CaptureVideoUseCase
import com.dailyflash.domain.GetCalendarDataUseCase
import com.dailyflash.domain.ExportJournalUseCase
import com.dailyflash.domain.DeleteClipUseCase
import com.dailyflash.domain.GetAllVideosUseCase
import com.dailyflash.presentation.camera.CameraScreen
import com.dailyflash.presentation.camera.CameraViewModel
import com.dailyflash.presentation.calendar.CalendarScreen
import com.dailyflash.presentation.calendar.CalendarViewModel
import com.dailyflash.presentation.export.ExportScreen
import com.dailyflash.presentation.export.ExportViewModel
import com.dailyflash.presentation.gallery.GalleryScreen
import com.dailyflash.presentation.gallery.GalleryViewModel
import com.dailyflash.presentation.gallery.GalleryDetailScreen

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

@Composable
fun NavGraph(
    navController: NavHostController,
    cameraService: ICameraService,
    captureVideoUseCase: CaptureVideoUseCase,
    getCalendarDataUseCase: GetCalendarDataUseCase,
    exportJournalUseCase: ExportJournalUseCase,
    deleteClipUseCase: DeleteClipUseCase,
    getAllVideosUseCase: GetAllVideosUseCase,
    getUserSettingsUseCase: com.dailyflash.domain.settings.GetUserSettingsUseCase,
    updateReminderUseCase: com.dailyflash.domain.settings.UpdateReminderUseCase,
    updateAutoCleanupUseCase: com.dailyflash.domain.settings.UpdateAutoCleanupUseCase
) {
    NavHost(
        navController = navController,
        startDestination = "camera",
        enterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(400)) + slideInHorizontally { it / 3 } },
        exitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(400)) + slideOutHorizontally { -it / 3 } },
        popEnterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(400)) + slideInHorizontally { -it / 3 } },
        popExitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(400)) + slideOutHorizontally { it / 3 } }
    ) {
        composable("camera") {
            val viewModel = CameraViewModel(cameraService, captureVideoUseCase)
            CameraScreen(
                viewModel = viewModel,
                onNavigateToGallery = { navController.navigate("gallery") },
                onNavigateToCalendar = { navController.navigate("calendar") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("settings") {
            val viewModel = com.dailyflash.presentation.settings.SettingsViewModel(
                getUserSettingsUseCase,
                updateReminderUseCase,
                updateAutoCleanupUseCase
            )
            com.dailyflash.presentation.settings.SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("gallery") {
            val viewModel = GalleryViewModel(getAllVideosUseCase, deleteClipUseCase)
            GalleryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { index -> 
                    navController.navigate("gallery_detail/$index")
                }
            )
        }
        
        composable("gallery_detail/{initialIndex}") { backStackEntry ->
            val initialIndex = backStackEntry.arguments?.getString("initialIndex")?.toIntOrNull() ?: 0
            val viewModel = GalleryViewModel(getAllVideosUseCase, deleteClipUseCase)
            GalleryDetailScreen(
                viewModel = viewModel,
                initialIndex = initialIndex,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("calendar") {
            val viewModel = CalendarViewModel(getCalendarDataUseCase)
            CalendarScreen(
                viewModel = viewModel,
                onNavigateToCamera = { navController.popBackStack() },
                onNavigateToExport = { navController.navigate("export") }
            )
        }
        
        composable("export") {
            val viewModel = ExportViewModel(exportJournalUseCase)
            ExportScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
