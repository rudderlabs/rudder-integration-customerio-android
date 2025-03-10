# What is RudderStack?

[RudderStack](https://rudderstack.com/) is a **customer data pipeline** tool for collecting, routing and processing data from your websites, apps, cloud tools, and data warehouse.

More information on RudderStack can be found [here](https://github.com/rudderlabs/rudder-server)

## Integrating Customer IO with RudderStack's Android SDK

1. Add [Customer IO](https://www.customerio.com) as a destination in the [Dashboard](https://app.rudderstack.com/) and define ```apiToken```

2. Add the dependency under ```dependencies```
```
implementation 'com.rudderstack.android.sdk:core:[1.0,2.0)'
implementation 'com.rudderstack.android.integration:customerio:1.1.0'
```

3. Add required permissions to ```AndroidManifest.xml```
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Initialize ```RudderClient```

```
val rudderClient: RudderClient = RudderClient.getInstance(
    this,
    <WRITE_KEY>,
    RudderConfig.Builder()
        .withDataPlaneUrl(<DATA_PLANE_URL>)
        .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
        .withFactory(CustomerIOIntegrationFactory.FACTORY)
        .build()
)
```

## Add FCM and IN-APP module
1. Add the dependency under ```dependencies```
```
implementation "io.customer.android:messaging-push-fcm:4.2.0"
implementation "io.customer.android:messaging-in-app:4.2.0"
```

2. Add the next section right after the initialization of the SDK
```
rudderClient.onIntegrationReady("Customer IO") {
    val siteId = (it as CustomerIOIntegrationFactory).siteId
    val builder = it.builder
    val region = it.region

    builder.addCustomerIOModule(ModuleMessagingPushFCM())
    builder.addCustomerIOModule(
        ModuleMessagingInApp(
            config = MessagingInAppModuleConfig.Builder(
                siteId,
                region
            ).build()
        )
    )

    builder.build() // important and don't miss
}
```

## Send Events

Follow the steps from the [RudderStack Android SDK](https://github.com/rudderlabs/rudder-sdk-android).

## Contact Us

If you come across any issues while configuring or using this integration, please feel free to start a conversation on our [Slack](https://resources.rudderstack.com/join-rudderstack-slack) channel. We will be happy to help you.
