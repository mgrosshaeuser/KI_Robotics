package ki.robotics.utility.crisp;

public interface Message<T> {
    char getMessageGroup();

    String getMnemonic();

    T[] getParameters();

    T getParameter();

    @Override
    String toString();
}
