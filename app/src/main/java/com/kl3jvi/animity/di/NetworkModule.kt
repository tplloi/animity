package com.kl3jvi.animity.di

import com.kl3jvi.animity.data.network.anilist_service.AniListClient
import com.kl3jvi.animity.data.network.anilist_service.AniListService
import com.kl3jvi.animity.data.network.anime_service.AnimeApiClient
import com.kl3jvi.animity.data.network.anime_service.AnimeService
import com.kl3jvi.animity.utils.Constants.Companion.BASE_URL
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .build()
    }

    @Singleton
    @Provides
    fun provideAnimeService(retrofit: Retrofit): AnimeService {
        return retrofit.create(AnimeService::class.java)
    }

    @Provides
    @Singleton
    fun provideAnimeApiClient(animeService: AnimeService): AnimeApiClient {
        return AnimeApiClient(animeService)
    }


    @Singleton
    @Provides
    fun provideAniListService(retrofit: Retrofit): AniListService {
        return retrofit.create(AniListService::class.java)
    }

    @Provides
    @Singleton
    fun provideAniListClient(aniListService: AniListService): AniListClient {
        return AniListClient(aniListService)
    }
}


