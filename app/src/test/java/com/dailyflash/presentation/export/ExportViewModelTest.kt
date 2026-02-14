package com.dailyflash.presentation.export

import com.dailyflash.domain.ExportJournalUseCase
import com.dailyflash.domain.ExportOptions
import com.dailyflash.domain.ExportProgress
import com.dailyflash.util.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

class ExportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val exportJournalUseCase: ExportJournalUseCase = mock()
    private lateinit var viewModel: ExportViewModel

    @Before
    fun setup() {
        viewModel = ExportViewModel(exportJournalUseCase)
    }

    @Test
    fun `initial state defaults`() {
        val state = viewModel.uiState.value
        assertTrue(state is ExportUiState.Idle)
        val idle = state as ExportUiState.Idle
        assertEquals(LocalDate.now(), idle.endDate)
    }

    @Test
    fun `toggle overlay updates state`() {
        viewModel.toggleDateOverlay(true)
        val state = viewModel.uiState.value as ExportUiState.Idle
        assertTrue(state.includeDateOverlay)
    }

    @Test
    fun `start export calls usecase with options`() = runTest {
        whenever(exportJournalUseCase(any(), anyOrNull(), any())).thenReturn(flowOf(ExportProgress.Idle))
        
        viewModel.toggleDateOverlay(true)
        viewModel.startExport()
        
        verify(exportJournalUseCase).invoke(any(), anyOrNull(), any())
    }

    @Test
    fun `start export builds range based dateText when overlay enabled`() = runTest {
        whenever(exportJournalUseCase(any(), anyOrNull(), any())).thenReturn(flowOf(ExportProgress.Idle))

        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 1, 31)
        val expectedRange = start..end
        val optionsCaptor = argumentCaptor<ExportOptions>()

        viewModel.updateDateRange(start, end)
        viewModel.toggleDateOverlay(true)
        viewModel.startExport()

        verify(exportJournalUseCase).invoke(eq(expectedRange), anyOrNull(), optionsCaptor.capture())
        assertTrue(optionsCaptor.firstValue.includeDateOverlay)
        assertEquals("2026-01-01 - 2026-01-31", optionsCaptor.firstValue.dateText)
    }

    @Test
    fun `start export keeps dateText null when overlay disabled`() = runTest {
        whenever(exportJournalUseCase(any(), anyOrNull(), any())).thenReturn(flowOf(ExportProgress.Idle))

        val start = LocalDate.of(2026, 2, 1)
        val end = LocalDate.of(2026, 2, 14)
        val expectedRange = start..end
        val optionsCaptor = argumentCaptor<ExportOptions>()

        viewModel.updateDateRange(start, end)
        viewModel.toggleDateOverlay(false)
        viewModel.startExport()

        verify(exportJournalUseCase).invoke(eq(expectedRange), anyOrNull(), optionsCaptor.capture())
        assertTrue(!optionsCaptor.firstValue.includeDateOverlay)
        assertNull(optionsCaptor.firstValue.dateText)
    }
}
