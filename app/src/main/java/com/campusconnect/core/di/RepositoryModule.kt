package com.campusconnect.core.di

import com.campusconnect.data.repository.AuthRepositoryImpl
import com.campusconnect.data.repository.CloudinaryMediaRepository
import com.campusconnect.domain.repository.MediaRepository
import com.campusconnect.data.repository.ChatRepositoryImpl
import com.campusconnect.data.repository.ComplaintRepositoryImpl
import com.campusconnect.data.repository.EventRepositoryImpl
import com.campusconnect.data.repository.FriendRepositoryImpl
import com.campusconnect.data.repository.JobRepositoryImpl
import com.campusconnect.data.repository.NotificationRepositoryImpl
import com.campusconnect.data.repository.PostRepositoryImpl
import com.campusconnect.data.repository.UserRepositoryImpl
import com.campusconnect.domain.repository.AuthRepository
import com.campusconnect.domain.repository.ChatRepository
import com.campusconnect.domain.repository.ComplaintRepository
import com.campusconnect.domain.repository.EventRepository
import com.campusconnect.domain.repository.FriendRepository
import com.campusconnect.domain.repository.JobRepository
import com.campusconnect.domain.repository.NotificationRepository
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

    @Binds
    @Singleton
    abstract fun bindFriendRepository(
        impl: FriendRepositoryImpl
    ): FriendRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindComplaintRepository(
        impl: ComplaintRepositoryImpl
    ): ComplaintRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(
        impl: EventRepositoryImpl
    ): EventRepository

    @Binds
    @Singleton
    abstract fun bindJobRepository(
        impl: JobRepositoryImpl
    ): JobRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindMediaRepository(
        impl: CloudinaryMediaRepository
    ): MediaRepository

    @Binds
    @Singleton
    abstract fun bindAssistantRepository(
        impl: com.campusconnect.data.repository.AssistantRepositoryImpl
    ): com.campusconnect.domain.repository.AssistantRepository
}
