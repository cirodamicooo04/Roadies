package it.roadies.android_app.client.network

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.roadies.android_app.BuildConfig
import it.roadies.android_app.client.apis.AttivitApi
import it.roadies.android_app.client.apis.FavouriteListsManagementApi
import it.roadies.android_app.client.apis.MetadatiApi
import it.roadies.android_app.client.apis.ViaggiApi
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

    @Provides
    @Singleton
    fun provideFavouriteListsManagementApi(retrofit: Retrofit): FavouriteListsManagementApi =
        retrofit.create(FavouriteListsManagementApi::class.java)

    private val devConnectionBuilder = ConnectionBuilder { uri ->
        URL(uri.toString()).openConnection() as HttpURLConnection
    }
}
