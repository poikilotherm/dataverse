#!/usr/bin/env sh

# TEST FOR POSTBOOT: THIS WILL NEED TO BE ENHANCED WITH A) ENV VARS
#                    AND B) SECRETS FILE...

echo "AS_ADMIN_ALIASPASSWORD=test1234" > ${PAYARA_DIR}/dbpass

cat > ${POSTBOOT_COMMANDS}.tmp << 'EOF'
create-system-properties "postgres.host=postgres-1"
create-system-properties "postgres.port=5432"
create-system-properties "postgres.database=dataverse
create-system-properties "postgres.user=dataverse"
create-password-alias    "postgres-pass" --passwordfile ${PAYARA_DIR}/dbpass

# Do not validate resources, or JMS resources defined in
# glassfish-resources.xml cannot be created
create-system-properties "deployment.resource.validation=false"
EOF

# Set Dataverse environment variables
echo "# --- DATAVERSE CONFIGURATION OPTIONS FOLLOWING ---" >> ${POSTBOOT_COMMANDS}.tmp
env -0 | grep -z -Ee "^(dataverse|doi)_" | while IFS='=' read -r -d '' k v; do
    KEY=`echo "${k}" | tr '_' '.'`
    echo "create-system-properties \"${KEY}=${v}\"" >> ${POSTBOOT_COMMANDS}.tmp
done
# Delete environment variables not needed anymore
env -0 | grep -z -Ee "^delete_(dataverse|doi)_" | while IFS='=' read -r -d '' k v; do
    KEY=`echo "${k}" | sed -e 's#delete_##' | tr '_' '.'`
    echo "delete-system-property \"${KEY}\"" >> ${POSTBOOT_COMMANDS}.tmp
done

echo "$(cat ${POSTBOOT_COMMANDS}.tmp | cat - ${POSTBOOT_COMMANDS} )" > ${POSTBOOT_COMMANDS}
cat ${POSTBOOT_COMMANDS}