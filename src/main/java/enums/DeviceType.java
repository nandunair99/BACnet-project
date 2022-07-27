package enums;

public enum DeviceType {

    INDOOR_UNIT("Indoor Unit");
    public final String type;

    private DeviceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}