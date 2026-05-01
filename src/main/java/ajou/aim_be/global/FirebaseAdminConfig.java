package ajou.aim_be.global;

import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class FirebaseAdminConfig {

    private static final String DEFAULT_CREDENTIALS_CLASSPATH =
            "firebase/ajou-project-cafd9-firebase-adminsdk-fbsvc-e6d8a32d57.json";

    private final String credentialsPath;
    private final String credentialsJson;
    private final String storageBucket;

    public FirebaseAdminConfig(
            @Value("${firebase.credentials.path:}") String credentialsPath,
            @Value("${firebase.credentials.json:}") String credentialsJson,
            @Value("${firebase.storage.bucket:ajou-project-cafd9.firebasestorage.app}") String storageBucket) {
        this.credentialsPath = credentialsPath;
        this.credentialsJson = credentialsJson;
        this.storageBucket = storageBucket;
    }

    @PostConstruct
    public void init() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try {
            try (InputStream serviceAccount = openCredentialsStream()) {
                GoogleCredentials credentials = serviceAccount == null
                        ? GoogleCredentials.getApplicationDefault()
                        : GoogleCredentials.fromStream(serviceAccount);

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setStorageBucket(storageBucket)
                        .build();

                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FIREBASE_INIT_FAILED);
        }
    }

    private InputStream openCredentialsStream() throws IOException {
        if (StringUtils.hasText(credentialsJson)) {
            return new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
        }

        if (StringUtils.hasText(credentialsPath)) {
            return Files.newInputStream(Path.of(credentialsPath));
        }

        InputStream classpathCredentials =
                getClass().getClassLoader().getResourceAsStream(DEFAULT_CREDENTIALS_CLASSPATH);

        return classpathCredentials;
    }
}
