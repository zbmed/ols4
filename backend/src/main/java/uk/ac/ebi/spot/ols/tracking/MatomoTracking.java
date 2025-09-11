package uk.ac.ebi.spot.ols.tracking;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.matomo.java.tracking.MatomoRequest;
import org.matomo.java.tracking.MatomoTracker;
import org.matomo.java.tracking.servlet.JavaxHttpServletWrapper;
import org.matomo.java.tracking.servlet.ServletMatomoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;


@Component
@Aspect
public class MatomoTracking {

    @Autowired
    private MatomoTracker matomoTracker;

    private static final Logger logger = LoggerFactory.getLogger(MatomoTracking.class);

    @AfterReturning("@annotation(uk.ac.ebi.spot.ols.tracking.TrackMatomo)")
    public void trackMatomo(JoinPoint joinPoint) {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest();

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            java.lang.reflect.Method method = signature.getMethod();

            TrackMatomo annotation = AnnotationUtils.findAnnotation(method, TrackMatomo.class);

            if (annotation == null) {
                logger.warn("TrackMatomo annotation not found on method: {}", method.getName());
                return;
            }

            logger.debug("trackMatomo triggered for method {}", signature.getMethod().getName());

            String actionName = annotation.actionName();
            int siteId = annotation.siteId();

            String clientIp = extractClientIp(request);

            Map<Long, Object> dims = new HashMap<>();
            String query = request.getParameter("q");
            dims.put(1L, query);

            MatomoRequest matomoRequest = ServletMatomoRequest.fromServletRequest(
                            JavaxHttpServletWrapper.fromHttpServletRequest(request))
                    .siteId(siteId)
                    .actionName(actionName)
                    .visitorIp(clientIp)
                    .dimensions(dims)
                    .build();

            matomoTracker.sendRequestAsync(matomoRequest);
            logger.debug("Matomo tracking sent for action: {}", actionName);

        } catch (Exception e) {
            logger.error("Failed to send Matomo tracking request", e);
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim(); // first IP is the original client
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr(); // fallback
        }
        return ip;
    }
}
