FROM postgres:latest
LABEL authors="Piteux Nathan, Doudech Rayen"

# Copy the database schema to the container
COPY backup.sql /docker-entrypoint-initdb.d/backup.sql