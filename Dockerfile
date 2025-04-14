FROM openjdk:11

RUN apt-get update -y --allow-releaseinfo-change && \
    apt-get install -y bash sed git socat && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /opt/kopycat

COPY . /opt/kopycat/
COPY settings.generic.gradle.kts ./settings.gradle.kts

RUN echo "Copying m2 (if exists)..." && \
    if [ -d ./.m2 ]; then mv -v ./.m2 /root/.m2; fi && \
    echo "Setting up git repository..." && \
    git config --global init.defaultBranch master && \
    git config --global user.email unknown && \
    git config --global user.name unknown && \
    git init && \
    touch README.md && \
    git add README.md && \
    git commit -m "Initial commit" && \
    echo "Setting up the project..." && \
    sed -i 's/\r//' ./gradlew && \
    ./gradlew --no-daemon -i classes && \
    echo "Post-process clearance" && \
    ./gradlew --no-daemon -i clean && \
    rm -rf /root/.gradle && \
    echo "Finished"
