FROM java:7

ENV KNOWAGE_VERSION=Knowage-6.1-xyz
ENV KNOWAGE_URL=https://github.com/KnowageLabs/Knowage-Server/releases/download/v6.1-xyz/${KNOWAGE_VERSION}.zip

RUN apt-get update && apt-get install -y wget coreutils unzip mysql-client

#download knowage and extract it
WORKDIR /home
RUN wget "${KNOWAGE_URL}" -O Knowage.zip && \
	unzip Knowage.zip && \
	mv ${KNOWAGE_VERSION} Knowage && \
	rm Knowage.zip

ENV KNOWAGE_DIRECTORY /home/Knowage
WORKDIR ${KNOWAGE_DIRECTORY}/bin

#make the script executable by bash (not only sh) and
#make knowage running forever without exiting
RUN sed -i "s/bin\/sh/bin\/bash/" startup.sh && \
	sed -i "s/EXECUTABLE\" start/EXECUTABLE\" run/" startup.sh

COPY ./entrypoint.sh ./
#make all scripts executable
RUN chmod +x *.sh

#safe copy of original conf files
RUN cp ${KNOWAGE_DIRECTORY}/conf/server.xml ${KNOWAGE_DIRECTORY}/conf/server.xml.bak && \
	cp ${KNOWAGE_DIRECTORY}/webapps/knowage/WEB-INF/classes/hibernate.cfg.xml ${KNOWAGE_DIRECTORY}/webapps/knowage/WEB-INF/classes/hibernate.cfg.xml.bak && \
	cp ${KNOWAGE_DIRECTORY}/webapps/knowage/WEB-INF/classes/quartz.properties ${KNOWAGE_DIRECTORY}/webapps/knowage/WEB-INF/classes/quartz.properties.bak

EXPOSE 8080
#-d option is passed to run spagobi forever without exiting from container
ENTRYPOINT ["./entrypoint.sh"]
CMD ["./startup.sh"]
