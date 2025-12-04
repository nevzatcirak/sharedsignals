package com.nevzatcirak.sharedsignals.api.service;

import com.nevzatcirak.sharedsignals.api.model.PollCommand;
import com.nevzatcirak.sharedsignals.api.model.PollResult;

/**
 * Service to handle Poll Delivery requests.
 * Uses Pure DTOs.
 */
public interface EventRetrievalService {
    PollResult pollEvents(String streamId, PollCommand command);
}
