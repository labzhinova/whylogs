## How to develop
* OpenJDK 8 for Java development
* Python 3.7+ for Python develop. Recommend to have use `conda` or `virtualenv` for managing your python environment
* Docker and `gitlab-runner` to test with GitLab CI
* `protoc` installed. See [instructions](http://google.github.io/proto-lens/installing-protoc.html).
* Recommend to install `gdub` to for building with Gradle

## Build the package

Run the following command
```
./gradlew build
```