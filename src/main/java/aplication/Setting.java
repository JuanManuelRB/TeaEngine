package aplication;

public sealed interface Setting permits AbstractSetting, ApplicationSetting, GameSetting {
    void setName(String title);
}
