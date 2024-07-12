package com.rudderstack.android.integration.customerio;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;

import java.util.HashMap;
import java.util.Map;

import io.customer.sdk.CustomerIO;
import io.customer.sdk.data.model.Region;
import io.customer.sdk.util.CioLogLevel;

public class CustomerIOIntegrationFactory extends RudderIntegration<CustomerIO> {

    // String constants
    private static final String CUSTOMER_IO_KEY = "Customer IO";

    // Config Field constants
    private static final String API_KEY = "apiKey";
    private static final String SITE_ID = "siteID";
    private static final String AUTO_TRACK_DEVICE_ATTRIBUTES = "autoTrackDeviceAttributes";
    private static final String BACKGROUND_QUEUE_MIN_NUMBER_OF_TASKS = "backgroundQueueMinNumberOfTasks";
    private static final String BACKGROUND_QUEUE_SECONDS_DELAY = "backgroundQueueSecondsDelay";
    private static final String DATA_CENTER = "datacenter";
    private static final String TRACK_APPLICATION_LIFECYCLE_EVENTS = "trackApplicationLifecycleEvents";

    // customerio instance
    private CustomerIO customerIO;

    // Factory initialization
    public static final Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig rudderConfig) {
            return new CustomerIOIntegrationFactory(settings, rudderConfig);
        }

        @Override
        public String key() {
            return CUSTOMER_IO_KEY;
        }
    };

    private CustomerIOIntegrationFactory(Object config, RudderConfig rudderConfig) {
        Map<String, Object> destinationConfig = (Map<String, Object>) config;
        if (destinationConfig == null) {
            RudderLogger.logError("Destination config object is null. Aborting CustomerIO initialization.");
        } else if (RudderClient.getApplication() == null) {
            RudderLogger.logError("RudderClient is not initialized correctly. Application is null. Aborting CustomerIO initialization.");
        } else {
            // get siteId and return if null or blank
            String siteId = "";
            if (destinationConfig.containsKey(SITE_ID)) {
                siteId = (String) destinationConfig.get(SITE_ID);
            }
            if (TextUtils.isEmpty(siteId)) {
                RudderLogger.logError("Invalid siteId. Aborting CustomerIO initialization");
                return;
            }

            String apiKey = "";
            if (destinationConfig.containsKey(API_KEY)) {
                apiKey = (String) destinationConfig.get(API_KEY);
            }
            if (TextUtils.isEmpty(apiKey)) {
                RudderLogger.logError("Invalid apiKey. Aborting CustomerIO initialization");
                return;
            }

            // get region
            Region region = Region.US.INSTANCE;
            if (destinationConfig.containsKey(DATA_CENTER)) {
                String regionStr = (String) destinationConfig.get(DATA_CENTER);
                if (!TextUtils.isEmpty(regionStr)) {
                    if (regionStr.equalsIgnoreCase("eu")) {
                        region = Region.EU.INSTANCE;
                    }
                }
            }

            // extra config
            Map<String, Object> extraConfig = new HashMap<>();
            // extra configs from the SDK config
            extraConfig.put(TRACK_APPLICATION_LIFECYCLE_EVENTS, rudderConfig.isTrackLifecycleEvents());

            // customer.io builder object
            CustomerIO.Builder builder = new CustomerIO.Builder(
                    siteId, apiKey, region, RudderClient.getApplication(), extraConfig
            );

            // get autoTrackDeviceAttributes
            boolean autoTrackDeviceAttributes = true;
            if (destinationConfig.containsKey(AUTO_TRACK_DEVICE_ATTRIBUTES)) {
                autoTrackDeviceAttributes = Boolean.getBoolean(AUTO_TRACK_DEVICE_ATTRIBUTES);
            }
            builder.autoTrackDeviceAttributes(autoTrackDeviceAttributes);

            // get backgroundQueueMinNumberOfTasks
            int backgroundQueueMinNumberOfTasks = 10;
            if (destinationConfig.containsKey(BACKGROUND_QUEUE_MIN_NUMBER_OF_TASKS)) {
                String backgroundQueueMinNumberOfTasksStr = (String) destinationConfig.get(
                        BACKGROUND_QUEUE_MIN_NUMBER_OF_TASKS);
                if (!TextUtils.isEmpty(backgroundQueueMinNumberOfTasksStr)) {
                    try {
                        backgroundQueueMinNumberOfTasks = Integer.parseInt(
                                backgroundQueueMinNumberOfTasksStr);
                    } catch (NumberFormatException e) {
                        RudderLogger.logWarn("Invalid Number format. Reverting to default value");
                    }
                }
            }
            builder.setBackgroundQueueMinNumberOfTasks(backgroundQueueMinNumberOfTasks);

            // get backgroundQueueSecondsDelay
            double backgroundQueueSecondsDelay = 30.0;
            if (destinationConfig.containsKey(BACKGROUND_QUEUE_SECONDS_DELAY)) {
                String backgroundQueueSecondsDelayStr = (String) destinationConfig.get(
                        BACKGROUND_QUEUE_SECONDS_DELAY);
                if (!TextUtils.isEmpty(backgroundQueueSecondsDelayStr)) {
                    try {
                        backgroundQueueSecondsDelay = Double.parseDouble(
                                backgroundQueueSecondsDelayStr);
                    } catch (NumberFormatException e) {
                        RudderLogger.logWarn("Invalid Number format. Reverting to default value");
                    }
                }
            }
            builder.setBackgroundQueueSecondsDelay(backgroundQueueSecondsDelay);

            // auto record screen views
            builder.autoTrackScreenViews(rudderConfig.isRecordScreenViews());

            // log level
            builder.setLogLevel(mapLogLevel(rudderConfig.getLogLevel()));


            this.customerIO = builder.build();
            RudderLogger.logInfo("Configured Customer IO + Rudder integration and initialized customerio.");
        }
    }

    private void processTrackEvent(RudderMessage element) {
        String event = element.getEventName();
        if (event == null) {
            return;
        }

        Map<String, Object> properties = element.getProperties();
        if (properties == null) {
            this.customerIO.track(event);
        } else {
            this.customerIO.track(event, properties);
        }
    }

    private void processIdentifyEvent(RudderMessage element) {
        String userId = element.getUserId();
        String anonymousId = element.getAnonymousId();
        Map<String, Object> traits = element.getTraits();

        if (!TextUtils.isEmpty(userId)) {
            this.customerIO.identify(userId, traits);
        } else if (TextUtils.isEmpty(anonymousId)) {
            this.customerIO.identify(anonymousId, traits);
        }
    }

    private void processScreenEvent(RudderMessage element) {
        String name = element.getEventName();
        if (name == null) {
            return;
        }

        Map<String, Object> properties = element.getProperties();
        if (properties == null) {
            this.customerIO.screen(name);
        } else {
            this.customerIO.screen(name, properties);
        }
    }

    @Override
    public void flush() {
        super.flush();
        RudderLogger.logDebug("Customer IO doesn't support immediate flush call.");
    }

    @Override
    public void reset() {
        this.customerIO.clearIdentify();
    }

    @Override
    public void dump(@NonNull RudderMessage element) {
        if (customerIO != null) {
            if (element.getType() != null) {
                switch (element.getType()) {
                    case MessageType.TRACK:
                        processTrackEvent(element);
                        break;
                    case MessageType.IDENTIFY:
                        processIdentifyEvent(element);
                        break;
                    case MessageType.SCREEN:
                        processScreenEvent(element);
                        break;
                    case MessageType.ALIAS:
                    case MessageType.GROUP:
                        RudderLogger.logWarn("CustomerIOIntegrationFactory: MessageType is not supported");
                        break;
                    default:
                        RudderLogger.logWarn("CustomerIOIntegrationFactory: MessageType is not specified");
                        break;
                }
            }
        } else {
            RudderLogger.logWarn("CustomerIOIntegrationFactory: CustomerIO is not initialized");
        }
    }

    @Override
    public CustomerIO getUnderlyingInstance() {
        return customerIO;
    }

    private CioLogLevel mapLogLevel(int logLevel) {
        return switch (logLevel) {
            case RudderLogger.RudderLogLevel.VERBOSE, RudderLogger.RudderLogLevel.DEBUG ->
                    CioLogLevel.DEBUG;
            case RudderLogger.RudderLogLevel.INFO, RudderLogger.RudderLogLevel.WARN ->
                    CioLogLevel.INFO;
            case RudderLogger.RudderLogLevel.ERROR -> CioLogLevel.ERROR;
            default -> CioLogLevel.NONE;
        };
    }
}
