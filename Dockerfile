FROM public.ecr.aws/amazoncorretto/amazoncorretto:17-al2-jdk
RUN mkdir -p /u01/deploy
RUN mkdir -p /u01/deploy/state
RUN mkdir -p /u01/deploy/output
WORKDIR /u01/deploy

# install utilities
RUN yum install -y unzip

# add jar
COPY target/genai-selenium-1.0-SNAPSHOT.jar genai-selenium.jar

# install chromium

RUN yum -y install https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
RUN yum -y install epel-release
RUN yum -y install chromium.x86_64
RUN yum -y install chromedriver.x86_64

# Configure Crawler
RUN useradd -ms /bin/bash crawler
RUN chown -R crawler /u01

# Configure user
USER crawler

#health check if java process is running
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 CMD ps -ef | grep 'genai-selenium' || exit 1
#ENTRYPOINT [ "sh", "-c", "java -Xms512m -Xmx850m -Dwebdriver.chrome.whitelistedIps= -Dio.netty.noUnsafe -jar /u01/deploy/awsdoc-crawler.jar"]
#To get rid of the error: listen on IPv6 failed with error ERR_ADDRESS_INVALID
ENTRYPOINT [ "sh", "-c", "java -Xmx3g -Dwebdriver.chrome.whitelistedIps= -jar /u01/deploy/genai-selenium.jar"]
EXPOSE 9090