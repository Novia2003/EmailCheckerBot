package ru.tbank.emailcheckerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class EmailUIDService {

    private final EmailSessionPropertiesService emailSessionPropertiesService;

    public long getLastMessageUID(String email, String emailProvider, String token) {
        long lastUIDMessage = 0;

        try {
            Properties properties = emailSessionPropertiesService.getSessionProperties(emailProvider);

            Session session = Session.getInstance(properties);
            Store store = session.getStore("imap");
            store.connect(
                    properties.getProperty("mail.imap.host"),
                    email,
                    token
            );

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            UIDFolder uidFolder = (UIDFolder) inbox;
            uidFolder.getUIDNext();
            lastUIDMessage = uidFolder.getUIDNext() - 1;

            inbox.close(false);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lastUIDMessage;
    }
}
