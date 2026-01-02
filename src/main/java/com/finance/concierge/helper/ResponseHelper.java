package com.finance.concierge.helper;

import com.google.adk.events.Event;
import io.reactivex.rxjava3.core.Flowable;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for processing agent responses
 */
@Slf4j
@UtilityClass
public class ResponseHelper {

    /**
     * Collect final response from event stream
     */
    public static String collectFinalResponse(Flowable<Event> events) {
        StringBuilder responseBuilder = new StringBuilder();

        events.blockingForEach(event -> {
            if (event.finalResponse()) {
                String response = event.stringifyContent();
                log.debug("Collected response chunk: {}", response);
                responseBuilder.append(response);
            }
        });

        return responseBuilder.toString();
    }

    /**
     * Extract text content from response
     */
    public static String extractTextContent(String response) {
        if (response == null) {
            return "";
        }
        return response.trim();
    }

    /**
     * Validate response content
     */
    public static boolean isValidResponse(String response) {
        return response != null && !response.isBlank();
    }
}

