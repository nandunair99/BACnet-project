package listener;

public interface IBacNetVariableListener {
    public void onVariableChange(int unitObjectId,int objectId,Object oldValue,Object newValue);
}
