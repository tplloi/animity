package com.kl3jvi.animity.ui.fragments.details.animeDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kl3jvi.animity.data.model.ui_models.AniListMedia
import com.kl3jvi.animity.data.model.ui_models.EpisodeModel
import com.kl3jvi.animity.domain.repositories.DetailsRepository
import com.kl3jvi.animity.domain.repositories.FavoriteRepository
import com.kl3jvi.animity.domain.repositories.UserRepository
import com.kl3jvi.animity.utils.Result
import com.kl3jvi.animity.utils.asResult
import com.kl3jvi.animity.utils.ifAnyChanged
import com.kl3jvi.animity.utils.ifChanged
import com.kl3jvi.animity.utils.logError
import com.kl3jvi.animity.utils.or1
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val detailsRepository: DetailsRepository,
    private val userRepository: UserRepository,
    private val favoriteRepository: FavoriteRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    val animeMetaModel = MutableStateFlow(AniListMedia())
    val reverseState = MutableStateFlow(false)

    val episodeList: StateFlow<EpisodeListUiState> = animeMetaModel.flatMapLatest { media ->
        favoriteRepository.getGogoUrlFromAniListId(media.idAniList)
            .asResult()
            .flatMapLatest { result ->
                when (result) {
                    is Result.Error -> flowOf(EpisodeListUiState.Error)
                    Result.Loading -> flowOf(EpisodeListUiState.Loading)
                    is Result.Success -> {
                        detailsRepository.fetchAnimeInfo(
                            episodeUrl = result.data
                        ).flatMapLatest { animeInfo ->
                            detailsRepository.fetchEpisodeList(
                                id = animeInfo.id,
                                endEpisode = animeInfo.endEpisode,
                                alias = animeInfo.alias,
                                malId = media.idMal.or1()
                            )
                        }.reverseIf {
                            reverseState
                        }.map { episodes ->
                            EpisodeListUiState.Success(episodes)
                        }
                    }
                }
            }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        EpisodeListUiState.Loading
    )

    /**
     * > The function updates the anime as favorite in the AniList website
     */
    fun updateAnimeFavorite() {
        viewModelScope.launch(ioDispatcher) {
            animeMetaModel.flatMapLatest {
                userRepository.markAnimeAsFavorite(it.idAniList).ifChanged()
                    .catch { error -> logError(error) }
            }.collect()
        }
    }
}

private fun <T> Flow<List<T>>.reverseIf(predicate: () -> MutableStateFlow<Boolean>): Flow<List<T>> {
    return combine(this, predicate()) { list, bool ->
        if (bool) {
            list.asReversed()
        } else {
            list
        }
    }
}

sealed interface EpisodeListUiState {
    object Loading : EpisodeListUiState
    object Error : EpisodeListUiState
    data class Success(val data: List<EpisodeModel>) : EpisodeListUiState
}
