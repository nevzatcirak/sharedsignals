package com.nevzatcirak.sharedsignals.web.mapper;

import com.nevzatcirak.sharedsignals.api.model.AddSubjectCommand;
import com.nevzatcirak.sharedsignals.api.model.RemoveSubjectCommand;
import com.nevzatcirak.sharedsignals.web.model.SSFAddSubjectRequest;
import com.nevzatcirak.sharedsignals.web.model.SSFRemoveSubjectRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Web layer SSF models and API layer Command objects.
 * <p>
 * Follows the same pattern as PollMapper: Web layer uses Jackson-annotated models,
 * API layer uses framework-agnostic POJOs.
 */
@Component
public class SubjectMapper {

    /**
     * Converts SSF Add Subject Request to Command.
     * <p>
     * SSF Spec 8.1.3.2: If verified is omitted, default to true.
     *
     * @param request the web layer request
     * @return the API layer command
     */
    public AddSubjectCommand toCommand(SSFAddSubjectRequest request) {
        // SSF Spec: If verified is omitted, assume true
        boolean verified = request.getVerified() == null || request.getVerified();

        return new AddSubjectCommand(
                request.getStreamId(),
                request.getSubject(),
                verified
        );
    }

    /**
     * Converts SSF Remove Subject Request to Command.
     *
     * @param request the web layer request
     * @return the API layer command
     */
    public RemoveSubjectCommand toCommand(SSFRemoveSubjectRequest request) {
        return new RemoveSubjectCommand(
                request.getStreamId(),
                request.getSubject()
        );
    }
}