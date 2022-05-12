#! /bin/sh

echo Creating users.acl
echo "user default on sanitize-payload allkeys allchannels allcommands >${REDIS_PASSWORD}">/redis/users.acl
cat /redis/users.acl
echo Starting redis
exec redis-server /redis/redis.conf --aclfile /redis/users.acl
