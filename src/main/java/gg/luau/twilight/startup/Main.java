package gg.luau.twilight.startup;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final Emoji MONDAY = Emoji.fromMarkdown("😭");
    private static final Emoji TUESDAY = Emoji.fromMarkdown("😒");
    private static final Emoji WEDNESDAY = Emoji.fromMarkdown("😬");
    private static final Emoji THURSDAY = Emoji.fromMarkdown("☺️");
    private static final Emoji FRIDAY = Emoji.fromMarkdown("🥳");
    private static final Emoji SATURDAY = Emoji.fromMarkdown("🥴");
    private static final Emoji SUNDAY = Emoji.fromMarkdown("😰");
    private static final long DRAGONS_SCHEDULE = 746781834045685831L;

    public static void main(String... args) throws LoginException, InterruptedException {
        final var token = System.getenv("TWILIGHT_TOKEN");
        final var jda = JDABuilder.createLight(token)
                .addEventListeners(new Main())
                .setActivity(Activity.listening("Sam being silly."))
                .build();

        jda.upsertCommand("schedule", "Posts a schedule draft that players can interact with.").queue();
        jda.awaitReady();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("schedule"))
            return;

        if (!Util.isDragonsManager(event.getMember())) {
            event.reply("You cannot use this command as you are not a Dragons manager.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        final var embed = new EmbedBuilder()
                .setAuthor("Power Dragons Availability", null, "https://i.imgur.com/neWj6SS.png")
                .setColor(new Color(255, 70, 70))
                .setDescription("No one has responded yet.")
                .setFooter("Schedule generated by " + event.getMember().getUser().getAsTag(), event.getMember().getEffectiveAvatarUrl())
                .setTimestamp(OffsetDateTime.now())
                .build();

        final var message = new MessageBuilder()
                .setContent(String.format("<@&%s>", Util.DRAGONS_PLAYER))
                .setEmbeds(embed)
                .build();

        event.reply(message)
                .addActionRow(createSelectMenu("s0"))
                .addActionRow(Button.success("c0", "Confirm"), Button.danger("f0", "Cancel"))
                .queue();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        switch (event.getComponentId()) {
            case "c0" -> {
                if (event.getMember() == null) {
                    LOGGER.error("This should never happen. Interaction is apparently outside a guild.");
                    event.reply("There was a technical difficulty. Please post the schedule manually.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                if (!Util.isDragonsManager(event.getMember())) {
                    event.reply("You cannot confirm this poll as you not a Dragons manager.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                event.reply("Select the days that Dragons will play on.")
                        .addActionRow(createSelectMenu("confirm_s0"))
                        .setEphemeral(true)
                        .queue();
            }

            case "f0" -> {
                if (event.getMember() == null) {
                    LOGGER.error("This should never happen. Interaction is apparently outside a guild.");
                    event.reply("There was a technical difficulty. Please cancel the schedule manually.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                if (!Util.isDragonsManager(event.getMember())) {
                    event.reply("You cannot cancel this poll as you are not a Dragons manager.")
                            .setEphemeral(true)
                            .queue();

                    return;
                }

                final var embed = new EmbedBuilder()
                        .setAuthor("Power Dragons Availability", null, "https://i.imgur.com/neWj6SS.png")
                        .setColor(new Color(255, 70, 70))
                        .setDescription("This poll has been cancelled by a Dragons manager.")
                        .setFooter("Cancelled by " + event.getMember().getUser().getAsTag(), event.getMember().getEffectiveAvatarUrl())
                        .setTimestamp(OffsetDateTime.now())
                        .build();

                        event.editComponents(event.getMessage().getActionRows().get(1).asDisabled()).queue();
                event.getHook().editOriginalEmbeds(embed).queue();
            }

            default -> {}
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        if (event.getComponentId().equals("s0")) {
            if (!Util.canParticipate(event.getMember())) {
                event.reply("You cannot vote on this poll as you are not a Dragons player or tryout.")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            final var member = event.getMember();

            if (member == null) {
                LOGGER.error("This should never happen. Indicates interaction is outside of a guild?");
                event.reply("There was a technical issue during your vote. Please tag the Dragons manager manually!")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            final var days = convertOptionsToString(event.getSelectedOptions());
            final var originalEmbed = event.getMessage().getEmbeds().get(0);
            final var originalDescription = originalEmbed.getDescription();
            final var embedBuilder = new EmbedBuilder(originalEmbed);

            if (originalDescription == null) {
                LOGGER.error("Description was null.");
                event.reply("There was a technical issue during your vote. Please tag the Dragons manager manually!")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            if (originalDescription.startsWith("No")) {
                embedBuilder.setDescription(String.format("%s: %s", member.getAsMention(), days));
            } else {
                // It is bad to compile patterns at runtime, but I don't want to do substring hell / a long custom
                // solution for this. Also, the performance penalty is negligible at this scale.

                final var pattern = Pattern.compile("(\n?)(<@!?" + member.getId() + ">: )(.+)");
                final var matcher = pattern.matcher(originalDescription);

                if (!matcher.find())
                    embedBuilder.appendDescription("\n" + member.getAsMention() + ": " + days);
                else
                    embedBuilder.setDescription(matcher.replaceAll("$1$2" + days));
            }

            event.editMessageEmbeds(embedBuilder.build()).queue();
        } else if (event.getComponentId().equals("confirm_s0")) {
            final var messageReference = event.getMessage().getMessageReference();

            if (messageReference == null)
                LOGGER.warn("Message reference is null.");
            else
                messageReference.resolve().flatMap(Message::delete).queue();

            final var days = convertOptionsToString(event.getSelectedOptions());
            final var dragonsScheduleChannel = event.getJDA().getTextChannelById(DRAGONS_SCHEDULE);
            final var embed = new EmbedBuilder()
                    .setAuthor("Power Dragons Schedule", null, "https://i.imgur.com/neWj6SS.png")
                    .setColor(new Color(255, 70, 70))
                    .setDescription("We do be vibing on " + days + ". 🙂")
                    .setFooter("Schedule finalised by " + event.getMember().getUser().getAsTag(), event.getMember().getEffectiveAvatarUrl())
                    .setTimestamp(OffsetDateTime.now())
                    .build();

            if (dragonsScheduleChannel == null) {
                LOGGER.error("Dragons schedule channel is not in cache.");
                event.reply("There was an error when trying to post the schedule. Please post the schedule manually.")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            event.replyFormat("Posted schedule in <#%s>!", DRAGONS_SCHEDULE).setEphemeral(true).queue();
            dragonsScheduleChannel.sendMessageFormat("<@&%s>", Util.DRAGONS_PLAYER).setEmbeds(embed).queue();
        }
    }

    private String convertOptionsToString(List<SelectOption> options) {
        return "**" + options.stream()
                .map(SelectOption::getLabel)
                .collect(Collectors.joining(", ")) + "**";
    }

    private SelectMenu createSelectMenu(String componentId) {
        return SelectMenu.create(componentId)
                .setPlaceholder("Select the days you can play.")
                .addOption("Monday", "mon", "Indicate that you can play on Monday.", MONDAY)
                .addOption("Tuesday", "tue", "Indicate that you can play on Tuesday.", TUESDAY)
                .addOption("Wednesday", "wed", "Indicate that you can play on Wednesday.", WEDNESDAY)
                .addOption("Thursday", "thu", "Indicate that you can play on Thursday.", THURSDAY)
                .addOption("Friday", "fri", "Indicate that you can play on Friday.", FRIDAY)
                .addOption("Saturday", "sat", "Indicate that you can play on Saturday.", SATURDAY)
                .addOption("Sunday", "sun", "Indicate that you can play on Sunday.", SUNDAY)
                .setRequiredRange(1, 7)
                .build();
    }
}
