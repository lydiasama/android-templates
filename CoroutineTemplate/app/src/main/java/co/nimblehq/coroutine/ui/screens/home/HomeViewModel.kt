package co.nimblehq.coroutine.ui.screens.home

import co.nimblehq.coroutine.domain.usecase.GetUsersUseCase
import co.nimblehq.coroutine.model.UserUiModel
import co.nimblehq.coroutine.model.toUserUiModels
import co.nimblehq.coroutine.ui.base.BaseViewModel
import co.nimblehq.coroutine.ui.base.NavigationEvent
import co.nimblehq.coroutine.ui.screens.second.SecondBundle
import co.nimblehq.coroutine.util.DispatchersProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

interface Output {

    val userUiModels: StateFlow<List<UserUiModel>>

    fun navigateToSecond(bundle: SecondBundle)

    fun navigateToCompose()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    dispatchers: DispatchersProvider
) : BaseViewModel(dispatchers), Output {

    private val _userUiModels = MutableStateFlow<List<UserUiModel>>(emptyList())
    override val userUiModels: StateFlow<List<UserUiModel>>
        get() = _userUiModels

    init {
        fetchUsers()
    }

    override fun navigateToSecond(bundle: SecondBundle) {
        execute {
            _navigator.emit(NavigationEvent.Second(bundle))
        }
    }

    override fun navigateToCompose() {
        execute {
            _navigator.emit(NavigationEvent.Compose)
        }
    }

    private fun fetchUsers() {
        execute {
            showLoading()
            getUsersUseCase.execute().collect { result ->
                if (result.isSuccess) _userUiModels.value = result.getOrNull()!!.toUserUiModels()
                else _error.emit(result.exceptionOrNull()!!.message.orEmpty())
            }
            hideLoading()
        }
    }
}
