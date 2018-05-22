import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.base.Sex;
import com.vk.api.sdk.objects.groups.UserXtrRole;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.responses.*;
import com.vk.api.sdk.queries.users.UserField;

import java.util.*;

public class GroupBot
{
    public static final String SEARCH_BY_FIRST_NAME = "first_name";
    public static final String SEARCH_BY_LAST_NAME = "last_name";
    public static final UserField SEARCH_BY_SEX = UserField.SEX;
    public static final UserField SEARCH_BY_BDATE = UserField.BDATE;
    public static final UserField SEARCH_BY_CITY = UserField.CITY;
    public static final UserField SEARCH_BY_COUNTRY = UserField.COUNTRY;
    public static final UserField SEARCH_BY_HOME_TOWN = UserField.HOME_TOWN;
    public static final UserField SEARCH_BY_DOMAIN = UserField.DOMAIN;
    public static final UserField SEARCH_BY_NICKNAME = UserField.NICKNAME;
    public static final UserField SEARCH_BY_CONNECTIONS = UserField.CONNECTIONS;
    public static final UserField SEARCH_BY_SCREEN_NAME = UserField.SCREEN_NAME;
    public static final UserField SEARCH_BY_MAIDEN_NAME = UserField.MAIDEN_NAME;
    public static final UserField SEARCH_BY_CONTACTS = UserField.CONTACTS;
    public static final Sex MALE_SEX = Sex.MALE;
    public static final Sex FEMALE_SEX = Sex.FEMALE;
    public static final Sex UNKNOWM_SEX = Sex.UNKNOWN;

    private VkApiClient vk;
    private GroupActor actor;
    private Integer group_id;
    private String group_key;
    private boolean isConnected = false;

    public GroupBot(Integer group_id, String group_key)
    {
        this.group_id = group_id;
        this.group_key = group_key;
    }

    public void connect()
    {
        TransportClient transportClient = new HttpTransportClient();
        vk = new VkApiClient(transportClient);
        actor = new GroupActor(group_id, group_key);
        isConnected = true;
    }

    public void sendMessage(Integer userId, String message) throws ClientException, ApiException
    {
        if (isConnected)
        {
            vk.messages().send(actor).userId(userId).message(message).execute();
        }
    }

    public ArrayList<Integer> searchUserByFields(String query, UserField... fields) throws ClientException, ApiException
    {
        ArrayList<Integer> result = new ArrayList<>();
        if (isConnected)
        {
            List<UserXtrRole> users = vk.groups().getMembers(actor, fields).groupId(String.valueOf(group_id)).execute().getItems();
            for (UserXtrRole user : users)
            {
                if (user.getFirstName().equals(query))
                {
                    result.add(user.getId());
                }
            }
        }
        return result;
    }

    public List<Dialog> getUnreadDialogs() throws ClientException, ApiException
    {
        List<Dialog> unreadDialogs = null;
        if (isConnected)
        {
            GetDialogsResponse dialogs = vk.messages().getDialogs(actor).unread(true).execute();
            if (dialogs.getCount() > 20)
            {
                dialogs = vk.messages().getDialogs(actor).unread(true).count(dialogs.getCount()).execute();
            }
            unreadDialogs = dialogs.getItems();
        }
        return unreadDialogs;
    }

    public Integer getUserIdByDialog(Dialog dialog)
    {
        return dialog.getMessage().getUserId();
    }

    public List<Message> getUnreadMessagesFromUser(Integer userId, Boolean isReverse) throws ClientException, ApiException
    {
        ArrayList<Message> unreadMessages = new ArrayList<>();
        if (isConnected)
        {
            GetHistoryResponse dialog = vk.messages().getHistory(actor).userId(String.valueOf(userId)).execute();
            if (dialog.getUnread() != null)
            {
                if (dialog.getUnread() > 20)
                {
                    dialog = vk.messages().getHistory(actor).userId(String.valueOf(userId)).count(dialog.getUnread()).execute();
                }
                for (Message message : dialog.getItems())
                {
                    if (!message.isReadState())
                    {
                        if (isReverse)
                        {
                            unreadMessages.add(0, message);
                        }
                        else
                        {
                            unreadMessages.add(message);
                        }
                    }
                }
            }
        }
        return unreadMessages;
    }

    public String readMessage(Integer messageId) throws ClientException, ApiException
    {
        String messageBody = null;
        if (isConnected)
        {
            List<Message> messages = vk.messages().getById(actor, messageId).execute().getItems();
            messageBody = messages.get(0).getBody();
            vk.messages().markAsRead(actor).messageIds(messages.get(0).getId()).execute();
        }
        return messageBody;
    }

    public String readFirstUnreadMessage(Integer userId) throws ClientException, ApiException
    {
        List<Message> reverseUnreadMessages = this.getUnreadMessagesFromUser(userId, true);
        String message = null;
        if (reverseUnreadMessages.size() > 0)
        {
            message = this.readMessage(reverseUnreadMessages.get(0).getId());
        }
        return message;
    }
}
