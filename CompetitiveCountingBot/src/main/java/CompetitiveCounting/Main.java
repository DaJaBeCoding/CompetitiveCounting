/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;


import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;

/**
 *
 * @author DavidPrivat
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    private static CountingBot bot;

    public static void main(String[] args) {
        GatewayDiscordClient client;
        try {
        client = DiscordClientBuilder.create(Storage.loadConfig())
                .build()
                .login()
                .block();
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(event -> {
                    final User self = event.getSelf();
                    System.out.println(String.format(
                            "Logged in as %s#%s", self.getUsername(), self.getDiscriminator()
                    ));
                });
        bot = new CountingBot();
        MessageHandler messageHandler = new MessageHandler(bot);
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(messageHandler);
        // database
        /*
        DatabaseConnection databaseConnection = new DatabaseConnection();
        databaseConnection.printAll();
*/
        
        // database end
        
        client.onDisconnect().block();
    }
    
}
