mkdir -p tmp
SPECFILE=tmp/spec.json
curl http://localhost:8080/v3/api-docs --output $SPECFILE

docker run --rm --net=host -u="$(id -u)" -v ${PWD}:/local swaggerapi/swagger-codegen-cli-v3:3.0.68 generate \
    -i /local/tmp/spec.json \
    -l typescript-angular \
    -o /local/src/app/rest_client/ \
    --additional-properties ngVersion=19.1.4
