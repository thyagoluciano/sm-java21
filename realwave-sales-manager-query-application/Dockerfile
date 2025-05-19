FROM 319569500149.dkr.ecr.us-east-1.amazonaws.com/jvm-limited:2.0

ENV APP_TARGET target
ENV APP realwave-sales-manager-query-application.jar

RUN mkdir -p /opt
COPY ${APP_TARGET}/${APP} /opt

ENTRYPOINT MEM_PARAMS=$(/param_tuning.sh ${HEAP_SIZE:-512}); exec java ${JAVA_AGENT} ${JAVA_OPTS} ${MEM_PARAMS} -jar /opt/${APP}