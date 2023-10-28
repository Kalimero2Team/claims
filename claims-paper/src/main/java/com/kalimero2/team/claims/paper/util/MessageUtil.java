package com.kalimero2.team.claims.paper.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.intellij.lang.annotations.Subst;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MessageUtil {

    private final ResourceBundle messageBundle;
    private final List<TagResolver> defaultPlaceholders;

    public MessageUtil(Locale locale) {
        messageBundle = ResourceBundle.getBundle("messages", locale);

        // convert each placeholder.name in messageBundle to an array of TagResolvers

        defaultPlaceholders = new ArrayList<>();
        for (String key : messageBundle.keySet()) {
            if (key.startsWith("placeholder.")) {
                String message = messageBundle.getString(key);
                @Subst("") String substring = key.substring(12);
                defaultPlaceholders.add(Placeholder.parsed(substring, message));
            }
        }


    }

    public void sendMessage(CommandSender sender, String key, TagResolver... tagResolvers) {
        sender.sendMessage(getMessage(key, tagResolvers));
    }

    public Component getMessage(String key, TagResolver... tagResolvers) {
        ArrayList<TagResolver> placeholders = new ArrayList<>(defaultPlaceholders);
        placeholders.addAll(List.of(tagResolvers));
        return MiniMessage.miniMessage().deserialize(messageBundle.getString(key), placeholders.toArray(new TagResolver[0]));
    }

    public ResourceBundle getMessageBundle() {
        return messageBundle;
    }
}
