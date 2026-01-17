package com.dailyflash.presentation.settings

import com.dailyflash.domain.settings.GetUserSettingsUseCase
import com.dailyflash.domain.settings.UpdateAutoCleanupUseCase
import com.dailyflash.domain.settings.UpdateReminderUseCase
import com.dailyflash.domain.settings.UserSettings
import com.dailyflash.util.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalTime

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getUserSettingsUseCase: GetUserSettingsUseCase = mock()
    private val updateReminderUseCase: UpdateReminderUseCase = mock()
    private val updateAutoCleanupUseCase: UpdateAutoCleanupUseCase = mock()

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        whenever(getUserSettingsUseCase()).thenReturn(flowOf(UserSettings()))
        viewModel = SettingsViewModel(
            getUserSettingsUseCase,
            updateReminderUseCase,
            updateAutoCleanupUseCase
        )
    }

    @Test
    fun `toggle reminder updates usecase`() = runTest {
        viewModel.onReminderToggle(true)
        verify(updateReminderUseCase).invoke(true, LocalTime.of(20, 0))
    }

    @Test
    fun `update time updates settings`() = runTest {
        val newTime = LocalTime.of(10, 30)
        viewModel.onReminderTimeChange(newTime)
        verify(updateReminderUseCase).invoke(any(), org.mockito.kotlin.eq(newTime)) 
    }

    @Test
    fun `toggle cleanup updates usecase`() = runTest {
        viewModel.onAutoCleanupToggle(true)
        verify(updateAutoCleanupUseCase).invoke(true, 30) // Default days is 30
    }
}
