package co.nimblehq.coroutine.data.repository

import co.nimblehq.coroutine.data.response.toUsers
import co.nimblehq.coroutine.data.service.ApiService
import co.nimblehq.coroutine.domain.model.User
import co.nimblehq.coroutine.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserRepositoryImpl constructor(
    private val apiService: ApiService
) : UserRepository {

    override fun getUsers(): Flow<Result<List<User>>> = flow {
        val result = try {
            val data = apiService.getUsers().toUsers()
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
    }
}
