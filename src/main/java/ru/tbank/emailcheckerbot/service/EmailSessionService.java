package ru.tbank.emailcheckerbot.service;

import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.message.EmailMessage;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import java.io.IOException;
import java.util.Properties;

@Service
public class EmailSessionService {

    private final static String STORE_PROTOCOL = "imap";
    private final static String STORE_CONNECT_PROPERTY = "mail.imap.host";
    private final static String INBOX_FOLDER_NAME = "INBOX";

    private Folder getInboxFolder(Properties properties, String email, String token) throws MessagingException {
        Session session = Session.getInstance(properties);

        Store store = session.getStore(STORE_PROTOCOL);
        store.connect(
                properties.getProperty(STORE_CONNECT_PROPERTY),
                email,
                token
        );

        return store.getFolder(INBOX_FOLDER_NAME);
    }

    public Long getLastMessageUID(Properties properties, String email, String token) {
        long lastUIDMessage;

        Folder folder;
        try {
            folder = getInboxFolder(properties, email, token);
            folder.open(Folder.READ_ONLY);
            UIDFolder uidFolder = (UIDFolder) folder;
            uidFolder.getUIDNext();
            lastUIDMessage = uidFolder.getUIDNext() - 1;

            folder.close(false);
            folder.getStore().close();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        return lastUIDMessage;
    }

    public EmailMessage getMessageByUID(Properties properties, String email, String token, long messageUID) {
        EmailMessage emailMessage;

        Folder folder;
        try {
            folder = getInboxFolder(properties, email, token);
            folder.open(Folder.READ_WRITE);

            UIDFolder uidFolder = (UIDFolder) folder;
            uidFolder.getUIDNext();
            Message message = uidFolder.getMessageByUID(messageUID);
            message.setFlags(new Flags(Flags.Flag.SEEN), true);
            emailMessage = EmailMessage.of(messageUID, message);

            folder.close(false);
            folder.getStore().close();
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }

        return emailMessage;
    }

    public EmailMessage[] getNewMessages(Properties properties, String email, String token, long lastMessageUID) {
        EmailMessage[] emailMessages;

        Folder folder;
        try {
            folder = getInboxFolder(properties, email, token);
            folder.open(Folder.READ_ONLY);

            UIDFolder uidFolder = (UIDFolder) folder;
            Message[] newMessages = uidFolder.getMessagesByUID(lastMessageUID + 1, UIDFolder.MAXUID);

            emailMessages = new EmailMessage[newMessages.length];
            for (int i = 0; i < newMessages.length; i++) {
                Message message = newMessages[i];
                emailMessages[i] = EmailMessage.of(uidFolder.getUID(message), message);
            }

            folder.close(false);
            folder.getStore().close();
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }

        return emailMessages;
    }
}
