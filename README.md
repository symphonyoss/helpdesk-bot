Symphony Helpdesk Bot
=====================


[![Dependencies](https://www.versioneye.com/user/projects/5770f47919424d000f2e0095/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/5770f47919424d000f2e0095)
[![Build Status](https://travis-ci.org/symphonyoss/symphony-java-client.svg)](https://travis-ci.org/symphonyoss/symphony-java-client)
[![Validation Status](https://scan.coverity.com/projects/9112/badge.svg?flat=1)](https://scan.coverity.com/projects/symphonyoss-symphony-java-client)

Help Desk BOT(s) are Java based applications that implement a call routing workflow based framework leveraging the Symphony API.  To implement a common design pattern which supports a call routing workflow from a named end-point (network user) to an application (BOT) end-point named alias (operator/call router), which routes to a rota of other named user end-points who service the call.
     
##General Concepts
* All communications from initiating user end-points to application end-point are private.
* All communications from the application end-point to the rota of servicing user end-points are shared. Rules can be applied to prevent communications between servicing users.
* The application end-point will maintain session state with all end-points.
* The application end-point will have additional programmatic workflow hooks to support other implementations. These include event listeners and interfaces through the life-cycle of call handling. e.g. Connect, Validation, Session, SymMessage, Rules, Context, Commands, Termination


##Change log and notes
### V1.0.0 (SNAPSHOT)
* Upgrade to Symphony-Java-Client 1.0.0
* General features described below.



##Help Desk BOT Types

###Proxy Desk
See video [DEMO](https://www.youtube.com/watch?v=aXv35MU3szQ)

All communications flows through (ingress/egress) the BOT application, which handles all call routing.  Command line interface is provided to members to manage all calls. 
    
    U(SymUser) < - - > O(Operator)< - - > R(Rota of Users/members)
    (~~~~~~~~~~~~~~~~~~~~)< - - >Ru(Rota SymUser callback, proxy through O)

###Room Desk
See video [DEMO](https://www.youtube.com/watch?v=Uq_eS-L6Ud8)

All inbound user communications flows through the BOT into a chat room of members.  Command line interface is provided to all members, but calls are established through an external multi-party conversation outside the chat room.
    
    U(SymUser) < - - > O(Operator)< - > R(Rota of Users/members)
    (~~~~~) <- - - - - - - - - - - - - - - -Ru(Rota SymUser callback, direct)

###Web Desk
See video [DEMO](https://www.youtube.com/watch?v=CAhl18L7kXo)

All inbound users leverage an external web client, which communicates with a backend BOT service that implements the Room Desk construct.
      
      U(SymUser) < - -> Web Module < - - > O (Operator) <- > R(Rota of users/members)
      (~~~~~) <-------------------------------------------Ru(Rota SymUser callback)

##Requirements

###Certificates:
Please contact your Symphony local administrator to obtain the necessary certificates for the user/service account being used to access the POD.

        Server Truststore = Contains server certs
        SymUser Keystore = Symphony user client certificate

###Required System Properties:

        -Dkeystore.password=(Pass)
        -Dtruststore.password=(Pass)
        -Dsessionauth.url=https://(pod-host).symphony.com:8444/sessionauth
        //Note: you may have local HSM vs pod
        -Dkeyauth.url=https://(pod-host).symphony.com:8444/keyauth
        -Dsymphony.agent.pod.url=https://(symagent-host):8446/pod
        -Dsymphony.agent.agent.url=https://(symagent-host):8446/agent
        -Dcerts.dir=/dev/certs/
        -Dtruststore.file=/dev/certs/server.truststore
        -Dbot.user=(user name)
        -Dbot.domain=(domain of user)
        -Djson.files=/dev/json

## Docker

### Build docker image
From the project directory run:

```bash
docker build . --tag helpdesk-bot
```

It will download and install all deplendencies. You don't need to have Java or Maven installed to build the docker image.

### Run docker image

1. In any empty directory create a `certs` subdirectory.
2. Plase `<helpdesk_bot_user_name>.p12` cert and `server.truststore` keystore into the `certs` subdirectory
3. Create a `proxy_bot.env` environment file with your bot configurations. It should contain the following variables:

```bash
SYMPHONY_POD=https://<your pod host>/pod
SYMPHONY_AGENT=https://<your pod host>/agent
SESSION_AUTH=https://<your api host>/sessionauth
KEY_AUTH=https://<your keyauth host>/keyauth
BOT_USER=<your helpdesk user name. Should match the .p12 cert name>
BOT_DOMAIN=<your domain>
ADMIN_USER=<admin user email>
TRUSTSTORE_PASSWORD=<password for server.truststore>
KEYSTORE_PASSWORD=<password for helpdesk_bot_user_name.p12 cert>
```

4. Run the image with command:

```bash
docker run -it --rm \
  -v "$PWD"/certs:/usr/src/app/certs \
  -v "$PWD"/data:/usr/src/app/data \
  --env-file proxy_bot.env \
  helpdesk-bot
```

The running image will store runtime data into the `data` subdirectory.


## Contribute
This project was initiated at [IHS Markit](https://www.ihsmarkit.com) and has been developed as open-source from the very beginning.

Contributions are accepted via GitHub pull requests. All contributors must be covered by contributor license agreements to comply with the [Code Contribution Process](https://symphonyoss.atlassian.net/wiki/display/FM/Code+Contribution+Process).
