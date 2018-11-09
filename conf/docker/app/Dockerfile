FROM payara/server-full:latest

ARG LIB_PATH="${PAYARA_DIR}/glassfish/domains/${DOMAIN_NAME}/lib/"

COPY --chown=payara maven/lib/* ${LIB_PATH}

COPY maven/${project.build.finalName}.war ${DEPLOY_DIR}