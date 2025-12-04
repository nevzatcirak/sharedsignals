package com.nevzatcirak.sharedsignals.api.service;

import com.nevzatcirak.sharedsignals.api.model.TransmitterMetadata;

/**
 * Service interface for providing SSF and RISC configuration metadata.
 */
public interface TransmitterMetadataService {
    TransmitterMetadata getMetadata();
}
