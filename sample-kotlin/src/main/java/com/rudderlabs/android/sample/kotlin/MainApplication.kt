package com.rudderlabs.android.sample.kotlin

import android.app.Application
import com.rudderlabs.android.sample.kotlin.BuildConfig
import com.rudderstack.android.integration.customerio.CustomerIOIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        lateinit var rudderClient: RudderClient
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
            this,
            BuildConfig.WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(BuildConfig.DATA_PLANE_URL)
                .withControlPlaneUrl("https://69f7-2405-201-8025-58f8-8c03-fbbc-e2f9-595a.ngrok-free.app")
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .withFactory(CustomerIOIntegrationFactory.FACTORY)
                .withTrackLifecycleEvents(false)
                .withRecordScreenViews(false)
                .withSleepCount(3)
                .build()
        )
    }
}
