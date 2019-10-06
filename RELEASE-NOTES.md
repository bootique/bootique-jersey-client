## 1.1

* #43 Java 10+: explicit activation and jaxb dependencies are required
* #44 "apiKeyHeader" and "apiKeyParameter" authenticators
* #45 Allow parameterized templates in mapped target "url" 

## 1.0

## 1.0.RC1

* #24 Response time healthcheck
* #25 Logging client requests
* #31 Cleaning up APIs deprecated since <= 0.25
* #32 Update Jackson to 2.9.5
* #33 Metrics renaming to follow naming convention
* #38 Exception on JDK11
* #39 Upgrading Jersey to 2.27

## 0.25

* #21 Implement token refresh capability for OAuth2 authentication
* #23 Ability to register healthchecks for remote web services
* #26 HttpTargets : Configuring named targets
* #27 OAuth2TokenAuthenticator must create its internal Client via HttpClientFactory
* #28 Named truststores, Client builder
* #29 Flip 'followRedirects' config default to true, clarify other defaults
* #30 Upgrade to bootique-modules-parent 0.8

## 0.24

* #20 Oauth2AuthenticatorFactory should not get token on startup
* #22 StackOverflowError in Oauth2AuthenticatorFactory or we should not reuse Configuration in AuthenticatorFactory

## 0.10

* #19 Upgrade to BQ 0.23 

## 0.9

* #18 Upgrade to bootique 0.22 , replace contribute API with "extend"

## 0.8

* #17 Bootique 0.21 and annotated config help

## 0.7

* #15 Support for configuring trust channel
* #16 Upgrade to Bootique 0.20

## 0.6

* #10 Support Gzip compression by default
* #14 Move to io.bootique namespace.

## 0.5

* #11  Use a different Set<Feature> from bootique-jersey
* #12 bootique-jersey-client-instrumented
* #13 Upgrade to BQ 0.17

## 0.4

* #8 Client requests logger
* #9 AuthenticatorFactory should have access to Injector

## 0.3

* #4 Support for OAuth 2 authenticator
* #5 Support for client-side Feature contributions
* #6 Support injection into services that are registered via features
* #7 Upgrade to Bootique 0.14

## 0.2:

* #2 Support for authentication filters
* #3 Switch to ServiceLoader-based polymorphic configs for AuthenticatorFactory

## 0.1:

* #1 Initial client implementation
