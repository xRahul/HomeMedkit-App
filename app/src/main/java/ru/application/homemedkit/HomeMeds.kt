package ru.application.homemedkit

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.allowRgb565
import coil3.request.crossfade
import me.zhanghai.compose.preference.isDefaultPreferenceFlowAndroidLongSupportEnabled
import ru.application.homemedkit.R.string.channel_exp_desc
import ru.application.homemedkit.R.string.channel_intakes_desc
import ru.application.homemedkit.R.string.channel_pre_desc
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import ru.application.homemedkit.di.appModule
import ru.application.homemedkit.utils.CHANNEL_ID_EXP
import ru.application.homemedkit.utils.CHANNEL_ID_INTAKES
import ru.application.homemedkit.utils.CHANNEL_ID_PRE
import ru.application.homemedkit.utils.coil.IconMapper
import ru.application.homemedkit.utils.extensions.createNotificationChannel


class HomeMeds : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@HomeMeds)
            workManagerFactory()
            modules(appModule)
        }

        isDefaultPreferenceFlowAndroidLongSupportEnabled = true

        mapOf(
            CHANNEL_ID_INTAKES to channel_intakes_desc,
            CHANNEL_ID_PRE to channel_pre_desc,
            CHANNEL_ID_EXP to channel_exp_desc
        ).forEach { (id, name) -> createNotificationChannel(id, name) }
    }

    override fun newImageLoader(context: PlatformContext) = ImageLoader(context).newBuilder()
        .components {
            add(IconMapper(context))
        }
        .crossfade(200)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.25)
                .build()
        }
        .diskCachePolicy(CachePolicy.ENABLED)
        .diskCache {
            DiskCache.Builder()
                .maxSizeBytes(32 * 1024 * 1024L)
                .directory(context.cacheDir)
                .build()
        }
        .allowRgb565(true)
        .build()
}