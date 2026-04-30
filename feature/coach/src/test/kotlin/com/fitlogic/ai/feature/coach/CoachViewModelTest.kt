package com.fitlogic.ai.feature.coach

import com.fitlogic.ai.core.domain.model.AiInsight
import com.fitlogic.ai.core.domain.model.AiInsightType
import com.fitlogic.ai.core.domain.repository.AiInsightRepository
import com.fitlogic.ai.core.domain.usecase.ai.DetectPlateauUseCase
import com.fitlogic.ai.core.domain.usecase.ai.GenerateWeeklyReportUseCase
import com.fitlogic.ai.core.domain.usecase.ai.MarkAiInsightAsReadUseCase
import com.fitlogic.ai.core.domain.usecase.ai.ObserveAiInsightDetailUseCase
import com.fitlogic.ai.core.domain.usecase.ai.ObserveAiInsightsUseCase
import com.fitlogic.ai.core.domain.usecase.ai.SendCoachMessageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoachViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Test
    fun `should block blank message`() =
        runTest {
            Dispatchers.setMain(dispatcher)
            val repository = FakeAiInsightRepository()
            val viewModel = createViewModel(repository)

            viewModel.onDraftChanged("   ")
            viewModel.sendMessage()

            assertEquals("Lutfen bir mesaj girin.", viewModel.uiState.value.chatError)
            assertTrue(viewModel.uiState.value.chatMessages.isEmpty())
            Dispatchers.resetMain()
        }

    @Test
    fun `should append assistant response and clear sending`() =
        runTest {
            Dispatchers.setMain(dispatcher)
            val repository = FakeAiInsightRepository(nextMessageResult = Result.success("Bugun tempo kontrollu devam et."))
            val viewModel = createViewModel(repository)

            viewModel.onDraftChanged("Bugun ne yapayim?")
            viewModel.sendMessage()
            dispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isSending)
            assertEquals(2, state.chatMessages.size)
            assertEquals(CoachMessageRole.USER, state.chatMessages.first().role)
            assertEquals(CoachMessageRole.ASSISTANT, state.chatMessages.last().role)
            assertEquals("Bugun tempo kontrollu devam et.", state.chatMessages.last().text)
            Dispatchers.resetMain()
        }

    @Test
    fun `should prevent duplicate send while in progress`() =
        runTest {
            Dispatchers.setMain(dispatcher)
            val repository = FakeAiInsightRepository(nextMessageResult = Result.success("Tek cevap"))
            val viewModel = createViewModel(repository)

            viewModel.onDraftChanged("Mesaj 1")
            viewModel.sendMessage()
            viewModel.sendMessage()
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(1, repository.sentMessages.size)
            assertEquals(2, viewModel.uiState.value.chatMessages.size)
            Dispatchers.resetMain()
        }
}

private fun createViewModel(repository: FakeAiInsightRepository): CoachViewModel =
    CoachViewModel(
        observeAiInsightsUseCase = ObserveAiInsightsUseCase(repository),
        observeAiInsightDetailUseCase = ObserveAiInsightDetailUseCase(repository),
        generateWeeklyReportUseCase = GenerateWeeklyReportUseCase(repository),
        detectPlateauUseCase = DetectPlateauUseCase(repository),
        markAiInsightAsReadUseCase = MarkAiInsightAsReadUseCase(repository),
        sendCoachMessageUseCase = SendCoachMessageUseCase(repository),
    )

private class FakeAiInsightRepository(
    private val nextMessageResult: Result<String> = Result.success("Varsayilan yanit"),
) : AiInsightRepository {
    private val insightsFlow = MutableStateFlow<List<AiInsight>>(emptyList())
    val sentMessages = mutableListOf<String>()

    override fun observeActiveInsights(limit: Int): Flow<List<AiInsight>> = insightsFlow

    override fun observeInsightDetail(insightId: String): Flow<AiInsight?> = flowOf(null)

    override suspend fun markInsightAsRead(insightId: String): Result<Unit> = Result.success(Unit)

    override suspend fun generateWeeklyReport(): Result<AiInsight> = Result.success(dummyInsight())

    override suspend fun detectPlateau(): Result<AiInsight?> = Result.success(null)

    override suspend fun getPostWorkoutInsight(workoutId: String): Result<AiInsight> = Result.success(dummyInsight())

    override suspend fun sendCoachMessage(message: String): Result<String> {
        sentMessages += message
        return nextMessageResult
    }

    private fun dummyInsight(): AiInsight =
        AiInsight(
            id = "1",
            userId = "u1",
            type = AiInsightType.WEEKLY_REPORT,
            title = "t",
            body = "b",
            relatedWorkoutId = null,
            createdAt = 1L,
            updatedAt = 1L,
        )
}
