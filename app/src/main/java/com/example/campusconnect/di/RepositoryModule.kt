package com.example.campusconnect.di

import com.example.campusconnect.data.repository.*
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
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNotesRepository(
        notesRepositoryImpl: NotesRepositoryImpl
    ): NotesRepository

    @Binds
    @Singleton
    abstract fun bindLostAndFoundRepository(
        lostAndFoundRepositoryImpl: LostAndFoundRepositoryImpl
    ): LostAndFoundRepository

    @Binds
    @Singleton
    abstract fun bindRideRepository(
        rideRepositoryImpl: RideRepositoryImpl
    ): RideRepository

    @Binds
    @Singleton
    abstract fun bindBloodRepository(
        bloodRepositoryImpl: BloodRepositoryImpl
    ): BloodRepository

    @Binds
    @Singleton
    abstract fun bindComplaintRepository(
        complaintRepositoryImpl: ComplaintRepositoryImpl
    ): ComplaintRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(
        eventRepositoryImpl: EventRepositoryImpl
    ): EventRepository

    @Binds
    @Singleton
    abstract fun bindJobRepository(
        jobRepositoryImpl: JobRepositoryImpl
    ): JobRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindSocietyRepository(
        societyRepositoryImpl: SocietyRepositoryImpl
    ): SocietyRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        reportRepositoryImpl: ReportRepositoryImpl
    ): ReportRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository
}
