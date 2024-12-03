package stuff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Model;
import jakarta.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;

@ApplicationScoped
@Model
@Named("something")
public class SomeBean {
    private static final Logger logger = LoggerFactory.getLogger(SomeBean.class);

    private BeanManager bm;

    @Inject
    public SomeBean(BeanManager bm) {
        this.bm = bm;
    }

    @Deprecated // proxy constructor
    SomeBean() {}

    public String getFoo() {
        logger.info("What's meaning of life?");
        return "42";
    }
}
