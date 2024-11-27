# Start with the Confluent Kafka Connect base image
FROM confluentinc/cp-kafka-connect:latest

# Set environment variables
ENV CONNECT_PLUGIN_PATH="/usr/share/java"

# Download Debezium PostgreSQL Connector
RUN mkdir -p $CONNECT_PLUGIN_PATH/debezium-connector-postgres && \
    curl -sSL https://repo1.maven.org/maven2/io/debezium/debezium-connector-postgres/2.2.0.Final/debezium-connector-postgres-2.2.0.Final-plugin.tar.gz | \
    tar -xz -C $CONNECT_PLUGIN_PATH/debezium-connector-postgres

# Download Confluent JDBC Sink Connector and PostgreSQL Driver
RUN mkdir -p $CONNECT_PLUGIN_PATH/jdbc-sink-connector && \
    curl -sSL https://packages.confluent.io/maven/io/confluent/kafka-connect-jdbc/10.7.3/kafka-connect-jdbc-10.7.3.jar \
         -o $CONNECT_PLUGIN_PATH/jdbc-sink-connector/kafka-connect-jdbc-10.7.3.jar && \
    curl -sSL https://repo1.maven.org/maven2/org/postgresql/postgresql/42.5.0/postgresql-42.5.0.jar \
         -o $CONNECT_PLUGIN_PATH/jdbc-sink-connector/postgresql-42.5.0.jar

# Ensure all dependencies are listed (optional for debugging)
RUN ls $CONNECT_PLUGIN_PATH

