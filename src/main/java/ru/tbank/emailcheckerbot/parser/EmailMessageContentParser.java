package ru.tbank.emailcheckerbot.parser;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;
import java.util.ArrayList;
import java.util.List;

public class EmailMessageContentParser {

    private static final String TEXT_PLAIN_TYPE = "text/plain";
    private static final String TEXT_HTML_TYPE = "text/html";
    private static final String MULTIPART_TYPE = "multipart/*";

    public static String getTextFromMessage(Message message) {
        try {
            if (message.isMimeType(TEXT_HTML_TYPE)) {
                return message.getContent().toString();
            }

            if (message.isMimeType(TEXT_PLAIN_TYPE)) {
                return message.getContent().toString();
            }

            if (message.isMimeType(MULTIPART_TYPE)) {
                return getTextFromMimeMultipart((MimeMultipart) message.getContent());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "Возникли трудности с извлечением текста сообщения";
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();

        List<List<Integer>> parts = List.of(
                new ArrayList<>(), // htmlPartsIndexes
                new ArrayList<>(), // plainPartsIndexes
                new ArrayList<>()  // multiPartsIndexes
        );

        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);

            if (bodyPart.isMimeType(TEXT_HTML_TYPE)) {
                parts.get(0).add(i);
            }

            if (bodyPart.isMimeType(TEXT_PLAIN_TYPE)) {
                parts.get(1).add(i);
            }

            if (bodyPart.getContent() instanceof MimeMultipart) {
                parts.get(2).add(i);
            }
        }

        for (List<Integer> partsIndexes : parts) {
            if (!partsIndexes.isEmpty()) {
                appendContentFromIndexes(result, mimeMultipart, partsIndexes);
                return result.toString();
            }
        }

        return result.toString();
    }

    private static void appendContentFromIndexes(StringBuilder result, MimeMultipart mimeMultipart, List<Integer> indexes) throws Exception {
        for (int i : indexes) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);

            if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            } else {
                result.append(bodyPart.getContent().toString());
            }
        }
    }

}
