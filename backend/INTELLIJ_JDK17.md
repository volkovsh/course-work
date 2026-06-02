# Настройка JDK 17 в IntelliJ IDEA

Проект собирается под **Java 17**. Если в IDE выбран JDK 24 (или другой), сборка может падать с ошибкой `TypeTag :: UNKNOWN` (несовместимость с Lombok).

## Как выставить JDK 17 в IntelliJ

1. **File** → **Project Structure** (или `Ctrl+Alt+S` / `Cmd+;`).
2. Слева: **Platform Settings** → **SDKs**.
3. Нажмите **+** (Add SDK) → **Download JDK...**.
4. Выберите:
   - **Version:** 17  
   - **Vendor:** Eclipse Temurin (или Amazon Corretto)  
   - **Location:** оставьте по умолчанию.
5. Нажмите **Download** и дождитесь установки.
6. В **Project Structure** откройте **Project** (слева) и в поле **SDK** выберите только что скачанный **17**.
7. **Project language level** установите в **17**.
8. Нажмите **Apply** → **OK**.

После этого пересоберите проект: **Build** → **Rebuild Project**.

## Альтернатива: установка Java 17 в системе

- **SDKMAN** (в терминале):  
  `sdk install java 17.0.18-tem`  
  затем в IntelliJ: **Add SDK** → **Add JDK** → указать путь к `~/.sdkman/candidates/java/17.0.18-tem`.

- **Homebrew**:  
  `brew install openjdk@17`  
  затем в IntelliJ указать путь к установленному JDK (обычно `/opt/homebrew/opt/openjdk@17` или `/usr/local/opt/openjdk@17`).
