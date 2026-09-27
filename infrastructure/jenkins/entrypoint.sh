#!/bin/bash
set -e

# Start background TCP port forwarders so requests to localhost:8761 and localhost:8080
# inside the container seamlessly reach eureka-server and api-gateway on the revhire Docker network.
perl /usr/local/bin/port-forward.pl 8761 eureka-server 8761 &
perl /usr/local/bin/port-forward.pl 8080 api-gateway 8080 &

# Hand over to the standard Jenkins startup entrypoint
exec /usr/bin/tini -- /usr/local/bin/jenkins.sh "$@"
