FROM carsharing/alpine-oraclejdk8-bash
MAINTAINER JacksonTu 674717739@qq.com

ENV PROJECT_HOME /home/project
ENV OUTPUT_HOME /output
ENV JAVA_OPTS="-server -Xms128m -Xmx128m -Xmn64m -Xss1m"

RUN mkdir -p "$PROJECT_HOME" && mkdir -p "$OUTPUT_HOME"

COPY bright-auth.jar $PROJECT_HOME/bright-auth.jar

WORKDIR /home/project

# 指定容器内的时区
RUN echo "Asia/Shanghai" > /etc/timezone

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar $PROJECT_HOME/bright-auth.jar" ]
