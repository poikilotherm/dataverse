FROM quay.io/poikilotherm/payara-full:latest

# Disable "phone home"
RUN rm ${PAYARA_DIR}/glassfish/modules/phonehome-bootstrap.jar

COPY maven/${project.build.finalName}.war ${DEPLOY_DIR}

COPY init_2_configure.sh ${SCRIPT_DIR}