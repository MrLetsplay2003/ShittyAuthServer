FROM maven:latest as builder

COPY . /workspace

WORKDIR /workspace

RUN mvn clean package



FROM alpine:latest

ENV UID=1000
ENV GID=1000

RUN apk update && apk add openjdk11 shadow sudo

COPY --from=mrletsplay/docker_launcher /usr/local/bin/docker_launcher /usr/local/bin/docker_launcher
COPY ./docker/launcher_config.json /shittyauth/launcher_config.json
COPY --from=builder /workspace/target/ShittyAuthServer-*.jar /shittyauth/ShittyAuthServer.jar

RUN useradd shittyauth

RUN mkdir /shittyauth/data && chown -R shittyauth /shittyauth

VOLUME ["/shittyauth/data"]

WORKDIR /shittyauth/data

EXPOSE 8880

ENTRYPOINT [ "docker_launcher", "--config", "/shittyauth/launcher_config.json", "sudo", "-E", "-u", "shittyauth" ]
CMD [ "java", "-jar", "/shittyauth/ShittyAuthServer.jar" ]
