FROM 319569500149.dkr.ecr.us-east-1.amazonaws.com/jvm-limited:2.0

ENV APP_TARGET target
ENV APP realwave-sales-manager-command-application.jar

RUN mkdir -p /opt /opt/certs
COPY ${APP_TARGET}/${APP} /opt

ENTRYPOINT keytool -import -file ${CERT_PATH} -keystore ${JAVA_HOME}/lib/security/cacerts -alias realwave-crt -storepass ${CERT_PASS} -noprompt; MEM_PARAMS=$(/param_tuning.sh ${HEAP_SIZE:-512}); exec java ${JAVA_AGENT} ${JAVA_OPTS} ${MEM_PARAMS} -jar /opt/${APP}
