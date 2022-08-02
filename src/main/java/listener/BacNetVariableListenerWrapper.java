package listener;

public class BacNetVariableListenerWrapper {
    private IBacNetVariableListener iBacNetVariableListener;
    private Object oldValue;
    private Object newValue;

    public IBacNetVariableListener getiBacNetVariableListener() {
        return iBacNetVariableListener;
    }

    public void setiBacNetVariableListener(IBacNetVariableListener iBacNetVariableListener) {
        this.iBacNetVariableListener = iBacNetVariableListener;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }
}
