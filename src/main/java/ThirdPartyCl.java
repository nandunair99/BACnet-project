import listener.IBacNetVariableListener;

public class ThirdPartyCl implements IBacNetVariableListener {
    @Override
    public void onVariableChange(int unitObjectId, int objectId, Object newValue) {
        System.out.println("Device ID: " + unitObjectId + " Object ID: " + objectId + " New value: " + newValue);
    }
}
