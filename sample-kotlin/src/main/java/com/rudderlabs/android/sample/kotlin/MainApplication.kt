package com.rudderlabs.android.sample.kotlin

import android.app.Application
import com.rudderstack.android.integration.customerio.CustomerIOIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger
import io.customer.messaginginapp.MessagingInAppModuleConfig
import io.customer.messaginginapp.ModuleMessagingInApp
import io.customer.messagingpush.ModuleMessagingPushFCM
import io.customer.sdk.CustomerIOBuilder
import io.customer.sdk.data.model.Region

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
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .withFactory(CustomerIOIntegrationFactory.FACTORY)
                .withTrackLifecycleEvents(false)
                .withRecordScreenViews(false)
                .withSleepCount(3)
                .build()
        )

        // important to add right after initialization
        rudderClient.onIntegrationReady("Customer IO") {
            val siteId = (it as CustomerIOIntegrationFactory).siteId
            val builder: CustomerIOBuilder = it.builder
            val region: Region = it.region

            builder.addCustomerIOModule(ModuleMessagingPushFCM())
            builder.addCustomerIOModule(
                ModuleMessagingInApp(
                    config = MessagingInAppModuleConfig.Builder(
                        siteId,
                        region
                    ).build()
                )
            )

            builder.build()
        }
    }
}
