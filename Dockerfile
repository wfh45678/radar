# Radar 运行镜像：预构建前端已在 jar 内（static/），无需 node 构建阶段
FROM eclipse-temurin:17-jre

ENV TZ=Asia/Shanghai \
    JAVA_OPTS=""

WORKDIR /app

COPY target/radar.jar app.jar

# 手机归属地 / ip2region 数据文件（dev profile 默认路径 /radar/resources/*）
COPY resources/ /radar/resources/
RUN mkdir -p /radar/workdir

EXPOSE 6580

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
