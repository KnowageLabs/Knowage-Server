FROM java:7

ENV KNOWAGE_VERSION=Knowage-6.0.0-CE-Installer-Unix-20170710
ENV KNOWAGE_URL=http://download.forge.ow2.org/knowage/${KNOWAGE_VERSION}.zip

RUN apt-get update && apt-get install -y wget coreutils unzip mysql-client

#download knowage and extract it
WORKDIR /home
RUN wget "${KNOWAGE_URL}" -O Knowage.zip && \
	unzip Knowage.zip && \
	mv ${KNOWAGE_VERSION}.sh Knowage.sh && \
	rm Knowage.zip
	
RUN chmod +x *.sh

ENV KNOWAGE_DIRECTORY /home/Knowage
WORKDIR ${KNOWAGE_DIRECTORY}/bin

COPY ./entrypoint.sh ./
#make all scripts executable
RUN chmod +x *.sh

EXPOSE 8080
#-d option is passed to run spagobi forever without exiting from container
ENTRYPOINT ["./entrypoint.sh"]
CMD ["./startup.sh"]
