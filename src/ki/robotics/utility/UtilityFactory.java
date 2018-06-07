package ki.robotics.utility;

import ki.robotics.utility.crisp.InstructionSequence;
import ki.robotics.utility.crisp.InstructionSequenceImpl;
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
}
