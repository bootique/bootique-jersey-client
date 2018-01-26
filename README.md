[![Build Status](https://travis-ci.org/bootique/bootique-jersey-client.svg)](https://travis-ci.org/bootique/bootique-jersey-client)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.bootique.jersey.client/bootique-jersey-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.bootique.jersey.client/bootique-jersey-client/)

# bootique-jersey-client

Integrates JAX-RS-based HTTP client in [Bootique](http://bootique.io) with support for various types of 
server authentication (BASIC, OAuth2, etc.). Implementation is built on top of [Jersey](https://jersey.java.net/)
and Grizzly connector.
 
## Quick Start

Add the module to your Bootique app:

```xml
<dependency>
	<groupId>io.bootique.jersey.client</groupId>
	<artifactId>bootique-jersey-client</artifactId>
</dependency>
```

Or if you want HTTPS clients with health checks and metrics:

```xml
<dependency>
	<groupId>io.bootique.jersey.client</groupId>
	<artifactId>bootique-jersey-client-instrumented</artifactId>
</dependency>
```

Inject `HttpClientFactory` and create client instances:

```java
@Inject
private HttpClientFactory clientFactory;

public void doSomething() {

    Client client = clientFactory.newClient();
    Response response = client
        .target("https://example.org")
        .request()
        .get();
} 
```

## Configuring Connection Parameters

You can specify a number of runtime parameters for your HTTP clients via
the app ```.yml``` (or any other Bootique configuration mechanism):

```yml
jerseyclient:
  followRedirects: true
  readTimeoutMs: 2000
  connectTimeoutMs: 2000
  asyncThreadPoolSize: 10
```

## Mapping URL Targets

In the example above we injected client factory, and had to hardcoded the
endpoint URL in Java. Instead you can map multiple URLs in the ```.yml```,
assigning each URL a symbolic name and optionally providing URL-specific
runtime parameters.

```yml
jerseyclient:
  targets:
    google:
      url: "https://google.com"
    bootique:
      url: "https://bootique.io"
      followRedirects: false
```


Now you can inject `HttpTargets` instance, and use it to access specific
targets by name:

```java
@Inject
private HttpTargets targets;

public void doSomething() {

    Response response = targets.newTarget("bootique").request().get();
}
```
This not only reduces the amount of code, but more importantly allows
to manage your URLs (and their runtime parameters) via configuration.
E.g. you might use a different URL between test and production environments.

## Using BASIC Authentication

Create named auth configuration (can be more than one):

```yml
jerseyclient:
  auth:
    myauth:
      type: basic
      username: myuser
      password: mypassword
```

Reference named configuration when creating a client:
```java
@Inject
private HttpClientFactory clientFactory;

public void doSomething() {

    Client client = clientFactory.newAuthenticatedClient("myauth");
    Response response = client
        .target("https://example.org")
        .request()
        .get();
} 
```
Or reference it to in a named target:

```yml
jerseyclient:
  ...
  targets:
    secret:
      url: "https://example.org"
      auth: myauth
```

## Using OAuth2 Authentication

OAuth2 authentication is very similar tio BASIC. They are no different
on the Java end. In YAML the type is "oauth2", and an extra key - "tokenUrl" is required.
Here is an example auth for a Twitter client:

```yml
jerseyclient:
  auth:
    twitter:
      type: oauth2
      tokenUrl: https://api.twitter.com/oauth2/token
      username: sdfjkdferefxfkdsf
      password: Efcdsfdsflkurecdsfj 
```

