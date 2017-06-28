#!/bin/bash
set -e
shopt -s nocasematch

if [ $# -eq 0 ]; then
    echo "Please provide a Bot name or linux command like 'bash'"
    echo "Possible bot names:"
    echo "  ProxyBot"
    echo "  RoomBot"
    echo "  WebBot"
    exit 1
fi


case "$1" in
    "ProxyDeskBot"|"ProxyDesk"|"ProxyBot"|"proxy_bot"|"proxy")
        mkdir -p /usr/src/app/data/proxydesk-json/
        exec /usr/src/app/proxy-desk/target/proxy-desk-0.9.0-SNAPSHOT/bin/ProxyDeskBot
        ;;
    "RoomDeskBot"|"RoomDesk"|"RoomBot"|"room_bot"|"room")
        mkdir -p /usr/src/app/data/roomdesk-json/
        exec /usr/src/app/room-desk/target/room-desk-0.9.0-SNAPSHOT/bin/RoomDeskBot
        ;;
    "WebDeskBot"|"WebDesk"|"web_bot"|"web")
        mkdir -p /usr/src/app/data/webdesk-json/
        exec /usr/src/app/web-desk/target/web-desk-0.9.0-SNAPSHOT/bin/WebDeskBot
        ;;
    *)
        exec "$@"
esac


