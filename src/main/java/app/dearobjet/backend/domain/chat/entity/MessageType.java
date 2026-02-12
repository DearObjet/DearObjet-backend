package app.dearobjet.backend.domain.chat.entity;

public enum MessageType {
    TEXT("텍스트"),
    IMAGE("이미지"),
    FILE("파일"),
    SYSTEM("시스템 메시지");

    private final String description;

    MessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 미디어 타입 확인 (이미지 또는 파일)
     */
    public boolean isMediaType() {
        return this == IMAGE || this == FILE;
    }
}