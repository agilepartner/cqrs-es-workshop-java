# Step 01 : Create an empty Spring Boot app

## Initializing the project

We are going to use [Spring Initializr](https://start.spring.io/) to bootstrap the project with Gradle

1. Choose *Generate a Gradle project*
2. Choose *Java* as a language
3. Type the namespace you want to use i.e. *net.agilepartner.workshops.cqrs*
4. Type *app* for the project name
5. Choose the latest stable version i.e. *2.1.3*
6. Add dependencies
    * DevTools
    * Lombok
    * Kafka
    * Kafka Streams

## Edit the *.gitignore*

Edit the *.gitignore* file and add the following to make sure that Java artifacts are ignored.

```yaml
# Bin
/bin/

# Compiled class file
*.class

# Log file
*.log

# BlueJ files
*.ctxt

# Mobile Tools for Java (J2ME)
.mtj.tmp/

# Package Files #
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# virtual machine crash logs, see http://www.java.com/en/download/help/error_hotspot.xml
hs_err_pid*
```

## Run the tests

To make sure that everything works fine, open [DemoApplicationTests](app/src/test/java/net/agilepartner/workshops/cqrs/app/DemoApplicationTests.java) and run the tests using your IDE.

## What's next

In the next step, we will add our core interfaces and classes.

* Go to [Step 02](../Step02/Step02.md)
* Go back to [Home](../README.md)