package co.nimblehq.sample.compose.ui.screens.home

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.GrantPermissionRule
import co.nimblehq.sample.compose.R
import co.nimblehq.sample.compose.domain.model.Model
import co.nimblehq.sample.compose.domain.usecase.*
import co.nimblehq.sample.compose.ui.AppDestination
import co.nimblehq.sample.compose.ui.screens.BaseScreenTest
import co.nimblehq.sample.compose.ui.screens.MainActivity
import co.nimblehq.sample.compose.ui.theme.ComposeTheme
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class HomeScreenTest : BaseScreenTest() {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    /**
     * More test samples with Runtime Permissions https://alexzh.com/ui-testing-of-android-runtime-permissions/
     */
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.CAMERA
    )

    private val mockGetModelsUseCase: GetModelsUseCase = mockk()
    private val mockIsFirstTimeLaunchPreferencesUseCase: IsFirstTimeLaunchPreferencesUseCase =
        mockk()
    private val mockUpdateFirstTimeLaunchPreferencesUseCase: UpdateFirstTimeLaunchPreferencesUseCase =
        mockk()

    private lateinit var viewModel: HomeViewModel
    private var expectedAppDestination: AppDestination? = null

    @Before
    fun setUp() {
        every { mockGetModelsUseCase() } returns flowOf(
            listOf(Model(1), Model(2), Model(3))
        )
        every { mockIsFirstTimeLaunchPreferencesUseCase() } returns flowOf(false)
        coEvery { mockUpdateFirstTimeLaunchPreferencesUseCase(any()) } just Runs
    }

    @Test
    fun `When entering the Home screen for the first time, it shows a toast confirming that`() {
        every { mockIsFirstTimeLaunchPreferencesUseCase() } returns flowOf(true)

        initComposable {
            composeRule.waitForIdle()
            advanceUntilIdle()

            ShadowToast.showedToast(activity.getString(R.string.message_first_time_launch)) shouldBe true
        }
    }

    @Test
    fun `When entering the Home screen NOT for the first time, it doesn't show the toast confirming that`() {
        initComposable {
            composeRule.waitForIdle()
            advanceUntilIdle()

            ShadowToast.showedToast(activity.getString(R.string.message_first_time_launch)) shouldBe false
        }
    }

    @Test
    fun `When entering the Home screen and loading the list item successfully, it shows the list item correctly`() =
        initComposable {
            onNodeWithText("Home").assertIsDisplayed()

            onNodeWithText("1").assertIsDisplayed()
            onNodeWithText("2").assertIsDisplayed()
            onNodeWithText("3").assertIsDisplayed()
        }

    @Test
    fun `When entering the Home screen and loading the list item failure, it shows the corresponding error`() {
        setStandardTestDispatcher()

        val error = Exception()
        every { mockGetModelsUseCase() } returns flow { throw error }

        initComposable {
            composeRule.waitForIdle()
            advanceUntilIdle()

            ShadowToast.showedToast(activity.getString(R.string.error_generic)) shouldBe true
        }
    }

    @Test
    fun `When clicking on a list item, it navigates to Second screen`() = initComposable {
        onNodeWithText("1").performClick()

        assertEquals(expectedAppDestination, AppDestination.Second)
    }

    private fun initComposable(
        testBody: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.() -> Unit,
    ) {
        initViewModel()

        composeRule.activity.setContent {
            ComposeTheme {
                HomeScreen(
                    viewModel = viewModel,
                    navigator = { destination -> expectedAppDestination = destination },
                )
            }
        }
        testBody(composeRule)
    }

    private fun initViewModel() {
        viewModel = HomeViewModel(
            mockGetModelsUseCase,
            mockIsFirstTimeLaunchPreferencesUseCase,
            mockUpdateFirstTimeLaunchPreferencesUseCase,
            coroutinesRule.testDispatcherProvider
        )
    }
}
