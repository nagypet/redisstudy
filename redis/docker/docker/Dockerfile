FROM redis:6.2.6

COPY ./conf /redis
COPY ./etc/rc.local /etc/rc.local

RUN mkdir /redisdata
RUN chmod -R 777 /redisdata

EXPOSE 6379

CMD ["sh", "/redis/start-redis.sh"]