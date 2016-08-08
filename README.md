[![Build Status](https://travis-ci.org/bootique/bootique-jersey-client.svg)](https://travis-ci.org/bootique/bootique-jersey-client)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.bootique.jersey.client/bootique-jersey-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.bootique.jersey.client/bootique-jersey-client/)

# bootique-jersey-client

Integrates JAX-RS-based HTTP client in [Bootique](http://bootique.io) with support for various types of 
server authentication (BASIC, OAuth2, etc.). Implementation is built on top of [Jersey](https://jersey.java.net/) 
and Grizzly connector.
 
## Quick Configuration

Add the module to your Bootique app:

```xml
<dependency>
	<groupId>id.bootique.jersey.client</groupId>
	<artifactId>bootique-jersey-client</artifactId>
</dependency>
```

Or if you want to use metrics, add the instrumented flavor of the client instead:

```xml
<dependency>
	<groupId>id.bootique.jersey.client</groupId>
	<artifactId>bootique-jersey-client-instrumented</artifactId>
</dependency>
```

Configure Jersey client module in your app ```.yml``` (or via any other Bootique-compatible mechanism). E.g.:

```yml
jerseyclient:
  followRedirects: true
  readTimeoutMs: 2000
  connectTimeoutMs: 2000
  asyncThreadPoolSize: 10
```

Inject ```HttpClientFactory``` and create client instances:

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

## Using Oauth2 Authentication

Very similar to BASIC above, but the type is "oauth2", and a new key - "tokenUrl" is required. Here is an example of
getting a Twitter client:

```yml
jerseyclient:
  auth:
    twitter:
      type: oauth2
      tokenUrl: https://api.twitter.com/oauth2/token
      username: sdfjkdferefxfkdsf
      password: Efcdsfdsflkurecdsfj 
```

```java
@Inject
private HttpClientFactory clientFactory;

public void doSomething() {

    Client client = clientFactory.newAuthenticatedClient("twitter");
    Response response = client
        .target("https://api.twitter.com/1.1/search/tweets.json")
        .queryParam("q", "BootiqueProject") 
        .request()
        .get();
} 
```
