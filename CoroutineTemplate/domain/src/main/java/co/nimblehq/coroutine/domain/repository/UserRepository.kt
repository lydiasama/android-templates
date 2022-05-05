package co.nimblehq.coroutine.domain.repository

import co.nimblehq.coroutine.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun getUsers(): Flow<Result<List<User>>>
}
