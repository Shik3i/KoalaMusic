package net.koalastuff.music.core.data

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Duration
import javax.inject.Singleton
import net.koalastuff.music.core.database.CapabilityDao
import net.koalastuff.music.core.database.KoalaMusicDatabase
import net.koalastuff.music.core.database.LibraryDao
import net.koalastuff.music.core.database.QueueDao
import net.koalastuff.music.core.database.ServerProfileDao
import net.koalastuff.music.core.database.SyncStateDao
import net.koalastuff.music.core.opensubsonic.OpenSubsonicClientFactory
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object DataProvidesModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): KoalaMusicDatabase =
        Room.databaseBuilder(context, KoalaMusicDatabase::class.java, "koalamusic.db")
            .build()

    @Provides
    fun profiles(database: KoalaMusicDatabase): ServerProfileDao = database.serverProfileDao()

    @Provides
    fun capabilities(database: KoalaMusicDatabase): CapabilityDao = database.capabilityDao()

    @Provides fun library(database: KoalaMusicDatabase): LibraryDao = database.libraryDao()

    @Provides fun queue(database: KoalaMusicDatabase): QueueDao = database.queueDao()

    @Provides fun sync(database: KoalaMusicDatabase): SyncStateDao = database.syncStateDao()

    @Provides
    @Singleton
    fun httpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(15))
        .readTimeout(Duration.ofSeconds(30))
        .writeTimeout(Duration.ofSeconds(30))
        .callTimeout(Duration.ofMinutes(2))
        .retryOnConnectionFailure(true)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    @Provides
    @Singleton
    fun openSubsonicClientFactory(httpClient: OkHttpClient) = OpenSubsonicClientFactory(httpClient)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {
    @Binds
    abstract fun musicRepository(implementation: DefaultMusicRepository): MusicRepository
}
