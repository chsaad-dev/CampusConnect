package com.campusconnect.core.di

import com.campusconnect.data.repository.AuthRepositoryImpl
import com.campusconnect.data.repository.PostRepositoryImpl
import com.campusconnect.data.repository.UserRepositoryImpl
import com.campusconnect.domain.repository.AuthRepository
import com.campusconnect.domain.repository.PostRepository
import com.campusconnect.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        impl: PostRepositoryImpl
    ): PostRepository
}
