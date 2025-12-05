
package com.nevzatcirak.sharedsignals.web.mapper;

import com.nevzatcirak.sharedsignals.api.model.TriggerVerificationCommand;
import com.nevzatcirak.sharedsignals.web.model.SSFVerificationRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Web layer SSF verification models 
 * and API layer Command objects.
 */
@Component
public class VerificationMapper {

    /**
     * Converts SSF Verification Request to Command.
     *
     * @param request the web layer request
     * @return the API layer command
     */
    public TriggerVerificationCommand toCommand(SSFVerificationRequest request) {
        return new TriggerVerificationCommand(
                request.getStreamId(),
                request.getState()
        );
    }
}