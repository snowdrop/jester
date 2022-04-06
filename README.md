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

jCloud Unit is a [JUnit 5](https://junit.org/junit5) extension that allows us to test our applications in cloud environments
like Kubernetes.

Main features:

- Easily deploy multiple applications and third-party components in a single test
- Write the test once and run it everywhere (Kubernetes, bare metal, etc.)
- Developer and Test friendly
- Test isolation: ephemeral namespace, configuration, ...

Concepts:
- `JCloud` - entry point for the jCloud JUnit 5 extension
- `Services` - how to use services in your tests
- `Managed Resource` - how resources are going to be deployed in the target test? The framework will locate the right managed resource and attach it to the service. This is transparently done for users. 

Content:

- [Getting Started](#getting-started)
  - [With Containers](#jcloud-containers)
  - [With Quarkus](#jcloud-quarkus)
  - [With Spring Boot](#jcloud-spring)
  - [With Operators](#jcloud-operators)
  - [With JMH Benchmarks](#jcloud-benchmarks)
- [Services](#services)
  - [Default Service](#default-service)
  - [REST Service](#rest-service)
  - [Kafka Operator Service](#kafka-operator-service)
- [Configuration](#configuration)
  - [Common](#service-configuration)
  - [Kubernetes](#kubernetes-configuration)
  - [Docker on Local](#docker-service-configuration)
  - [Quarkus](#quarkus-service-configuration)
  - [Spring](#spring-service-configuration)
- [Architecture](#architecture)
- [Contributing](#contributing)

## Getting Started

The framework has been designed to be fully extensible via jCloud dependencies. In addition, we can extend the
framework via the Extensions API to support new target environments and/or add new features.

Let's find out the existing jCloud dependencies and their features:

| Dependencies | Description | 
|--------------|-------------| 
| [jcloud-core](#jcloud-core)       | API, JUnit extension, allow using `@JCloud` and `@RunOnKubernetes` |
| [jcloud-containers](#jcloud-containers) | Allow using `@Container` to run  and `@LocalProject` annotations |
| [jcloud-quarkus](#jcloud-quarkus)    | Allow using `@Quarkus` annotation |
| [jcloud-spring](#jcloud-spring)     | Allow using `@Spring` annotation |

### Requirements

- JDK 11+
- Maven 3+
- Docker
- (for Kubernetes tests), you must be logged into the Kubernetes cluster.

### jCloud Core

This dependency is the minimal requirement to run the framework and includes all the necessary APIs to extend the functionality. 
To know more about the API and the extension API, go to the [Architecture](#architecture) section.

### jCloud Containers

This extension allows using containers to run tests on bare metal and/or Kubernetes. 

In this guide, we'll use the image: `quay.io/<your username>/quarkus-test:latest` (to generate this image, you need to go to [this folder](images/quarkus-rest) and execute `mvn clean install -Dquarkus.container-image.push=true -Dquarkus.container-image.registry=quay.io -Dquarkus.container-image.group=<your username>`). 

Let's start by adding the jCloud containers dependency into the Maven pom file:

```xml
<dependencies>
  <dependency>
    <groupId>io.jcloud</groupId>
    <artifactId>jcloud-containers</artifactId>
    <scope>test</scope>
  </dependency>
<dependencies>
```

And add the test that will run the above image and map the container port 8080 to a local port:

```java
import io.jcloud.api.Container;
import io.jcloud.api.RestService;
import io.jcloud.api.JCloud;

@JCloud
public class ContainerTest {

    @Container(image = "quay.io/<your username>/quarkus-test:latest", ports = 8080, expectedLog = "Installed features")
    static RestService app = new RestService();

    @Test
    public void testServiceIsUpAndRunning() {
        app.given.get("/hello").then().statusCode(HttpStatus.SC_OK);
    }
}
```

The `RestService` service implementation will resolve the mapped port 8080 for HTTP endpoints and the service host. 

When running the test, it should pass, and we should see the app logs:

```
[14:12:39.210] [INFO] [greetings] Initialize service (quay.io/<your username>/quarkus-test:latest) 
[14:12:44.736] [INFO] [greetings] Starting the Java application using /opt/jboss/container/java/run/run-java.sh ... 
[14:12:44.739] [INFO] [greetings] INFO exec  java -Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager -XX:+UseParallelGC -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=20 -XX:GCTimeRatio=4 -XX:AdaptiveSizePolicyWeight=90 -XX:+ExitOnOutOfMemoryError -cp "." -jar /deployments/quarkus-run.jar  
[14:12:44.741] [INFO] [greetings] __  ____  __  _____   ___  __ ____  ______  
[14:12:44.744] [INFO] [greetings]  --/ __ \/ / / / _ | / _ \/ //_/ / / / __/  
[14:12:44.747] [INFO] [greetings]  -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \    
[14:12:44.749] [INFO] [greetings] --\___\_\____/_/ |_/_/|_/_/|_|\____/___/    
[14:12:44.751] [INFO] [greetings] 2022-03-21 13:12:42,028 INFO  [io.quarkus] (main) quarkus-rest 1.0.0-SNAPSHOT on JVM (powered by Quarkus 2.7.4.Final) started in 0.639s. Listening on: http://0.0.0.0:8080 
[14:12:44.754] [INFO] [greetings] 2022-03-21 13:12:42,041 INFO  [io.quarkus] (main) Profile prod activated.  
[14:12:44.756] [INFO] [greetings] 2022-03-21 13:12:42,041 INFO  [io.quarkus] (main) Installed features: [cdi, resteasy-reactive, smallrye-context-propagation, vertx] 
[14:12:45.080] [INFO] [greetings] Service started (quay.io/<your username>/quarkus-test:latest) 
[14:12:45.643] [INFO] ## Running test ServiceLifecycleIT.testServiceIsUpAndRunning() 
[14:12:49.858] [INFO] [greetings] Service stopped (quay.io/<your username>/quarkus-test:latest)
```

Now, let's reuse the same test to run it in Kubernetes. For doing this, we can either extend our test `ContainerTest` with a new test class and the annotation `@RunOnKubernetes`:

```java
@RunOnKubernetes
public class KubernetesContainerIT extends ContainerTest {
    
}
```

Or we can run the test via command line using the property `ts.jcloud.target=kubernetes`.

Kubernetes will try to pull the image from a container registry (by default, it's `localhost:5000`). We can provide the registry via the property `ts.services.all.image.registry=quay.io`, or add this property in the `test.properties` or `global.properties` or configure your service using the `@ServiceConfiguration` annotation. More about how to configure your services in the [Configuration](#configuration) section.

When running the Kubernetes test, we should see the app logs again and also the Kubernetes commands that the framework used:

```
[14:15:05.347] [INFO] [greetings] Initialize service (quay.io/<your username>/quarkus-test:latest) 
[14:15:05.414] [INFO] Running command: kubectl apply -f /home/jcarvaja/sources/snowdrop/jcloud-unit/jcloud-containers/target/KubernetesServiceLifecycleIT/greetings/kubernetes.yml -n ts-dnnsgsrtrj 
[14:15:05.600] [INFO] kubectl: deployment.apps/greetings created 
[14:15:05.605] [INFO] Running command: kubectl expose deployment greetings --port=8080 --name=greetings -n ts-dnnsgsrtrj 
[14:15:05.726] [INFO] kubectl: service/greetings exposed 
[14:15:05.730] [INFO] Running command: kubectl scale deployment/greetings --replicas=1 -n ts-dnnsgsrtrj 
[14:15:05.840] [INFO] kubectl: deployment.apps/greetings scaled 
[14:15:06.741] [INFO] [greetings] __  ____  __  _____   ___  __ ____  ______  
[14:15:06.744] [INFO] [greetings]  --/ __ \/ / / / _ | / _ \/ //_/ / / / __/  
[14:15:06.747] [INFO] [greetings]  -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \    
[14:15:06.749] [INFO] [greetings] --\___\_\____/_/ |_/_/|_/_/|_|\____/___/    
[14:15:06.751] [INFO] [greetings] 2022-03-21 13:12:42,028 INFO  [io.quarkus] (main) quarkus-rest 1.0.0-SNAPSHOT on JVM (powered by Quarkus 2.7.4.Final) started in 0.639s. Listening on: http://0.0.0.0:8080 
[14:15:06.754] [INFO] [greetings] 2022-03-21 13:12:42,041 INFO  [io.quarkus] (main) Profile prod activated.  
[14:15:06.756] [INFO] [greetings] 2022-03-21 13:12:42,041 INFO  [io.quarkus] (main) Installed features: [cdi, resteasy-reactive, smallrye-context-propagation, vertx] 
[14:15:06.080] [INFO] [greetings] Service started (quay.io/<your username>/quarkus-test:latest) 
[14:15:06.643] [INFO] ## Running test KubernetesServiceLifecycleIT.testServiceIsUpAndRunning() 
[14:15:10.858] [INFO] [greetings] Service stopped (quay.io/<your username>/quarkus-test:latest)
```

**Note**: the generated resources are placed at `target/<SERVICE NAME>/kubernetes.yml`

Find one example using Containers in [here](examples/quarkus-oidc).

#### Containers from a local project

If your service can be shipped within a container and the sources are in a local folder, we can use the annotation `@LocalProject`. Let's see how to use the same services from the previous example but using this annotation:

```java
import io.jcloud.api.LocalProject;
import io.jcloud.api.RestService;
import io.jcloud.api.JCloud;

@JCloud
public class LocalProjectTest {

    @LocalProject(location = "../images/quarkus-rest", 
            buildCommands = { "mvn", "clean", "install" }, 
            dockerfile = "../images/quarkus-rest/src/main/docker/Dockerfile.jvm",
            ports = 8080, 
            expectedLog = "Installed features")
    static RestService app = new RestService();

    @Test
    public void testServiceIsUpAndRunning() {
        app.given.get("/hello").then().statusCode(HttpStatus.SC_OK);
    }
}
```

**Note**: paths are relative to the project where the test is located. 

Using `@LocalProject`, we don't need to previously build and/or push the container. 

### jCloud Quarkus

This extension allows testing Quarkus applications within the same module without the need of using containers. Let's see how to use it. It's important to note that this extension does not bring any Quarkus dependencies.

The first thing you need is the Quarkus project where we'll add our tests using jCloud. To create a Quarkus project, follow the [Getting Started from Quarkus guide](https://quarkus.io/guides/getting-started).

Then, we need to add the jCloud Quarkus dependency into the Maven pom file:

```xml
<dependencies>
  <dependency>
    <groupId>io.jcloud</groupId>
    <artifactId>jcloud-quarkus</artifactId>
    <scope>test</scope>
  </dependency>
<dependencies>
```

And now, let's write our first test:

```java
@JCloud @Quarkus
public class GreetingResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             .body(is("Hello RESTEasy"));
    }

}
```

Output:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running org.acme.getting.started.GreetingResourceTest
08:38:57,019 INFO  JBoss Threads version 3.4.2.Final
08:38:58,054 Quarkus augmentation completed in 1479ms
08:38:58,054 INFO  Quarkus augmentation completed in 1479ms
08:38:58,072 INFO  [app] Initialize service (Quarkus JVM mode)
08:38:58,085 INFO  Running command: java -Dquarkus.log.console.format=%d{HH:mm:ss,SSS} %s%e%n -Dquarkus.http.port=1101 -jar /home/jcarvaja/sources/tmp/getting-started/target/GreetingResourceTest/app/quarkus-app/quarkus-run.jar
08:39:01,130 INFO  [app] __  ____  __  _____   ___  __ ____  ______ 
08:39:01,134 INFO  [app]  --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
08:39:01,135 INFO  [app]  -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
08:39:01,136 INFO  [app] --\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
08:39:01,137 INFO  [app] 08:38:58,980 Quarkus 2.3.0.Final on JVM started in 0.813s. Listening on: http://0.0.0.0:1101
08:39:01,138 INFO  [app] 08:38:58,985 Profile prod activated. 
08:39:01,139 INFO  [app] 08:38:58,986 Installed features: [cdi, resteasy, smallrye-context-propagation, vertx]
08:39:01,147 INFO  [app] Service started (Quarkus JVM mode)
08:39:01,575 INFO  ## Running test GreetingResourceTest.testHelloEndpoint()
08:39:06,804 INFO  [app] Service stopped (Quarkus JVM mode)
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 12.72 s - in org.acme.getting.started.GreetingResourceTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

About the Quarkus configuration, the test framework will use the `application.properties` file at `src/main/resources` folder, if you want to use a different application properties file for all tests only, you can add the `application.properties` file at `src/test/resources` and the test framework will use this instead.

Find this Quarkus example in [here](examples/quarkus-greetings).

To configure the Quarkus services, go to the [Quarkus Configuration](#quarkus-service-configuration) section.

### Native

The jCloud Quarkus extension is fully compatible with Native compilation. We can enable the native configuration using the recommended approach by Quarkus via the `native` Maven profile:

```xml
<profile>
  <id>native</id>
  <activation>
    <property>
      <name>native</name>
    </property>
  </activation>
  <build>
    <plugins>
      <plugin>
        <artifactId>maven-failsafe-plugin</artifactId>
        <executions>
          <execution>
            <configuration>
              <systemProperties>
                <native.image.path>${project.build.directory}/${project.build.finalName}-runner</native.image.path>
                <quarkus.package.type>${quarkus.package.type}</quarkus.package.type>
              </systemProperties>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
  <properties>
    <quarkus.package.type>native</quarkus.package.type>
  </properties>
</profile>
```

And create the Native version of our test by extending `GreetingResourceTest`:

```java
public class NativeGreetingResourceIT extends GreetingResourceTest {
}
```

Finally, run the Maven command using the standard Native Quarkus instructions (more in [here](https://quarkus.io/guides/building-native-image)):

```s
mvn clean verify -Dnative
```

### Build/Runtime properties

Quarkus performs many optimizations when building the Quarkus application to boost the application performance. These optimizations are based on build-time properties. The build-time properties are normal application properties but that are only taken into account when building the application. So, once the application is built, users can only configure applications runtime properties.

To overcome this situation, the jCloud Quarkus extension will detect build-time properties provided by users at each test and if any, it will build the Quarkus application, so final users won't need to deal with build or runtime properties when testing Quarkus. If no build properties are provided, then the framework will reuse the binary generated by Maven. 

For example:

```java
@JCloud
public class PingPongResourceIT {

    @Quarkus
    static final RestService app = new RestService()
        .withProperty("io.quarkus.qe.PongClient/mp-rest/url", "http://host:port") // runtime property!
        .withProperty("quarkus.datasource.db-kind", "h2"); // build property!

    // ...
}
```

The framework will detect that `quarkus.datasource.db-kind` is a build property, so it will trigger a new build.

### Multiple Quarkus Applications

In the previous example, we have created our first test using the test framework, configured the Failsafe Maven plugin, and execute our tests on Native. Let's now create a test with multiple Quarkus instances.

First, we're going to create a Ping-Pong application with the following endpoints:

`PingResource.java`:
```java
@Path("/ping")
public class PingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "ping";
    }
}
```

`PongResource.java`:
```java
@Path("/pong")
public class PongResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String pong() {
        return "pong";
    }
}
```

Let's write our test:

```java
@JCloud @Quarkus
public class PingPongResourceIT {
    @Test
    public void shouldPingPongWorks() {
        given().get("/ping").then().statusCode(HttpStatus.SC_OK).body(is("ping"));
        given().get("/pong").then().statusCode(HttpStatus.SC_OK).body(is("pong"));
    }
}
```

In this test, we're starting only 1 instance with all the resources, but what about if we want to create multiple instances with different sources. Let's see how we can do it using the test framework:

```java
@JCloud
public class PingPongResourceIT {

    @Quarkus(classes = PingResource.class)
    static final RestService ping = new RestService();

    @Quarkus(classes = PongResource.class)
    static final RestService pong = new RestService();

    // will include ping and pong resources
    @Quarkus
    static final RestService pingpong = new RestService();

    @Test
    public void shouldPingWorks() {
        ping.given().get("/ping").then().statusCode(HttpStatus.SC_OK).body(is("ping"));
        ping.given().get("/pong").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void shouldPongWorks() {
        pong.given().get("/pong").then().statusCode(HttpStatus.SC_OK).body(is("pong"));
        pong.given().get("/ping").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void shouldPingPongWorks() {
        pingpong.given().get("/ping").then().statusCode(HttpStatus.SC_OK).body(is("ping"));
        pingpong.given().get("/pong").then().statusCode(HttpStatus.SC_OK).body(is("pong"));
    }
}
```

### Forced Dependencies

We can also specify dependencies per Quarkus application that are not part of the Maven `pom.xml` file by doing:

```java
@JCloud
public class GreetingResourceIT {

    private static final String HELLO = "Hello";
    private static final String HELLO_PATH = "/hello";

    @Quarkus(dependencies = @Dependency(groupId = "io.quarkus", artifactId = "quarkus-resteasy"))
    static final RestService classic = new RestService();

    @Quarkus(dependencies = @Dependency(groupId = "io.quarkus", artifactId = "quarkus-resteasy-reactive"))
    static final RestService reactive = new RestService();

    @Test
    public void shouldPickTheForcedDependencies() {
        // classic
        classic.given().get(HELLO_PATH).then().body(is(HELLO));

        // reactive
        reactive.given().get(HELLO_PATH).then().body(is(HELLO));
    }
}
```

If no group ID and no version are provided, the framework will assume that the dependency is a Quarkus extension, so it will use the `quarkus.platform.groupId` (or `io.quarkus`) and the default Quarkus version.

This also can be used to append other dependencies apart from Quarkus.

### Disable Tests annotations

- On a Concrete Quarkus version:

```java
@JCloud
@DisabledOnQuarkusVersion(version = "1\\.13\\..*", reason = "https://github.com/quarkusio/quarkus/issues/XXX")
public class GreetingResourceIT {
    
}
```

This test will not run if the quarkus version is `1.13.X`.

Moreover, if we are building Quarkus upstream ourselves, we can also disable tests on Quarkus upstream snapshot version (999-SNAPSHOT) using `@DisabledOnQuarkusSnapshot`.

- On Native build:

```java
@DisabledOnNative
public class OnlyOnJvmIT {
    
}
```

This test will be disabled if we run the test on Native. Similarly, we can enable tests to be run only on Native build by using the `@EnabledOnNative` annotation.

### jCloud Spring

This extension allows testing Spring Boot applications within the same module without the need of using containers. Let's see how to use it. It's important to note that this extension does not bring any Spring Boot dependencies.

The first thing you need is the Spring project where we'll add our tests using jCloud. To create a Spring Boot project, follow the [Getting Started from Spring Boot guide](https://spring.io/guides/gs/spring-boot/).

Then, we need to add the jCloud Spring dependency into the Maven pom file:

```xml
<dependencies>
  <dependency>
    <groupId>io.jcloud</groupId>
    <artifactId>jcloud-spring</artifactId>
    <scope>test</scope>
  </dependency>
<dependencies>
```

And now, let's write our first test:

```java
@JCloud @Spring
public class GreetingResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             .body(is("Hello World"));
    }

}
```

Output:

```
[09:11:50.920] [INFO] [spring] Initialize service (Spring Boot) 
[09:11:50.953] [INFO] [spring] Running command: java -Dserver.port=1101 -jar /home/jcarvaja/sources/snowdrop/jcloud-unit/examples/spring-greetings/target/examples-spring-greetings-0.0.0-SNAPSHOT.jar 
[09:11:55.011] [INFO] [spring]   .   ____          _            __ _ _ 
[09:11:55.016] [INFO] [spring]  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \ 
[09:11:55.018] [INFO] [spring] ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \ 
[09:11:55.020] [INFO] [spring]  \\/  ___)| |_)| | | | | || (_| |  ) ) ) ) 
[09:11:55.023] [INFO] [spring]   '  |____| .__|_| |_|_| |_\__, | / / / / 
[09:11:55.025] [INFO] [spring]  =========|_|==============|___/=/_/_/_/ 
[09:11:55.027] [INFO] [spring]  :: Spring Boot ::                (v2.6.4) 
[09:11:55.029] [INFO] [spring] 2022-03-22 09:11:51.980  INFO 301749 --- [           main] i.j.e.s.greetings.GreetingApplication    : Starting GreetingApplication using Java 11.0.14.1 on localhost.localdomain with PID 301749 (/home/jcarvaja/sources/snowdrop/jcloud-unit/examples/spring-greetings/target/examples-spring-greetings-0.0.0-SNAPSHOT.jar started by jcarvaja in /home/jcarvaja/sources/snowdrop/jcloud-unit/examples/spring-greetings/target/GreetingApplicationIT/spring) 
[09:11:55.031] [INFO] [spring] 2022-03-22 09:11:51.984  INFO 301749 --- [           main] i.j.e.s.greetings.GreetingApplication    : No active profile set, falling back to 1 default profile: "default" 
[09:11:55.034] [INFO] [spring] 2022-03-22 09:11:53.007  INFO 301749 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 1101 (http) 
[09:11:55.037] [INFO] [spring] 2022-03-22 09:11:53.017  INFO 301749 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat] 
[09:11:55.039] [INFO] [spring] 2022-03-22 09:11:53.018  INFO 301749 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.58] 
[09:11:55.042] [INFO] [spring] 2022-03-22 09:11:53.086  INFO 301749 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext 
[09:11:55.044] [INFO] [spring] 2022-03-22 09:11:53.086  INFO 301749 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1041 ms 
[09:11:55.046] [INFO] [spring] 2022-03-22 09:11:53.547  INFO 301749 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 1101 (http) with context path '' 
[09:11:55.048] [INFO] [spring] 2022-03-22 09:11:53.558  INFO 301749 --- [           main] i.j.e.s.greetings.GreetingApplication    : Started GreetingApplication in 2.034 seconds (JVM running for 2.587) 
[09:11:56.055] [INFO] [spring] Service started (Spring Boot) 
[09:11:56.566] [INFO] ## Running test GreetingApplicationIT.testSpringApp() 
[09:11:58.340] [INFO] [spring] 2022-03-22 09:11:57.218  INFO 301749 --- [nio-1101-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet' 
[09:11:58.342] [INFO] [spring] 2022-03-22 09:11:57.218  INFO 301749 --- [nio-1101-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet' 
[09:11:58.344] [INFO] [spring] 2022-03-22 09:11:57.219  INFO 301749 --- [nio-1101-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms 
[09:11:59.456] [INFO] [spring] Service stopped (Spring Boot) 
```

The same test works locally and in Kubernetes.

Find this Spring example in [here](examples/spring-greetings).

To configure the Spring services, go to the [Configuration](#spring-service-configuration) section.

### jCloud Operators

This extension allows easily writing test cases with operators. 

First, we need to add the jCloud Operators dependency into the Maven pom file:

```xml
<dependencies>
  <dependency>
    <groupId>io.jcloud</groupId>
    <artifactId>jcloud-operators</artifactId>
    <scope>test</scope>
  </dependency>
<dependencies>
```

Then, we need to create our Custom Resource YAML file, for example, for Kafka:

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: Kafka
metadata:
  name: kafka-instance
spec:
  ...
```

Now, we can create an OperatorService to load this YAML as part of an Operator installation:

```java
@JCloud
@RunInKubernetes
public class OperatorExampleIT {

    @Operator(name = "my-operator", source = "...")
    static final OperatorService operator = new OperatorService().withCrd("kafka-instance", "/my-crd.yaml");

    @QuarkusApplication
    static final RestService app = new RestService();

    // ...
}
```

The framework will install the operator and load the Custom Resource YAML file.

**Note**: that the framework will wait for the operator to be installed before loading the CRD yaml files, but will not wait for the CRDs to be ready. If you are working with CRDs that update conditions, then we can ease this for you by providing the custom resource definition:

```java
@Version("v1beta2")
@Group("kafka.strimzi.io")
@Kind("Kafka")
public class KafkaInstanceCustomResource
        extends CustomResource<CustomResourceSpec, CustomResourceStatus>
        implements Namespaced {
}
```

And then registering the CRD with this type:

```java
@OpenShiftScenario
public class OperatorExampleIT {

    @Operator(name = "my-operator", source = "...")
    static final OperatorService operator = new OperatorService().withCrd("kafka-instance", "/my-crd.yaml", KafkaInstanceCustomResource.class);

    @QuarkusApplication
    static final RestService app = new RestService();

    // ...
}
```

Now, the framework will wait for the operator to be installed and the custom resource named `kafka-instance` to be with a condition "Ready" as "True".

### jCloud Benchmarks

This extension allows easily writing benchmarks using [Java Microbenchmark Harness (JMH)](https://github.com/openjdk/jmh) with the benefit of setting up tests using the jCloud extensions.

First, we need to add the jCloud Benchmark dependency into the Maven pom file:

```xml
<dependencies>
  <dependency>
    <groupId>io.jcloud</groupId>
    <artifactId>jcloud-benchmark</artifactId>
    <scope>test</scope>
  </dependency>
<dependencies>
```

And now, we need to extend our test class with the interface `EnableBenchmark`:

```java
@JCloud @Spring
public class GreetingResourceBenchmark implements EnableBenchmark {

    @Benchmark // Annotations from JMH tool
    @BenchmarkMode(Mode.Throughput)
    public ValidatableResponse helloEndpointThroughput() {
        return given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             .body(is("Hello World"));
    }

}
```

Output:

```
[14:34:22.912] [INFO] ## Running test GreetingApplicationBenchmark.benchmarkRunner() 
# JMH version: 1.35
# VM version: JDK 11.0.14.1, OpenJDK 64-Bit Server VM, 11.0.14.1+1
# VM invoker: /usr/lib/jvm/java-11-openjdk-11.0.14.1.1-5.fc35.x86_64/bin/java
# VM options: -ea -Didea.test.cyclic.buffer.size=1048576 -javaagent:/home/jcarvaja/.local/share/JetBrains/Toolbox/apps/IDEA-C/ch-0/212.5080.55/lib/idea_rt.jar=40447:/home/jcarvaja/.local/share/JetBrains/Toolbox/apps/IDEA-C/ch-0/212.5080.55/bin -Dfile.encoding=UTF-8
# Blackhole mode: full + dont-inline hint (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 1 iterations, 10 s each
# Measurement: 3 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 50 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: io.jcloud.examples.benchmark.apps.GreetingApplicationBenchmark.helloEndpointThroughput

# Run progress: 0,00% complete, ETA 00:00:40
# Fork: N/A, test runs in the host VM
# *** WARNING: Non-forked runs may silently omit JVM options, mess up profilers, disable compiler hints, etc. ***
# *** WARNING: Use non-forked runs only for debugging purposes, not for actual performance runs. ***
[14:34:28.748] [INFO] [springWeb] 2022-03-31 14:34:25.491  INFO 94104 --- [io-1101-exec-48] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet' 
[14:34:28.764] [INFO] [springWeb] 2022-03-31 14:34:25.492  INFO 94104 --- [io-1101-exec-48] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet' 
[14:34:28.766] [INFO] [springWeb] 2022-03-31 14:34:25.494  INFO 94104 --- [io-1101-exec-48] o.s.web.servlet.DispatcherServlet        : Completed initialization in 2 ms 
# Warmup Iteration   1: 547,141 ops/s
Iteration   1: 942,183 ops/s
Iteration   2: 1783,304 ops/s
Iteration   3: 3037,837 ops/s

Result "io.jcloud.examples.benchmark.apps.GreetingApplicationBenchmark.helloEndpointThroughput":
  1921,108 ±(99.9%) 19239,854 ops/s [Average]
  (min, avg, max) = (942,183, 1921,108, 3037,837), stdev = 1054,601
  CI (99.9%): [≈ 0, 21160,962] (assumes normal distribution)


# Run complete. Total time: 00:00:44

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                                              Mode  Cnt     Score       Error  Units
GreetingApplicationBenchmark.helloEndpointThroughput  thrpt    3  1921,108 ± 19239,854  ops/s

Benchmark result is saved to target/benchmarks-results/GreetingApplicationBenchmark.json
```

The benchmark results are saved at `target/benchmarks-results/<name of the benchmark class>.json`. 

**Note**: If you want to visualize the benchmark results in graphs, you can submit these files in [this JMH visualizer online](https://jmh.morethan.io/) to generate the plots and graphs. 

Find a more complex test that uses several Quarkus and Spring applications at the same benchmark in [here](examples/apps-benchmark).

## Services

The service is the point of contact between the test and the managed resource, regardless of the environment where the resource is running. Example:

```java
@Container(...)
DefaultService app = new DefaultService();
```

The service interface defines a Service API that the following common capabilities:

- Service Properties: `withProperty` and/or `withProperties` to provide additional properties via map or files

```java
@Container(...)
DefaultService app = new DefaultService()
        .withProperty("prop", "value")
        .withProperties("custom-file.properties");
```

- Service Lifecycle: `onPreStart` and/or `onPostStart` to add action hooks within the service lifecycle:

```java
@Container(...)
DefaultService app = new DefaultService()
        .onPreStart(() -> ...)
        .onPostStart(() -> ...);
```

- Services are startable and stoppable: `start`, `restart` and `stop` methods:

```java
@Container(...)
DefaultService app = new DefaultService();

@Test
public void testStart() {
    app.start();
    app.stop();
    app.restart();
}
```

- Services Logging: verify any traces that the managed resources are logging:

```java
@Container(...)
DefaultService app = new DefaultService();

@Test
public void verifyLogs() {
    app.logs().assertContains("This appears at app start");
}
```

### External Resources

To use an external resource from a local application, we simply need to configure the application with the location where the external resource is (for example: `to.property=/path/to/resource.config`). However, the same won't work when running the application in another environment like Kubernetes because the resource won't exist. 

To ease up the usage of external resources regardless if we run the test locally or in Kubernetes, we need to specify external resource locations by appending the `resource::` tag. For example: `.withProperty("to.property", "resource::/path/to/resource.config");` and this time it will work in all environments.

Internally, when deploying the application in Kubernetes, the framework will detect the external resource via the `resource::` tag and will deploy a ConfigMap containing the file and map it to the application container. 

The same works for secret resources: using the `secret::` tag. For example: `.withProperty("to.property", "secret::/file.yaml");`. For local, there will be no difference, but when deploying on Kubernetes, one secret will be pushed instead of a ConfigMap. This only works for file system resources (secrets from classpath are not supported).

### Test Expected Failures

With the test framework, we can assert startup failures using `service.setAutoStart(false)`. When disabling this flag, the
test framework will not start the service and users will need to manually start them by doing `service.start()` at each test case. For example:

```java
@Quarkus
static final RestService app = new RestService()
        .setAutoStart(false);

@Test
public void shouldFailOnStart() {
    // the service "app" is not started at this moment.
    // we now start it by doing "app.start()":
    assertThrows(AssertionError.class, () -> app.start(),
            "Should fail because runtime exception in ValidateCustomProperty");
    // even though the application has failed to start as expected, we can verify the service logs:
    app.logs().assertContains("Missing property a.b.z");
}
```

Also, we can try to fix the application during the test execution. For example, beforehand the application was not starting because there was a missing property "a.b.z", so if we add it, we can verify that now the application works:

```java
@Test
public void shouldWorkWhenPropertyIsCorrect() {
    app.withProperty("a.b.z", "here you have!");
    app.start();
    app.given().get("/hello").then().statusCode(HttpStatus.SC_OK);
}
```

### Services Start-Up Order

By default, the services are initialized in the natural order of presence. For example:

```java
class MyParent {
    @Quarkus
    static final RestService firstAppInParent = new RestService();

    @Quarkus
    static final RestService secondAppInParent = new RestService();

}

@JCloud
class MyChildIT extends MyParent {
    @Quarkus
    static final RestService firstAppInChild = new RestService();

    @Quarkus
    static final RestService secondAppInChild = new RestService();
}
```

Then, the framework will initialize the services at this order: `firstAppInParent`,  `secondAppInParent`, `firstAppInChild` and `secondAppInChild`.

We can change this order by using the `@LookupService` annotation:

```java
class MyParent {
    @LookupService
    static final RestService appInChild; // field name must match with the service name declared in MyChildIT.

    @Quarkus
    static final RestService appInParent = new RestService().withProperty("x", () -> appInChild.getHost());
}

@JCloud
class MyChildIT extends MyParent {
    @Quarkus
    static final RestService appInChild = new RestService();
}
```

**Note**: that field name of the `@LookupService` must match with the service name declared in MyChildIT.

Now, the framework will initialize the `appInChild` service first and then the `appInParent` service.

### Services Implementations

We can add custom implementations of services to share common functionality. The test framework provides the following services:

#### Default Service

This implementation is used when you do not need to interact with the managed resource at all via any network protocol.

Example:
```java
@JCloud
public class PingPongResourceIT {
    @Quarkus
    DefaultService ping = new DefaultService();
    
    // ...
}
```

#### REST Service

The REST service implementation will automatically configure REST assured by you using the host of the internal managed resource and the mapped port 8080 (it's configurable).

Example:
```java
@JCloud
public class PingPongResourceIT {
    @Quarkus
    static final RestService ping = new RestService();

    @Test
    public void shouldPingWorks() {
        ping.given().get("/ping").then().statusCode(HttpStatus.SC_OK).body(is("ping"));
        
        // or directly using RestAssured API
        given().get("/ping").then().statusCode(HttpStatus.SC_OK).body(is("ping"));
    }
}
```

#### Implement your custom service implementation

We can implement our custom services to, for example, support a different network protocol other than REST or have common functionality that we can reuse along with our test suite.

```java
public class YourCustomService extends BaseService<YourCustomService> {

    // your new methods
}
```

And use it:

```java
@JCloud
public class GreetingResourceIT {

    @Container // ... or @Quarkus ..
    static final YourCustomService app = new YourCustomService();
    
    // your methods will be available
}
```

#### Kafka Operator Service

The Kafka operator service implementation will automatically install the Strimzi Kafka operator and create an instance of Kafka. 

To use the Kafka Operator service, you need first to add the jCloud Service Kafka extension:

```xml
<dependencies>
  <dependency>
    <groupId>io.jcloud</groupId>
    <artifactId>jcloud-service-kafka</artifactId>
    <scope>test</scope>
  </dependency>
<dependencies>
```

And now, we can simply create our Kafka instance by doing:

Example:
```java
@JCloud
@RunOnKubernetes
public class KubernetesKafkaOperatorIT {
    @Operator(name = "strimzi-kafka-operator")
    static final KafkaOperatorService kafka = new KafkaOperatorService();
}
```

See an example in [here](jcloud-service-kafka/src/test/java/io/jcloud/test/KubernetesKafkaOperatorIT.java).

## Configuration

Each service can be configured via (1) annotations, (2) file properties, and (3) system properties; (in this order).

For (1) annotations, we can annotate the test using `@ServiceConfiguration` (for common properties):

```java
@JCloud
@JCloudConfiguration(forService = "app", startupTimeout = "10s")
public class MyTest {
    @Quarkus
    static final RestService app = new RestService();
    // ...
}
```

For jCloud extension configuration, there are additional annotations, for example: `@KubernetesServiceConfiguration`, `@DockerServiceConfiguration`, `@QuarkusServiceConfiguration`, ... 

For (2) file properties, you can add a properties file named `test.properties` at the `src/test/resources` folder where to place the service's properties. To use the same properties file for a multi-module test suite, you specify it using the system property `-Dts.test.resources.file.location=path/to/custom-global.properties`.

**Note**: The `<SERVICE NAME>` is the name of the service within the test. For example, in the following example, the `<SERVICE NAME>` value is `app`.

```java
@JCloud
public class PingPongResourceIT {

    @Quarkus
    static final RestService app = new RestService();

    // ...
}
```

If you want to configure all the services with the same property, replace `<SERVICE NAME>` with `all`.

### Service Configuration

The service configuration options that are common for all the services are:

| Name | Description | Default | Property | Annotation | 
|------|-------------|---------|----------|------------| 
| Start Up Timeout | | 5 min | `ts.services.<SERVICE NAME>.startup.timeout=5m` | `@ServiceConfiguration(forService = "<SERVICE NAME>", startupTimeout = "5m")` |
| Start Up Check Poll Interval | | 2 seconds | `ts.services.<SERVICE NAME>.startup.check-poll-interval=2s` | `@ServiceConfiguration(forService = "<SERVICE NAME>", startupCheckPollInterval = "2s")` |
| Factor Timeout | If your environment is twice and a half slower, then the factor timeout should be 2.5, so the rest of timeout properties will be incremented accordingly | 1.0 | `ts.services.<SERVICE NAME>.factor.timeout=1` | `@ServiceConfiguration(forService = "<SERVICE NAME>", factorTimeout = 1.0)` |
| Delete Service Folder On Close | Delete `/target/<SERVICE NAME>` folder on service close | true | `ts.services.<SERVICE NAME>.delete.folder.on.close=true` | `@ServiceConfiguration(forService = "<SERVICE NAME>", deleteFolderOnClose = true)` |
| Log Enabled | Enable/Disable the logs for the current service | true | `ts.services.<SERVICE NAME>.log.enabled=true` | `@ServiceConfiguration(forService = "<SERVICE NAME>", logEnabled = true)` |
| Log Level | Tune the log level for the current service. Possible values in {@link java.util.logging.Level} | INFO | `ts.services.<SERVICE NAME>.log.level=INFO` | `@ServiceConfiguration(forService = "<SERVICE NAME>", logLevel = "INFO")` |
| Port Range Min | Port resolution with range min | 1101 | `ts.services.<SERVICE NAME>.port.range.min=1101` | `@ServiceConfiguration(forService = "<SERVICE NAME>", portRangeMin = 1101)` |
| Port Range Max | Port resolution with range max | 49151 | `ts.services.<SERVICE NAME>.port.range.max=49151` | `@ServiceConfiguration(forService = "<SERVICE NAME>", portRangeMax = 49151)` |
| Port Resolution Strategy | Strategy to resolve the ports to assign to the service. Possible values are: "incremental" or "random" | incremental | `ts.services.<SERVICE NAME>.port.resolution.strategy=incremental` | `@ServiceConfiguration(forService = "<SERVICE NAME>", portResolutionStrategy = "incremental")` |
| Image Registry | Configure the image registry to use for services | localhost:5000 | `ts.services.<SERVICE NAME>.image.registry=localhost:5000` | `@ServiceConfiguration(forService = "<SERVICE NAME>", imageRegistry = "localhost:5000")` |

### Kubernetes Configuration

The configuration that is only available for Kubernetes deployments is:

| Name | Description | Default | Property | Annotation | 
|------|-------------|---------|----------|------------|
| Print cluster info on failures | Print pods, events and status when there are test failures | true  | `ts.kubernetes.print.info.on.error=true` | `@RunOnKubernetes(printInfoOnError = true)` |
| Delete namespace after all tests | Delete namespace after running all the tests | true  | `ts.kubernetes.delete.namespace.after.all=true` | `@RunOnKubernetes(deleteNamespaceAfterAll = true)` |
| Use ephemeral namespaces or the current logged namespace | Run the tests on Kubernetes in an ephemeral namespace that will be deleted afterwards | true  | `ts.kubernetes.ephemeral.namespaces.enabled=true` | `@RunOnKubernetes(ephemeralNamespaceEnabled = true)` |
| Load additional resources | Load the additional resources before running all the tests |  | `ts.kubernetes.additional-resources` | `@RunOnKubernetes(additionalResources = [...])` |
| Template | Template for the initial deployment resource. The custom template should be located at the `src/test/resources` folder |  | `ts.services.<SERVICE NAME>.kubernetes.template=/custom-deployment.yaml` | `@KubernetesServiceConfiguration(forService = "<SERVICE NAME>", template = "/custom-deployment.yaml")` |
| Use as internal service | Use internal routing instead of exposed network interfaces. This is useful to integration several services that are running as part of the same namespace or network |  | `ts.services.<SERVICE NAME>.kubernetes.use-internal-service-as-url=false` | `@KubernetesServiceConfiguration(forService = "<SERVICE NAME>", useInternalService = false)` |

### Docker Service Configuration

The configuration that is only available when running services annotated with `@Container` in local is:

| Name | Description | Default | Property | Annotation | 
|------|-------------|---------|----------|------------| 
| Privileged | Configure the running container using privileged mode | false | `ts.services.<SERVICE NAME>.docker.privileged-mode=FALSE` | `@DockerServiceConfiguration(forService = "<SERVICE NAME>", privileged = false)` |

### Quarkus Service Configuration

The configuration that is only available when running services annotated with `@Quarkus` is:

| Name | Description | Default | Property | Annotation | 
|------|-------------|---------|----------|------------| 
| Expected Log | Configure the expected log for the Quarkus service | Installed features | `ts.services.<SERVICE NAME>.quarkus.expected-log=Installed features` | `@QuarkusServiceConfiguration(forService = "<SERVICE NAME>", expectedLog = "Installed features")` |

### Spring Service Configuration

The configuration that is only available when running services annotated with `@Spring` is:

| Name | Description | Default | Property | Annotation | 
|------|-------------|---------|----------|------------| 
| Expected Log | Configure the expected log for the Spring service | initialization completed | `ts.services.<SERVICE NAME>.spring.expected-log=initialization completed` | `@SpringServiceConfiguration(forService = "<SERVICE NAME>", expectedLog = "initialization completed")` |

## Architecture

The framework has been designed to fully extend new features and/or customize the behavior via the Extension API. This API uses the [Java ServiceLoader API](https://docs.oracle.com/javase/9/docs/api/java/util/ServiceLoader.html) to load custom implementations.

Extension API:

- `Extension bootstrap point` - to set up common things along all the services. For example, [the Kubernetes extension bootstrap](jcloud-core/src/main/java/io/jcloud/core/extensions/KubernetesExtensionBootstrap.java) is used to create the Kubernetes namespace before running the tests and inject the Kubernetes client to all the services and tests. This extension is registered in [META-INF/services/io.jcloud.api.extensions.ExtensionBootstrap](jcloud-core/src/main/resources/META-INF/services/io.jcloud.api.extensions.ExtensionBootstrap).
- `Extension binding point` - create your custom annotations to deploy custom resources. For example, the [Container annotation](jcloud-containers/src/main/java/io/jcloud/api/Container.java) is registered in [META-INF/services/io.jcloud.api.extensions.AnnotationBinding](jcloud-containers/src/main/resources/META-INF/services/io.jcloud.api.extensions.AnnotationBinding) using the binding [ContainerAnnotationBinding.java](jcloud-containers/src/main/java/io/jcloud/resources/containers/ContainerAnnotationBinding.java)
- `Extension Managed Resources point` - deploy your resources into the target environment. Each extension binding point will deploy the resources locally, though we can easily extend it to deploy services in any kind of target environment. For example, for containers, we provide the [ContainerManagedResourceBinding.java](jcloud-containers/src/main/java/io/jcloud/api/extensions/ContainerManagedResourceBinding.java) extension point that we can provide to support other environments as we have done for [Kubernetes](jcloud-containers/src/main/java/io/jcloud/resources/containers/kubernetes/KubernetesContainerManagedResourceBinding.java).

### Packages Convention

Modules within the testing framework must conform to the following package naming conventions:

- `io.jcloud.api` - the API to use services and resources
- `io.jcloud.configuration` - configure the services and test configuration
- `io.jcloud.core` - the core functionality of the framework
- `io.jcloud.logging` - logging facilities and handlers
- `io.jcloud.resources` - the supported resources within the current jcloud dependency
- `io.jcloud.utils` - more utilities
- 
## Contributing

**Want to contribute? Great!**
We try to make it easy, and all contributions, even the smaller ones, are more than welcome. This includes bug reports, fixes,
documentation, examples... But first, [read this page](CONTRIBUTING.md).