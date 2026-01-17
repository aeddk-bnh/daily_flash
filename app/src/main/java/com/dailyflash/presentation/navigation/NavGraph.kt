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

@Composable
fun NavGraph(
    navController: NavHostController,
    cameraService: ICameraService,
    captureVideoUseCase: CaptureVideoUseCase,
    getCalendarDataUseCase: GetCalendarDataUseCase,
    exportJournalUseCase: ExportJournalUseCase,
    deleteClipUseCase: DeleteClipUseCase,
    getAllVideosUseCase: GetAllVideosUseCase
) {
    NavHost(
        navController = navController,
        startDestination = "camera"
    ) {
        composable("camera") {
            val viewModel = CameraViewModel(cameraService, captureVideoUseCase)
            CameraScreen(
                viewModel = viewModel,
                onNavigateToGallery = { navController.navigate("gallery") },
                onNavigateToCalendar = { navController.navigate("calendar") }
            )
        }

        composable("gallery") {
            val viewModel = GalleryViewModel(getAllVideosUseCase)
            GalleryScreen(
                viewModel = viewModel,
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
