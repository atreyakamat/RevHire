FROM jenkins/jenkins:lts

USER root

# Install CA certificate to trust network/proxy TLS termination
COPY bin/cyberoam.crt* /usr/local/share/ca-certificates/
RUN update-ca-certificates 2>/dev/null || true

# Install Apache Maven 3.9.x
COPY bin/apache-maven-3.9.9 /opt/maven
ENV MAVEN_HOME=/opt/maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

# Install Docker CLI
COPY bin/docker /usr/local/bin/docker
RUN chmod +x /usr/local/bin/docker

# Install Docker Compose CLI plugin and link to /usr/local/bin/docker-compose
RUN mkdir -p /usr/local/lib/docker/cli-plugins
COPY bin/docker-compose /usr/local/lib/docker/cli-plugins/docker-compose
RUN chmod +x /usr/local/lib/docker/cli-plugins/docker-compose && \
    ln -sf /usr/local/lib/docker/cli-plugins/docker-compose /usr/local/bin/docker-compose

# Create docker group with host docker socket GID (967) and add jenkins user
RUN groupadd -g 967 docker 2>/dev/null || true && \
    usermod -aG docker jenkins 2>/dev/null || true

# Copy port forwarder script and custom entrypoint
COPY port-forward.pl /usr/local/bin/port-forward.pl
RUN chmod +x /usr/local/bin/port-forward.pl

COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

USER jenkins

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
