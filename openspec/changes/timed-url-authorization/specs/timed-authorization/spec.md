## ADDED Requirements

### Requirement: Authorization duration selection
When a parent enters the correct password to unblock a URL, the system SHALL present duration options: 5 minutes, 15 minutes, 30 minutes, 1 hour, permanent, and custom.

#### Scenario: Parent selects a preset duration
- **WHEN** parent enters correct password and selects "15 minutes"
- **THEN** the domain is authorized for 15 minutes and the page loads immediately

#### Scenario: Parent selects permanent
- **WHEN** parent enters correct password and selects "permanent"
- **THEN** the domain is added to the whitelist rules as a DOMAIN type rule and the page loads immediately

#### Scenario: Parent selects custom duration
- **WHEN** parent enters correct password and selects "custom"
- **THEN** a dialog appears allowing the parent to input a number of minutes, and after confirming, the domain is authorized for that duration

### Requirement: Timed authorization expiration
The system SHALL automatically revoke access to a domain when its temporary authorization expires.

#### Scenario: Authorization expires during browsing
- **WHEN** a domain's temporary authorization time runs out
- **THEN** the next navigation within that domain SHALL trigger the password dialog again

#### Scenario: Authorization checked on navigation
- **WHEN** the user navigates to a URL whose domain has an expired temporary authorization
- **THEN** the system SHALL treat it as unauthorized and show the password dialog

### Requirement: Temporary authorization management in settings
The settings page SHALL display all currently active temporary authorizations with their remaining time.

#### Scenario: View active temporary authorizations
- **WHEN** a parent opens the settings page
- **THEN** a "Temporary Authorizations" section SHALL show each temporarily authorized domain and its remaining time

#### Scenario: Revoke a temporary authorization
- **WHEN** a parent taps the revoke button on a temporary authorization entry
- **THEN** the authorization is immediately removed and future access to that domain requires re-authorization

#### Scenario: No active authorizations
- **WHEN** there are no active temporary authorizations
- **THEN** the section SHALL display a message indicating no temporary authorizations are active
