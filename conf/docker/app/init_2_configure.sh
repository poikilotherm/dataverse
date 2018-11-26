#!/usr/bin/env sh

# TEST FOR POSTBOOT: THIS WILL NEED TO BE ENHANCED WITH A) ENV VARS
#                    AND B) SECRETS FILE...

echo "AS_ADMIN_ALIASPASSWORD=test1234" > ${PAYARA_DIR}/dbpass

cat > ${POSTBOOT_COMMANDS}.tmp << 'EOF'
create-system-properties "postgres.host=postgres"
create-system-properties "postgres.port=5432"
create-system-properties "postgres.database=dataverse
create-system-properties "postgres.user=dataverse"
create-password-alias    "postgres-pass" --passwordfile ${PAYARA_DIR}/dbpass
EOF

echo "$(cat ${POSTBOOT_COMMANDS}.tmp | cat - ${POSTBOOT_COMMANDS} )" > ${POSTBOOT_COMMANDS}
cat ${POSTBOOT_COMMANDS}