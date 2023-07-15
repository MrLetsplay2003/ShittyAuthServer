FROM maven:latest as builder

ADD . /workspace

WORKDIR /workspace

RUN mvn clean package

FROM alpine:latest

RUN apk update && apk add openjdk11

COPY --from=builder /workspace/target/ShittyAuthServer-*.jar /shittyauth/ShittyAuthServer.jar

RUN apk update && apk add shadow

RUN useradd shittyauth

RUN mkdir /shittyauth/data && chown -R shittyauth /shittyauth

VOLUME ["/shittyauth/data"]

WORKDIR /shittyauth/data

USER shittyauth

EXPOSE 8880

CMD [ "java", "-jar", "/shittyauth/ShittyAuthServer.jar" ]
