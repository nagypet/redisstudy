FROM alpine:3.12
VOLUME /tmp

RUN apk add mc --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community
RUN apk add openjdk11 --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community
RUN apk add --no-cache tzdata

# setting image timezone to Europe/Budapest
ENV TZ Europe/Budapest
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /usr/src/redisstudy

EXPOSE 8080

COPY ./dependencies ./lib
COPY ./snapshot-dependencies* ./lib
COPY ./bin ./bin
COPY ./application ./lib

WORKDIR /usr/src/redisstudy/bin

CMD ["sh", "./redisstudy"]
