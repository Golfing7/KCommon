package com.golfing8.kcommon.config.lang;

import com.golfing8.kcommon.command.impl.KPagerCommand;
import com.golfing8.kcommon.util.MS;
import com.golfing8.kcommon.util.MathUtil;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A wrapper for a message that should be paged.
 */
@Getter
public final class PagedMessage {
    public static final String PAGE_MOVEABLE_FORMAT = "<green><hover:show_text:'<yellow>{DIRECTION} Page'><click:run_command:/pager {ID} {PAGE}>{FORMAT}</click></hover></green>";
    public static final String PAGE_IMMOVABLE_FORMAT = "<gray>{FORMAT}</gray>";
    public static final String NEXT_BUTTON = "[>]";
    public static final String PREVIOUS_BUTTON = "[\\<]";
    public static final String DEFAULT_PAGE_HEADER = "&6&m-------------&r &a{PREVIOUS} &ePage &e{PAGE}&7/&e{MAX_PAGE} &a{NEXT} &6&m-------------";
    public static final String DEFAULT_PAGE_FOOTER = "&6&m----------------------------------------------------";
    public static final int DEFAULT_PAGE_HEIGHT = 10;

    private final String id = UUID.randomUUID().toString();
    private final List<String> parsedMessages;
    private final List<List<String>> pagedMessages;
    /**
     * The total amount of pages
     */
    private final int totalPages;
    private final int messagesPerPage;

    private final String pageHeader;
    private final String pageFooter;

    public PagedMessage(List<String> parsedMessages, int messagesPerPage, String pageHeader, String pageFooter) {
        if (parsedMessages.isEmpty()) {
            this.parsedMessages = Lists.newArrayList("&cThe message was empty :(");
        } else {
            this.parsedMessages = new ArrayList<>(parsedMessages);
        }
        this.messagesPerPage = messagesPerPage;

        this.pageHeader = pageHeader;
        this.pageFooter = pageFooter;
        this.pagedMessages = Lists.partition(this.parsedMessages, messagesPerPage);
        this.totalPages = this.pagedMessages.size();
    }

    public PagedMessage(Message message) {
        this(message.getMessages(), message.getPageHeight(), message.getPageHeader(), message.getPageFooter());
    }

    /**
     * Displays the given page to the given sender.
     *
     * @param sender the sender.
     * @param page   the page, coerced into range
     */
    public void displayTo(CommandSender sender, int page, Object... placeholders) {
        page = MathUtil.clamp(page, 1, totalPages);

        sendHeader(sender, page);
        MS.pass(sender, this.pagedMessages.get(page - 1), placeholders);
        MS.pass(sender, pageFooter);

        // Add or refresh this message.
        KPagerCommand.getInstance().addMessage(this);
    }

    /**
     * Sends the header of the message to the sender.
     *
     * @param sender the sender.
     * @param page   the page of the header.
     */
    private void sendHeader(CommandSender sender, int page) {
        String nextButton = MS.parseSingle(page < totalPages ? PAGE_MOVEABLE_FORMAT : PAGE_IMMOVABLE_FORMAT,
                "ID", this.id,
                "PAGE", page + 1,
                "DIRECTION", "Next",
                "FORMAT", NEXT_BUTTON);
        String previousButton = MS.parseSingle(page > 1 ? PAGE_MOVEABLE_FORMAT : PAGE_IMMOVABLE_FORMAT,
                "ID", this.id,
                "PAGE", page - 1,
                "DIRECTION", "Previous",
                "FORMAT", PREVIOUS_BUTTON);

        MS.pass(sender, pageHeader,
                "PREVIOUS", previousButton,
                "NEXT", nextButton,
                "PAGE", page,
                "MAX_PAGE", totalPages);
    }
}
