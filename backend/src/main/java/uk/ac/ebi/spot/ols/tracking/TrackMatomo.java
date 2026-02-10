package uk.ac.ebi.spot.ols.tracking;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TrackMatomo {
    String actionName() default "Default API Call";
    int siteId() default 16;
}
