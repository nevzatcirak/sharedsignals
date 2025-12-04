package com.nevzatcirak.sharedsignals.api.service;

import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import java.util.List;

/**
 * Service interface for managing SSF Streams.
 * Secured with Owner validation.
 */
public interface StreamConfigurationService {

    StreamConfiguration createStream(StreamConfiguration request, String owner);

    StreamConfiguration getStream(String streamId, String owner);

    List<StreamConfiguration> listStreams(String owner);

    StreamConfiguration updateStream(String streamId, StreamConfiguration patch, String owner);

    StreamConfiguration replaceStream(String streamId, StreamConfiguration put, String owner);

    void deleteStream(String streamId, String owner);
}
