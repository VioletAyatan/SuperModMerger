@echo off
chcp 65001
mvn clean package -DskipTests -Pnative
