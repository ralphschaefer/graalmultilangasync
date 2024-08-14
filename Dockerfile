FROM debian:bookworm-slim

RUN apt-get update && apt-get upgrade -f -y && \
    apt-get --no-install-recommends install -y build-essential apt-transport-https ca-certificates xz-utils wget apt-transport-https gnupg software-properties-common curl vim less && \
RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list
RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list
RUN curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | gpg --no-default-keyring --keyring gnupg-ring:/etc/apt/trusted.gpg.d/scalasbt-release.gpg --import
RUN chmod 644 /etc/apt/trusted.gpg.d/scalasbt-release.gpg
RUN apt-get update  && apt-get install sbt -yqq
RUN mkdir -p /usr/lib/jvm
WORKDIR /usr/lib/jvm
RUN wget -q https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-20.0.1/graalvm-community-jdk-20.0.1_linux-x64_bin.tar.gz
RUN tar -xzf graalvm-community-jdk-20.0.1_linux-x64_bin.tar.gz
RUN rm graalvm-community-jdk-20.0.1_linux-x64_bin.tar.gz
RUN /usr/bin/update-alternatives --install /usr/bin/java java /usr/lib/jvm/graalvm-community-openjdk-20.0.1+9.1/bin/java 2
RUN /usr/bin/update-alternatives --set java /usr/lib/jvm/graalvm-community-openjdk-20.0.1+9.1/bin/java
RUN /usr/lib/jvm/graalvm-community-openjdk-20.0.1+9.1/bin/gu install js
RUN mkdir -p /build
WORKDIR /build
COPY . .
RUN sbt assembly
CMD ["java", "-jar", "/build/target/jsasync.jar"]