package by.vadarod.autoparts.kafka;

public enum OrderEventTag {
    CREATE("создание"),
    UPDATE("изменение"),
    DELETE("удаление");

    private final String value;

    OrderEventTag(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
