Symphony Room Desk Bot
--------------------
Description:


     The Room Desk is an implementation of the symphony help bot where incoming messages are handled in multi-user chat rooms.
     A client seeking help can send a message directly to the bot in a on eon one chat. The bot will then relay that message into
     a multi-user chat room containing all the members of the room desk. A member can then accept a client into a call. When a
     client is accepte into the call, the bot will place itself, the member and the client in their own multi-party conversation
     within symphony. From there, a member can answer the clients questions and then proceed to exit the call to pick up more 
     incoming help requests. While commanding the bot could have been implemented within the actual multi-user member chat room,
     all member commands go directly to the bot. This prevents other members from seeing commands and permmited the implementation
     of an alias system. Finally, in the current implementation, the bot only really listens on the one to one user chats, as 
     conversations between users are handled through symphony itself. 
     
Features:

       -Member alias system
       -See client queue
       -Call notifications
       -Members can add members
       -Track calls between users
       