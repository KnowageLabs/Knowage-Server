FROM java:8

ENV KNOWAGE_VERSION=6.0.0-CE-Installer-Unix
ENV KNOWAGE_RELEASE_DATE=20170710
ENV KNOWAGE_URL=http://download.forge.ow2.org/knowage/Knowage-${KNOWAGE_VERSION}-${KNOWAGE_RELEASE_DATE}.zip

ENV KNOWAGE_DIRECTORY .

RUN apt-get update && apt-get install -y wget coreutils unzip mysql-client

#download knowage and extract it
RUN wget "${KNOWAGE_URL}" && \
	unzip Knowage-${KNOWAGE_VERSION}-${KNOWAGE_RELEASE_DATE}.zip && \
	rm Knowage-${KNOWAGE_VERSION}-${KNOWAGE_RELEASE_DATE}.zip
	
COPY ./entrypoint.sh ./
COPY ./params.properties ./
#make all scripts executable
RUN chmod +x *.sh

EXPOSE 8080
#-d option is passed to run knowage forever without exiting from container
ENTRYPOINT ["./entrypoint.sh"]

CMD ["./Knowage-${KNOWAGE_VERSION}-${KNOWAGE_RELEASE_DATE}.sh -q -varfile params.properties"]

WORKDIR ${KNOWAGE_DIRECTORY}/Knowage-Server-CE/bin
CMD ["./startup.sh"]
