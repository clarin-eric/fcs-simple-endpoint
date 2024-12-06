# Changelog

# [1.8.0](https://github.com/clarin-eric/fcs-simple-endpoint/releases/tag/FCSSimpleEndpoint-1.8.0) - 2024-12-06

- Additions:
  - Add `EndpointDescription#getResource`, `SimpleEndpointDescription#getResourcePids` helper functions
  - Add `authenticated-search` capability
  - Add `<AvailabilityRestriction>` support (serialization, enum, field in `ResourceInfo`)
  - Add functions to `AuthenticationProvider` to load keys from any InputStream
  - Add support for JWT Issuer claim verification
    - New `web.xml` parameter: `eu.clarin.sru.server.fcs.authentication.issuer.<key-id>`
  - Add support for [JWKS](https://auth0.com/docs/secure/tokens/json-web-tokens/json-web-key-sets) public key configuration (used to verify JWT authentication headers)
    - New `web.xml` parameter: `eu.clarin.sru.server.fcs.authentication.jwks.<key-id>`
    - requires FCS clients (JWT Issuers) to be publicly available with a JWKS endpoint at `https://<fcs-client>/.well-known/jwks.json` with a **single** public RSA key!

- Changes:
  - Change visibility of a few useful FCS constants for library users (capability URLs, SRU FCS request extension parameters)
  - Fix breaking change due to `fcs-sru-server:1.11.0` interface change

- Dependencies:
  - Bump [`sru-server`](https://github.com/clarin-eric/fcs-sru-server) to `1.11.0`
  - Bump `org.slf4j` to `2.0.16`
  - Change `org.slf4j:slf4j-log4j12` to `org.slf4j:slf4j-reload4j`
  - Bump `org.bouncycastle:bcprov-ext-jdk15on` to `org.bouncycastle:bcprov-jdk18on:1.79`
  - Bump `com.auth0:java-jwt` to `4.4.0`
  - Add `com.auth0:jwks-rsa:0.22.1` (JWKS, for public RSA key retrieval)

# [1.7.0](https://github.com/clarin-eric/fcs-simple-endpoint/releases/tag/FCSSimpleEndpoint-1.7.0) - 2024-02-02

- Additions:
  - Add support for `<Institution>` element to `<Resource>`s in `<EndpointDescription>`.
  - Add [Github Pages](https://clarin-eric.github.io/fcs-simple-endpoint/) with [JavaDoc](https://clarin-eric.github.io/fcs-simple-endpoint/project-reports.html)
  - Add Changelog document

- Bug Fixes:
  - Typos

- Dependencies:
  - Bump [`sru-server`](https://github.com/clarin-eric/fcs-sru-server) to `1.10.0`
  - Add `maven-release-plugin`
  - Bump Maven build plugin versions
  - Bump `org.slf4j` to `1.7.36`
  - Bump `com.auth0:java-jwt` to `3.19.4`

- General:
  - Cleanup, whitespaces, copyright

For older changes, see commit history at [https://github.com/clarin-eric/fcs-simple-endpoint/commits/main/](https://github.com/clarin-eric/fcs-simple-endpoint/commits/main/?after=7ae7147468decda43891311aae8925033e12ae2d+0&branch=main&qualified_name=refs%2Fheads%2Fmain)