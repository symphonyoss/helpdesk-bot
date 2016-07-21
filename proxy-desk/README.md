Symphony Proxy Desk Bot
-----------------------
Description:


     The Proxy Bot is an implementation of the help desk bot, where all messages and help requests are handled directly
     through the bot. Users seeking help can talk directly to the proxy desk bot.The bot itself will then detect that a 
     new one on one conversation has begun and start listening on the chat. In addition, the bot will also cache the user 
     as a client and place the user in queue for clients waiting to be helped. By listening on the chat, the bot can detect 
     when new messages have been sent by the user. This allows the bot to store all the clients help requests messages, and 
     route those messages to online, active members. From there, a member can accept a client into a virtual, completly private 
     chat session between the users. In these chat sessions, other members will be unable to see incoming messages from the client,
     and the member will be unable to recieve help request messagesfrom other clients seeking help. Instead, when a user sends 
     a message to the bot, the message will be routed by the bot to the opposite party. Essentialy, the bot creates a new listener 
     for the call, removes the orignal listeners for both party's respective chats and registers the new chat listener
     on both partiy's chats. Finally, members can use this chat session to answer the clients question and then exit the
     session, and begin the entire help process over again.
     
Features:

       -Member alias system
       -Set if the member is online
       -Automatically shut down chat session, after a certain period of inactivity
       -Set tags that permit context routing for help requests
       -See online mebers
       -See member queues
       -Members can join existing chat sessions
       -Veiw a list of active calls (chat sessions)
       -Members can add members
       -Bot detects member presences and will not send messages to members who are busy
       
Setup:
       
       -Set the location for JSON member files to be saved via properties file, enviroment variable or system property
       -Set the location of your certs via properties file, enviroment variable or system property
       -Set the location of your trust store file via properties file, enviroment variable or system property
       -Set the admin user using their email via properties file, enviroment variable or system property
       -Set the name of the bot account via properties file, enviroment variable or system property
     
     