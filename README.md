<p align="center">
    <a href="https://github.com/snowdrop/jcloud-unit/graphs/contributors" alt="Contributors">
        <img src="https://img.shields.io/github/contributors/snowdrop/jcloud-unit"/></a>
    <a href="https://github.com/snowdrop/jcloud-unit/pulse" alt="Activity">
        <img src="https://img.shields.io/github/commit-activity/m/snowdrop/jcloud-unit"/></a>
    <a href="https://github.com/snowdrop/jcloud-unit/actions/workflows/push.yaml" alt="Build Status">
        <img src="https://github.com/snowdrop/jcloud-unit/actions/workflows/push.yaml/badge.svg"></a>
    <a href="https://github.com/snowdrop/jcloud-unit" alt="Top Language">
        <img src="https://img.shields.io/github/languages/top/snowdrop/jcloud-unit"></a>
    <a href="https://github.com/snowdrop/jcloud-unit" alt="Coverage">
        <img src=".github/badges/jacoco.svg"></a>
</p>

# jCloud Unit

The framework is designed using Extension Model architecture patterns, so supporting additional features or environments options
like AWS is possible by implementing extension points and adding dependencies into the classpath.

Main features:

- Easily deploy multiple applications and third party components in a single scenario
- Write the test scenario once and run it everywhere (cloud, bare metal, etc.)
- Developer and Test friendly
- Test isolation: for example, in OpenShift or Kubernetes, tests will be executed in an ephemeral namespace

# Requirements

- JDK 11+
- Maven 3+
- Docker