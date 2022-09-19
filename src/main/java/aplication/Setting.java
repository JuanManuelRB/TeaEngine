package aplication;

public sealed interface Setting permits ApplicationSetting, GameSetting {
    void setName(String title);
}
