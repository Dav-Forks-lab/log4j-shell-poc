FROM tomcat:8.0.36-jre8

# Replace Debian's backported JRE with original Oracle JRE 8u102
COPY jre1.8.0_102 /opt/jre1.8.0_102
ENV JAVA_HOME=/opt/jre1.8.0_102
ENV JRE_HOME=/opt/jre1.8.0_102
ENV PATH=/opt/jre1.8.0_102/bin:$PATH

# avoid double stack java mapping to IPv6
#ENV CATALINA_OPTS="-Djava.net.preferIPv4Stack=true"

RUN rm -rf /usr/local/tomcat/webapps/*
ADD target/log4shell-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080 
CMD ["catalina.sh", "run"]
