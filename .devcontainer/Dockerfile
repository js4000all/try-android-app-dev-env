FROM eclipse-temurin:17-jdk-jammy

ARG ANDROID_CMDLINE_TOOLS_VERSION=15859902
ARG ANDROID_CMDLINE_TOOLS_SHA256=4e4c464f145a7512b57d088ac6c278c03c9eea610886b35a5e0804e74eedf583
ARG ANDROID_PLATFORM=36
ARG ANDROID_BUILD_TOOLS=35.0.0

ENV DEBIAN_FRONTEND=noninteractive \
    ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    PATH=/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools:${PATH}

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       bash \
       ca-certificates \
       curl \
       git \
       openssh-client \
       sudo \
       unzip \
       zip \
    && rm -rf /var/lib/apt/lists/*

RUN useradd -m -s /bin/bash vscode \
    && echo 'vscode ALL=(ALL) NOPASSWD:ALL' > /etc/sudoers.d/vscode \
    && chmod 0440 /etc/sudoers.d/vscode

RUN mkdir -p "${ANDROID_HOME}/cmdline-tools" \
    && curl -fsSL \
       "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_TOOLS_VERSION}_latest.zip" \
       -o /tmp/android-commandlinetools.zip \
    && echo "${ANDROID_CMDLINE_TOOLS_SHA256}  /tmp/android-commandlinetools.zip" | sha256sum -c - \
    && unzip -q /tmp/android-commandlinetools.zip -d /tmp/android-commandlinetools \
    && mv /tmp/android-commandlinetools/cmdline-tools "${ANDROID_HOME}/cmdline-tools/latest" \
    && rm -rf /tmp/android-commandlinetools /tmp/android-commandlinetools.zip

RUN yes | sdkmanager --licenses >/dev/null \
    && sdkmanager \
       "platform-tools" \
       "platforms;android-${ANDROID_PLATFORM}" \
       "build-tools;${ANDROID_BUILD_TOOLS}" \
    && chown -R vscode:vscode "${ANDROID_HOME}"

USER vscode
WORKDIR /workspace
