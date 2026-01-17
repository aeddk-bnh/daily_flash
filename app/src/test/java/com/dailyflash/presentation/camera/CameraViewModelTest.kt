package com.dailyflash.presentation.camera

import android.net.Uri
import com.dailyflash.core.camera.ICameraService
import com.dailyflash.domain.CaptureVideoUseCase
import com.dailyflash.domain.VideoEntity
import com.dailyflash.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockCameraService: ICameraService = mock()
    private val mockCaptureUseCase: CaptureVideoUseCase = mock()
    
    // SUT
    private val viewModel by lazy { CameraViewModel(mockCameraService, mockCaptureUseCase) }

    @Test
    fun `initial state is Idle`() = runTest {
        assertEquals(CameraUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `startRecording updates state and calls service`() = runTest {
        val uri = mock<Uri>()
        whenever(mockCameraService.recordClip(any())).thenReturn(Result.success(uri))
        whenever(mockCaptureUseCase(any())).thenReturn(
            Result.success(VideoEntity("id", uri, LocalDate.now(), 1000, null))
        )

        viewModel.startRecording()

        verify(mockCameraService).recordClip(1000)
        verify(mockCaptureUseCase).invoke(uri)
        assertTrue(viewModel.uiState.value is CameraUiState.Success)
    }

    @Test
    fun `startRecording handles service failure`() = runTest {
        whenever(mockCameraService.recordClip(any())).thenReturn(Result.failure(RuntimeException("Error")))

        viewModel.startRecording()

        assertTrue(viewModel.uiState.value is CameraUiState.Error)
        assertEquals("Error", (viewModel.uiState.value as CameraUiState.Error).message)
    }
}
