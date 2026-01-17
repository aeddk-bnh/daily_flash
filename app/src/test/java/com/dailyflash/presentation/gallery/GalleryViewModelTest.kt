package com.dailyflash.presentation.gallery

import com.dailyflash.domain.DeleteClipUseCase
import com.dailyflash.domain.GetAllVideosUseCase
import com.dailyflash.domain.VideoEntity
import com.dailyflash.util.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import android.net.Uri
import java.time.LocalDate

class GalleryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getAllVideosUseCase: GetAllVideosUseCase = mock()
    private val deleteClipUseCase: DeleteClipUseCase = mock()

    private lateinit var viewModel: GalleryViewModel

    @Test
    fun `load videos success`() = runTest {
        val uri = mock<Uri>()
        val video = VideoEntity("1", uri, LocalDate.now(), 1000)
        whenever(getAllVideosUseCase()).thenReturn(flowOf(listOf(video)))

        viewModel = GalleryViewModel(getAllVideosUseCase, deleteClipUseCase)

        val state = viewModel.uiState.value
        assertTrue(state is GalleryUiState.Success)
        assertTrue((state as GalleryUiState.Success).videos.contains(video))
    }

    @Test
    fun `delete video calls usecase`() = runTest {
        val uri = mock<Uri>()
        val video = VideoEntity("1", uri, LocalDate.now(), 1000)
        whenever(getAllVideosUseCase()).thenReturn(flowOf(listOf(video)))
        viewModel = GalleryViewModel(getAllVideosUseCase, deleteClipUseCase)

        viewModel.deleteVideo(video)
        verify(deleteClipUseCase).invoke("1")
    }
}
