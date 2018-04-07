package ki.robotics.utility.crisp;

import java.util.ArrayList;

public class Message<T> {
    private char messageGroup;
    private String mnemonic;
    private T[] parameters;


    public Message(String mnemonic, T ... parameters) {
        if (mnemonic.length() != 4) {
            mnemonic = CRISP.UNSUPPORTED_INSTRUCTION;
        }
        this.mnemonic = mnemonic;
        this.parameters = parameters;
        this.messageGroup = identifyMessageGroup(mnemonic);
    }


    public static ArrayList<Message> decodeTransmission(String transmission) {
        ArrayList<Message> messages = new ArrayList<>();
        String[] isolatedMessages = transmission.split(",");
        for (String s : isolatedMessages) {
            String trimmedMessage = s.trim();
            messages.add(decodeMessage(trimmedMessage));
        }
        return messages;
    }

    private static Message decodeMessage(String message) {
        String[] elements = message.split(" ");
        String mnemonic = elements[0];
        try {
            Integer[] intParameters = new Integer[elements.length - 1];
            for (int i = 1  ;  i < elements.length  ;  i++) {
                intParameters[i-1] = Integer.parseInt(elements[i]);
            }
            return new Message<>(mnemonic, intParameters);
        } catch (NumberFormatException e) {
            try {
                Double[] doubleParameters = new Double[elements.length - 1];
                for (int i = 1; i < elements.length; i++) {
                    doubleParameters[i - 1] = Double.parseDouble(elements[i]);
                }
                return new Message<>(mnemonic, doubleParameters);
            } catch (Exception e1) {
                return new Message<>(CRISP.UNSUPPORTED_INSTRUCTION);
            }
        } catch (Exception e) {
            return new Message<>(CRISP.UNSUPPORTED_INSTRUCTION);
        }
    }

    public char getMessageGroup() {
        return messageGroup;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public T[] getParameters() {
        return parameters;
    }

    public T getParameter() {
        if (parameters == null) {
            throw new IndexOutOfBoundsException();
        }
        return parameters[0];
    }

    private char identifyMessageGroup(String mnemonic) {
        char firstLetter = Character.toUpperCase(mnemonic.charAt(0));
        switch (firstLetter) {
            case CRISP.BOT_INSTRUCTION:
                return CRISP.BOT_INSTRUCTION;
            case CRISP.SENSOR_INSTRUCTION:
                return CRISP.SENSOR_INSTRUCTION;
            case CRISP.CAMERA_INSTRUCTION:
                return CRISP.CAMERA_INSTRUCTION;
            default:
                return CRISP.OTHER_INSTRUCTION;
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(mnemonic);
        for (T p : parameters) {
            sb.append(" ").append(String.valueOf(p));
        }
        return sb.toString();
    }
}