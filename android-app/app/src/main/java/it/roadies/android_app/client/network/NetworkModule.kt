package it.roadies.android_app.client.network

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.roadies.android_app.BuildConfig
import it.roadies.android_app.client.apis.booking.GestioneDocumentiApi
import it.roadies.android_app.client.apis.booking.GestionePagamentoApi
import it.roadies.android_app.client.apis.booking.GestionePrenotazioniApi
import it.roadies.android_app.client.apis.travel.AttivitApi
import it.roadies.android_app.client.apis.travel.FavouriteListsManagementApi
import it.roadies.android_app.client.apis.travel.MetadatiApi
import it.roadies.android_app.client.apis.travel.PhotonApi
import it.roadies.android_app.client.apis.travel.ViaggiApi
import it.roadies.android_app.client.apis.user.DocumentManagementApi
import it.roadies.android_app.client.apis.user.FriendshipManagementApi
import it.roadies.android_app.client.apis.user.UserManagementApi
import it.roadies.android_app.client.infrastructure.Serializer
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationService
import net.openid.appauth.connectivity.ConnectionBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val API_BASE_URL = "http://10.0.2.2:8080/"
    private const val PHOTON_BASE_URL = "https://photon.komoot.io"

    @Provides
    @Singleton
    fun provideAppAuthConfiguration(): AppAuthConfiguration =
        AppAuthConfiguration.Builder()
            .setConnectionBuilder(devConnectionBuilder)
            .build()

    @Provides
    @Singleton
    fun provideAuthorizationService(
        @ApplicationContext context: Context,
        appAuthConfiguration: AppAuthConfiguration
    ): AuthorizationService = AuthorizationService(context, appAuthConfiguration)

    @Provides
    @Singleton
    fun provideOkHttpClient(authTokenInterceptor: AuthTokenInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authTokenInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BASIC
                        }
                    )
                }
            }
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(Serializer.gson))
            .build()

    // travel service apis
    @Provides
    @Singleton
    fun provideViaggiApi(retrofit: Retrofit): ViaggiApi =
        retrofit.create(ViaggiApi::class.java)

    @Provides
    @Singleton
    fun provideAttivitApi(retrofit: Retrofit): AttivitApi =
        retrofit.create(AttivitApi::class.java)

    @Provides
    @Singleton
    fun provideMetadatiApi(retrofit: Retrofit): MetadatiApi =
        retrofit.create(MetadatiApi::class.java)

    //booking service provides
    @Provides
    @Singleton
    fun provideGestionePrenotazioniApi(retrofit: Retrofit): GestionePrenotazioniApi =
        retrofit.create(GestionePrenotazioniApi::class.java)

    @Provides
    @Singleton
    fun provideGestionePagamentoApi(retrofit: Retrofit): GestionePagamentoApi =
        retrofit.create(GestionePagamentoApi::class.java)

    @Provides
    @Singleton
    fun provideGestioneDocumentiApi(retrofit: Retrofit): GestioneDocumentiApi =
        retrofit.create(GestioneDocumentiApi::class.java)

    @Provides
    @Singleton
    fun provideFavouriteListsManagementApi(retrofit: Retrofit): FavouriteListsManagementApi =
        retrofit.create(FavouriteListsManagementApi::class.java)

    //user service provide
    @Provides
    @Singleton
    fun provideFriendshipManagementApi(retrofit: Retrofit): FriendshipManagementApi =
        retrofit.create(FriendshipManagementApi::class.java)

    @Provides
    @Singleton
    fun provideDocumentManagementApi(retrofit: Retrofit): DocumentManagementApi =
        retrofit.create(DocumentManagementApi::class.java)

    @Provides
    @Singleton
    fun provideUserManagementApi(retrofit: Retrofit): UserManagementApi =
        retrofit.create(UserManagementApi::class.java)

    @Provides
    @Singleton
    fun providePhotonApi(): PhotonApi {
        val cleanClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BASIC
                        }
                    )
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("$PHOTON_BASE_URL/")
            .client(cleanClient)
            .addConverterFactory(GsonConverterFactory.create(Serializer.gson))
            .build()
            .create(PhotonApi::class.java)
    }

    private val devConnectionBuilder = ConnectionBuilder { uri ->
        URL(uri.toString()).openConnection() as HttpURLConnection
    }
}
