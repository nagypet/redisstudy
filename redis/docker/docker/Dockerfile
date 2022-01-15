FROM redis:6.2.6

COPY ./conf/redis.conf /redis.conf
COPY ./conf/rc.local /etc/rc.local

RUN mkdir /redisdata
RUN chmod -R 777 /redisdata

EXPOSE 6379

CMD ["sh", "-c", "exec redis-server /redis.conf --requirepass \"$REDIS_PASSWORD\""]