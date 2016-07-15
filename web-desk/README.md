Symphony Web Desk Bot
---------------------
Description:


     The web desk is a web based service that has been integrated with the room desk. A user seeking help can
     fill out a help form on a web site and send it up to a multi-user chat room containing all the members, within
     symphony. In addition, the clients are placed in a web chat session upon form submission where they can make
     additional help requests. If the user does not provide a email registered within symphony, the client will be unable
     to connect to the web desk. Once a user has successfully requested help, a member can accept the client. The bot will
     then create a multi-user chat room on symphony with the bot, the web-client's symphony account and the member. All
     messages sent in this multi-user chat will be routed from symphony to the web chat session by the bot. Likewise, all
     web chat session messages will be routed from the web session to symphony by the bot. From a more technical perspective,
     a listener is listening for chat messages events on symphony, in order to route them to the web client. Similarly, 
     another listener is listening for chat messages on the web client, in order to route them to symphony. 
     
Features:

       -Join chats with clients on symphony and cleints on the webdesk
       -Member alias system
       -See client queue
       -Call notifications
       -Members can add members
       -Track calls between users
       -Members Chat