package com.nevzatcirak.sharedsignals.web.mapper;

import com.nevzatcirak.sharedsignals.api.model.PollCommand;
import com.nevzatcirak.sharedsignals.api.model.PollResult;
import com.nevzatcirak.sharedsignals.web.model.SSFPollRequest;
import com.nevzatcirak.sharedsignals.web.model.SSFPollResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class PollMapper {

    public PollCommand toCommand(SSFPollRequest request) {
        Map<String, PollCommand.PollError> errors = new HashMap<>();

        if (request.getSetErrs() != null) {
            request.getSetErrs().forEach((jti, err) ->
                errors.put(jti, new PollCommand.PollError(err.getErr(), err.getDescription()))
            );
        }

        return new PollCommand(
            request.getAck() != null ? request.getAck() : Collections.emptyList(),
            errors,
            (request.getMaxEvents() != null && request.getMaxEvents() > 0) ? request.getMaxEvents() : 10,
            request.getReturnImmediately() != null ? request.getReturnImmediately() : false
        );
    }

    public SSFPollResponse toResponse(PollResult result) {
        return new SSFPollResponse(
            result.getEvents(),
            result.isMoreAvailable()
        );
    }
}
