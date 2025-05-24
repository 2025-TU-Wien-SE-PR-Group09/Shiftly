New-Item -ItemType Directory -Force -Path .\tmp
$SPECFILE = ".\tmp\spec.json"
Invoke-WebRequest -Uri "http://localhost:8080/v3/api-docs" -OutFile $SPECFILE

docker run --rm -v ${PWD}:/local openapitools/openapi-generator-cli generate `
    -i /local/tmp/spec.json `
    -g typescript-angular `
    -o /local/src/app/rest_client/ `
    --additional-properties ngVersion=19.1.4, providedInRoot=true, useOverride=true
