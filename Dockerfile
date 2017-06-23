FROM maven:3.5-jdk-8-onbuild-alpine

VOLUME ["/usr/src/app/certs", "/usr/src/app/data"]

ENV JAVA_OPTS="-Dbot.config.dir=/usr/src/app/docker_config -Dlog4j.configurationFile=/usr/src/app/docker_config/log4j.properties"

ENTRYPOINT ["/usr/src/app/docker_exec/run_bot.sh"]
CMD ["ProxyDeskBot"] 

