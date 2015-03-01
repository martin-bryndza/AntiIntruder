package eu.bato.anyoffice.frontend.security;

//import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
public class HeaderUtil {

    private static final Logger log = LoggerFactory.getLogger(HeaderUtil.class);

    private final EncryptionUtil encryptionUtil = new EncryptionUtil();

    private static final String HEADER_NAME = "X-Auth-Token";

    private Period sessionMaxAge;

    private String seed;

    @Autowired
    private Environment environment;

    @PostConstruct
    private void init() {
        String encryptionEnabled = environment.getProperty("auth.encryption.enabled");
        if (encryptionEnabled!=null && !encryptionEnabled.isEmpty() && Boolean.parseBoolean(encryptionEnabled)) {
            encryptionUtil.encryptionEnabled(true);
            seed = environment.getRequiredProperty("auth.encryption.seed");
        }
        sessionMaxAge = getSessionMaxAge();
    }

    public String getUserName(HttpServletRequest request) {
        String header = request.getHeader(HEADER_NAME);
        return (header != null && !header.isEmpty()) ? extractUserName(header) : null;
    }

    private String extractUserName(String value) {

        try {
            String decryptedValue = encryptionUtil.decrypt(value, seed);
            String[] split = decryptedValue.split("\\|");
            String username = split[0];
            DateTime timestamp = new DateTime(Long.parseLong(split[1]));
            if (timestamp.isAfter(DateTime.now().minus(sessionMaxAge))) {
                return username;
            }
        } catch (IOException | GeneralSecurityException e) {
            log.debug("Unable to decrypt header", e);
        }
        return null;
    }

    public void addHeader(HttpServletResponse response, String userName) {
        try {
            String encryptedValue = createAuthToken(userName);
            response.setHeader(HEADER_NAME, encryptedValue);
        } catch (IOException | GeneralSecurityException e) {
            log.error("Unable to encrypt header", e);
        }
    }

    public String createAuthToken(String userName) throws IOException, GeneralSecurityException {
        String value = userName + "|" + System.currentTimeMillis();
        return encryptionUtil.encrypt(value, seed);
    }

    private Period getSessionMaxAge() {
        String maxAge;
        try{
            maxAge = environment.getRequiredProperty("auth.session.maxAge");
        } catch (IllegalStateException e) {
            log.warn("Unable to get required property auth.session.maxAge. Using default: 1 day");
            maxAge = "1d";
        }
        PeriodFormatter format = new PeriodFormatterBuilder()
                .appendDays()
                .appendSuffix("d", "d")
                .printZeroRarelyFirst()
                .appendHours()
                .appendSuffix("h", "h")
                .printZeroRarelyFirst()
                .appendMinutes()
                .appendSuffix("m", "m")
                .toFormatter();
        Period sesionMaxAge = format.parsePeriod(maxAge);
        if (log.isDebugEnabled()) {
            log.debug("Session maxAge is: "
                    + formatIfNotZero(sesionMaxAge.getDays(), "days", "day")
                    + formatIfNotZero(sesionMaxAge.getHours(), "hours", "hour")
                    + formatIfNotZero(sesionMaxAge.getMinutes(), "minutes", "minute")
            );
        }
        return sesionMaxAge;
    }

    private static String formatIfNotZero(int value, String plural, String singleton) {
        if (value > 0) {
            if (value > 1) {
                return "" + value + " " + plural;
            }
            return "" + value + " " + singleton;
        }
        return "";
    }

}
