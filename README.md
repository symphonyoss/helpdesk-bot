**Symphony Helpdesk Bot**
------------------------

[![Dependencies](https://www.versioneye.com/user/projects/5770f47919424d000f2e0095/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/5770f47919424d000f2e0095)
[![Build Status](https://travis-ci.org/symphonyoss/symphony-java-client.svg)](https://travis-ci.org/symphonyoss/symphony-java-client)
[![Validation Status](https://scan.coverity.com/projects/9112/badge.svg?flat=1)](https://scan.coverity.com/projects/symphonyoss-symphony-java-client)

Description:


     Help Desk BOT(s) are Java based applications that implement a call routing workflow
     based framework leveraging the Symphony API.  To implement a common design pattern which 
     supports a call routing workflow from a named end-point (network user) to an application 
     (BOT) end-point named alias (operator/call router), which routes to a rota of other named
     user end-points who service the call.
     
General Concepts:

    All communications from initiating user end-points to application end-point are private.
    
    All communications from the application end-point to the rota of servicing user end-points4
    are shared. Rules can be applied to prevent communications between servicing users.
     
    The application end-point will maintain session state with all end-points.
     
    The application end-point will have additional programmatic workflow hooks to support other
    implementations. These include event listeners and interfaces through the life-cycle of call handling.
    e.g. Connect, Validation, Session, Message, Rules, Context, Commands, Termination

**Help Desk BOT Types:**

Proxy Desk: 

    All commuications flows through (ingress/egress) the BOT application, which handles all call routing.
    Command line interface is provided to members to manage all calls. 
    
    U(User) < - - > O(Operator)< - - > R(Rota of Users/members)
    (~~~~~~~~~~~~~~~~~~~~)< - - >Ru(Rota User callback, proxy through O)


Room Desk:

    All inbound user communications flows through the BOT into a chat room of members.  Command line interface is 
    provided to all members, but calls are established through an external multi-party conversation outside the 
    chatroom.
    
    U(User) < - - > O(Operator)< - > R(Rota of Users/members)
    (~~~~~) <- - - - - - - - - - - - - - - -Ru(Rota User callback, direct)


Web Desk:

    All incound users leverage an external web client, which communicates with a backend BOT service that implements
     the Room Desk construct.
      
      U(User) < - -> Web Module < - - > O (Operator) <- > R(Rota of users/members)
      (~~~~~) <-------------------------------------------Ru(Rota User callback)


**Requirements:**


Certificates:

        Please contact your Symphony local administrator to obtain the necessary certificates
        for the user/service account being used to access the POD.

        Server Truststore = Contains server certs
        User Keystore = Symphony user client certificate


Required System Properties:

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