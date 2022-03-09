FROM zhisuanx/alpine-java:8_server-jre_cn_unlimited

MAINTAINER zhisuanjava@qq.com

RUN mkdir -p /zhisuan/flowdesign

WORKDIR /zhisuan/flowdesign

EXPOSE 9999

ADD ./target/zhisuan-flow-design.jar ./app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

CMD ["--spring.profiles.active=test"]
