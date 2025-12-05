package com.nevzatcirak.sharedsignals.core.security;

/**
 * SSF Security Policy Documentation.
 * <p>
 * This class documents the security considerations implemented
 * as per SSF Specification Section 9.
 * <p>
 * <b>9.1 Subject Probing:</b>
 * <ul>
 *   <li>Returns 200 OK for add subject even if subject not actually added</li>
 *   <li>Silent ignore for subjects that don't exist or are opted-out</li>
 *   <li>Prevents information leakage about subject existence</li>
 * </ul>
 * <p>
 * <b>9.2 Information Harvesting:</b>
 * <ul>
 *   <li>Validates all subject formats before acceptance</li>
 *   <li>Checks opt-out status before adding subjects</li>
 *   <li>Transmitters should validate sharing permissions before sending events</li>
 *   <li>Not obligated to send events for every added subject</li>
 * </ul>
 * <p>
 * <b>9.3 Malicious Subject Removal:</b>
 * <ul>
 *   <li>Grace period of 7 days (configurable) after subject removal</li>
 *   <li>Events continue being sent during grace period</li>
 *   <li>Protects against malicious removal attacks</li>
 *   <li>Receivers must tolerate events for removed subjects</li>
 * </ul>
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-secevent-sharedsignals#section-9">SSF Spec Section 9</a>
 */
public final class SsfSecurityPolicy {
    private SsfSecurityPolicy() {
        // Utility class - no instantiation
    }

    /**
     * Default grace period for removed subjects (7 days in seconds).
     */
    public static final int DEFAULT_GRACE_PERIOD_SECONDS = 604800;
}