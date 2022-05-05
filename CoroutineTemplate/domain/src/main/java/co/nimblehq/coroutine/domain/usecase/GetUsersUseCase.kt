package co.nimblehq.coroutine.domain.usecase

import co.nimblehq.coroutine.domain.model.User
import co.nimblehq.coroutine.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(private val userRepository: UserRepository) {

    fun execute(): Flow<Result<List<User>>> {
        return userRepository.getUsers()
    }
}
