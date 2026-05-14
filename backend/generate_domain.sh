#!/bin/bash

# usage: ./generate_domain.sh domainName
# example: ./generate_domain.sh payment

if [ -z "$1" ]; then
  echo "❌ Please provide a domain name"
  echo "Usage: ./generate_domain.sh <domain>"
  exit 1
fi

DOMAIN=$1
BASE=src/main/java/com/team05/petmeeting/domain/$DOMAIN
PACKAGE_BASE=com.team05.petmeeting.domain.$DOMAIN

# capitalize first letter (payment -> Payment)
CLASS_PREFIX="$(tr '[:lower:]' '[:upper:]' <<< ${DOMAIN:0:1})${DOMAIN:1}"

create_file() {
  FILE_PATH=$1
  PACKAGE=$2
  CLASS_NAME=$3
  TYPE=$4  # class / enum

  if [ -f "$FILE_PATH" ]; then
    echo "⏭ Skipping: $FILE_PATH"
    return
  fi

  mkdir -p "$(dirname "$FILE_PATH")"

  if [ "$TYPE" = "enum" ]; then
    cat <<EOF > "$FILE_PATH"
package $PACKAGE;

public enum $CLASS_NAME {

}
EOF
  else
    cat <<EOF > "$FILE_PATH"
package $PACKAGE;

public class $CLASS_NAME {
}
EOF
  fi

  echo "✅ Created: $FILE_PATH"
}

echo "🚀 Generating domain: $DOMAIN"

# folders
SUBDIRS=(controller service entity dto repository enums errorCode)

for dir in "${SUBDIRS[@]}"; do
  mkdir -p "$BASE/$dir"
done

# core files
create_file "$BASE/controller/${CLASS_PREFIX}Controller.java" "$PACKAGE_BASE.controller" "${CLASS_PREFIX}Controller" "class"
create_file "$BASE/service/${CLASS_PREFIX}Service.java" "$PACKAGE_BASE.service" "${CLASS_PREFIX}Service" "class"
create_file "$BASE/entity/${CLASS_PREFIX}.java" "$PACKAGE_BASE.entity" "$CLASS_PREFIX" "class"
create_file "$BASE/repository/${CLASS_PREFIX}Repository.java" "$PACKAGE_BASE.repository" "${CLASS_PREFIX}Repository" "class"

# DTOs (default set)
create_file "$BASE/dto/${CLASS_PREFIX}Req.java" "$PACKAGE_BASE.dto" "${CLASS_PREFIX}Req" "class"
create_file "$BASE/dto/${CLASS_PREFIX}Res.java" "$PACKAGE_BASE.dto" "${CLASS_PREFIX}Res" "class"

# enum + error
create_file "$BASE/enums/${CLASS_PREFIX}Status.java" "$PACKAGE_BASE.enums" "${CLASS_PREFIX}Status" "enum"
create_file "$BASE/errorCode/${CLASS_PREFIX}ErrorCode.java" "$PACKAGE_BASE.errorCode" "${CLASS_PREFIX}ErrorCode" "class"

echo "🎉 Done generating $DOMAIN domain!"