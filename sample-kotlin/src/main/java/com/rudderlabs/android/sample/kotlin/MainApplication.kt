package com.rudderlabs.android.sample.kotlin

import android.app.Application
import com.rudderstack.android.integration.customerio.CustomerIOIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger
import io.customer.messaginginapp.MessagingInAppModuleConfig
import io.customer.messaginginapp.ModuleMessagingInApp
import io.customer.messaginginapp.type.InAppEventListener
import io.customer.messaginginapp.type.InAppMessage
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
        // Refer here for more details push notifications: https://customer.io/docs/sdk/android/push/push/
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
                    )
                        // Refer here for more details in-app messaging: https://customer.io/docs/sdk/android/in-app/in-app-event-listeners/
                        .setEventListener(object : InAppEventListener {
                            override fun messageShown(message: InAppMessage) {
                                // Implement your own logic here
                            }

                            override fun messageDismissed(message: InAppMessage) {
                                // Implement your own logic here
                            }

                            override fun errorWithMessage(message: InAppMessage) {
                                // Implement your own logic here
                            }

                            override fun messageActionTaken(
                                message: InAppMessage,
                                actionValue: String,
                                actionName: String
                            ) {
                                // Implement your own logic here
                            }
                        })
                        .build()
                )
            )

            builder.build()
        }
    }
}
