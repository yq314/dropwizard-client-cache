# dropwizard-client-cache

[![Build Status](https://travis-ci.org/yq314/dropwizard-client-cache.svg?branch=master)](https://travis-ci.org/yq314/dropwizard-client-cache)
[![Coverage Status](https://img.shields.io/coveralls/yq314/dropwizard-client-cache.svg)](https://coveralls.io/r/yq314/dropwizard-client-cache)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.yq314/dropwizard-client-cache/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.yq314/dropwizard-client-cache/)

Provides a HttpClientBuilder implementation that uses [http-client-cache](https://hc.apache.org/httpcomponents-client-4.5.x/tutorial/html/caching.html) to support response caching.

## Dropwizard Version Support

Only supports Dropwizard v2.0.x

## Usage

Add dependency on library.
```xml
<dependency>
  <groupId>com.github.yq314</groupId>
  <artifactId>dropwizard-client-cache</artifactId>
  <version>${dropwizard-client-cache.version}</version>
</dependency>
```

