# What is RudderStack?

**Short answer:** 
RudderStack is an open-source Segment alternative written in Go, built for the enterprise. .

**Long answer:** 
RudderStack is a platform for collecting, storing and routing customer event data to dozens of tools. Rudder is open-source, can run in your cloud environment (AWS, GCP, Azure or even your data-centre) and provides a powerful transformation framework to process your event data on the fly.

## Getting Started with Localytics Integration of Android SDK
1. Add [Localytics](https://localytics.google.com) as a destination in the [Dashboard](https://app.rudderlabs.com/)

2. Add these lines to your ```app/build.gradle```
```
repositories {
  maven { url "https://dl.bintray.com/rudderstack/rudderstack" }
}
```
3. Add the dependency under ```dependencies```
```
implementation 'com.rudderstack.android.sdk:core:1.0.1'
implementation 'com.rudderstack.android.integration:localytics:0.1.1'
```

## Initialize ```RudderClient```
```
val rudderClient: RudderClient = RudderClient.getInstance(
    this,
    <WRITE_KEY>,
    RudderConfig.Builder()
        .withDataPlaneUrl(<DATA_PLANE_URL>)
        .withFactory(LocalyticsIntegrationFactory.FACTORY)
        .build()
)
```

## Send Events
Follow the steps from [Rudder Android SDK](https://github.com/rudderlabs/rudder-sdk-android)

## Contact Us
If you come across any issues while configuring or using RudderStack, please feel free to [contact us](https://rudderstack.com/contact/) or start a conversation on our [Slack](https://resources.rudderstack.com/join-rudderstack-slack) channel. We will be happy to help you.
