package com.kl3jvi.animity.domain.use_cases

import com.apollographql.apollo3.api.ApolloResponse
import com.kl3jvi.animity.AnimeListCollectionQuery
import com.kl3jvi.animity.data.mapper.ProfileData
import com.kl3jvi.animity.data.mapper.ProfileRow
import com.kl3jvi.animity.data.repository.fragment_repositories.ProfileRepositoryImpl
import com.kl3jvi.animity.data.repository.fragment_repositories.UserRepositoryImpl
import com.kl3jvi.animity.utils.NetworkResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetAnimeListForProfileUseCase @Inject constructor(
    private val profileRepositoryImpl: ProfileRepositoryImpl,
    private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * `profileRepositoryImpl.getProfileAnimes(userId = userId).flowOn(ioDispatcher)`
     *
     * This function is a `suspend` function that returns a `Flow` of
     * `NetworkResource<List<ProfileRow>>`
     *
     * @param userId The user id of the user whose profile you want to fetch.
     * @return A flow of network resource of list of profile rows
     */
    operator fun invoke(userId: Int?): Flow<NetworkResource<List<ProfileRow>>> {
        return profileRepositoryImpl.getProfileAnimes(userId = userId).flowOn(ioDispatcher)
    }
}