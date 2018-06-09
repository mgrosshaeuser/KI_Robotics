package ki.robotics.utility;

import ki.robotics.utility.crisp.InstructionSequence;
import ki.robotics.utility.crisp.InstructionSequenceImpl;
import ki.robotics.utility.crisp.Message;
import ki.robotics.utility.crisp.MessageImpl;
import ki.robotics.utility.map.MapProvider;
import ki.robotics.utility.map.MapProviderImpl;


import java.util.ArrayList;

abstract public class UtilityFactory {
    public static InstructionSequence createNewInstructionSequence() {
        return new InstructionSequenceImpl();
    }

    public static MapProvider getMapProvider() {
        return MapProviderImpl.getInstance();
    }

    public static Message createMeassage(String mnemonic) {
        return new MessageImpl<>(mnemonic, 0);
    }

    public static Message createMessage(String mnemonic, int ... args) {
        Integer[] wrappedArray = new Integer[args.length];
        for (int i = 0   ;   i < args.length   ; i++) {
            wrappedArray[i] = args[i];
        }
        return new MessageImpl<>(mnemonic, wrappedArray);
    }

    public static Message createMessage(String mnemonic, double ... args) {
        Double[] wrappedArray = new Double[args.length];
        for (int i = 0   ;   i < args.length   ; i++) {
            wrappedArray[i] = args[i];
        }
        return new MessageImpl<>(mnemonic, wrappedArray);
    }

    public static ArrayList<Message> createMessageListFromTransmission(String transmission) {
        return MessageImpl.decodeTransmission(transmission);
    }
}
