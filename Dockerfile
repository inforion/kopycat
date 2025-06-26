FROM docker.io/openjdk:11 AS base

RUN apt-get update -y --allow-releaseinfo-change && \
    apt-get install -y bash socat dos2unix && \
    rm -rf /var/lib/apt/lists/*

FROM base AS builder

RUN apt-get update -y && \
    apt-get install -y git git-lfs && \
    rm -rf /var/lib/apt/lists/*

RUN git clone https://github.com/inforion/kotlin-extensions.git /opt/kexts && \
    cd /opt/kexts && \
    rm -rf .git && \
    chmod +x gradlew && \
    dos2unix gradlew && \
    ./gradlew --no-daemon publishToMavenLocal

RUN git clone https://github.com/inforion/kopycat.git /opt/kopycat && \
    cd /opt/kopycat && \
    rm -rf .git && \
    chmod +x gradlew && \
    dos2unix gradlew && \
    ./gradlew --no-daemon buildKopycatModule && \
    ./gradlew --no-daemon createKopycatConfig

RUN mkdir /opt/kcroot && \
    cd /opt/kopycat && \
    for i in $(find . -type d -wholename '*/kopycat-modules/**/src/main/resources/**/scripts'); do \
        mkdir -p "/opt/kcroot/$i"; \
        cp -r "$i" "/opt/kcroot/$i/../"; \
    done && \
    cp /opt/kopycat/temp/config/bash/*.sh /opt/kcroot/

FROM base AS result

WORKDIR /opt/kopycat
COPY --from=builder /opt/kopycat/production /opt/kopycat/production
COPY --from=builder /opt/kcroot /opt/kopycat

RUN chmod +x *.sh && sed -i '/^\.\/gradlew/d' *.sh
