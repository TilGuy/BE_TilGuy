#!/bin/bash

BLUE_PORT=9998
GREEN_PORT=9999

CURRENT_PORT=$(curl -s http://localhost/actuator/health | grep -q "$BLUE_PORT" && echo $BLUE_PORT || echo $GREEN_PORT)
NEW_PORT=$([ "$CURRENT_PORT" == "$BLUE_PORT" ] && echo $GREEN_PORT || echo $BLUE_PORT)

if [ "$NEW_PORT" == "$BLUE_PORT" ]; then
  COMPOSE_FILE=compose-matilda-blue.yml
  CONTAINER_NAME=matilda-blue
else
  COMPOSE_FILE=compose-matilda-green.yml
  CONTAINER_NAME=matilda-green
fi

echo "▶ Building $CONTAINER_NAME on port $NEW_PORT"
docker-compose -f $COMPOSE_FILE build --no-cache
docker-compose -f $COMPOSE_FILE up -d

# 헬스체크
until curl -s http://localhost:$NEW_PORT/actuator/health | grep -q '"status":"UP"'; do
  echo "⏳ Waiting for $CONTAINER_NAME..."
  sleep 2
done

# Nginx 포트 전환
sudo sed -i "s/127.0.0.1:$CURRENT_PORT/127.0.0.1:$NEW_PORT/" /etc/nginx/sites-enabled/default
sudo nginx -t && sudo nginx -s reload

# 이전 컨테이너 종료
if [ "$CURRENT_PORT" == "$BLUE_PORT" ]; then
  docker-compose -f compose-matilda-blue.yml down
else
  docker-compose -f compose-matilda-green.yml down
fi
