package com.rudderstack.android.integration.customerio;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;

import java.util.Map;

import io.customer.sdk.CustomerIO;
import io.customer.sdk.CustomerIOBuilder;
import io.customer.sdk.core.util.CioLogLevel;
import io.customer.sdk.data.model.Region;

public class CustomerIOIntegrationFactory extends RudderIntegration<CustomerIOIntegrationFactory> {

    // String constants
    private static final String CUSTOMER_IO_KEY = "Customer IO";

    // Config Field constants
    private static final String API_KEY = "apiKey";
    private static final String SITE_ID = "siteID";
    private static final String AUTO_TRACK_DEVICE_ATTRIBUTES = "autoTrackDeviceAttributes";
    private static final String DATA_CENTER = "datacenter";

    private String siteId = null;
    private CustomerIOBuilder customerIOBuilder = null;
    private Region region = Region.US.INSTANCE;

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
            if (destinationConfig.containsKey(SITE_ID)) {
                this.siteId = (String) destinationConfig.get(SITE_ID);
            }
            if (TextUtils.isEmpty(this.siteId)) {
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
            if (destinationConfig.containsKey(DATA_CENTER)) {
                String regionStr = (String) destinationConfig.get(DATA_CENTER);
                if (!TextUtils.isEmpty(regionStr)) {
                    if (regionStr.equalsIgnoreCase("eu")) {
                        this.region = Region.EU.INSTANCE;
                    }
                }
            }

            // customer.io builder object
            this.customerIOBuilder = new CustomerIOBuilder(
                    RudderClient.getApplication(), apiKey
            );

            customerIOBuilder.region(this.region);

            // get autoTrackDeviceAttributes
            boolean autoTrackDeviceAttributes = true;
            if (destinationConfig.containsKey(AUTO_TRACK_DEVICE_ATTRIBUTES)) {
                autoTrackDeviceAttributes = Boolean.getBoolean(AUTO_TRACK_DEVICE_ATTRIBUTES);
            }
            this.customerIOBuilder.autoTrackDeviceAttributes(autoTrackDeviceAttributes);

            // log level
            this.customerIOBuilder.logLevel(mapLogLevel(rudderConfig.getLogLevel()));

            // auto record screen views
            this.customerIOBuilder.autoTrackActivityScreens(rudderConfig.isRecordScreenViews());

            // do not build. let the developer build after adding the fcm and in-app module

            RudderLogger.logInfo("Configured Customer IO + Rudder integration builder created. initialize customer.io by calling builder.build()");
        }
    }

    private void processTrackEvent(RudderMessage element) {
        String event = element.getEventName();
        if (event == null) {
            return;
        }

        Map<String, Object> properties = element.getProperties();
        if (properties == null) {
            CustomerIO.instance().track(event);
        } else {
            CustomerIO.instance().track(event, properties);
        }
    }

    private void processIdentifyEvent(RudderMessage element) {
        String userId = element.getUserId();
        String anonymousId = element.getAnonymousId();
        Map<String, Object> traits = element.getTraits();

        if (!TextUtils.isEmpty(userId)) {
            CustomerIO.instance().identify(userId, traits);
        } else if (TextUtils.isEmpty(anonymousId)) {
            CustomerIO.instance().identify(anonymousId, traits);
        }
    }

    private void processScreenEvent(RudderMessage element) {
        String name = element.getEventName();
        if (name == null) {
            return;
        }

        Map<String, Object> properties = element.getProperties();
        if (properties == null) {
            CustomerIO.instance().screen(name);
        } else {
            CustomerIO.instance().screen(name, properties);
        }
    }

    @Override
    public void flush() {
        super.flush();
        RudderLogger.logDebug("Customer IO doesn't support immediate flush call.");
    }

    @Override
    public void reset() {
        CustomerIO.instance().clearIdentify();
    }

    @Override
    public void dump(@NonNull RudderMessage element) {
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
    }

    @Override
    public CustomerIOIntegrationFactory getUnderlyingInstance() {
        return this;
    }

    public String getSiteId() {
        return this.siteId;
    }

    public CustomerIOBuilder getBuilder() {
        return this.customerIOBuilder;
    }

    public Region getRegion() {
        return this.region;
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
